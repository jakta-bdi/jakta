import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.SingleThreadedExecutionStrategy
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.kotest.core.spec.style.DescribeSpec
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

class TestEnvironment : DescribeSpec({
    describe("An environment") {
        it("should be shared between agents") {
            val env = Environment.of()

            val start = Atom.of("start")
            val alice = Agent.of(
                events = listOf(Event.ofAchievementGoalInvocation(Achieve(start))),
                planLibrary = PlanLibrary.of(
                    Plan.ofAchievementGoalInvocation(
                        value = start,
                        goals = listOf(
                            ActInternally(Struct.of("print", Atom.of("Hello, I'm Alice"))),
                        ),
                    ),
                ),
            )

            val bob = Agent.of(
                events = listOf(Event.ofAchievementGoalInvocation(Achieve(start))),
                planLibrary = PlanLibrary.of(
                    Plan.ofAchievementGoalInvocation(
                        value = start,
                        goals = listOf(
                            ActInternally(Struct.of("print", Atom.of("Hello, I'm Bob"))),
                        ),
                    ),
                ),
            )

            val mas = Mas.of(SingleThreadedExecutionStrategy(), env, alice, bob)

            mas.start()
        }
    }
})
