package it.unibo.jakta.agents.bdi.dsl

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.jakta.JaktaLogicProgrammingScope

class TestLogicProgrammingScopeExtensions :
    DescribeSpec({

        fun <R> jaktalp(function: JaktaLogicProgrammingScope.() -> R): R = JaktaLogicProgrammingScope.empty().function()

        describe("Beliefs creation using DSL") {
            it("Self-sourced starting from a string") {
                val outputBelief =
                    jaktalp {
                        "pippo".fromSelf
                    }
                Belief.wrap(outputBelief) shouldBe Belief.fromSelfSource(Atom.of("pippo"))
            }
            it("Self-sourced starting from a 2pkt Struct") {
                val outputBelief =
                    jaktalp {
                        "pippo"("pluto").fromSelf
                    }
                Belief.wrap(outputBelief) shouldBe Belief.fromSelfSource(Struct.of("pippo", Atom.of("pluto")))
            }

            it("Percept-sourced starting from a string") {
                val outputBelief =
                    jaktalp {
                        "pippo".fromPercept
                    }
                Belief.wrap(outputBelief) shouldBe Belief.fromPerceptSource(Atom.of("pippo"))
            }
            it("Percept-sourced starting from a 2pkt Struct") {
                val outputBelief =
                    jaktalp {
                        "pippo"("pluto").fromPercept
                    }
                Belief.wrap(outputBelief) shouldBe Belief.fromPerceptSource(Struct.of("pippo", Atom.of("pluto")))
            }
        }
    })
