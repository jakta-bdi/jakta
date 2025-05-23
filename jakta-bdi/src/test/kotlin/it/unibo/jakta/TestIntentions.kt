package it.unibo.jakta

import io.kotest.assertions.fail
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.unibo.jakta.actions.effects.BeliefChange
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.stdlib.Achieve
import it.unibo.jakta.actions.stdlib.AddBelief
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.fsm.time.Time
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.IntentionPoolStaticFactory
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Var
import java.util.Calendar

class TestIntentions : DescribeSpec({
    @Suppress("VariableNaming")
    val X = Var.of("X")
    val buySomething = ASBelief.fromSelfSource(Struct.of("buy", X))
    val eatSomething = ASBelief.fromSelfSource(Struct.of("eat", X))

    val plan = ASPlan.ofAchievementGoalInvocation(
        Struct.of("test"),
        listOf(AddBelief(buySomething), AddBelief(eatSomething)),
    )

    val intention = ASIntention.of(plan)

    describe("An intention") {
        it("should return the next goal to satisfy with nextGoal() invocation") {
            when (val nextGoal = intention.nextTask()) {
                is AddBelief -> nextGoal.belief shouldBe buySomething.content.head
                else -> fail("Next PrologGoal has wrong type")
            }
        }

        it("should retrieve the right action after pop() invocation") {
            val updatedIntention = intention.pop()
            updatedIntention shouldNotBeNull {
                this.signature.arity shouldBe 1
                this.signature.name shouldBe "AddBelief"
                val sideEffects = this.invoke(
                    ActionRequest.of(ASAgent.of().context, Time.real(Calendar.getInstance().timeInMillis))
                )
                sideEffects.size shouldBe 1
                sideEffects shouldContain BeliefChange.BeliefAddition(buySomething)

                intention.pop()
                intention.recordStack shouldBe emptyList()
            }

        }

        it("should add on top of the record stack after a push() invocation") {
            val newActivationRecord = ASPlan.ofAchievementGoalInvocation(
                Struct.of("test"),
                listOf(Achieve(Atom.of("clean"))),
            ).toActivationRecord()
            intention.push(newActivationRecord)
            intention.nextTask() shouldBe Achieve(Atom.of("clean"))
        }

        it("should apply a substitution on the actual Activation Record") {
            // val bb = ASBeliefBase.of(listOf(ASBelief.of(Struct.of(buy()))))

            val substitution = Substitution.of(X, Atom.of("chocolate"))
            val newIntention = ASIntention.of(
                recordStack = (intention.recordStack +
                    ASPlan.ofAchievementGoalInvocation(
                        Struct.of("test"),
                        listOf(Achieve(Struct.of("clean", X))),
                    ).toActivationRecord()).toMutableList(),
            )
            newIntention.recordStack.size shouldBe 2
            newIntention.applySubstitution(substitution)
            newIntention.recordStack.size shouldBe 2
            newIntention.recordStack.first().taskQueue.forEach {
                it.signature.name shouldBe "Achieve"
            }
            newIntention.recordStack.last().taskQueue.forEach {
                it.signature.name shouldBe "Achieve"
            }
        }
    }

    describe("An intention pool") {
        val intention2 = ASIntention.of(
            ASPlan.ofAchievementGoalInvocation(
                Struct.of("test"),
                listOf(Achieve(Struct.of("clean", Atom.of("home")))),
            ),
        )
        val intentionPool = IntentionPoolStaticFactory.of(listOf(intention, intention2))

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
            val intention = intentionPool.pop()
            intentionPool.size shouldBe 1
            intentionPool.keys shouldBe setOf(intention2.id)
            intentionPool.values.first() shouldBe intention2
        }
        it("should add a new intention after the update() invocation") {
            val intentionPool2 = IntentionPoolStaticFactory.of(listOf(intention))
            intentionPool2.updateIntention(intention2)
            intentionPool2 shouldBe intentionPool

            intention2.pop()
            val newPool = IntentionPoolStaticFactory.of(listOf(intention2))
            newPool[intention2.id] shouldBe intention2
            newPool.size shouldBe 2
        }
    }
})
