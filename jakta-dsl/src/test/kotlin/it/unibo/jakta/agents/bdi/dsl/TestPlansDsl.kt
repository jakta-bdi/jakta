package it.unibo.jakta.agents.bdi.dsl

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeTypeOf
import it.unibo.jakta.agents.bdi.dsl.plans.PlansScope
import it.unibo.jakta.agents.bdi.events.AchievementGoalFailure
import it.unibo.jakta.agents.bdi.events.AchievementGoalInvocation
import it.unibo.jakta.agents.bdi.events.BeliefBaseAddition
import it.unibo.jakta.agents.bdi.events.BeliefBaseRemoval
import it.unibo.jakta.agents.bdi.events.TestGoalFailure
import it.unibo.jakta.agents.bdi.events.TestGoalInvocation
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.Act
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.goals.AddBelief
import it.unibo.jakta.agents.bdi.goals.EmptyGoal
import it.unibo.jakta.agents.bdi.goals.RemoveBelief
import it.unibo.jakta.agents.bdi.goals.Test
import it.unibo.jakta.agents.bdi.goals.UpdateBelief
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.core.Var

class TestPlansDsl : DescribeSpec({
    fun testingPlans(function: PlansScope.() -> Unit): Iterable<Plan> = PlansScope().also { it.function() }.build()

    describe("An achievement trigger plan") {
        it("should be created with an invocation trigger") {
            val plan = testingPlans {
                +achieve("send_ping"(R)) onlyIf { "turn"("source"("self"), "me") } then {
                    +"turn"("source"("self"), "other")
                }
            }.first()
            plan.trigger.value.equals(Struct.of("send_ping", Var.of("R")), false) shouldBe true
            plan.trigger.shouldBeTypeOf<AchievementGoalInvocation>()
            plan.goals.size shouldBe 1
            plan.goals.first().value.equals(
                Struct.of("turn", Struct.of("source", Atom.of("self")), Atom.of("other")),
                false,
            ) shouldBe true
            plan.guard.equals(
                Struct.of("turn", Struct.of("source", Atom.of("self")), Atom.of("me")),
                false,
            ) shouldBe true
        }

        it("should be created with a failure trigger") {
            val plan = testingPlans {
                -achieve("send_ping"(R)) then { }
            }.first()
            plan.trigger.value.equals(Struct.of("send_ping", Var.of("R")), false) shouldBe true
            plan.trigger.shouldBeTypeOf<AchievementGoalFailure>()
            plan.goals.size shouldBe 1
            plan.goals.first() shouldBe EmptyGoal()
            plan.guard shouldBe Truth.TRUE
        }
    }
    describe("A test trigger plan") {
        it("should be created with an invocation trigger") {
            val plan = testingPlans {
                +test("send_ping") then { }
            }.first()
            plan.trigger.value.equals(Atom.of("send_ping"), false) shouldBe true
            plan.trigger.shouldBeTypeOf<TestGoalInvocation>()
            plan.goals.size shouldBe 1
            plan.goals.first() shouldBe EmptyGoal()
            plan.guard shouldBe Truth.TRUE
        }

        it("should be created with an failure trigger") {
            val plan = testingPlans {
                -test("send_ping") then { }
            }.first()
            plan.trigger.value.equals(Atom.of("send_ping"), false) shouldBe true
            plan.trigger.shouldBeTypeOf<TestGoalFailure>()
        }
    }

    describe("A belief trigger plan") {
        it("should be created with an addition trigger") {
            val plan = testingPlans {
                +"send_ping"("source"("me")) then { }
            }.first()
            plan.trigger.value.equals(
                Struct.of("send_ping", Struct.of("source", Atom.of("me"))),
                false,
            ) shouldBe true
            plan.trigger.shouldBeTypeOf<BeliefBaseAddition>()
            plan.goals.size shouldBe 1
            plan.goals.first() shouldBe EmptyGoal()
            plan.guard shouldBe Truth.TRUE
        }
        it("should be created with an removal trigger") {
            val plan = testingPlans {
                -"send_ping"("source"("self")) then { }
            }.first()
            plan.trigger.value.equals(
                Struct.of("send_ping", Struct.of("source", Atom.of("self"))),
                false,
            ) shouldBe true
            plan.trigger.shouldBeTypeOf<BeliefBaseRemoval>()
        }
    }
    describe("A Plan Body") {
        it("can have an achieve goal") {
            val plan = testingPlans {
                +achieve("send_ping"(R)) then {
                    achieve("sendMessage"(R, "ping"))
                }
            }.first()
            plan.goals.size shouldBe 1
            plan.goals.first().value.equals(
                Struct.of("sendMessage", Var.of("R"), Atom.of("ping")),
                false,
            ) shouldBe true
            plan.goals.first().shouldBeInstanceOf<Achieve>()
        }

        it("can have a test goal") {
            val plan = testingPlans {
                +achieve("send_ping"(R)) then {
                    test("send_ping"("source"("self")))
                }
            }.first()
            plan.goals.size shouldBe 1
            plan.goals.first().value.equals(
                Struct.of("send_ping", Struct.of("source", Atom.of("self"))),
                false,
            ) shouldBe true
            plan.goals.first().shouldBeInstanceOf<Test>()
        }

        it("can have a belief base addition goal") {
            val plan = testingPlans {
                +achieve("send_ping"(R)) then {
                    add("send_ping"("source"("self")))
                    +"send_ping"("source"("self"))
                }
            }.first()
            plan.goals.size shouldBe 2
            plan.goals.forEach {
                it.value.equals(
                    Struct.of("send_ping", Struct.of("source", Atom.of("self"))),
                    false,
                ) shouldBe true
            }
            plan.goals.forEach {
                it.shouldBeInstanceOf<AddBelief>()
            }
        }

        it("can both specify the source or not of the belief base addition goal") {
            val planExplicit = testingPlans {
                +achieve("send_ping"(R)) then {
                    add("send_ping"("source"("self")))
                    +"send_ping"("source"("self"))
                }
            }.first()
            val planImplicit = testingPlans {
                +achieve("send_ping"(R)) then {
                    add("send_ping")
                    +"send_ping"
                }
            }.first()
            planExplicit.goals.size shouldBe planImplicit.goals.size
            (planExplicit.goals + planImplicit.goals).forEach {
                it.value.equals(
                    Struct.of("send_ping", Struct.of("source", Atom.of("self"))),
                    false,
                ) shouldBe true
            }
            (planExplicit.goals + planImplicit.goals).forEach {
                it.shouldBeInstanceOf<AddBelief>()
            }
        }

        it("can have a belief base removal goal") {
            val plan = testingPlans {
                +achieve("send_ping"(R)) then {
                    remove("send_ping"("source"("other")))
                    -"send_ping"("source"("other"))
                }
            }.first()
            plan.goals.size shouldBe 2
            plan.goals.forEach {
                it.value.equals(
                    Struct.of("send_ping", Struct.of("source", Atom.of("other"))),
                    false,
                ) shouldBe true
            }
            plan.goals.forEach {
                it.shouldBeInstanceOf<RemoveBelief>()
            }
        }
        it("can have a belief base update goal") {
            val plan = testingPlans {
                +achieve("send_ping"(R)) then {
                    update("send_ping"("source"("percept")))
                }
            }.first()
            plan.goals.size shouldBe 1
            plan.goals.forEach {
                it.value.equals(
                    Struct.of("send_ping", Struct.of("source", Atom.of("percept"))),
                    false,
                ) shouldBe true
            }
            plan.goals.forEach {
                it.shouldBeInstanceOf<UpdateBelief>()
            }
        }
        it("can perform an external action") {
            val plan = testingPlans {
                +achieve("send_ping"(R)) then {
                    execute("send_ping"("source"("self")))
                }
            }.first()
            plan.goals.size shouldBe 1
            plan.goals.forEach {
                it.value.equals(
                    Struct.of("send_ping", Struct.of("source", Atom.of("self"))),
                    false,
                ) shouldBe true
            }
            plan.goals.forEach {
                it.shouldBeInstanceOf<Act>()
            }
        }
        it("can perform an internal action") {
            val plan = testingPlans {
                +achieve("send_ping"(R)) then {
                    iact("send_ping"("source"("pong")))
                }
            }.first()
            plan.goals.size shouldBe 1
            plan.goals.forEach {
                it.value.equals(
                    Struct.of("send_ping", Struct.of("source", Atom.of("pong"))),
                    false,
                ) shouldBe true
            }
            plan.goals.forEach {
                it.shouldBeInstanceOf<ActInternally>()
            }
        }
    }
    describe("A Plan") {
        it("should keep the scope of the variables") {
            val plan = testingPlans {
                +achieve("send_ping"(R)) then {
                    achieve("sendMessage"(R, "ping"))
                }
            }.first()
            plan.trigger.value.args.first().shouldBeInstanceOf<Var>()
            plan.trigger.value.args.first() shouldBe plan.goals.first().value.args.first()
        }
    }
})
