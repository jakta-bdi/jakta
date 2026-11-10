package it.unibo.jakta.agent

import it.unibo.jakta.event.GoalAddEvent
import it.unibo.jakta.plan.GoalAdditionPlan
import it.unibo.jakta.plan.Plan
import it.unibo.jakta.plan.PlanSelection
import kotlin.coroutines.ContinuationInterceptor
import kotlin.reflect.typeOf
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

/**
 * The agent chooses among the applicable plans with its [PlanSelection] (#1070).
 */
class TestPlanSelection {
    private val ran = mutableListOf<String>()
    private val guardsEvaluated = mutableListOf<String>()

    private fun plan(name: String, binding: Int) = GoalAdditionPlan<String, String, Int, Unit>(
        trigger = { goal -> 0.takeIf { goal == "go" } },
        guard = {
            guardsEvaluated += name
            binding
        },
        body = { ran += name },
        resultType = typeOf<Unit>(),
    )

    private val plans = listOf(plan("first", 1), plan("second", 3), plan("third", 2))

    @Test
    fun byDefaultTheFirstApplicablePlanRunsWithoutEvaluatingTheOtherGuards() = runTest {
        val completion = handleGoalWith(PlanSelection.first())

        assertTrue(completion.isCompleted && completion.getCompletionExceptionOrNull() == null)
        assertEquals(listOf("first"), ran)
        assertEquals(listOf("first"), guardsEvaluated, "The default selection should stop at the first option")
    }

    @Test
    fun aCustomSelectionCanChooseAnotherOption() = runTest {
        handleGoalWith { _, options -> options.lastOrNull() }

        assertEquals(listOf("third"), ran)
        assertEquals(listOf("first", "second", "third"), guardsEvaluated, "Each guard should be evaluated once")
    }

    @Test
    fun theSelectionSeesTheContextOfEachOption() = runTest {
        handleGoalWith { _, options -> options.maxByOrNull { it.context as Int } }

        assertEquals(listOf("second"), ran, "The option with the highest binding should run")
    }

    @Test
    fun theSelectionSeesTheEventAndThePlans() = runTest {
        var seenGoal: Any? = null
        handleGoalWith { event, options ->
            seenGoal = (event as GoalAddEvent<*, *>).goal
            options.firstOrNull { it.plan == plans[1] }
        }

        assertEquals("go", seenGoal)
        assertEquals(listOf("second"), ran)
    }

    @Test
    fun selectingNoOptionFailsTheGoalAsIfNoPlanWereApplicable() = runTest {
        val completion = handleGoalWith { _, _ -> null }

        assertTrue(ran.isEmpty(), "No plan should run")
        assertNotNull(completion.getCompletionExceptionOrNull(), "The goal should fail")
    }

    private fun TestScope.handleGoalWith(selection: PlanSelection<String, String>): CompletableDeferred<Any?> {
        val lifecycle = createLifecycle(selection)
        val completion = CompletableDeferred<Any?>()
        lifecycle.executableAgent.internalInbox.send(GoalAddEvent("go", typeOf<Any?>(), completion))
        val dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
        repeat(MAX_STEPS) {
            if (completion.isCompleted) return completion
            lifecycle.tryStep(dispatcher)
            testScheduler.runCurrent()
        }
        return completion
    }

    private fun createLifecycle(selection: PlanSelection<String, String>): BaseAgentLifecycle<String, String> {
        val goalPlans: List<Plan.Goal<String, String, *, *>> = plans
        val specification =
            object : AgentSpecification<String, String, Unit> {
                override val body: Unit = Unit
                override val id: AgentID = BaseAgentID("test-agent")
                override val initialGoals: List<String> = emptyList()
                override val initialState: AgentState<String, String> = BaseAgentState(
                    beliefs = emptyList(),
                    intentions = emptySet(),
                    beliefPlans = emptyList(),
                    goalPlans = goalPlans,
                    perceptionHandler = { null },
                    messageHandler = { null },
                )
                override val planSelection: PlanSelection<String, String> = selection
            }
        return BaseAgentLifecycle(BaseAgent(specification))
    }

    private companion object {
        const val MAX_STEPS = 50
    }
}
