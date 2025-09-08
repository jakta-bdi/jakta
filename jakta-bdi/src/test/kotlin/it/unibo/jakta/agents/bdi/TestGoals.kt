package it.unibo.jakta.agents.bdi

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Var
import org.junit.jupiter.api.Assertions.fail

class TestGoals :
    DescribeSpec({
        val strawberryDesire = Belief.fromSelfSource(Struct.of("desire", Atom.of("strawberry")))
        val belief = Belief.fromSelfSource(Struct.of("desire", Var.of("X")))

        describe("A Goal") {
            it("should apply a substitution to its variables") {
                val substitution =
                    BeliefBase
                        .of(listOf(strawberryDesire))
                        .solve(belief)
                        .substitution

                val goal = Achieve.of(belief.rule)
                goal.value shouldBe belief.rule

                goal.applySubstitution(substitution).value shouldBe strawberryDesire.rule
            }
            it("should perform copy and keep the actual Goal type") {
                val goal = ActInternally.of(strawberryDesire.rule)
                goal.value shouldBe strawberryDesire.rule
                when (val goalCopy = goal.copy(belief.rule)) {
                    is ActInternally -> goalCopy.value shouldBe belief.rule
                    else -> fail { "Copy doesn't keep the Goal type" }
                }
            }
        }
    })
