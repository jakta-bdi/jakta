package it.unibo.jakta.agent

import it.unibo.jakta.event.GoalAddEvent
import it.unibo.jakta.plan.GoalAdditionPlan
import it.unibo.jakta.plan.Plan
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout

@OptIn(ExperimentalCoroutinesApi::class)
class TestAgentStep {
    private val firstLineOfPlanBody = CompletableDeferred<Unit>()
    private val secondLineOfPlanBody = CompletableDeferred<Unit>()
    private val planCompleted = CompletableDeferred<Unit>()
    private val slowPlan = GoalAdditionPlan<String, String, Unit, String, Unit>(
        trigger = { goal -> goal.takeIf { it == "slow" } },
        guard = { it },
        body = {
            firstLineOfPlanBody.complete(Unit)
            it.agent.print("firstLineOfPlanBody")
            secondLineOfPlanBody.complete(Unit)
            it.agent.print("secondLineOfPlanBody")
            delay(1)
            it.agent.print("After delay")
            planCompleted.complete(Unit)
            it.agent.print("Plan Completed")
        },
        resultType = typeOf<Unit>(),
    )

	@Test
	fun tryStepReturnsImmediatelyWhenNoEventsAreAvailable() = runTest {
        val scope = CoroutineScope(Dispatchers.Main) //TODO("Can we put this directly inside tryStep?")

		val lifecycle = createLifecycle()

		withTimeout(250) {
			lifecycle.tryStep(scope)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun tryStepDoesNotWaitForPlanCompletion()  {
        val scope = CoroutineScope(Dispatchers.Main) //TODO("Can we put this directly inside tryStep?")
		val lifecycle = createLifecycle(goalPlans = listOf(slowPlan))
		lifecycle.executableAgent.internalInbox.send(GoalAddEvent.withNoResult("slow"))

        // First iteration of the agent lifecycle
        runTest {
            try {
                withTimeout(250) {
                    lifecycle.tryStep(scope)
                }
            } catch (e: TimeoutCancellationException) {
                fail("The invocation of tryStep never returned")
            }

        }

		assertFalse(
            firstLineOfPlanBody.isCompleted && secondLineOfPlanBody.isCompleted && planCompleted.isCompleted,
            "The first iteration of agent lifecycle should schedule plan for execution, not execute it",
        )

        // Second iteration of agent lifecycle
        lifecycle.tryStep(scope)
        assertTrue(
            firstLineOfPlanBody.isCompleted && secondLineOfPlanBody.isCompleted,
            "The second iteration of the agent lifecycle should execute the plan body until reaching the delay",
        )
        assertFalse(
            planCompleted.isCompleted,
            "The second iteration shouldn't execute after the delay",
        )

        // Third iteration of agent lifecycle
        lifecycle.tryStep(scope)
        lifecycle.tryStep(scope)
        lifecycle.tryStep(scope)
        lifecycle.tryStep(scope)
        lifecycle.tryStep(scope)
        lifecycle.tryStep(scope)
        assertTrue(
            firstLineOfPlanBody.isCompleted && secondLineOfPlanBody.isCompleted && planCompleted.isCompleted,
            "The third iteration of the agent lifecycle should complete plan body execution",
        )

	}

	private fun createLifecycle(
		goalPlans: List<Plan.Goal<String, String, Unit, *, *>> = emptyList(),
	): BaseAgentLifecycle<String, String, Unit> {
		val specification =
			object : AgentSpecification<String, String, Unit, Unit> {
				override val body: Unit = Unit
				override val id: AgentID = BaseAgentID("test-agent")
				override val initialGoals: List<String> = emptyList()
				override val initialState: AgentState<String, String, Unit> = BaseAgentState(
					beliefs = emptyList(),
					intentions = emptySet(),
					beliefPlans = emptyList(),
					goalPlans = goalPlans,
					perceptionHandler = { null },
					messageHandler = { null },
					skills = Unit,
				)
			}

		return BaseAgentLifecycle(BaseAgent(specification))
	}
}
