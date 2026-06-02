package it.unibo.jakta

import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.initialBelief
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
        val originalPrologRule = Rule.of(
            Struct.of("sibling", Var.of("X"), Var.of("Y")),
            Struct.of("parent", Var.of("Z"), Var.of("X")),
            Struct.of("parent", Var.of("Z"), Var.of("Y")),
            Struct.of("\\=", Var.of("X"), Var.of("Y")),
        )

        val prologFacts = listOf(
            Fact.of(Struct.of("parent", Atom.of("alice"), Atom.of("bob"))),
            Fact.of(Struct.of("parent", Atom.of("alice"), Atom.of("charlie"))),
        )

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

        val basicQuery = Struct.of("parent", Atom.of("alice"), Atom.of("charlie"))
        val queryNoRule = Struct.of("parent", Var.of("X"), Var.of("Y"))
        val queryRule = Struct.of("sibling", Var.of("X"), Var.of("Y"))
        val solution = theory.unifiesWith(queryRule)

        when (solution) {
            is Solution.Yes -> assert(true).also { println("The solution is: $solution") }
            is Solution.No -> assert(false) { "No solution found" }
            is Solution.Halt -> assert(false) { "The solving process was halted: ${solution.exception}" }
        }
    }
}
