@file:JvmName("SharedToken")

package it.unibo.jakta.test

import it.unibo.alchemist.jakta.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.util.Iterables.randomElement
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.dsl.AgentScope
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromPercept
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromSelf
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import java.awt.Color

val knownAgents = SimpleMolecule("knownAgents")

@OptIn(ExperimentalStdlibApi::class)
fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.entrypoint(): Agent {
    val myColor = randomGenerator::nextFloat.let { Color.getHSBColor(it(), it(), it()) }
    return tokenPassAgent("Agent#${myColor.rgb.toHexString()}@${node.id}", myColor)
}

@OptIn(ExperimentalStdlibApi::class)
fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.entrypointWithColor(color: Color): Agent {
    return tokenPassAgent("Agent#${color.rgb.toHexString()}@${node.id}", color)
}
data class ColoredAgent(val name: String, val location: Node<Any?>, val color: Color)

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.tokenPassAgent(name: String, color: Color): Agent =
    with(AgentScope(name)) {
        val myColor = ColoredAgent(name, node, color)
        beliefs {
            fact("myColor"(ObjectRef.of(color)))
        } // [myColor(source(self), $color), ball(source(percept), nodeX, red), knownAgents(source(percept),
        // [ColoredAgent("foo", red)])
        goals {
            achieve("init")
        }
        plans {
            +achieve("init") then {
                execute("writeData"(name, ObjectRef.of(myColor)))
//                execute(run, {
//                    val listOfAgents = node.getConcentration(knownAgents) as? Set<*>
//
//                    fun Node<Any?>.update() = setConcentration(
//                        knownAgents,
//                        arrayListOf<Any>(myColor).also { it.addAll(listOfAgents.orEmpty().filterNotNull()) },
//                    )
//                    alchemistEnvironment.getNeighborhood(node).forEach {
//                        it.update()
//                    }
//                    node.update()
//                })
            }

            +"ball"(`_`).fromPercept onlyIf {
                "myColor"(X).fromSelf and "ball"(X).fromPercept
            } then {
                execute(run, {
                    println(data)
                    val colors = data.filter { it.key.startsWith("Agent") }.values
                        .filterIsInstance<ColoredAgent>()
                        .subtract(setOf(myColor))

                    // val known = A.fix<Set<ColoredAgent>>() subtract setOf(myColor)
                    if (colors.isNotEmpty()) {
                        val (_, destination, newColor) = colors.randomElement(randomGenerator)
                        removeData("ball")
                        destination.setConcentration(SimpleMolecule("ball"), newColor)
                        println("sent ball to $destination with color $newColor")
                    } else {
                        println("Why is it empty?")
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
