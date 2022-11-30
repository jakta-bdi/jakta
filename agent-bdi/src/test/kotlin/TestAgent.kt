import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.Dispatcher
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.goals.actions.ImperativeInternalAction
import io.github.anitvam.agents.bdi.goals.actions.InternalActions
import io.github.anitvam.agents.bdi.goals.actions.InternalRequest
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.kotest.core.spec.style.DescribeSpec
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.utils.plus

class TestAgent : DescribeSpec({

    describe("An agent") {
        it("should call a predefined internal action specifying its name") {
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
        it("can declare custom actions to be performed") {
            val INCREMENT = object : ImperativeInternalAction("increment", 2) {
                override fun InternalRequest.action() {
                    val first: Atom = arguments.first().castToAtom()
                    val second: Var = arguments[1].castToVar()
                    val computation = Atom.of((Integer.parseInt(first.value) + 1).toString())
                    result = Substitution.of(second to computation)
                }
            }
            val start = Atom.of("start")
            val X = Var.of("X")
            val agent = Agent.of(
                events = listOf(Event.ofAchievementGoalInvocation(Achieve(start))),
                planLibrary = PlanLibrary.of(
                    Plan.ofAchievementGoalInvocation(
                        value = start,
                        goals = listOf(
                            ActInternally(Struct.of("increment", Atom.of("5"), X)),
                            ActInternally(Struct.of("print", X)),
                        )
                    )
                ),
                internalActions = InternalActions.default() + (INCREMENT.signature.name to INCREMENT)
            )
            Dispatcher.threadOf(agent).run()
        }
        it("can fail its intention using the internal action fail") {
            val start = Atom.of("start")
            val agent = Agent.of(
                events = listOf(Event.ofAchievementGoalInvocation(Achieve(start))),
                planLibrary = PlanLibrary.of(
                    Plan.ofAchievementGoalInvocation(
                        value = start,
                        goals = listOf(
                            ActInternally(Struct.of("fail")),
                            ActInternally(Struct.of("print", Atom.of("Hello World!"))),
                        ),
                    ),
                    Plan.ofAchievementGoalFailure(
                        value = start,
                        goals = listOf(
                            ActInternally(Struct.of("print", Atom.of("This Plan Failed!"))),
                        ),
                    )
                ),
            )
            Dispatcher.threadOf(agent).run()
        }
        it("fails its intention if the action to execute is not found") {
            val start = Atom.of("start")
            val agent = Agent.of(
                events = listOf(Event.ofAchievementGoalInvocation(Achieve(start))),
                planLibrary = PlanLibrary.of(
                    Plan.ofAchievementGoalInvocation(
                        value = start,
                        goals = listOf(
                            ActInternally(Struct.of("print2", Atom.of("Hello World!"))),
                            ActInternally(Struct.of("print", Atom.of("This Should Not Be Print!"))),
                        ),
                    )
                ),
            )
            Dispatcher.threadOf(agent).run()
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
                goals = listOf(
                    ActInternally(Struct.of("print2", Atom.of("Hello World!"))),
                    ActInternally(Struct.of("print", Atom.of("This Should Not Be Print!"))),
                ),
            )
        ),
    )
    Dispatcher.threadOf(agent).run()
}
