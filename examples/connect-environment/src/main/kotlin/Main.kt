import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.PlanScope
import kotlinx.coroutines.runBlocking

private class Room(var temperature: Int)

private data class TemperatureChanged(val degrees: Int) : Perception

private data class Temperature(val degrees: Int)

private class Heater(private val room: Room, private val node: Node<*>) {
    fun sense() = node.publishEvent(TemperatureChanged(room.temperature))

    fun heat() {
        room.temperature += 1
        node.publishEvent(TemperatureChanged(room.temperature))
    }
}

context(heater: Heater)
private val PlanScope<*, *, *>.heater get() = heater

private const val WARM_ENOUGH = 20

/**
 * A thermostat agent heating a room until it is warm enough.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    val room = Room(temperature = 17)
    mas(NodeBuilders.baseNode()) {
        node {
            context(Heater(room, node)) {
                agent<Temperature, String> {
                    embodiedAs { Any() }
                    handlesPerceptionEvents { perception ->
                        when (perception) {
                            is TemperatureChanged -> AgentUpdate.Belief(
                                additions = setOf(Temperature(perception.degrees)),
                                removals = beliefs.toSet(), // forget the old temperature
                            )

                            else -> null
                        }
                    }
                    hasInitialGoals { !"keepWarm" }
                    hasPlanLibrary {
                        adding.goal {
                            takeIf { it == "keepWarm" }
                        } triggers {
                            heater.sense()
                        }
                        adding.belief {
                            takeIf { it.degrees < WARM_ENOUGH }
                        } triggers {
                            agent.print("It's ${context.degrees}°C, heating")
                            heater.heat()
                        }
                        adding.belief {
                            takeIf { it.degrees >= WARM_ENOUGH }
                        } triggers {
                            agent.print("It's ${context.degrees}°C, warm enough")
                            node.terminateNode()
                        }
                    }
                }
            }
        }
    }.runLocally()
}
