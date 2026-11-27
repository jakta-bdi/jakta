package it.unibo.jakta

import it.unibo.jakta.belief.BeliefBaseFactory
import it.unibo.jakta.dsl.belief.AnnotatedBeliefRevision
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.event.UnlimitedChannelQueue
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.MutableSubstitutionPlanContext
import it.unibo.jakta.logic.allSolutionsOf
import it.unibo.jakta.logic.annotatedMguWith
import it.unibo.jakta.logic.applyToTermAndAnnotations
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Annotations in guards, queries and rules, solved by the Prolog solver with backtracking (#1033).
 */
class TestAnnotationsInSolver {
    private val facts = listOf(
        initialBelief { "propose"(1, 5)[source("alice")] },
        initialBelief { "propose"(1, 3)[source("bob")] },
        initialBelief { "ready"(1) },
    )

    private fun Collection<Rule>.solve(query: JaktaLogicProgrammingScope.() -> Struct): List<Map<String, String>> =
        allSolutionsOf(with(JaktaLogicProgrammingScope()) { query() })
            .filter { it.isYes }
            .map { solution -> solution.substitution.entries.associate { (v, t) -> v.name to t.toString() } }

    private val both =
        listOf(mapOf("I" to "1", "T" to "5", "S" to "alice"), mapOf("I" to "1", "T" to "3", "S" to "bob"))

    @Test
    fun `a source variable is bound at the top level and in a conjunction`() {
        assertEquals(both, facts.solve { "propose"(I, T)[source(S)] })
        assertEquals(both, facts.solve { "ready"(I) and "propose"(I, T)[source(S)] })
    }

    @Test
    fun `a query without annotations matches beliefs with any`() {
        assertEquals(2, facts.solve { "propose"(I, T) }.size)
        assertEquals(1, facts.solve { "ready"(I) }.size)
    }

    @Test
    fun `a constant source filters, also under negation`() {
        assertEquals(
            listOf(mapOf("I" to "1", "T" to "5")),
            facts.solve {
                "ready"(I) and
                    "propose"(I, T)[source("alice")]
            },
        )
        assertEquals(1, facts.solve { "ready"(I) and not("propose"(I, T)[source("carol")]) }.size)
        assertEquals(0, facts.solve { "ready"(I) and not("propose"(I, T)[source("alice")]) }.size)
    }

    @Test
    fun `a rule body binds the source of the head`() {
        val theory = facts + inferenceRule { "from"(I, S) impliedBy "propose"(I, T)[source(S)] }
        assertEquals(
            listOf(mapOf("I" to "1", "S" to "alice"), mapOf("I" to "1", "S" to "bob")),
            theory.solve { "from"(I, S) },
        )
        val fromBob = facts + inferenceRule { "from_bob"(I, T) impliedBy "propose"(I, T)[source("bob")] }
        assertEquals(listOf(mapOf("I" to "1", "T" to "3")), fromBob.solve { "from_bob"(I, T) })
    }

    @Test
    fun `annotations belong to beliefs, so an annotated query looks at beliefs only`() {
        val theory = listOf(
            initialBelief { "quote"(10) },
            inferenceRule { "price"(X) impliedBy "quote"(X) },
            initialBelief { "price"(5)[source("alice")] },
        )
        assertEquals(listOf(mapOf("X" to "5", "S" to "alice")), theory.solve { "price"(X)[source(S)] })
        assertEquals(listOf(mapOf("X" to "10"), mapOf("X" to "5")), theory.solve { "price"(X) })
    }

    @Test
    fun `a rule's annotations are those of the whole clause, and a clause query finds them`() {
        val toldByBob = inferenceRule { ("cheap"(X) impliedBy ("price"(X) and (X lowerThan 7)))[source("bob")] }
        val beliefBase = BeliefBaseFactory.of(
            UnlimitedChannelQueue(),
            listOf(inferenceRule { "price"(X) impliedBy "quote"(X) }, toldByBob, initialBelief { "quote"(5) }),
            AnnotatedBeliefRevision,
        )
        val beliefs = beliefBase.snapshot().toList()
        assertEquals(listOf(sources("self"), sources("bob"), sources("self")), beliefs.map { it.annotations })
        // a rule's head has no annotations: derived beliefs have none, and annotated queries look at beliefs only
        assertEquals(listOf(mapOf("X" to "5")), beliefs.solve { "cheap"(X) })
        assertEquals(0, beliefs.solve { "cheap"(X)[source(S)] }.size)
        // as a clause, the rule is found with its source
        assertEquals(listOf("bob"), beliefs.solve { ("cheap"(X) impliedBy B)[source(S)] }.map { it["S"] })
        assertEquals(1, beliefs.solve { ("price"(X) impliedBy B)[source("self")] }.size)
        assertEquals(0, beliefs.solve { ("price"(X) impliedBy B)[source("bob")] }.size)
        // the rule goes with its last source
        beliefBase.remove(toldByBob)
        assertEquals(listOf("price", "quote"), beliefBase.snapshot().map { it.head.functor })
    }

    @Test
    fun `a rule takes annotations on its clause, not on its head`() {
        assertFailsWith<IllegalArgumentException> {
            inferenceRule { "price"(X)[source("market")] impliedBy "quote"(X) }
        }
    }

    private fun sources(vararg names: String) = names.map { source(it) }.toSet()

    @Test
    fun `a variable bound earlier constrains the annotation`() {
        val theory = facts + initialBelief { "trusted"("alice") }
        assertEquals(
            listOf(mapOf("S" to "alice", "I" to "1", "T" to "5")),
            theory.solve { "trusted"(S) and "propose"(I, T)[source(S)] },
        )
    }

    @Test
    fun `a variable checked later backtracks over the sources`() {
        val theory = listOf(
            initialBelief { "price"(10)[source("alice"), source("bob")] },
            initialBelief { "trusted"("bob") },
        )
        assertEquals(listOf(mapOf("S" to "bob")), theory.solve { "price"(10)[source(S)] and "trusted"(S) })
    }

    @Test
    fun `a rule trusts a belief told by a trusted source, backtracking over its sources`() {
        val theory = listOf(
            initialBelief { "b"(1)[source("alice"), source("bob")] },
            initialBelief { "b"(2)[source("alice")] },
            initialBelief { "trusted"("bob") },
            inferenceRule { "reliable"(X) impliedBy ("b"(X)[source(S)] and "trusted"(S)) },
        )
        assertEquals(listOf(mapOf("X" to "1")), theory.solve { "reliable"(X) })
    }

    @Test
    fun `the substitution of the plan reaches the annotations`() {
        // the guard of a plan whose trigger bound S, as satisfies { } applies the plan's substitution
        val guard = with(JaktaLogicProgrammingScope()) {
            ("propose"(I, T)[source(S)]).applyToTermAndAnnotations(Substitution.of(S, atomOf("alice"))).castToStruct()
        }
        assertEquals(listOf(mapOf("I" to "1", "T" to "5")), facts.solve { guard })
    }

    @Test
    fun `annotations match as written, as a subset, each at most once`() {
        val tagged = listOf(initialBelief { "p"(1)[source("a"), "tag"("x")] })
        assertEquals(listOf(mapOf("Y" to "x")), tagged.solve { "p"(1)["tag"(Y)] })
        assertEquals(0, tagged.solve { "p"(1)[source(X), source(Y)] }.size)
        assertEquals(0, facts.solve { "ready"(1)[source(S)] }.size)
    }

    @Test
    fun `annotations backtrack, in the solver and in plan triggers`() {
        // a greedy match binds S to alice first, and then finds no source(alice) left
        val told = initialBelief { "p"(1)[source("alice"), source("bob")] }
        assertEquals(listOf(mapOf("S" to "bob")), listOf(told).solve { "p"(1)[source(S), source("alice")] })
        with(JaktaLogicProgrammingScope()) {
            assertEquals("bob", told.head.annotatedMguWith("p"(1)[source(S), source("alice")])[S].toString())
        }
    }

    @Test
    fun `with annotated beliefs, a belief told by two agents is found with both sources`() {
        val beliefBase = BeliefBaseFactory.of(
            UnlimitedChannelQueue(),
            listOf(initialBelief { "refuse"(3)[source("bob")] }, initialBelief { "refuse"(3)[source("carol")] }),
            AnnotatedBeliefRevision,
        )
        val beliefs = beliefBase.snapshot().toList()
        assertEquals(1, beliefs.size)
        assertEquals(listOf(mapOf("S" to "bob"), mapOf("S" to "carol")), beliefs.solve { "refuse"(3)[source(S)] })
        assertEquals(listOf(mapOf("L" to "[bob, carol]")), beliefs.solve { "findall"(S, "refuse"(3)[source(S)], L) })
    }

    @Test
    fun `rules calling built-ins keep the bindings of their head`() {
        val family = listOf(
            initialBelief { "parent"("alice", "bob") },
            initialBelief { "parent"("alice", "charlie") },
            inferenceRule { "sibling"(X, Y) impliedBy ("parent"(Z, X) and "parent"(Z, Y) and (X neq Y)) },
        )
        assertEquals(
            listOf(mapOf("X" to "bob", "Y" to "charlie"), mapOf("X" to "charlie", "Y" to "bob")),
            family.solve { "sibling"(X, Y) },
        )
        val counting = listOf(
            initialBelief { "at"(1) },
            inferenceRule { "next"(X, Y) impliedBy ("at"(X) and (Y `is` (X + 1))) },
        )
        assertEquals(listOf(mapOf("X" to "1", "Y" to "2")), counting.solve { "next"(X, Y) })
    }

    @Test
    fun `a belief built in a plan has the plan's substitution in its annotations, and must be ground there too`() {
        val scope = JaktaLogicProgrammingScope()
        val bound = MutableSubstitutionPlanContext(Substitution.of(scope.S, scope.atomOf("alice")))
        val built = with(scope) { with(bound) { belief { "x"(1)[source(S)] } } }
        with(scope) { assertEquals("alice", built.head.annotatedMguWith("x"(1)[source(S)])[S].toString()) }
        assertFailsWith<IllegalArgumentException> {
            with(scope) { with(MutableSubstitutionPlanContext(Substitution.empty())) { belief { "x"(1)[source(S)] } } }
        }
    }
}
