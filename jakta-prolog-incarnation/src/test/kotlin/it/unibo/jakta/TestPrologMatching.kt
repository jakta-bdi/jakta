package it.unibo.jakta

import it.unibo.jakta.dsl.belief.contextualBeliefQuery
import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.unifiesWith
import it.unibo.tuprolog.solve.Solution
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class TestPrologMatching {

    @Test
    fun `test matching with rules`() {
        val theory = listOf(
            initialBelief {
                "parent"("alice", "bob")
            },
            initialBelief {
                "parent"("alice", "charlie")
            },
            inferenceRule {
                "sibling"(X, Y) impliedBy (
                    "parent"(Z, X)
                        and "parent"(Z, Y)
                        and (X neq Y)
                    )
            },
        )

        with(JaktaLogicProgrammingScope()) {
            val queryRule = contextualBeliefQuery { "sibling"(X, Y) }
            when (val solution = theory.unifiesWith(queryRule)) {
                is Solution.Yes -> {
                    assertEquals("bob", solution.substitution[X].toString())
                    assertEquals("charlie", solution.substitution[Y].toString())
                }

                is Solution.No -> assert(false) { "No solution found" }

                is Solution.Halt -> assert(false) { "The solving process was halted: ${solution.exception}" }
            }
        }
    }

    @Test
    fun `annotated fact should match the source`() {
        val theory = listOf(
            initialBelief {
                "parent"("alice", "bob")
            },
            initialBelief {
                "parent"("alice", "charlie")[source("bob")]
            },
        )
        with(JaktaLogicProgrammingScope()) {
            val queryRule = contextualBeliefQuery { "parent"(X, Y)[source("bob")] }
            when (val solution = theory.unifiesWith(queryRule)) {
                is Solution.Yes -> assertEquals("charlie", solution.substitution[Y].toString())
                is Solution.No -> assert(false) { "No solution found" }
                is Solution.Halt -> assert(false) { "The solving process was halted: ${solution.exception}" }
            }
        }
    }

    @Test
    fun `not annotated query matches the first belief regardless of source`() {
        val theory = listOf(
            initialBelief {
                "parent"("alice", "charlie")[source("bob")]
            },
            initialBelief {
                "parent"("alice", "bob")
            },
        )
        with(JaktaLogicProgrammingScope()) {
            val queryRule = contextualBeliefQuery { "parent"(X, Y) }
            when (val solution = theory.unifiesWith(queryRule)) {
                is Solution.Yes -> assertEquals("charlie", solution.substitution[Y].toString())
                is Solution.No -> assert(false) { "No solution found" }
                is Solution.Halt -> assert(false) { "The solving process was halted: ${solution.exception}" }
            }
        }
    }

    @Test
    fun `not annotated fact is implicitly matching source(self)`() {
        val theory = listOf(
            initialBelief {
                "parent"("alice", "charlie")[source("bob")]
            },
            initialBelief {
                "parent"("alice", "bob")
            },
        )
        with(JaktaLogicProgrammingScope()) {
            val queryRule = contextualBeliefQuery { "parent"(X, Y)[source(self)] }
            when (val solution = theory.unifiesWith(queryRule)) {
                is Solution.Yes -> assertEquals("bob", solution.substitution[Y].toString())
                is Solution.No -> assert(false) { "No solution found" }
                is Solution.Halt -> assert(false) { "The solving process was halted: ${solution.exception}" }
            }
        }
    }
}
