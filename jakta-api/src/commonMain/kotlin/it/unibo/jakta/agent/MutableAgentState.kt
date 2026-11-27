package it.unibo.jakta.agent

import it.unibo.jakta.InternalJaktaAPI
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.intention.MutableIntentionPool
import it.unibo.jakta.plan.Plan
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.Duration

/**
 * Mutable state of an agent, allowing modifications to its beliefs, plans, and event handlers.
 */
interface MutableAgentState<Belief : Any, Goal : Any> :
    AgentState<Belief, Goal>,
    Agent {

    /**
     * The mutable pool of intentions that the agent is currently pursuing.
     */
    val mutableIntentionPool: MutableIntentionPool

    /**
     * Modifies the perception handler function that defines which external events are of interest of the agent.
     * @param handler the new function handler the agent will use starting from next iteration of its lifecycle.
     */
    fun setPerceptionHandler(handler: AgentState<Belief, Goal>.(AgentEvent.External.Perception) -> AgentUpdate<*>?)

    /**
     * Modifies the message handler function that defines which Messages are interest of the agent.
     * @param handler the new function handler that the agent will use, starting from the next lifecycle iteration.
     */
    fun setMessageHandler(handler: AgentState<Belief, Goal>.(AgentEvent.External.Message<*>) -> AgentUpdate<*>?)

    /**
     * Adds a new [Plan.Goal] that agent can use for its reasoning process.
     * @param plan the new [Plan.Goal].
     */
    fun addPlan(plan: Plan.Goal<Belief, Goal, *, *>)

    /**
     * Adds a new [Plan.Belief] that agent can use for its reasoning process.
     * @param plan the new [Plan.Belief].
     */
    fun addPlan(plan: Plan.Belief<Belief, Goal, *, *>)

    /**
     * Adds an event to the agent's queue to achieve a goal and suspends until the goal is achieved.
     * !! This method is deprecated as it is an internal method that should not be used directly.
     * @param[goal] The goal to be achieved.
     * @param[resultType] The type of result expected from the plan that will handle this goal.
     * @return The result of the plan that achieved the goal.
     */
    @InternalJaktaAPI
    suspend fun <PlanResult> internalAchieve(goal: Goal, resultType: KType): PlanResult

    /**
     * Adds an event to the agent's queue to achieve a goal and don't wait for it to complete or return a result.
     * @param[goal] The goal to be achieved.
     */
    fun alsoAchieve(goal: Goal)

    /**
     * Add the belief to the agent's belief base (eventually generating events).
     * @param belief The belief to be added.
     */
    fun believe(belief: Belief)

    /**
     * Remove the belief from the agent's belief base (eventually generating events).
     * @param belief The belief to be removed.
     */
    fun forget(belief: Belief)

    /**
     * Makes the beliefs in [scope] become [replacements]: those in scope and not among [replacements] are forgotten,
     * and the new ones believed, through the agent's belief revision, which sees the whole replacement at once.
     * Everything in [scope] that is not replaced is forgotten, so keep the scope narrow: incarnations usually offer
     * safer helpers built on this one, such as a scope given by queries, or Jason's `-+`.
     * @param scope for each belief held, the belief to remove from it, or `null` if it is out of scope: the belief
     * itself, or, for beliefs with metadata, the belief with only the metadata in scope (see
     * [it.unibo.jakta.belief.BeliefRevision]).
     * @throws IllegalArgumentException if any of the [replacements] is not in [scope].
     */
    fun replace(replacements: List<Belief>, scope: (Belief) -> Belief?)

    /**
     * Logs a message to the agent's output.
     * @param[message] The message to be printed.
     */
    fun print(message: String)

    /**
     * Suspends the intention until the agent handles an event for which [eventFilter] returns a non-null value,
     * or the [timeout] is passed.
     * The filter is called on each event the agent handles while the intention waits, and returning `null` means
     * "not this one": the intention keeps waiting. Only the events handled after the call are considered, and the
     * steps of intentions ([AgentEvent.Internal.Step]) are not among them.
     * Use it for what an event tells and the agent's state does not keep: which message a reply belongs to, how a goal
     * ended, a belief that came and went; to wait until something holds, even if it already does, use [waitUntil].
     * Ready-made filters: [it.unibo.jakta.event.beliefAdded], [it.unibo.jakta.event.beliefRemoved],
     * [it.unibo.jakta.event.messageReceived] and [it.unibo.jakta.event.perceived].
     * @param eventFilter the event filter to match what agent is waiting for continuing its execution.
     * @param timeout the maximum [Duration] the agent waits if no event matches the filter.
     * @return the value returned by the eventFilter, or `null` if the timeout passed.
     */
    suspend fun <T : Any> waitForEvent(eventFilter: (AgentEvent) -> T?, timeout: Duration? = null): T?

    /**
     * Suspends the intention until [condition] holds on the agent's state, and returns what it returns;
     * returns `null` if the [timeout] passes first.
     *
     * Unlike [waitForEvent], it checks [condition] right away: if it already holds, it returns
     * **without suspending at all**. Otherwise, it checks it again after each event the agent handles, when the state
     * may have changed; what changes without an event (e.g. the time) is seen at the next event.
     * The [condition] returns `null` while it does not hold; for a plain check, use `takeIf { it }`:
     * ```kotlin
     * agent.waitUntil(5.seconds) { beliefs.firstOrNull { it.startsWith("parcel") } }
     * agent.waitUntil(5.seconds) { (beliefs.size >= 3).takeIf { it } }
     * ```
     * @param timeout the maximum [Duration] the agent waits for [condition] to hold.
     * @param condition what the agent is waiting for, checked on its current state.
     * @return the value returned by [condition], or `null` if the timeout passed.
     */
    suspend fun <T : Any> waitUntil(timeout: Duration? = null, condition: AgentState<Belief, Goal>.() -> T?): T? =
        condition() ?: waitForEvent({ condition() }, timeout)
}

/**
 * Public-facing extension function to achieve a goal with a specific return type, using reified type parameters.
 * @param goal The goal to be achieved.
 * @return The result of the plan execution of type [PlanResult].
 */
@OptIn(InternalJaktaAPI::class)
suspend inline fun <Goal : Any, reified PlanResult> MutableAgentState<*, Goal>.achieveWithResult(
    goal: Goal,
): PlanResult = internalAchieve(goal, typeOf<PlanResult>())

/**
 * Public-facing extension function to achieve a goal and wait for its completion, discarding its result.
 * @param goal The goal to be achieved.
 */
@OptIn(InternalJaktaAPI::class)
suspend inline fun <Goal : Any> MutableAgentState<*, Goal>.achieve(goal: Goal): Unit =
    internalAchieve(goal, typeOf<Unit>())
