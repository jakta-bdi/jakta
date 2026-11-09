import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.agent.achieveWithResult
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/** The goals of the barista. */
sealed interface Task

/** Open the café and serve the orders. */
data object Open : Task

/** Play some music. */
data object PlayMusic : Task

/** Serve a [coffee] to a customer. */
data class Serve(val coffee: String) : Task

/** Prepare a [coffee], returning the cup. */
data class Prepare(val coffee: String) : Task

/** Grind some beans. */
data object Grind : Task

/*
 * A barista serving two orders, showing the ways a plan pursues other goals.
 * Each order is a sub-goal achieved in the same intention, and preparing a coffee returns the cup it made,
 * while music plays in an intention of its own.
 * Grinding needs beans, and there are only enough for one coffee: the second grind has no applicable plan and fails,
 * so preparing the cappuccino fails too, until a failure plan buys beans and prepares it again.
 */

/**
 * Runs the barista.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node {
            agent<String, Task> {
                embodiedAs { Any() }
                believes {
                    +"beans"
                }
                hasInitialGoals {
                    !Open
                }
                hasPlanLibrary {
                    adding.goal { this as? Open } triggers {
                        agent.alsoAchieve(PlayMusic)
                        agent.achieve(Serve("espresso"))
                        agent.achieve(Serve("cappuccino"))
                        agent.print("All orders served")
                        node.terminateNode()
                    }
                    adding.goal { this as? PlayMusic } triggers {
                        delay(500.milliseconds)
                        agent.print("Music on, while the barista works")
                    }
                    adding.goal { this as? Serve } triggers {
                        agent.print("Order: ${context.coffee}")
                        val cup: String = agent.achieveWithResult(Prepare(context.coffee))
                        agent.print("Here is your $cup!")
                    }
                    adding.goal { this as? Prepare } triggers {
                        agent.achieve(Grind)
                        agent.print("Brewing the ${context.coffee}")
                        "cup of ${context.coffee}"
                    }
                    adding.goal { this as? Grind } onlyWhen { context.takeIf { "beans" in beliefs } } triggers {
                        delay(1.seconds)
                        agent.print("Grinding the beans")
                        agent.forget("beans")
                    }
                    failing.goal { this as? Prepare } triggers {
                        agent.print("I could not prepare the ${context.coffee}: buying beans and trying again")
                        agent.believe("beans")
                        val cup: String = agent.achieveWithResult(context)
                        cup
                    }
                }
            }
        }
    }.runLocally()
}
