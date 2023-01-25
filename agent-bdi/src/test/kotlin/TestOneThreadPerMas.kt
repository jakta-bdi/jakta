import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.JasonParser
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.actions.InternalAction
import io.github.anitvam.agents.bdi.actions.InternalRequest
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.executionstrategies.OneThreadPerMas
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

fun main() {
    val alice = Agent.of(
        name = "Alice",
        events = listOf(
            Event.ofAchievementGoalInvocation(Achieve.of(JasonParser.parser.parseStruct("my_thread")))
        ),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = JasonParser.parser.parseStruct("my_thread"),
                goals = listOf(
                    ActInternally.of(JasonParser.parser.parseStruct("thread"))
                )
            )
        ),
        internalActions = mapOf(
            "thread" to object : InternalAction("thread", 0) {
                override fun InternalRequest.action() {
                    println("Thread: ${Thread.currentThread().name}")
                }
            }
        )
    )

    val bob = Agent.of(
        name = "Alice",
        events = listOf(
            Event.ofAchievementGoalInvocation(Achieve.of(JasonParser.parser.parseStruct("my_thread")))
        ),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = JasonParser.parser.parseStruct("my_thread"),
                goals = listOf(
                    ActInternally.of(JasonParser.parser.parseStruct("thread"))
                )
            )
        ),
        internalActions = mapOf(
            "thread" to object : InternalAction("thread", 0) {
                override fun InternalRequest.action() {
                    println("Thread: ${Thread.currentThread().name}")
                    System.out.flush()
                }
            }
        )
    )
    val environment = Environment.of()

    Mas.of(OneThreadPerMas(), environment, alice, bob).start()
    // Mas.of(OneThreadPerAgent(), environment, alice, bob).start()
}
