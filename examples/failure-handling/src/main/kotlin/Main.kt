import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.satisfies
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.jakta.print
import it.unibo.tuprolog.core.Atom
import kotlinx.coroutines.runBlocking

private val start = Atom.of("start")
private val beans = Atom.of("beans")

/**
 * A barista showing how plan failure is handled, in the spirit of Jason's `failure` demo.
 *
 * Grinding needs beans, and there are only enough for one coffee. When the second `grind` goal has no applicable
 * plan it fails, and so do the goals above it, until one has a failure plan (`-!prepare`, here `failing.goal`).
 * That plan recovers by buying beans and trying again, so the goals above it carry on as if nothing happened.
 */
val barista = agent<PrologBelief, PrologGoal, Any>(BaseAgentID("barista")) {
    embodiedAs { Any() }
    believes {
        +initialBelief { "has"(beans) }
    }
    hasInitialGoals {
        !initialGoal { start }
    }
    hasPlanLibrary {
        prologPlan {
            adding.goal { matchingGoal { start } } triggers {
                agent.achieve(goal { "serve"(Atom.of("espresso")) })
                agent.achieve(goal { "serve"(Atom.of("cappuccino")) })
                agent.print("All orders served.")
                node.terminateNode()
            }
        }
        prologPlan {
            adding.goal { matchingGoal { "serve"(C) } } triggers {
                agent.print("Order: ", C)
                agent.achieve(goal { "prepare"(C) })
                agent.print("Here is your ", C, "!")
            }
        }
        prologPlan {
            adding.goal { matchingGoal { "prepare"(C) } } triggers {
                agent.achieve(goal { "grind"(beans) })
                agent.print("Brewing ", C)
            }
        }
        // the only plan to grind: without beans there is no applicable plan, and the goal fails
        prologPlan {
            adding.goal { matchingGoal { "grind"(beans) } } onlyWhen {
                satisfies { "has"(beans) }
            } triggers {
                agent.print("Grinding the beans")
                agent.forget(belief { "has"(beans) })
            }
        }
        // intercepts the failure of prepare (coming from grind, two levels below) and recovers
        prologPlan {
            failing.goal { matchingGoal { "prepare"(C) } } triggers {
                agent.print("I could not prepare the ", C, ": buying beans and trying again")
                agent.believe(belief { "has"(beans) })
                agent.achieve(goal { "prepare"(C) })
            }
        }
    }
}

/**
 * Entrypoint of the failure-handling example.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node {
            withAgents(barista)
        }
    }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
}
