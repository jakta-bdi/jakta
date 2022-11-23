import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.goals.Achieve
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Var

class TestGoal : DescribeSpec({
    val strawberryDesire = Belief.of(Struct.of("desire", Atom.of("strawberry")))
    val struct = Struct.of("desire", Var.of("X"))

    describe("A Goal") {
        it("should apply a substitution to its variables") {
            val substitution = BeliefBase.of(listOf(strawberryDesire))
                .solve(struct)
                .substitution

            val goal = Achieve(struct)
            goal.value shouldBe struct

            goal.applySubstitution(substitution).value shouldBe strawberryDesire.head
        }
    }
})
