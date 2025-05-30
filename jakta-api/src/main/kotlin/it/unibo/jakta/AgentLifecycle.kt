package it.unibo.jakta

import it.unibo.jakta.events.Event
import it.unibo.jakta.resolution.Matcher

/** BDI Agent definition*/
interface AgentLifecycle<Belief : Any, Query : Any, Response> {

    val agent: Agent<Belief, Query, Response>
    val environment: AgentProcess
    val debugEnabled: Boolean

    /** Performs the whole procedure (10 steps) of the BDI Agent's Reasoning Cycle.
     *  @param environment the [AgentProcess]
     *  @return true if the environment has been changed as a result of this operation
     */
    fun runOneCycle() {
        val eventToManage = sense()
        deliberate(eventToManage)
        act()
    }

    /**
     * Performs the sensing phase of the reasoning cycle, in particular:
     *  - STEP1: Perceive the Environment
     *  - STEP2: Update the ASBeliefBase
     *  - STEP3: Receiving Communication from Other Agents
     *  - STEP4: Selecting "Socially Acceptable" Messages
     *  @param environment the [AgentProcess]
     */
    fun sense(): Event.Internal?

    /** Performs the reason phase of the reasoning cycle, in particular:
     *  - STEP5: Selecting an ASEvent
     *  - STEP6: Retrieving all Relevant Plans
     *  - STEP7: Determining the Applicable Plans
     *  - STEP8: Selecting one Applicable Plan
     */
    fun deliberate(event: Event.Internal?)

    /**
     * Performs the reason phase of the reasoning cycle, in particular:
     *  - STEP9: Select an Intention for Further Execution
     *  - STEP10: Executing one Step on an Intention
     *  @param environment [AgentProcess]
     *  @return true if the environment has been changed as a result of this operation.
     */
    fun act()

    companion object {
        context(_: Matcher<Belief, Query, Response>)
        fun <Belief : Any, Query : Any, Response> of(
            agent: Agent<Belief, Query, Response>,
            environment: AgentProcess,
            debugEnabled: Boolean = false,
        ) = object : AgentLifecycle<Belief, Query, Response> {
            override val debugEnabled: Boolean
                get() = debugEnabled
            override val agent: Agent<Belief, Query, Response>
                get() = agent
            override val environment: AgentProcess
                get() = environment

            override fun sense(): Event.Internal? = agent.sense(environment)

            override fun deliberate(event: Event.Internal?) = when {
                event != null -> agent.deliberate(environment, event)
                else -> Unit
            }

            override fun act() = agent.act(environment)
        }
    }
}
