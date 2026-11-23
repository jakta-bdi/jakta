package it.unibo.jakta.js

import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.await
import kotlinx.coroutines.test.runTest

class TestJaktaJs {

    @Test
    fun agentAchievesSubgoalsAndReactsToBeliefs() = runTest {
        val log = mutableListOf<Any?>()
        val alice = agent("alice")
            .believes(arrayOf("ready"))
            .hasInitialGoals(arrayOf("main"))
            .onGoalAdded({ if (it == "double") 21 else null }, { scope ->
                // An async JS-like body, resolving later with the plan result.
                Promise { resolve, _ -> resolve((scope.context as Int) * 2) }
            })
            .onGoalAdded({ if (it == "main") true else null }, { scope ->
                scope.achieve("double").then { result ->
                    log += result
                    scope.believe("done")
                }
            }, { guard -> if ("ready" in guard.beliefs) guard.context else null })
            .onBeliefAdded({ if (it == "done") true else null }, { scope ->
                log += scope.agentName
                scope.terminateNode()
            })
        runMas(arrayOf(alice)).await()
        assertEquals(listOf<Any?>(42, "alice"), log)
    }
}
