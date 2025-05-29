package it.unibo.jakta

import io.kotest.assertions.fail
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.effects.BeliefChange
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.stdlib.AbstractExecutionAction
import it.unibo.jakta.actions.stdlib.Achieve
import it.unibo.jakta.actions.stdlib.AddBelief
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.fsm.time.Time
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASMutableIntentionPool
import it.unibo.jakta.intentions.IntentionPoolStaticFactory
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var

class TestIntentions : DescribeSpec({
    @Suppress("VariableNaming")
    val X = Var.of("X")
    val buySomething = ASBelief.fromSelfSource(Struct.of("buy", X))
    val eatSomething = ASBelief.fromSelfSource(Struct.of("eat", X))

    val plan = ASPlan.ofAchievementGoalInvocation(
        Struct.of("test"),
        listOf(AddBelief(buySomething), AddBelief(eatSomething)),
    )
    lateinit var intention: ASIntention
    beforeEach {
        intention = ASIntention.of(plan)
    }

    describe("An intention") {
        it("should return the next goal to satisfy with nextGoal() invocation") {
            when (val nextGoal = intention.nextTask()) {
                is AddBelief -> nextGoal.belief shouldBe buySomething
                else -> fail("Next PrologGoal has wrong type")
            }
        }

        it("should retrieve the right action after pop() invocation") {
            val action = intention.nextActionToExecute()
            action shouldNotBeNull {
                val sideEffects = this.invoke(
                    ActionRequest.of(ASAgent.of().context, Time.real()),
                )

                sideEffects.size shouldBe 1
                sideEffects shouldContain BeliefChange.BeliefAddition(buySomething)

                val newIntention = intention.pop()
                newIntention.recordStack.size shouldBe 1
                newIntention.recordStack.first().nextActionToExecute().shouldBeInstanceOf<AddBelief>()
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

            data class VerifySubstitution(
                var value: Term,
            ) : AbstractExecutionAction.WithoutSideEffects() {
                override fun applySubstitution(substitution: Substitution) = VerifySubstitution(
                    value = substitution.applyTo(value) ?: error("Cannot apply substitution to $value"),
                )

                override fun execute(context: ActionInvocationContext) {
                    value.isAtom shouldBe true
                    value.castToAtom().value shouldBe "chocolate"
                }
            }

            val substitution = Substitution.of(X, Atom.of("chocolate"))
            val newIntention = ASIntention.of(
                (
                    intention.recordStack + ASPlan.ofAchievementGoalInvocation(
                        Struct.of("test"),
                        listOf(VerifySubstitution(X)),
                    ).toActivationRecord()
                    ).toMutableList(),
            )
            newIntention.recordStack.size shouldBe 2
            val substitutedIntention = newIntention.applySubstitution(substitution)
            substitutedIntention.recordStack.size shouldBe 2
            println(substitutedIntention)
            substitutedIntention.recordStack.first().nextActionToExecute().shouldBeInstanceOf<AddBelief>()
            substitutedIntention.recordStack.last().nextActionToExecute().shouldBeInstanceOf<VerifySubstitution>()
            newIntention.nextActionToExecute()?.invoke(ActionRequest.of(ASAgent.of().context, Time.real()))
        }
    }

    describe("An intention pool") {
        val intention2 = ASIntention.of(
            ASPlan.ofAchievementGoalInvocation(
                Struct.of("test"),
                listOf(Achieve(Struct.of("clean", Atom.of("home")))),
            ),
        )
        lateinit var intentionPool: ASMutableIntentionPool
        beforeEach {
            intentionPool = IntentionPoolStaticFactory.of(listOf(intention, intention2))
        }

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
            intentionPool.pop()
            intentionPool.size shouldBe 1
            intentionPool.keys shouldBe setOf(intention2.id)
            intentionPool.values.first() shouldBe intention2
        }
        it("should add a new intention after the update() invocation") {
            val intentionPool2 = IntentionPoolStaticFactory.of(listOf(intention))
            intentionPool2.updateIntention(intention2)
            intentionPool2 shouldBe intentionPool

            val i3 = intention2.pop()
            println(i3)
            intentionPool2.updateIntention(i3)
            i3.recordStack.isEmpty() shouldBe true
            println(intentionPool2)
            intentionPool2.size shouldBe 1
        }
    }
})
