import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.AddBelief
import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.plans.ActivationRecord
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Var

class TestIntentions : DescribeSpec({
    val X = Var.of("X")
    val buySomething = Belief.fromSelfSource(Struct.of("buy", X))
    val eatSomething = Belief.fromSelfSource(Struct.of("eat", X))

    val activationRecord = ActivationRecord.of(
        listOf(AddBelief(buySomething), AddBelief(eatSomething)),
        Struct.of("test"),
    )
    val intention = Intention.of(listOf(activationRecord))

    describe("An intention") {
        it("should return the next goal to satisfy with nextGoal() invocation") {
            val nextGoal = intention.nextGoal()
            nextGoal.shouldBeTypeOf<AddBelief>()
            nextGoal.belief shouldBe buySomething.rule.head
        }

        it("should remove the right goal with pop() invocation") {
            val updatedIntention = intention.pop()
            updatedIntention.recordStack.size shouldBe 1
            updatedIntention.nextGoal().value shouldBe eatSomething.rule.head
            updatedIntention.pop().recordStack shouldBe emptyList()
        }

        it("should add on top of the record stack after a push() invocation") {
            val newActivationRecord = ActivationRecord.of(
                listOf(Achieve(Atom.of("clean"))),
                Struct.of("test"),
            )
            val updatedIntention = intention.push(newActivationRecord)
            updatedIntention.nextGoal() shouldBe Achieve(Atom.of("clean"))
        }

        it("should apply a substitution on the actual Activation Record") {
            // val bb = BeliefBase.of(listOf(Belief.of(Struct.of(buy()))))

            val substitution = Substitution.of(X, Atom.of("chocolate"))
            val newIntention = Intention.of(
                intention.recordStack +
                    ActivationRecord.of(
                        listOf(Achieve(Struct.of("clean", X))),
                        Struct.of("test")
                    )
            )
            newIntention.recordStack.size shouldBe 2
            val computedIntention = newIntention.applySubstitution(substitution)
            computedIntention.recordStack.size shouldBe 2
            computedIntention.recordStack.first().goalQueue.forEach {
                it.value.args[1] shouldBe Atom.of("chocolate")
            }
            computedIntention.recordStack.last().goalQueue.forEach {
                it.value.args.first() shouldBe X
            }
        }
    }

    describe("An intention pool") {
        val intention2 = Intention.of(
            listOf(
                ActivationRecord.of(
                    listOf(
                        Achieve(Struct.of("clean", Atom.of("home")))
                    ),
                    Struct.of("test"),
                )
            )
        )
        val intentionPool = IntentionPool.of(listOf(intention, intention2))

        it("should be created correctly providing a list of intentions") {
            intentionPool[intention.id] shouldBe intention
            intentionPool[intention2.id] shouldBe intention2
            intentionPool.size shouldBe 2
        }
        it("should return the correct next intention after nextIntention() invocation") {
            val nextIntention = intentionPool.nextIntention()
            nextIntention shouldBe intention
        }
        it("should remove the next intention to be executed after pop() invocation") {
            val newPool = intentionPool.pop()
            newPool.size shouldBe 1
            newPool.keys shouldBe setOf(intention2.id)
            newPool.values.first() shouldBe intention2
        }
        it("should add a new intention after the update() invocation") {
            val intentionPool2 = IntentionPool.of(listOf(intention))
            var newPool = intentionPool2.updateIntention(intention2)
            newPool shouldBe intentionPool

            val updatedIntention2 = intention2.pop()
            newPool = newPool.updateIntention(updatedIntention2)
            newPool[intention2.id] shouldBe updatedIntention2
            newPool.size shouldBe 2
        }
    }
})
