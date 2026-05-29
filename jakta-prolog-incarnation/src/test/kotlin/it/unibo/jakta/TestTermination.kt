package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.belief.matching
import it.unibo.jakta.dsl.mas.mas
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.condition
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.CoroutineNodeRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class TestTermination {


    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Warn)
    }

    @Test
    fun `test termination with recurring non-blocking plans`() {
        runTest {
            val job = launch {
                mas(LocalNodeBuilder()) {
                    node {
                        agent("Alice") {
                            embodiedAs { object {} }
                            withSkills { TerminationSkill(it) }
                            believes {
                                +initialBelief { "belief"(1) }
                            }
                            hasPlans {

                                prologPlan {
                                    adding.belief {
                                        matching { "belief"(N) }
                                    } triggers {
                                        with(context) {
                                            val n = N.value.getAs<Int>()
                                            agent.print("Belief is $n")
                                            agent.believe(belief { "belief"(n + 1) })
                                            skills.terminateNode()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.run(CoroutineNodeRunner())
            }
            job.join()
        }
    }
}
