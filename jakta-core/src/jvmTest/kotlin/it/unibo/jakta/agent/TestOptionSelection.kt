package it.unibo.jakta.agent

import it.unibo.jakta.event.GoalAddEvent
import it.unibo.jakta.plan.GoalAdditionPlan
import it.unibo.jakta.plan.Plan
import kotlin.coroutines.ContinuationInterceptor
import kotlin.reflect.typeOf
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

/**
 * Plan selection produces an option (plan + context) that is run as it is (#1069).
 */
class TestOptionSelection {

    @Test
    fun planRunsWithTheContextItWasSelectedWithEvenIfBeliefsChangeBeforeItStarts() = runTest {
        var triggerEvaluations = 0
        var guardEvaluations = 0
        val ranWith = CompletableDeferred<String>()
        val plan = GoalAdditionPlan<String, String, String, Unit>(
            trigger = { goal -> goal.takeIf { it == "go" }.also { triggerEvaluations++ } },
            guard = {
                guardEvaluations++
                beliefs.firstOrNull { it.startsWith("target") }
            },
            body = { ranWith.complete(it.context) },
            resultType = typeOf<Unit>(),
        )
        val lifecycle = createLifecycle(beliefs = listOf("target(1)"), goalPlans = listOf(plan))
        val goal = GoalAddEvent<String, Any?>("go", typeOf<Any?>(), CompletableDeferred())
        lifecycle.executableAgent.internalInbox.send(goal)

        // Selects the plan with the context "target(1)" and schedules it: it hasn't started yet.
        lifecycle.tryStep(dispatcher())
        assertTrue(!ranWith.isCompleted, "The plan should be scheduled, not started")

        // Before the plan starts, the belief the guard bound is replaced.
        lifecycle.executableAgent.state.forget("target(1)")
        lifecycle.executableAgent.state.believe("target(2)")

        stepUntil(lifecycle) { ranWith.isCompleted || goal.completion!!.isCompleted }

        assertEquals("target(1)", ranWith.await(), "The plan should run with the context it was selected with")
        assertEquals(1, triggerEvaluations, "The trigger should be evaluated once")
        assertEquals(1, guardEvaluations, "The guard should be evaluated once")
    }

    @Test
    fun goalDoesNotFailWhenTheGuardStopsHoldingBeforeThePlanStarts() = runTest {
        val ran = CompletableDeferred<Unit>()
        val plan = GoalAdditionPlan<String, String, String, Unit>(
            trigger = { goal -> goal.takeIf { it == "go" } },
            guard = { context.takeIf { "ready" in beliefs } },
            body = { ran.complete(Unit) },
            resultType = typeOf<Unit>(),
        )
        val lifecycle = createLifecycle(beliefs = listOf("ready"), goalPlans = listOf(plan))
        val completion = CompletableDeferred<Any?>()
        lifecycle.executableAgent.internalInbox.send(GoalAddEvent("go", typeOf<Any?>(), completion))

        lifecycle.tryStep(dispatcher())
        lifecycle.executableAgent.state.forget("ready")
        stepUntil(lifecycle) { completion.isCompleted }

        assertTrue(ran.isCompleted, "The selected plan should run")
        assertTrue(!completion.isCancelled && completion.getCompletionExceptionOrNull() == null, "The goal should not fail")
    }

    @Test
    fun goalsAreHandledByPlansWithACompatibleResultType() = runTest {
        val stringPlan = GoalAdditionPlan<String, String, String, String>(
            trigger = { goal -> goal.takeIf { it == "answer" } },
            guard = { context },
            body = { "forty-two" },
            resultType = typeOf<String>(),
        )
        val intPlan = GoalAdditionPlan<String, String, String, Int>(
            trigger = { goal -> goal.takeIf { it == "answer" } },
            guard = { context },
            body = { 42 },
            resultType = typeOf<Int>(),
        )
        val lifecycle = createLifecycle(goalPlans = listOf(stringPlan, intPlan))
        val completion = CompletableDeferred<Int>()
        lifecycle.executableAgent.internalInbox.send(GoalAddEvent("answer", typeOf<Int>(), completion))

        stepUntil(lifecycle) { completion.isCompleted }

        assertEquals(42, completion.await(), "achieve<Int> should run the plan returning an Int")
    }

    @Test
    fun achieveWithoutResultAcceptsPlansReturningAValue() = runTest {
        val outerDone = CompletableDeferred<Unit>()
        val outer = GoalAdditionPlan<String, String, String, Unit>(
            trigger = { goal -> goal.takeIf { it == "outer" } },
            guard = { context },
            body = {
                it.agent.achieve("inner")
                outerDone.complete(Unit)
            },
            resultType = typeOf<Unit>(),
        )
        val inner = GoalAdditionPlan<String, String, String, Int>(
            trigger = { goal -> goal.takeIf { it == "inner" } },
            guard = { context },
            body = { 7 },
            resultType = typeOf<Int>(),
        )
        val lifecycle = createLifecycle(goalPlans = listOf(outer, inner))
        lifecycle.executableAgent.internalInbox.send(GoalAddEvent.withNoResult("outer"))

        stepUntil(lifecycle) { outerDone.isCompleted }

        assertTrue(outerDone.isCompleted, "achieve should complete with a plan whose result is discarded")
    }

    private fun TestScope.dispatcher(): CoroutineDispatcher =
        coroutineContext[ContinuationInterceptor] as CoroutineDispatcher

    private fun TestScope.stepUntil(lifecycle: BaseAgentLifecycle<String, String>, condition: () -> Boolean) {
        repeat(MAX_STEPS) {
            if (condition()) return
            lifecycle.tryStep(dispatcher())
            testScheduler.runCurrent()
        }
        assertTrue(condition(), "The condition did not hold after $MAX_STEPS steps")
    }

    private fun createLifecycle(
        beliefs: List<String> = emptyList(),
        goalPlans: List<Plan.Goal<String, String, *, *>> = emptyList(),
    ): BaseAgentLifecycle<String, String> {
        val specification =
            object : AgentSpecification<String, String, Unit> {
                override val body: Unit = Unit
                override val id: AgentID = BaseAgentID("test-agent")
                override val initialGoals: List<String> = emptyList()
                override val initialState: AgentState<String, String> = BaseAgentState(
                    beliefs = beliefs,
                    intentions = emptySet(),
                    beliefPlans = emptyList(),
                    goalPlans = goalPlans,
                    perceptionHandler = { null },
                    messageHandler = { null },
                )
            }
        return BaseAgentLifecycle(BaseAgent(specification))
    }

    private companion object {
        const val MAX_STEPS = 50
    }
}
