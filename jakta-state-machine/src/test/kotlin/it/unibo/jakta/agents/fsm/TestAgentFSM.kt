package it.unibo.jakta.agents.fsm

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.fsm.impl.State
import it.unibo.jakta.agents.utils.Promise
import kotlinx.coroutines.delay
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.ExecutionException

class TestAgentFSM :
    DescribeSpec({
        describe("A Thread Agent") {

            it("should stop after controller.stop() invocation") {
                lateinit var runner: Runner

                runner =
                    Runner.threadOf(
                        Activity.of(
                            onBeginProcedure = { runner.state shouldBe State.CREATED },
                            onStepProcedure = { it.stop() },
                            onEndProcedure = { runner.state shouldBe State.RUNNING },
                        ),
                    )

                runner.isOver shouldBe false
                runner.state shouldBe State.CREATED
                val promise: Promise<Unit> = runner.run()
                promise.get()
                runner.state shouldBe null
            }

            it("should pause after controller.pause() invocation") {
                lateinit var runner: Runner
                lateinit var c: Activity.Controller

                runner =
                    Runner.threadOf(
                        Activity.of(
                            onBeginProcedure = { c = it },
                            onStepProcedure = { it.pause() },
                        ),
                    )

                runner.isOver shouldBe false
                runner.state shouldBe State.CREATED
                runner.run()
                delay(2000)
                runner.state shouldBe State.PAUSED
                c.resume()
                delay(2000)
                runner.state shouldBe State.PAUSED
                c.stop()
            }

            it("should restart after controller.restart() invocation") {
                lateinit var c: Activity.Controller
                var beginCounter = 0

                Runner
                    .threadOf(
                        Activity.of(
                            onBeginProcedure = {
                                c = it
                                beginCounter++
                            },
                            onStepProcedure = { it.pause() },
                        ),
                    ).run()

                delay(2000)
                c.restart()
                delay(2000)
                c.stop()
                beginCounter shouldBe 2
            }

            it("should throw ExecutionException containing an IllegalArgumentException") {
                val promise =
                    Runner
                        .threadOf(
                            Activity.of(
                                onStepProcedure = { it.stop() },
                                onEndProcedure = { it.pause() },
                            ),
                        ).run()
                assertThrows<ExecutionException> { promise.get() }
            }
        }

        describe("A Sync Agent") {
            it("should not go on paused state") {
                val promise =
                    Runner
                        .syncOf(
                            Activity.of { it.pause() },
                        ).run()
                assertThrows<ExecutionException> { promise.get() }
            }

            it("should be run on the same thread of the invoker") {
                val invokerThread = Thread.currentThread()
                Runner
                    .syncOf(
                        Activity.of {
                            Thread.currentThread() shouldBe invokerThread
                            it.stop()
                        },
                    ).run()
            }
        }
    })
