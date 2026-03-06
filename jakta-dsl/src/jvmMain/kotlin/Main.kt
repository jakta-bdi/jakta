package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.mas.mas
import it.unibo.jakta.node.LocalNodeBuilder
import it.unibo.jakta.node.baseImpl.CoroutineNodeRunner
import it.unibo.jakta.plan.triggers
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main(): Unit = runBlocking {

    val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    launch() {
        Logger.setMinSeverity(Severity.Info)
        mas(LocalNodeBuilder()) {
            node {
                val skills = object {
                    val node = this@node
                }

                agent("Pippo") {
                    body = object {}
                    withSkills { skills }
                    believes {
                        +"testBelief"
                    }
                    hasPlans {
                        adding.belief {
                            this.takeIf { it == "testBelief" }
                        } triggers {
                            agent.print(Thread.currentThread().toString() + "Belief added: $context")
                            delay(1000)
                            agent.print(Thread.currentThread().toString() + "dopo delay")
                        }
                    }
                }
                agent("Pluto") {
                    body = object {}
                    withSkills { skills }
                    believes {
                        +"testBelief"
                    }
                    hasPlans {
                        adding.belief {
                            this.takeIf { it == "testBelief" }
                        } triggers {
                            agent.print(Thread.currentThread().toString() + "Belief added: $context")
                            delay(1000)
                            agent.print(Thread.currentThread().toString() + "dopo delay")
                        }
                    }
                }
            }
            node {
                val skills = object { val node = this@node}

                agent("Paperino") {
                    body = object {}
                    withSkills { skills }
                    believes {
                        +"testBelief"
                    }
                    hasPlans {
                        adding.belief {
                            this.takeIf { it == "testBelief" }
                        } triggers  {
                            agent.print(Thread.currentThread().toString() + "Belief added: $context")
                            delay(1000)
                            agent.print(Thread.currentThread().toString() + "dopo delay")
                        }
                    }
                }
            }
        }.run(CoroutineNodeRunner())
    }

}
