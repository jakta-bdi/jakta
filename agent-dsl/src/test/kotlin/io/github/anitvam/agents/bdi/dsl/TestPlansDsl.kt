package io.github.anitvam.agents.bdi.dsl

import io.github.anitvam.agents.bdi.events.AchievementGoalFailure
import io.github.anitvam.agents.bdi.events.AchievementGoalInvocation
import io.github.anitvam.agents.bdi.events.BeliefBaseAddition
import io.github.anitvam.agents.bdi.events.BeliefBaseRemoval
import io.github.anitvam.agents.bdi.events.TestGoalFailure
import io.github.anitvam.agents.bdi.events.TestGoalInvocation
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.Act
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.goals.AddBelief
import io.github.anitvam.agents.bdi.goals.EmptyGoal
import io.github.anitvam.agents.bdi.goals.RemoveBelief
import io.github.anitvam.agents.bdi.goals.Test
import io.github.anitvam.agents.bdi.goals.UpdateBelief
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeTypeOf
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Atom

class TestPlansDsl : DescribeSpec({
    describe("An achievement trigger plan") {
        it("should be created with an invocation trigger") {
            val p1 = mas {
                agent("test") {
                    plans {
                        +achieve("send_ping"(R)) onlyIf { "turn"("source"("self"), "me") } then {
                            +"turn"("source"("self"), "other")
                        }
                    }
                }
            }
            val plan = p1.agents.first().context.planLibrary.plans.first()
            plan.trigger.value.equals(Struct.of("send_ping", Var.of("R")), false) shouldBe true
            plan.trigger.shouldBeTypeOf<AchievementGoalInvocation>()
            plan.goals.size shouldBe 1
            plan.goals.first().value.equals(
                Struct.of("turn", Struct.of("source", Atom.of("self")), Atom.of("other")),
                false
            ) shouldBe true
            plan.guard.equals(
                Struct.of("turn", Struct.of("source", Atom.of("self")), Atom.of("me")),
                false
            ) shouldBe true
        }
        it("should be created with a failure trigger") {
            val p1 = mas {
                agent("test") {
                    plans {
                        -achieve("send_ping"(R)) then { }
                    }
                }
            }
            val plan = p1.agents.first().context.planLibrary.plans.first()
            plan.trigger.value.equals(Struct.of("send_ping", Var.of("R")), false) shouldBe true
            plan.trigger.shouldBeTypeOf<AchievementGoalFailure>()
            plan.goals.size shouldBe 1
            plan.goals.first() shouldBe EmptyGoal()
            plan.guard shouldBe Truth.TRUE
        }
    }
    describe("A test trigger plan") {
        it("should be created with an invocation trigger") {
            val p1 = mas {
                agent("test") {
                    plans {
                        +test("send_ping") then { }
                    }
                }
            }
            val plan = p1.agents.first().context.planLibrary.plans.first()
            plan.trigger.value.equals(Atom.of("send_ping"), false) shouldBe true
            plan.trigger.shouldBeTypeOf<TestGoalInvocation>()
            plan.goals.size shouldBe 1
            plan.goals.first() shouldBe EmptyGoal()
            plan.guard shouldBe Truth.TRUE
        }
        it("should be created with an failure trigger") {
            val p1 = mas {
                agent("test") {
                    plans {
                        -test("send_ping") then { }
                    }
                }
            }
            val plan = p1.agents.first().context.planLibrary.plans.first()
            plan.trigger.value.equals(Atom.of("send_ping"), false) shouldBe true
            plan.trigger.shouldBeTypeOf<TestGoalFailure>()
        }
    }

    describe("A belief trigger plan") {
        it("should be created with an addition trigger") {
            val p1 = mas {
                agent("test") {
                    plans {
                        +"send_ping"("source"("me")) then { }
                    }
                }
            }
            val plan = p1.agents.first().context.planLibrary.plans.first()
            plan.trigger.value.equals(
                Struct.of("send_ping", Struct.of("source", Atom.of("me"))),
                false
            ) shouldBe true
            plan.trigger.shouldBeTypeOf<BeliefBaseAddition>()
            plan.goals.size shouldBe 1
            plan.goals.first() shouldBe EmptyGoal()
            plan.guard shouldBe Truth.TRUE
        }
        it("should be created with an removal trigger") {
            val p1 = mas {
                agent("test") {
                    plans {
                        -"send_ping"("source"("self")) then { }
                    }
                }
            }
            val plan = p1.agents.first().context.planLibrary.plans.first()
            plan.trigger.value.equals(
                Struct.of("send_ping", Struct.of("source", Atom.of("self"))),
                false
            ) shouldBe true
            plan.trigger.shouldBeTypeOf<BeliefBaseRemoval>()
        }
    }
    describe("A Plan Body") {
        it("can have an achieve goal") {
            val p1 = mas {
                agent("test") {
                    plans {
                        +achieve("send_ping"(R)) then {
                            achieve("sendMessage"(R, "ping"))
                        }
                    }
                }
            }
            val plan = p1.agents.first().context.planLibrary.plans.first()
            plan.goals.size shouldBe 1
            plan.goals.first().value.equals(
                Struct.of("sendMessage", Var.of("R"), Atom.of("ping")),
                false
            ) shouldBe true
            plan.goals.first().shouldBeInstanceOf<Achieve>()
        }
        it("can have a test goal") {
            val p1 = mas {
                agent("test") {
                    plans {
                        +achieve("send_ping"(R)) then {
                            test("send_ping"("source"("self")))
                        }
                    }
                }
            }
            val plan = p1.agents.first().context.planLibrary.plans.first()
            plan.goals.size shouldBe 1
            plan.goals.first().value.equals(
                Struct.of("send_ping", Struct.of("source", Atom.of("self"))),
                false
            ) shouldBe true
            plan.goals.first().shouldBeInstanceOf<Test>()
        }
        it("can have a belief base addition goal") {
            val p1 = mas {
                agent("test") {
                    plans {
                        +achieve("send_ping"(R)) then {
                            add("send_ping"("source"("self")))
                            +"send_ping"("source"("self"))
                        }
                    }
                }
            }
            val plan = p1.agents.first().context.planLibrary.plans.first()
            plan.goals.size shouldBe 2
            plan.goals.forEach {
                it.value.equals(
                    Struct.of("send_ping", Struct.of("source", Atom.of("self"))),
                    false
                ) shouldBe true
            }
            plan.goals.forEach {
                it.shouldBeInstanceOf<AddBelief>()
            }
        }
        it("can have a belief base removal goal") {
            val p1 = mas {
                agent("test") {
                    plans {
                        +achieve("send_ping"(R)) then {
                            remove("send_ping"("source"("other")))
                            -"send_ping"("source"("other"))
                        }
                    }
                }
            }
            val plan = p1.agents.first().context.planLibrary.plans.first()
            plan.goals.size shouldBe 2
            plan.goals.forEach {
                it.value.equals(
                    Struct.of("send_ping", Struct.of("source", Atom.of("other"))),
                    false
                ) shouldBe true
            }
            plan.goals.forEach {
                it.shouldBeInstanceOf<RemoveBelief>()
            }
        }
        it("can have a belief base update goal") {
            val p1 = mas {
                agent("test") {
                    plans {
                        +achieve("send_ping"(R)) then {
                            update("send_ping"("source"("percept")))
                        }
                    }
                }
            }
            val plan = p1.agents.first().context.planLibrary.plans.first()
            plan.goals.size shouldBe 1
            plan.goals.forEach {
                it.value.equals(
                    Struct.of("send_ping", Struct.of("source", Atom.of("percept"))),
                    false
                ) shouldBe true
            }
            plan.goals.forEach {
                it.shouldBeInstanceOf<UpdateBelief>()
            }
        }
        it("can perform an external action") {
            val p1 = mas {
                agent("test") {
                    plans {
                        +achieve("send_ping"(R)) then {
                            act("send_ping"("source"("self")))
                        }
                    }
                }
            }
            val plan = p1.agents.first().context.planLibrary.plans.first()
            plan.goals.size shouldBe 1
            plan.goals.forEach {
                it.value.equals(
                    Struct.of("send_ping", Struct.of("source", Atom.of("self"))),
                    false
                ) shouldBe true
            }
            plan.goals.forEach {
                it.shouldBeInstanceOf<Act>()
            }
        }
        it("can perform an internal action") {
            val p1 = mas {
                agent("test") {
                    plans {
                        +achieve("send_ping"(R)) then {
                            iact("send_ping"("source"("pong")))
                        }
                    }
                }
            }
            val plan = p1.agents.first().context.planLibrary.plans.first()
            plan.goals.size shouldBe 1
            plan.goals.forEach {
                it.value.equals(
                    Struct.of("send_ping", Struct.of("source", Atom.of("pong"))),
                    false
                ) shouldBe true
            }
            plan.goals.forEach {
                it.shouldBeInstanceOf<ActInternally>()
            }
        }
    }
    describe("A Plan") {
        it("should keep the scope of the variables") {
            val p1 = mas {
                agent("test") {
                    plans {
                        +achieve("send_ping"(R)) then {
                            achieve("sendMessage"(R, "ping"))
                        }
                    }
                }
            }
            val plan = p1.agents.first().context.planLibrary.plans.first()
            plan.trigger.value.args.first().shouldBeInstanceOf<Var>()
            plan.trigger.value.args.first() shouldBe plan.goals.first().value.args.first()
        }
    }
})
