package it.unibo.jakta

import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.belief.noSubstitutionBeliefQuery
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.goalQuery
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.annotatedMguWith
import it.unibo.jakta.logic.unifiesWith
import it.unibo.tuprolog.solve.Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

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
            val query = noSubstitutionBeliefQuery { "sibling"(X, Y) }
            when (val solution = theory.unifiesWith(query)) {
                is Solution.Yes -> {
                    assertEquals("bob", solution.substitution[X].toString())
                    assertEquals("charlie", solution.substitution[Y].toString())
                }

                is Solution.No -> fail("No solution found")

                is Solution.Halt -> fail("The solving process was halted: ${solution.exception}")
            }
        }
    }

    @Test
    fun `annotated belief mgu with annotated query`() {
        val belief = initialBelief {
            "parent"("alice", "charlie")[source("bob")]
        }
        with(JaktaLogicProgrammingScope()) {
            val query = noSubstitutionBeliefQuery { "parent"(X, Y)[source("bob")] }
            val sub = belief.annotatedMguWith(query)
            if (sub.isFailed) {
                fail("Failed to find a match")
            }
            assertEquals("alice", sub[X].toString())
            assertEquals("charlie", sub[Y].toString())
        }
    }

    @Test
    fun `annotated belief mgu with not annotated query`() {
        val belief = initialBelief {
            "parent"("alice", "charlie")[source("bob")]
        }
        with(JaktaLogicProgrammingScope()) {
            val query = noSubstitutionBeliefQuery { "parent"(X, Y) }
            val sub = belief.annotatedMguWith(query)
            if (sub.isFailed) {
                fail("Failed to find a match")
            }
            assertEquals("alice", sub[X].toString())
            assertEquals("charlie", sub[Y].toString())
        }
    }

    @Test
    fun `not annotated belief is implicitly annotated with source(self)`() {
        val belief = initialBelief {
            "parent"("alice", "charlie")
        }
        with(JaktaLogicProgrammingScope()) {
            val query = noSubstitutionBeliefQuery { "parent"(X, Y)[source(Z)] }
            val sub = belief.annotatedMguWith(query)
            if (sub.isFailed) {
                fail("Failed to find a match")
            }
            assertEquals("alice", sub[X].toString())
            assertEquals("charlie", sub[Y].toString())
            assertEquals(self.toString(), sub[Z].toString())
        }
    }

    @Test
    fun `annotated belief solves annotated query`() {
        val theory = listOf(
            initialBelief {
                "parent"("alice", "bob")
            },
            initialBelief {
                "parent"("alice", "charlie")[source("bob")]
            },
        )
        with(JaktaLogicProgrammingScope()) {
            val query = noSubstitutionBeliefQuery { "parent"(X, Y)[source("bob")] }
            when (val solution = theory.unifiesWith(query)) {
                is Solution.Yes -> assertEquals("charlie", solution.substitution[Y].toString())
                is Solution.No -> fail("No solution found")
                is Solution.Halt -> fail("The solving process was halted: ${solution.exception}")
            }
        }
    }

    @Test
    fun `not annotated query is solved by the first matching belief regardless of source`() {
        val theory = listOf(
            initialBelief {
                "parent"("alice", "charlie")[source("a")]
            },
        )
        with(JaktaLogicProgrammingScope()) {
            val query = noSubstitutionBeliefQuery { "parent"(X, Y) }
            when (val solution = theory.unifiesWith(query)) {
                is Solution.Yes -> assertEquals("charlie", solution.substitution[Y].toString())
                is Solution.No -> fail("No solution found")
                is Solution.Halt -> fail("The solving process was halted: ${solution.exception}")
            }
        }
    }

    @Test
    fun `not annotated goal is implicitly annotated with source(self)`() {
        with(JaktaLogicProgrammingScope()) {
            val goal: PrologGoal = "parent"("alice", "charlie")
            val sub = goal.annotatedMguWith(goalQuery { "parent"(X, Y)[source(Z)] })
            if (sub.isFailed) {
                fail("Failed to find a match")
            }
            assertEquals("alice", sub[X].toString())
            assertEquals(self.toString(), sub[Z].toString())
        }
    }

    @Test
    fun `annotated goal mgu with annotated query`() {
        with(JaktaLogicProgrammingScope()) {
            val goal = "parent"("alice", "charlie").tag(source("bob"))
            val sub = goal.annotatedMguWith(goalQuery { "parent"(X, Y)[source(Z)] })
            if (sub.isFailed) {
                fail("Failed to find a match")
            }
            assertEquals("bob", sub[Z].toString())
        }
    }

    @Test
    fun `annotated goal mgu with not annotated query`() {
        with(JaktaLogicProgrammingScope()) {
            val goal = "parent"("alice", "charlie").tag(source("bob"))
            val sub = goal.annotatedMguWith(goalQuery { "parent"(X, Y) })
            if (sub.isFailed) {
                fail("Failed to find a match")
            }
            assertEquals("alice", sub[X].toString())
        }
    }

    @Test
    fun `annotated goal does not match a query with a different source`() {
        with(JaktaLogicProgrammingScope()) {
            val goal = "parent"("alice", "charlie").tag(source("bob"))
            assertEquals(true, goal.annotatedMguWith(goalQuery { "parent"(X, Y)[source("alice")] }).isFailed)
            val selfGoal: PrologGoal = "parent"("alice", "charlie")
            assertEquals(true, selfGoal.annotatedMguWith(goalQuery { "parent"(X, Y)[source("bob")] }).isFailed)
        }
    }
}
