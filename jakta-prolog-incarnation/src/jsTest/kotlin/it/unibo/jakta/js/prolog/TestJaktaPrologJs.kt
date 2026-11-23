package it.unibo.jakta.js.prolog

import it.unibo.jakta.js.runMas
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.await
import kotlinx.coroutines.test.runTest

class TestJaktaPrologJs {

    @Test
    fun agentUsesPrologTermsFromJs() = runTest {
        val log = mutableListOf<String>()
        val bodies = jsBodies { log += it }
        val counter = prologAgent("counter")
            .believes(arrayOf("limit(3)", "big(X) :- limit(L), X >= L"))
            .hasInitialGoals(arrayOf("start(0, 3)"))
            .onGoalAdded("start(N, N)", bodies.done)
            .onGoalAdded("start(N, X)", bodies.count, "N < X, S is N + 1")
            .onBeliefAdded("reached(R)", bodies.reached)

        runMas(arrayOf(counter)).await()

        assertEquals(
            listOf("count 0", "count 1", "count 2", "done 3", """{"L":3}""", """{"R":3,"L":3}""", "big"),
            log,
        )
    }

    private companion object {
        /** Native JS plan bodies, compiled through `Function` because Kotlin's `js()` does not parse `async`. */
        val jsBodies: ((String) -> Unit) -> dynamic = js("Function")(
            "log",
            """return ({
                count: async (self) => {
                    log("count " + self.context.N);
                    await self.achieve("start(S, X)");
                },
                done: (self) => {
                    log("done " + self.context.N);
                    self.believe("reached(" + self.context.N + ")");
                },
                reached: (self) => {
                    log(JSON.stringify(self.query("reached(R), limit(L)")));
                    log(JSON.stringify(self.context));
                    if (self.query("big(R)") !== null) log("big");
                    self.terminateNode();
                },
            })""",
        )
    }
}
