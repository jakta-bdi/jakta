package it.unibo.jakta

import it.unibo.jakta.dsl.belief.beliefQuery
import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.unifiesWith
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.Solution
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

        val queryRule = with(JaktaLogicProgrammingScope()) {
            "sibling"(X, Y)
        }
        val solution = theory.unifiesWith(queryRule)

        when (solution) {
            is Solution.Yes -> println("The solution is: $solution")
            is Solution.No -> assert(false) { "No solution found" }
            is Solution.Halt -> assert(false) { "The solving process was halted: ${solution.exception}" }
        }
    }

    @Test
    fun `test matching with annotations`() {
        val theory = listOf(
            initialBelief {
                "parent"("alice", "bob")["source"("self")]
            },
            initialBelief {
                "parent"("alice", "charlie")["source"("bob")]
            },
        )
        val queryRule = with(JaktaLogicProgrammingScope()) {
            "parent"(X, Y)["source"("bob")]
        }
        val solution = theory.unifiesWith(queryRule)

        when (solution) {
            is Solution.Yes -> println("The solution is: $solution")
            is Solution.No -> assert(false) { "No solution found" }
            is Solution.Halt -> assert(false) { "The solving process was halted: ${solution.exception}" }
        }
    }
}
