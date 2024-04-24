@file:JvmName("Entrypoint")

package it.unibo.jakta.examples.simulation.littlethumb

import it.unibo.alchemist.jakta.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.util.Iterables.randomElement
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.dsl.AgentScope
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromPercept
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromSelf
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import java.awt.Color

val knownAgents = SimpleMolecule("knownAgents")

@OptIn(ExperimentalStdlibApi::class)
fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.entrypoint(sightRadius: Double): Agent {
    val myColor = randomGenerator::nextFloat.let { Color.getHSBColor(it(), it(), it()) }
    return tokenPassAgent("Agent#${myColor.rgb.toHexString()}@${node.id}", myColor, sightRadius)
}

@OptIn(ExperimentalStdlibApi::class)
fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.entrypointWithColor(color: Color, sightRadius: Double): Agent {
    return tokenPassAgent("Agent#${color.rgb.toHexString()}@${node.id}", color, sightRadius)
}
data class ColoredAgent(val name: String, val nodeId: Int, val color: Color)

fun colorToStruct(color: Color): Struct = Struct.of(
    color.toString(),
    Integer.of(color.red),
    Integer.of(color.blue),
    Integer.of(color.green),
)

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.tokenPassAgent(
    name: String,
    color: Color,
    sightRadius: Double,
): Agent =
    with(AgentScope(name)) {
        val myColor = ColoredAgent(name, node.id, color)
        node.setConcentration(SimpleMolecule("sightRadius"), sightRadius)
        beliefs {
            fact("myColor"(ObjectRef.of(color)))
        }
        goals {
            achieve("init")
        }
        plans {
            +achieve("init") then {
                execute("writeData"(name, ObjectRef.of(myColor)))
                execute("print"("Hello world!"))
                achieve("checkBall")
            }

            +"ball"(`_`).fromPercept onlyIf {
                "myColor"(X).fromSelf and "ball"(X).fromPercept
            } then {
                execute(run, {
                    println("Hey! My position is ${alchemistEnvironment.getPosition(node)}")
                    val colors = data.filter { it.key.startsWith("Agent") }.values
                        .filterIsInstance<ColoredAgent>()
                        .subtract(setOf(myColor))

                    // val known = A.fix<Set<ColoredAgent>>() subtract setOf(myColor)
                    if (colors.isNotEmpty()) {
                        val (_, destination, newColor) = colors.randomElement(randomGenerator)
                        node.removeConcentration(SimpleMolecule("ball"))
                        alchemistEnvironment.nodes
                            .first { it.id == destination }
                            .setConcentration(SimpleMolecule("ball"), newColor)
                        println("sent ball to $destination with color $newColor")
                    } else {
                        println("Additional colours not found!")
                    }
                })
            }
        }.build()
    }

// fun entypoint(environmentForAlchemist: JaktaEnvironmentForAlchemist): Alc =
//    jaktaLocality {
//        environment(OnweAget) {
//            actions {
//                action("writeData") {
//                    setData(argument[0], argument[1])
//                }
//            }
//        }
//        agens {
//
//        }
//    }
