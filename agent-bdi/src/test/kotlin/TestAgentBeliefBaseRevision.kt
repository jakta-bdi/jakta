import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.Dispatcher
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

class TestAgentBeliefBaseRevision : DescribeSpec({
    describe("An empty agent") {
        it("should have empty sets in its Context") {
            val agentContext = Agent.empty().context
            agentContext.beliefBase.count() shouldBe 0
            agentContext.events.size shouldBe 0
            agentContext.intentions.size shouldBe 0
            agentContext.planLibrary.plans.size shouldBe 0
        }
    }

    describe("An agent") {
        it("should print") {
        }
    }
})

fun main() {
    val start = Atom.of("start")
    val agent = Agent.of(
        events = listOf(Event.ofAchievementGoalInvocation(Achieve(start))),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = start,
                goals = listOf(ActInternally(Struct.of("print", Atom.of("Hello World!"))))
            )
        )
    )
    Dispatcher.threadOf(agent).run()
}
