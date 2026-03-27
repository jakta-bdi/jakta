@file:JvmName("HelloWorldMolecule")
package it.unibo.jakta.test

import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Position
import it.unibo.jakta.dsl.RuntimeNodes
import it.unibo.jakta.dsl.device
import it.unibo.jakta.node.LocalNodeBuilder
import it.unibo.jakta.plan.triggers

    fun <P : Position<P>> JaktaForAlchemistRuntime<P>.entrypoint() =
        device(LocalNodeBuilder()) {
            node {
                agent("Alice") {
                    body = object {}
                    withSkills { object{} }
                    hasInitialGoals {
                        !"greet"
                    }
                    hasPlans {
                        adding.goal {
                            this.takeIf { it == "greet" }
                        } triggers {
                            agent.print("Hello World!")
                        }
                    }
                }
            }
        }
