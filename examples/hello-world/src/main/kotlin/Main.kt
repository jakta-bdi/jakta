import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matching
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.tuprolog.core.Atom
import kotlinx.coroutines.runBlocking

/**
 * The goal atom to request the helloWorldAgent to say hello.
 */
val helloGoal = Atom.of("sayHello")

/**
 * The helloWorld helloWorldAgent implementation.
 */
val helloWorldAgent = agent<PrologBelief, PrologGoal, Any> {
    embodiedAs { Any() }
    hasInitialGoals {
        !initialGoal { helloGoal }
    }
    hasPlanLibrary {
        prologPlan {
            adding.goal {
                matching { helloGoal }
            } triggers {
                agent.print("Hello, world!")
                node.terminateNode()
            }
        }
    }
}

/**
 * Entrypoint of the hello-world application.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node {
            withAgents(helloWorldAgent)
        }
    }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
}
