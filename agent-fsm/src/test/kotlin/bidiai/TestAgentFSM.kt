package bidiai

import bidiai.impl.ThreadRunner
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.ExecutionException

class TestAgentFSM : DescribeSpec({
    describe("An Agent") {

        it("should stop after controller.stop() invocation") {
            lateinit var runner: ThreadRunner

            val agent = object : Activity {
                override fun onBegin(controller: Activity.Controller) = runner.state shouldBe State.CREATED

                override fun onStep(controller: Activity.Controller) {
                    controller.stop()
                }

                override fun onEnd(controller: Activity.Controller) = runner.state shouldBe State.RUNNING
            }
            runner = ThreadRunner(agent)
            runner.isOver shouldBe false
            runner.state shouldBe State.CREATED
            val promise: Promise<Unit> = runner.run()
            promise.get()
            runner.state shouldBe null
        }

        it("should pause after controller.pause() invocation") {
            lateinit var runner: ThreadRunner
            lateinit var c: Activity.Controller

            val agent = object : Activity {
                override fun onBegin(controller: Activity.Controller) {
                    c = controller
                }

                override fun onStep(controller: Activity.Controller) = controller.pause()
                override fun onEnd(controller: Activity.Controller) = Unit
            }
            runner = ThreadRunner(agent)
            runner.isOver shouldBe false
            runner.state shouldBe State.CREATED
            runner.run()
            Thread.sleep(2000)
            runner.state shouldBe State.PAUSED
            c.resume()
            Thread.sleep(1000)
            runner.state shouldBe State.PAUSED
            c.stop()
        }

        it("should restart after controller.restart() invocation") {
            lateinit var runner: ThreadRunner
            lateinit var c: Activity.Controller
            var beginCounter = 0

            val agent = object : Activity {
                override fun onBegin(controller: Activity.Controller) {
                    c = controller
                    beginCounter ++
                }

                override fun onStep(controller: Activity.Controller) = controller.pause()
                override fun onEnd(controller: Activity.Controller) = Unit
            }
            runner = ThreadRunner(agent)
            runner.run()
            Thread.sleep(2000)
            c.restart()
            Thread.sleep(1000)
            c.stop()
            beginCounter shouldBe 2
        }

        it("should throw") {
            lateinit var runner: ThreadRunner
            val agent = object : Activity {
                override fun onBegin(controller: Activity.Controller) = Unit
                override fun onStep(controller: Activity.Controller) = controller.stop()
                override fun onEnd(controller: Activity.Controller) = controller.pause()
            }
            runner = ThreadRunner(agent)
            val promise = runner.run()
            assertThrows<ExecutionException> { promise.get() }
        }
    }
})
