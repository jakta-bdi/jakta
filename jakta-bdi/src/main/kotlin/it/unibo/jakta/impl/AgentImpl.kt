package it.unibo.jakta.impl

import it.unibo.jakta.ASAgent
import it.unibo.jakta.AgentID
import it.unibo.jakta.AgentProcess
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.effects.ActivitySideEffect
import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.*
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASIntentionPool
import it.unibo.jakta.intentions.ASMutableIntentionPool
import it.unibo.jakta.intentions.IntentionPoolStaticFactory
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.impl.PlanImpl
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution

internal class AgentImpl(
    override var controller: Activity.Controller?,
    agentID: AgentID = AgentID(),
    agentName: String = "Agent-$agentID",
    events: List<Event.Internal.Goal<ASBelief, Struct, Solution>> = emptyList(),
    beliefBase: ASMutableBeliefBase = ASMutableBeliefBase.of(),
    plans: MutableCollection<Plan<ASBelief, Struct, Solution>> = mutableListOf(),
    intentions: ASMutableIntentionPool = IntentionPoolStaticFactory.empty(),
    override var tags: Map<String, Any> = emptyMap(),
) : ASAgent {
    override val context: MutableContext = MutableContext(
        agentID,
        agentName,
        beliefBase,
        plans,
        intentions,
        events.asSequence(),
    )

    data class MutableContext(
        override val agentID: AgentID,
        override val agentName: String,
        override val beliefBase: ASMutableBeliefBase,
        override val plans: MutableCollection<Plan<ASBelief, Struct, Solution>>,
        override val intentions: ASMutableIntentionPool,
        private var events: Sequence<Event.Internal.Goal<ASBelief, Struct, Solution>> = emptySequence(),
    ) : ASAgent.ASAgentContext, ASAgent.ASMutableAgentContext {

        override fun enqueue(event: Event.Internal.Goal<ASBelief, Struct, Solution>) {
            events += event
        }

        override fun drop(event: Event.Internal.Goal<ASBelief, Struct, Solution>) {
            // TODO: decide what to do if there are two events that are the same
            events = events.filter { it != event }
        }

        override fun poll(): Event.Internal.Goal<ASBelief, Struct, Solution>? {
            val localEvent = events.firstOrNull()
            if (localEvent != null) {
                events = events.drop(1)
            }
            return localEvent
        }
    }

    fun updateContext(function: (mutableContext: ASAgent.ASMutableAgentContext) -> Unit): Unit =
        function.invoke(context)

    override fun toString(): String = Jakta.asAslSyntax(this, true)

    override fun selectEvent(environment: BasicEnvironment): Event? =
        context.poll() ?: this.context.beliefBase.poll() ?: environment.poll()

    override fun scheduleIntention(): ASIntention = this.context.intentions.nextIntention()

    override fun runIntention(intention: ASIntention): ActionResponse =
        intention.nextTask()?.run(ActionRequest.of(context, controller?.currentTime())) ?: ActionResponse(
            it.unibo.tuprolog.core.Substitution.failed(),
            emptyList(),
        )

    override fun sense(environment: BasicEnvironment): Event.Internal? {
        // STEP1: Perceive the BasicEnvironment.
        val perceptions = environment.percept()

        // STEP2: Update the ASBeliefBase
        this.context.beliefBase.update(perceptions)

        // println("pre-run -> $context")

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
        return selectEvent(environment).let { it as? Event.Internal ?: TODO("Transform external events into internal events") }
    }

    override fun replaceTags(tags: Map<String, Any>): ASAgent {
        this.tags += tags
        return this
    }

    override fun selectRelevantPlans(event: Event.Internal, planLibrary: List<Plan<ASBelief, Struct, Solution>>): List<Plan<ASBelief, Struct, Solution>> =
        planLibrary.filter { it.isRelevant(event) }

    override fun isPlanApplicable(
        event: Event.Internal,
        plan: Plan<ASBelief, Struct, Solution>,
        beliefBase: ASBeliefBase,
    ): Boolean = plan.isApplicable(event, beliefBase)

    override fun selectApplicablePlan(plans: List<Plan<ASBelief, Struct, Solution>>): Plan<ASBelief, Struct, Solution>? = plans.firstOrNull()

    override fun assignPlanToIntention(
        event: Event.Internal,
        plan: Plan<ASBelief, Struct, Solution>,
        intentions: ASIntentionPool,
    ): ASIntention? = when (event) {
        // TODO: when brought "upstairs", use the common type
        is AchievementGoalFailure ->
            event.intention?.copy(recordStack = mutableListOf(plan.toActivationRecord()))
        is TestGoalFailure ->
            event.intention?.copy(recordStack = mutableListOf(plan.toActivationRecord()))
        // else -> intentions[event.intention!!.id]!!.push(plan.toActivationRecord())
        is AchievementGoalInvocation -> {
            event.intention?.pop()
            event.intention?.push(plan.toActivationRecord())
            event.intention
        }
        is TestGoalInvocation -> {
            event.intention?.pop()
            event.intention?.push(plan.toActivationRecord())
            event.intention
        }
    }

    override fun sense(agentProcess: AgentProcess): Event.Internal? = when {
        agentProcess is BasicEnvironment -> sense(agentProcess)
        else -> null
    }

    override fun deliberate(agentProcess: AgentProcess, event: Event.Internal) = when {
        agentProcess is BasicEnvironment -> deliberate(agentProcess, event)
        else -> Unit
    }

    override fun deliberate(environment: BasicEnvironment, event: Event.Internal) {
        // STEP6: Retrieving all Relevant Plans.
        val relevantPlans = selectRelevantPlans(event, this.context.plans.toList())
        // if the set of relevant plans is empty, the event is simply discarded.

        // STEP7: Determining the Applicable Plans.
        val applicablePlans = relevantPlans.filter {
            isPlanApplicable(event, it, this.context.beliefBase)
        }

        // STEP8: Selecting one Applicable Plan.
        val selectedPlan = selectApplicablePlan(applicablePlans)

        // Add plan to intentions
        if (selectedPlan != null) {
            if (environment.debugEnabled) {
                println("[${this.context.agentName}] Selected the event: $event")
            }
            // if (debugEnabled) println("[${agent.agentName}] Selected the plan: $selectedPlan")
            val updatedIntention = assignPlanToIntention(
                event,
                selectedPlan.applicablePlan(event, this.context.beliefBase),
                this.context.intentions,
            )
            // if (debugEnabled) println("[${agent.agentName}] Updated Intention: $updatedIntention")
            if (updatedIntention != null) {
                this.context.intentions.updateIntention(updatedIntention)
            }
        } else {
            if (environment.debugEnabled) {
                println("[${this.context.agentName}] WARNING: There's no applicable plan for the event: $event")
            }
            when (event) {
                // TODO: when brought "upstairs", use the common type
                is AchievementGoalFailure ->
                    this.context.intentions.deleteIntention(event.intention!!.id)
                is TestGoalFailure ->
                    this.context.intentions.deleteIntention(event.intention!!.id)
                is AchievementGoalInvocation ->
                    this.context.intentions.deleteIntention(event.intention!!.id)
                is TestGoalInvocation ->
                    this.context.intentions.deleteIntention(event.intention!!.id)
                else -> error("Marmellata")
            }
        }
    }

    override fun act(agentProcess: AgentProcess) = when {
        agentProcess is BasicEnvironment -> act(agentProcess)
        else -> Unit
    }

    override fun act(environment: BasicEnvironment) {
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
        if (!this.context.intentions.isEmpty()) {
            // STEP9: Select an Intention for Further Execution.
            val intentionToExecute = scheduleIntention()

            // STEP10: Executing one Step on an Intention
            if (intentionToExecute.stack.isNotEmpty()) {
                if (environment.debugEnabled)
                    println("[${context.agentName}] RUN -> ${intentionToExecute.nextActionToExecute()}")
                val sideEffects = runIntention(intentionToExecute)
                sideEffects.forEach { sideEffect ->
                    val processController = controller
                    when {
                        sideEffect is ActivitySideEffect && processController != null -> sideEffect.invoke(
                            processController,
                        )
                        sideEffect is EnvironmentChange -> sideEffect.invoke(environment)
                        sideEffect is AgentChange -> this.updateContext { sideEffect.invoke(context) }
                    }
                }
            }
            // println("post run -> ${newAgent.context}")
        }

//        // Generate BasicEnvironment Changes
//        val environmentChangesToApply = executionResult.environmentEffects + cachedEffects
//        cachedEffects = emptyList()
//        return environmentChangesToApply
    }
}
