package it.unibo.jakta.js

import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

/**
 * Each scenario is written twice, with the Kotlin DSL and with the JS facade using native `async`/`await` bodies,
 * and the two must produce the same trace.
 */
class TestKotlinParity {

    @Test
    fun interleavingIntentions() = assertParity(
        goals = listOf("g1", "g2"),
        kotlin = { log ->
            hasPlanLibrary {
                adding.goal { takeIf { it == "sub" } } triggers { log += "sub" }
                adding.goal { takeIf { it == "g1" || it == "g2" } } triggers {
                    log += "$context:a"
                    agent.achieve("sub")
                    log += "$context:b"
                    agent.achieve("sub")
                    log += "$context:c"
                    if (context == "g2") node.terminateNode()
                }
            }
        },
        js = { log ->
            onGoalAdded({ if (it == "sub") true else null }, { log("sub") })
                .onGoalAdded({ if (it == "g1" || it == "g2") it else null }, jsBodies(log).interleaving)
        },
    )

    @Test
    fun resumedCodeRunsBeforeLaterEvents() = assertParity(
        goals = listOf("g1", "g2"),
        kotlin = { log ->
            hasPlanLibrary {
                adding.goal { takeIf { it == "sub" } } triggers { log += "sub" }
                adding.goal { takeIf { it == "g1" } } triggers {
                    log += "g1:a"
                    agent.achieve("sub")
                    log += "g1:b"
                }
                adding.goal { takeIf { it == "g2" } } triggers { (1..3).forEach { agent.alsoAchieve("s$it") } }
                adding.goal { takeIf { it.startsWith("s") && it != "sub" } } triggers {
                    log += context
                    agent.alsoAchieve("t" + context.drop(1))
                }
                adding.goal { takeIf { it.startsWith("t") } } triggers {
                    log += context
                    if (context == "t3") node.terminateNode()
                }
            }
        },
        js = { log ->
            onGoalAdded({ if (it == "sub") true else null }, { log("sub") })
                .onGoalAdded({ if (it == "g1") true else null }, jsBodies(log).awaitThenLog)
                .onGoalAdded({ if (it == "g2") true else null }, { self ->
                    (1..3).forEach { self.alsoAchieve("s$it") }
                })
                .onGoalAdded({ if (it is String && it.startsWith("s") && it != "sub") it else null }, { self ->
                    log(self.context as String)
                    self.alsoAchieve("t" + (self.context as String).drop(1))
                })
                .onGoalAdded({ if (it is String && it.startsWith("t")) it else null }, { self ->
                    log(self.context as String)
                    if (self.context == "t3") self.terminateNode()
                })
        },
    )

    @Test
    fun cancellationWhileAchieving() = assertParity(
        goals = listOf("long", "stop"),
        kotlin = { log ->
            hasPlanLibrary {
                adding.goal { takeIf { it == "slow" } } triggers { delay(SLOW) }
                adding.goal { takeIf { it == "long" } } triggers {
                    try {
                        log += "before"
                        agent.achieve("slow")
                        log += "after"
                    } finally {
                        log += "finally"
                    }
                }
                adding.goal { takeIf { it == "stop" } } triggers {
                    log += "terminating"
                    node.terminateNode()
                }
            }
        },
        js = { log ->
            onGoalAdded({ if (it == "slow") true else null }, jsBodies(log).slow)
                .onGoalAdded({ if (it == "long") true else null }, jsBodies(log).achieveInTry)
                .onGoalAdded({ if (it == "stop") true else null }, { self ->
                    log("terminating")
                    self.terminateNode()
                })
        },
    )

    @Test
    fun cancellationWhileDelaying() = assertParity(
        goals = listOf("long", "stop"),
        kotlin = { log ->
            hasPlanLibrary {
                adding.goal { takeIf { it == "long" } } triggers {
                    log += "before"
                    delay(SLOW)
                    log += "after"
                }
                adding.goal { takeIf { it == "stop" } } triggers {
                    delay(SLOW / 2)
                    log += "terminating"
                    node.terminateNode()
                }
            }
        },
        js = { log ->
            onGoalAdded({ if (it == "long") true else null }, jsBodies(log).delayThenLog)
                .onGoalAdded({ if (it == "stop") true else null }, jsBodies(log).delayThenTerminate)
        },
    )

    @Test
    fun subgoalFailureIsCaught() = assertParity(
        goals = listOf("main"),
        kotlin = { log ->
            hasPlanLibrary {
                adding.goal { takeIf { it == "bad" } } triggers {
                    log += "failing"
                    check(false) { "boom" }
                }
                adding.goal { takeIf { it == "main" } } triggers {
                    try {
                        agent.achieve("bad")
                        log += "not reached"
                    } catch (_: Exception) {
                        // Without a failure plan, the subgoal fails with a generic "No plan found" exception.
                        log += "caught"
                    }
                    node.terminateNode()
                }
            }
        },
        js = { log ->
            onGoalAdded({ if (it == "bad") true else null }, jsBodies(log).failing)
                .onGoalAdded({ if (it == "main") true else null }, jsBodies(log).catchFailure)
        },
    )

    private fun assertParity(
        goals: List<String>,
        kotlin: AgentBuilder<String, String, Any>.(MutableList<String>) -> Unit,
        js: JsAgent.((String) -> Unit) -> JsAgent,
    ) = runTest {
        val kotlinLog = mutableListOf<String>()
        val kotlinAgent = agent<String, String, Any> {
            embodiedAs { Any() }
            goals.forEach(::addGoal)
            kotlin(kotlinLog)
        }
        mas(NodeBuilders.baseNode<Any>()) { node { withAgents(kotlinAgent) } }.runLocally()
        delay(SLOW * 2)

        val jsLog = mutableListOf<String>()
        runMas(arrayOf(agent("js").hasInitialGoals(goals.toTypedArray<Any>()).js { jsLog += it })).await()
        realDelay(SLOW * 2).await()

        assertEquals(kotlinLog, jsLog)
    }

    private companion object {
        const val SLOW = 100L

        fun realDelay(millis: Long): Promise<Unit> = Promise { resolve, _ ->
            js("setTimeout")(resolve, millis.toInt())
        }

        /**
         * Native JS plan bodies, so that `await` is the engine's own rather than a Kotlin one.
         * Compiled through `Function` because Kotlin's `js()` does not parse `async`.
         */
        val jsBodies: ((String) -> Unit) -> dynamic = js("Function")(
            "log",
            """return ({
                interleaving: async (self) => {
                    const g = self.context;
                    log(g + ":a"); await self.achieve("sub");
                    log(g + ":b"); await self.achieve("sub");
                    log(g + ":c");
                    if (g === "g2") self.terminateNode();
                },
                awaitThenLog: async (self) => {
                    log("g1:a"); await self.achieve("sub"); log("g1:b");
                },
                slow: async (self) => { await self.delay(100); },
                delayThenLog: async (self) => {
                    log("before"); await self.delay(100); log("after");
                },
                delayThenTerminate: async (self) => {
                    await self.delay(50); log("terminating"); self.terminateNode();
                },
                failing: async (self) => { log("failing"); throw new Error("boom"); },
                catchFailure: async (self) => {
                    try {
                        await self.achieve("bad"); log("not reached");
                    } catch (e) {
                        log("caught");
                    }
                    self.terminateNode();
                },
                achieveInTry: async (self) => {
                    try {
                        log("before"); await self.achieve("slow"); log("after");
                    } finally {
                        log("finally");
                    }
                },
            })""",
        )
    }
}
