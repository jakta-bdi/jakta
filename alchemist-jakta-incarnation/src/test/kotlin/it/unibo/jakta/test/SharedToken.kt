@file:JvmName("SharedToken")

package it.unibo.jakta.test

import it.unibo.alchemist.jakta.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.fix
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
const val PASS_THE_BALL = "passTheBall"

@OptIn(ExperimentalStdlibApi::class)
fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.entrypoint(): Agent {
    val myColor = randomGenerator::nextFloat.let { Color.getHSBColor(it(), it(), it()) }
    return tokenPassAgent("Agent#${myColor.rgb.toHexString()}@${node.id}", myColor)
}

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.tokenPassAgent(name: String, color: Color): Agent =
    with(AgentScope(name)) {
        data class ColoredAgent(val name: String, val location: Node<Any?>, val color: Color)
        val myColor = ColoredAgent(name, node, color)
        beliefs {
            fact {
                "myColor"(ObjectRef.of(color))
            }
        } // [myColor(source(self), $color), ball(source(percept), nodeX, red), knownAgents(source(percept),
        // [ColoredAgent("foo", red)])
        goals {
            achieve("init")
            achieve(PASS_THE_BALL)
        }
        plans {
            +achieve("init") then {
                execute(run, {
                    val listOfAgents = node.getConcentration(knownAgents) as? Set<*>

                    // Cannot convert class it.unibo.jakta.test.SharedToken$tokenPassAgent$1$ColoredAgent into class it.unibo.tuprolog.core.Term
                    fun Node<Any?>.update() = setConcentration(
                        knownAgents,
                        arrayListOf<Any>(myColor).also { it.addAll(listOfAgents.orEmpty().filterNotNull()) },
                    )
                    alchemistEnvironment.getNeighborhood(node).forEach {
                        it.update()
                    }
                    node.update()
                })
            }

            +achieve(PASS_THE_BALL) onlyIf {
                "myColor"(X).fromSelf and "ball"(`_`, X).fromPercept and A `is` knownAgents.name(A).fromPercept
            } then {
                execute(run, {
                    val known = A.fix<Set<ColoredAgent>>() subtract setOf(myColor)
                    if (known.isNotEmpty()) {
                        val (_, destination, newColor) = known.randomElement(randomGenerator)
                        destination.setConcentration(SimpleMolecule("ball"), newColor)
                        println("sent ball to $destination with color $newColor")
                    }
                })
            }
        }.build()
    }
