package it.unibo.jakta.inspector

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentEvent.External
import it.unibo.jakta.event.AgentEvent.Internal
import it.unibo.jakta.intention.IntentionID
import it.unibo.jakta.node.NodeID
import it.unibo.jakta.plan.Plan
import kotlin.time.Duration
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The kinds of events an agent handles, plus its creation.
 */
enum class EventKind {
    /** The agent was created. */
    CREATED,

    /** A belief was added. */
    BELIEF_ADDED,

    /** A belief was removed. */
    BELIEF_REMOVED,

    /** A goal was added. */
    GOAL_ADDED,

    /** A goal was removed. */
    GOAL_REMOVED,

    /** A goal failed. */
    GOAL_FAILED,

    /** An intention ran one step. */
    STEP,

    /** A message arrived. */
    MESSAGE,

    /** Something was perceived. */
    PERCEPTION,
}

/**
 * What an agent had in mind right after it handled an event.
 *
 * @property time when the event was handled, since the inspector started.
 * @property kind the kind of event.
 * @property event the event, such as `+!goal` or `-belief`.
 * @property beliefs the beliefs of the agent.
 * @property intentions the intentions of the agent.
 * @property plans the plans of the agent, as their triggers and result types (plans have no names).
 */
data class MindSnapshot(
    val time: Duration,
    val kind: EventKind,
    val event: String,
    val beliefs: List<Any>,
    val intentions: List<IntentionView>,
    val plans: List<String>,
)

/**
 * An intention of an agent.
 *
 * @property number the number of the intention, counting the agent's intentions from 1.
 * @property goals the events it pursues, from the one it was created for to the current sub-goal.
 */
data class IntentionView(val number: Int, val goals: List<String>)

/**
 * An agent seen by a [MindInspector], with the history of its mind.
 *
 * @property id the agent.
 * @property node the node the agent was created on, if the inspector saw it.
 */
class InspectedAgent internal constructor(val id: AgentID, val node: NodeID?, private val historySize: Int) {
    private val mutableHistory = MutableStateFlow<List<MindSnapshot>>(emptyList())

    /**
     * The snapshots of the agent's mind, oldest first.
     */
    val history: StateFlow<List<MindSnapshot>> = mutableHistory.asStateFlow()

    // what the intentions pursue, which the API does not tell: only touched in the agent's loop
    private class Frame(val event: String, val completion: Job?)

    private val frames = mutableMapOf<IntentionID, MutableList<Frame>>()
    private val numbers = mutableMapOf<IntentionID, Int>()
    private var plans: List<String> = emptyList()

    internal fun recordCreation(agent: ExecutableAgent<*, *>, time: Duration) {
        if (mutableHistory.value.isEmpty()) snapshot(agent, time, EventKind.CREATED, "created")
    }

    internal fun record(agent: ExecutableAgent<*, *>, event: AgentEvent, time: Duration) {
        val (kind, description) = describe(event)
        if (event is Internal && event !is Internal.Step) {
            val frame = Frame(description, (event as? Internal.Goal<*, *>)?.completion)
            val target = event.intention
            if (target != null) {
                frames.getOrPut(target.id) { mutableListOf() } += frame
            } else {
                // an event from no intention starts a new one
                agent.state.intentions.filter { it.id !in frames }.forEach { frames[it.id] = mutableListOf(frame) }
            }
        }
        snapshot(agent, time, kind, description)
    }

    private fun snapshot(agent: ExecutableAgent<*, *>, time: Duration, kind: EventKind, event: String) {
        val state = agent.state
        val pool = state.intentions
        frames.keys.retainAll(pool.map { it.id }.toSet())
        frames.values.forEach { goals -> goals.removeAll { it.completion?.isCompleted == true } }
        // the pool may keep intentions whose plans are over: an intention runs while it has a plan running
        val intentions = pool.filter { it.job.children.any() }
        val goalPlans = state.goalPlans
        val beliefPlans = state.beliefPlans
        if (plans.size != goalPlans.size + beliefPlans.size) plans = (goalPlans + beliefPlans).map { it.describe() }
        val snapshot = MindSnapshot(
            time = time,
            kind = kind,
            event = event,
            beliefs = state.beliefs.toList(),
            intentions = intentions
                .map { IntentionView(number(it.id), frames[it.id].orEmpty().map(Frame::event)) }
                .sortedBy { it.number },
            plans = plans,
        )
        // a single writer, the agent's loop
        mutableHistory.value = (mutableHistory.value + snapshot).takeLast(historySize)
    }

    private fun number(id: IntentionID): Int = numbers.getOrPut(id) { numbers.size + 1 }

    private fun describe(event: AgentEvent): Pair<EventKind, String> = when (event) {
        is Internal.Belief.Add<*> -> EventKind.BELIEF_ADDED to "+${event.belief}"
        is Internal.Belief.Remove<*> -> EventKind.BELIEF_REMOVED to "-${event.belief}"
        is Internal.Goal.Add<*, *> -> EventKind.GOAL_ADDED to "+!${event.goal}"
        is Internal.Goal.Remove<*, *> -> EventKind.GOAL_REMOVED to "-!${event.goal}"
        is Internal.Goal.Failed<*, *> -> EventKind.GOAL_FAILED to "failed !${event.goal}"
        is Internal.Step -> EventKind.STEP to "step of intention #${number(event.intention.id)}"
        is External.Message<*> -> EventKind.MESSAGE to "message from ${event.sender.displayName}: ${event.payload}"
        is External.Perception -> EventKind.PERCEPTION to "perceived $event"
    }
}

private val packages = Regex("""\b[a-z]\w*\.""")

private fun Plan<*, *, *, *, *>.describe(): String {
    val trigger = when (this) {
        is Plan.Goal.Addition -> "+!goal"
        is Plan.Goal.Removal -> "-!goal"
        is Plan.Goal.Failure -> "failure of !goal"
        is Plan.Belief.Addition -> "+belief"
        is Plan.Belief.Removal -> "-belief"
    }
    return "$trigger, returning ${resultType.toString().replace(packages, "")}"
}
