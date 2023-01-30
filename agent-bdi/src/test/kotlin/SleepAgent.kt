import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.JasonParser
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

fun main() {
    val start = JasonParser.parser.parseStruct("start")
    val sleepingAgent = Agent.of(
        name = "Sleeping Agent",
        beliefBase = BeliefBase.of(Belief.fromSelfSource(JasonParser.parser.parseStruct("run"))),
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = JasonParser.parser.parseStruct("start"),
                goals = listOf(
                    ActInternally.of(JasonParser.parser.parseStruct("print(\"Before Sleep\")")),
                    ActInternally.of(JasonParser.parser.parseStruct("sleep(5000)")),
                    ActInternally.of(JasonParser.parser.parseStruct("print(\"After Sleep\")")),
                    ActInternally.of(JasonParser.parser.parseStruct("stop")),
                ),
            ),
        ),
    )
    Mas.of(ExecutionStrategy.oneThreadPerMas(), Environment.of(), sleepingAgent).start()
}
