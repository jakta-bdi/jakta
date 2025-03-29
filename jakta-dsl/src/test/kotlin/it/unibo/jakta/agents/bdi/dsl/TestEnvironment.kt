package it.unibo.jakta.agents.bdi.dsl

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.tuprolog.core.Struct

class TestEnvironment :
    DescribeSpec({
        describe("An Environment") {
            it("should be built from a DSL") {
                val e =
                    mas {
                        environment {
                            actions {
                                action("eat", 1) {
                                    // val food: Struct by argument(0)
                                    val food: Struct = argument(0)
                                    println("I'm eating $food")
                                }
                                action("dummy", 0) {
                                    println("I'm eating something")
                                }
                            }
                        }
                    }
                e.environment.externalActions.values.size shouldBe 2
                e.environment.externalActions.values
                    .first()
                    .signature.name shouldBe "eat"
                e.environment.externalActions.values
                    .last()
                    .signature.name shouldBe "dummy"
            }
        }
    })
