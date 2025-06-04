package it.unibo.jakta.impl

import it.unibo.jakta.Agent
import it.unibo.jakta.AgentID
import it.unibo.jakta.AgentProcess
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.effects.ActivitySideEffect
import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.MutableBeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.intentions.MutableIntentionPool
import it.unibo.jakta.resolution.Matcher
import it.unibo.jakta.plans.Plan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

internal class AgentImpl<Belief : Any, Query : Any, Response> @JvmOverloads constructor(
//    var controller: Activity.Controller?,
    agentID: AgentID = AgentID(),
    agentName: String = "Agent-$agentID",
    events: List<Event.Internal.Goal<Belief, Query, Response>> = emptyList(),
    beliefBase: MutableBeliefBase<Belief> = MutableBeliefBase.empty(),
    plans: MutableList<Plan<Belief, Query, Response>> = mutableListOf(),
    intentions: MutableIntentionPool<Belief, Query, Response>,
) : Agent<Belief, Query, Response> {

    override val context: Agent.Context<Belief, Query, Response> get() = internalContext

    private val internalContext = MutableContext(
        agentID,
        agentName,
        beliefBase,
        plans,
        intentions,
        events.asSequence(),
    )

    private data class MutableContext<Belief: Any, Query: Any, Result>(
        override val agentID: AgentID,
        override val agentName: String,
        override val beliefBase: MutableBeliefBase<Belief> = MutableBeliefBase.empty(),
        override val plans: MutableList<Plan<Belief, Query, Result>>,
        override val intentions: MutableIntentionPool<Belief, Query, Result>,
        var events: Sequence<Event.Internal.Goal<Belief, Query, Result>> = emptySequence(),
    ) : Agent.Context.Mutable<Belief, Query, Result> {

        var previousPercepts: BeliefBase<Belief> = MutableBeliefBase.empty()


//        override fun enqueue(event: Event.Internal.Goal<Belief, Query, Result>) {
//            events += event
//        }
//
//        override fun drop(event: Event.Internal.Goal<Belief, Query, Result>) {
//            // TODO: decide what to do if there are two events that are the same
//            events = events.filter { it != event }
//        }

        override fun poll(): Event.Internal.Goal<Belief, Query, Result>? {
            val localEvent = events.firstOrNull()
            if (localEvent != null) {
                events = events.drop(1)
            }
            return localEvent
        }
    }

//
//    override fun selectEvent(environment: BasicEnvironment): Event? =
//        context.poll() ?: this.context.beliefBase.poll() ?: environment.poll()
//
//    override fun scheduleIntention(): ASIntention = this.context.intentions.nextIntention()
//
//    override fun runIntention(intention: ASIntention): ActionResponse = intention.nextTask()?.let {
//        val asAction = it as? ASAction ?: error("Not executing the expected type of actions")
//        asAction.runAction(ActionRequest.of(this.context, controller?.currentTime()))
//    } ?: ActionResponse(
//        it.unibo.tuprolog.core.Substitution.failed(),
//        emptyList(),
//    )

    override fun sense(agentProcess: AgentProcess<Belief>): Event.Internal? {
        // STEP1: Perceive the BasicEnvironment.
        val perceptions = agentProcess.percept()
        // STEP2: Update the ASBeliefBase
        val removedBelief = internalContext.previousPercepts - perceptions
        internalContext.beliefBase.removeAll(removedBelief)
        val newExternalBeliefs = perceptions - internalContext.previousPercepts
        internalContext.beliefBase.addAll(newExternalBeliefs)
        internalContext.previousPercepts = MutableBeliefBase.of(perceptions)
        // TODO(STEP3: Receiving Communication from Other Agents)
        // val message = environment.getNextMessage(agent.name)
        // TODO(STEP4: Selecting "Socially Acceptable" Messages)
        // Parse message
//        if (message != null) {
//            newEvents = when (message.type) {
//                is it.unibo.jakta.messages.Achieve ->
//                    newEvents + Event.ofAchievementGoalInvocation(Achieve.of(message.value))
//                is Tell -> {
//                    val retrieveResult = newBeliefBase.add(Belief.fromMessageSource(message.from, message.value))
//                    newBeliefBase = retrieveResult.updatedBeliefBase
//                    generateEvents(newEvents, retrieveResult.modifiedBeliefs)
//                }
//            }
//            cachedEffects = cachedEffects + PopMessage(this.agent.name)
//        }
        // STEP5: Selecting an Event.
        return (context.poll() ?: this.context.beliefBase.poll() ?: agentProcess.poll()) as? Event.Internal
            ?: TODO("Transform external events into internal events")
    }
/*
    override fun replaceTags(tags: Map<String, Any>): Agent {
        this.tags += tags
        return this
    }*/

//    private fun assignPlanToIntention(
//        event: Event.Internal,
//        activationRecord: ActivationRecord<Belief, Query, Response>,
//    ): Intention<Belief, Query, Response>? = when {
//        // TODO: when brought "upstairs", use the common type
//        event is Event.Internal.Goal.Achieve.Remove<*, *, *, *> || event is Event.Internal.Goal.Test.Remove<*, *, *> ->
//            event.intention?.copy(recordStack = mutableListOf(activationRecord))
//        // else -> intentions[event.intention!!.id]!!.push(activationRecord)
//        event is Event.Internal.Goal.Achieve.Add<*, *, *, *> || event is Event.Internal.Goal.Test.Add<*, *, *> -> {
//            event.intention?.pop()
//            event.intention?.push(activationRecord)
//            event.intention
//        }
//        else -> null
//    }

    context(matcher: Matcher<Belief, Query, Response>)
    override fun deliberate(agentProcess: AgentProcess<Belief>, event: Event.Internal) {
        val newActivationRecord = matcher.matchPlanFor(event, context.plans, context.beliefBase)
        // Add plan to intentions
        val eventIntention: Intention<Belief, Query, Response>? = (event as? Event.Internal.Goal<Belief, Query, Response>)
            ?.intention
        if (newActivationRecord != null) {

//            if (environment.debugEnabled) {
//                println("[${this.context.agentName}] Selected the event: $event")
//            }
            // if (debugEnabled) println("[${agent.agentName}] Selected the plan: $selectedPlan")
            @Suppress("UNCHECKED_CAST")
            val intention: Intention<Belief, Query, Response> = eventIntention
                ?: Intention(newActivationRecord)
            internalContext.intentions.put(intention)
        } else {
//            if (environment.debugEnabled) {
//                println("[${this.context.agentName}] WARNING: There's no applicable plan for the event: $event")
//            }
            // Plan no longer applicable
            if (eventIntention != null) {
                when (event) {
                    is Event.Internal.Goal.Test.Add<*, *, *> ->
                        internalContext.events += object : Event.Internal.Goal.Test.Remove<Belief, Query, Response> {
                            override val intention: Intention<Belief, Query, Response>? = eventIntention
                            @Suppress("UNCHECKED_CAST")
                            override val query: Query = event.query as Query
                        }
                    is Event.Internal.Goal.Achieve.Add<*, *, *, *> -> {
                        val goal = event.goal
                        internalContext.events += object :
                            Event.Internal.Goal.Achieve.Remove<Belief, Query, Response, Any?> {
                            override val goal: Any? = goal
                            override val intention: Intention<Belief, Query, Response> = eventIntention
                        }
                    }
                    // TODO: Samuele is wrong an thinks exploding is bad
                    is Event.Internal.Goal.Test.Remove<*, *, *> -> error("Marmellata")
                    is Event.Internal.Goal.Achieve.Remove<*, *, *, *> -> error("Marmellata")
//                    if (it != null) {
//                        this.context.intentions.deleteIntention(it.id)
//                    }
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun act(agentProcess: AgentProcess<Belief>) {
        // Select intention to execute
//        if(environment.debugEnabled) {
//            println("----- INTENTIONS STACK -----")
//            if(context.intentions.isEmpty()) {
//                println("EMPTY")
//            } else {
//                context.intentions.values.forEach {
//                    println(it)
//                }
//            }
//            println("----------------------------")
//
//        }
        this.internalContext.intentions.step {
            CoroutineScope(context).launch(context) {
                val sideEffects = this@step.invoke(ActionInvocationContext(
                    this@AgentImpl.context,
                    controller?.currentTime()))
                sideEffects.forEach { sideEffect ->
                    val processController = controller
                    when {
                        sideEffect is ActivitySideEffect && processController != null -> sideEffect.invoke(
                            processController,
                        )

                        sideEffect is EnvironmentChange<*> -> sideEffect.invoke(agentProcess)
                        sideEffect is AgentChange<*, *, *> -> sideEffect.invoke(this@AgentImpl.internalContext)
                    }
                }
            }
        }

 //       if (!this.context.intentions.isEmpty()) {
            // STEP9: Select an Intention for Further Execution.
//            val intentionToExecute = scheduleIntention()

            // STEP10: Executing one Step on an Intention
//            if (intentionToExecute.stack.isNotEmpty()) {
//                if (environment.debugEnabled) {
//                    println("[${context.agentName}] RUN -> ${intentionToExecute.nextActionToExecute()}")
//                }
//                val sideEffects = runIntention(intentionToExecute)
//                sideEffects.forEach { sideEffect ->
//                    val processController = controller
//                    when {
//                        sideEffect is ActivitySideEffect && processController != null -> sideEffect.invoke(
//                            processController,
//                        )
//                        sideEffect is EnvironmentChange -> sideEffect.invoke(environment)
//                        sideEffect is AgentChange -> this.updateContext { sideEffect.invoke(context) }
//                    }
//                }
//            }
            // println("post run -> ${newAgent.context}")
  //      }

//        // Generate BasicEnvironment Changes
//        val environmentChangesToApply = executionResult.environmentEffects + cachedEffects
//        cachedEffects = emptyList()
//        return environmentChangesToApply
    }
}
