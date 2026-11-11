package it.unibo.jakta.plan

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.agent.BaseAgent
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.BaseAgentLifecycle
import it.unibo.jakta.agent.BaseAgentState
import it.unibo.jakta.agent.NoPlanException
import it.unibo.jakta.dsl.plan.PlanLibraryBuilderImpl
import it.unibo.jakta.dsl.plan.labelled
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.GoalAddEvent
import kotlin.coroutines.ContinuationInterceptor
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

/**
 * Plan result types follow one rule on every platform, can be declared with `returns<R>()`,
 * and a goal that finds no plan because of them says so (#805).
 */
class TestPlanResultTypes {
    private sealed interface Outcome

    private data class Ok(val value: Int) : Outcome

    private data object Failed : Outcome

    private val goalPlans = mutableListOf<Plan.Goal<String, String, *, *>>()
    private val library = PlanLibraryBuilderImpl<String, String>({ }, { goalPlans += it })

    @Test
    fun aPlanReturningAUserDefinedSubtypeIsRejectedWithAnExplanation() = runTest {
        library.adding.goal { takeIf { it == "answer" } } labelled "answer-ok" triggers { Ok(42) }

        val completion = pursue<Outcome>("answer")

        val error = completion.getCompletionExceptionOrNull()
        assertIs<NoPlanException>(error)
        val message = error.message.orEmpty()
        assertTrue("answer-ok" in message, "The message should name the rejected plan: $message")
        assertTrue("returns<" in message, "The message should suggest declaring the result type: $message")
    }

    @Test
    fun aDeclaredResultTypeIsSelected() = runTest {
        val plan = library.adding.goal { takeIf { it == "answer" } }.returns<Outcome>() triggers { Ok(42) }

        assertEquals(typeOf<Outcome>(), plan.resultType)
        assertEquals(Ok(42), pursue<Outcome>("answer").await())
    }

    @Test
    fun anInferredResultTypeStillWorksWithoutDeclaration() = runTest {
        library.adding.goal { takeIf { it == "answer" } } triggers { "forty-two" }

        assertEquals("forty-two", pursue<String>("answer").await())
    }

    @Test
    fun aFailurePlanCanDeclareTheResultTypeOfTheGoal() = runTest {
        library.adding.goal { takeIf { it == "answer" } }.returns<Outcome>() triggers { error("boom") }
        library.failing.goal { takeIf { it == "answer" } }.returns<Outcome>() triggers { Failed }

        assertEquals(Failed, pursue<Outcome>("answer").await())
    }

    @Test
    fun whoeverPursuedAFailedGoalGetsWhyItFailed() = runTest {
        library.adding.goal { takeIf { it == "answer" } }.returns<Outcome>() triggers {
            throw IllegalStateException("boom")
        }

        val error = pursue<Outcome>("answer").getCompletionExceptionOrNull()
        assertIs<IllegalStateException>(error, "The plan's exception, not 'no failure plan', should reach the caller")
        assertEquals("boom", error.message)
    }

    private inline fun <reified R> TestScope.pursue(goal: String): CompletableDeferred<R> {
        val lifecycle = createLifecycle()
        val completion = CompletableDeferred<R>()
        lifecycle.executableAgent.internalInbox.send(GoalAddEvent(goal, typeOf<R>(), completion))
        val dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
        repeat(MAX_STEPS) {
            if (completion.isCompleted) return completion
            lifecycle.tryStep(dispatcher)
            testScheduler.runCurrent()
        }
        return completion
    }

    private fun createLifecycle(): BaseAgentLifecycle<String, String> {
        val specification =
            object : AgentSpecification<String, String, Unit> {
                override val body: Unit = Unit
                override val id: AgentID = BaseAgentID("test-agent")
                override val initialGoals: List<String> = emptyList()
                override val initialState: AgentState<String, String> = BaseAgentState(
                    beliefs = emptyList(),
                    intentions = emptySet(),
                    beliefPlans = emptyList(),
                    goalPlans = goalPlans.toList(),
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
