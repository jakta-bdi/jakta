package it.unibo.jakta

import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.Event

/** BDI Agent definition*/
interface ASAgentLifecycle {

    val agent: ASAgent
    val environment: BasicEnvironment
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
        fun of(
            agent: ASAgent,
            environment: BasicEnvironment,
            debugEnabled: Boolean = false,
        ) = object : ASAgentLifecycle {
            override val debugEnabled: Boolean
                get() = debugEnabled
            override val agent: ASAgent
                get() = agent
            override val environment: BasicEnvironment
                get() = environment

            override fun sense(): Event.Internal? = agent.sense(environment, debugEnabled)

            override fun deliberate(event: Event.Internal?) = when {
                event != null -> agent.deliberate(environment, event, debugEnabled)
                else -> Unit
            }

            override fun act() = agent.act(environment)
        }
    }
}
