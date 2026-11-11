package it.unibo.jakta.agent

import it.unibo.jakta.dsl.plan.PlanLibraryBuilderImpl
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.event.GoalAddEvent
import it.unibo.jakta.plan.Plan
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

/**
 * A failure that nothing caught reaches the agent's failed-intention handler (#1084).
 */
class TestFailedIntentions {
    private val beliefPlans = mutableListOf<Plan.Belief<String, String, *, *>>()
    private val goalPlans = mutableListOf<Plan.Goal<String, String, *, *>>()
    private val library = PlanLibraryBuilderImpl<String, String>({ beliefPlans += it }, { goalPlans += it })
    private val handled = mutableListOf<FailedIntention<String, String>>()
    private val recordingHandler: FailedIntentionHandler<String, String> = { handled += this }

    @Test
    fun aRootGoalWithoutPlansReachesTheHandler() = runTest {
        val lifecycle = createLifecycle(recordingHandler)
        lifecycle.executableAgent.internalInbox.send(GoalAddEvent.withNoResult("go"))
        runSteps(lifecycle)

        val failure = handled.single()
        assertEquals("go", failure.goal)
        assertIs<NoPlanException>(failure.cause)
    }

    @Test
    fun aSubGoalFailureEscalatesAndTheHandlerSeesTheRootGoalAndTheOriginalCause() = runTest {
        library.adding.goal { takeIf { it == "outer" } } triggers { agent.achieve("inner") }
        library.adding.goal { takeIf { it == "inner" } } triggers { check(false) { "boom" } }
        val lifecycle = createLifecycle(recordingHandler)
        lifecycle.executableAgent.internalInbox.send(GoalAddEvent.withNoResult("outer"))
        runSteps(lifecycle)

        val failure = handled.single()
        assertEquals("outer", failure.goal, "The handler should see the root goal, not the failed sub-goal")
        val cause = failure.cause
        assertIs<IllegalStateException>(cause, "The handler should see the original exception")
        assertEquals("boom", cause.message)
    }

    @Test
    fun aFailurePlanThatRecoversKeepsTheHandlerOut() = runTest {
        library.adding.goal { takeIf { it == "go" } } triggers { check(false) { "boom" } }
        library.failing.goal { takeIf { it == "go" } } triggers { }
        val lifecycle = createLifecycle(recordingHandler)
        lifecycle.executableAgent.internalInbox.send(GoalAddEvent.withNoResult("go"))
        runSteps(lifecycle)

        assertTrue(handled.isEmpty())
    }

    @Test
    fun aFailurePlanThatFailsEscalatesTheOriginalCause() = runTest {
        library.adding.goal { takeIf { it == "go" } } triggers { check(false) { "first" } }
        library.failing.goal { takeIf { it == "go" } } triggers { require(false) { "second" } }
        val lifecycle = createLifecycle(recordingHandler)
        lifecycle.executableAgent.internalInbox.send(GoalAddEvent.withNoResult("go"))
        runSteps(lifecycle)

        val cause = handled.single().cause
        assertIs<IllegalStateException>(cause)
        assertEquals("first", cause.message)
    }

    @Test
    fun aFailureWithAPlanWaitingForTheGoalDoesNotReachTheHandler() = runTest {
        val lifecycle = createLifecycle(recordingHandler)
        val completion = CompletableDeferred<Any?>()
        lifecycle.executableAgent.internalInbox.send(GoalAddEvent("go", kotlin.reflect.typeOf<Any?>(), completion))
        runSteps(lifecycle)

        assertIs<NoPlanException>(completion.getCompletionExceptionOrNull(), "Whoever waits gets the failure")
        assertTrue(handled.isEmpty())
    }

    @Test
    fun theHandlerCanPursueTheGoalAgain() = runTest {
        var attempts = 0
        val achieved = CompletableDeferred<Int>()
        library.adding.goal { takeIf { it == "go" } } triggers {
            attempts++
            check(attempts > 1) { "first attempt fails" }
            achieved.complete(attempts)
        }
        var handlerCalls = 0
        val lifecycle = createLifecycle {
            handlerCalls++
            agent.alsoAchieve(goal!!)
        }
        lifecycle.executableAgent.internalInbox.send(GoalAddEvent.withNoResult("go"))
        runSteps(lifecycle)

        assertEquals(2, achieved.await())
        assertEquals(1, handlerCalls)
    }

    @Test
    fun theHandlerRunsOnAnIntentionAndCanAchieveGoals() = runTest {
        val recovered = CompletableDeferred<Unit>()
        library.adding.goal { takeIf { it == "recover" } } triggers { recovered.complete(Unit) }
        val lifecycle = createLifecycle { agent.achieve("recover") }
        lifecycle.executableAgent.internalInbox.send(GoalAddEvent.withNoResult("go"))
        runSteps(lifecycle)

        assertTrue(recovered.isCompleted)
    }

    @Test
    fun aFailedPlanReactingToABeliefReachesTheHandlerWithoutAGoal() = runTest {
        library.adding.belief { takeIf { it == "alarm" } } triggers { check(false) { "boom" } }
        val lifecycle = createLifecycle(recordingHandler)
        lifecycle.executableAgent.state.believe("alarm")
        runSteps(lifecycle)

        val failure = handled.single()
        assertNull(failure.goal)
        assertEquals(BeliefAddEvent("alarm"), failure.trigger)
        assertIs<IllegalStateException>(failure.cause)
    }

    @Test
    fun aBeliefNoPlanReactsToIsNotAFailure() = runTest {
        val lifecycle = createLifecycle(recordingHandler)
        lifecycle.executableAgent.state.believe("unnoticed")
        runSteps(lifecycle)

        assertTrue(handled.isEmpty())
    }

    @Test
    fun aFailingHandlerIsOnlyLogged() = runTest {
        var handlerCalls = 0
        val lifecycle = createLifecycle {
            handlerCalls++
            error("the handler fails too")
        }
        lifecycle.executableAgent.internalInbox.send(GoalAddEvent.withNoResult("go"))
        runSteps(lifecycle)

        assertEquals(1, handlerCalls, "A failing handler should not be called again")
    }

    @Test
    fun withoutAHandlerTheFailureIsOnlyLogged() = runTest {
        val lifecycle = createLifecycle(handler = null)
        lifecycle.executableAgent.internalInbox.send(GoalAddEvent.withNoResult("go"))
        runSteps(lifecycle)
    }

    private fun TestScope.runSteps(lifecycle: BaseAgentLifecycle<String, String>) {
        val dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
        repeat(STEPS) {
            lifecycle.tryStep(dispatcher)
            testScheduler.runCurrent()
        }
    }

    private fun createLifecycle(handler: FailedIntentionHandler<String, String>?): BaseAgentLifecycle<String, String> {
        val specification =
            object : AgentSpecification<String, String, Unit> {
                override val body: Unit = Unit
                override val id: AgentID = BaseAgentID("test-agent")
                override val initialGoals: List<String> = emptyList()
                override val initialState: AgentState<String, String> = BaseAgentState(
                    beliefs = emptyList(),
                    intentions = emptySet(),
                    beliefPlans = beliefPlans.toList(),
                    goalPlans = goalPlans.toList(),
                    perceptionHandler = { null },
                    messageHandler = { null },
                )
                override val failedIntentionHandler: FailedIntentionHandler<String, String>? = handler
            }
        return BaseAgentLifecycle(BaseAgent(specification))
    }

    private companion object {
        const val STEPS = 50
    }
}
