package it.unibo.jakta

import it.unibo.jakta.belief.BeliefBaseFactory
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.event.UnlimitedChannelQueue
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.MutableSubstitutionPlanContext
import it.unibo.jakta.logic.allSolutionsOf
import it.unibo.jakta.logic.annotatedMguWith
import it.unibo.jakta.logic.applyWithAnnotations
import it.unibo.jakta.logic.withAnnotationsAsTerms
import it.unibo.jakta.logic.withAnnotationsFromTerms
import it.unibo.tuprolog.core.List as PrologList
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.TermFormatter
import it.unibo.tuprolog.core.parsing.parse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Annotations in guards, queries and rules, solved by the Prolog solver (#1033), and written as plain terms to travel
 * as text (#960).
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
    fun `a query without annotations matches beliefs from any source`() {
        assertEquals(2, facts.solve { "propose"(I, T) }.size)
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
    fun `a variable bound earlier constrains the annotation, without crashing`() {
        val theory = facts + initialBelief { "trusted"("alice") }
        assertEquals(
            listOf(mapOf("S" to "alice", "I" to "1", "T" to "5")),
            theory.solve { "trusted"(S) and "propose"(I, T)[source(S)] },
        )
    }

    @Test
    fun `the substitution of the plan reaches the annotations`() {
        // the guard of a plan whose trigger bound S, as satisfies { } applies the plan's substitution
        val guard = with(JaktaLogicProgrammingScope()) {
            ("propose"(I, T)[source(S)]).applyWithAnnotations(Substitution.of(S, atomOf("alice"))).castToStruct()
        }
        val solutions = facts.allSolutionsOf(guard).filter { it.isYes }.map { it.substitution }
        assertEquals(
            listOf("5"),
            solutions.map { solution ->
                solution.entries.single { it.key.name == "T" }.value.toString()
            },
        )
    }

    @Test
    fun `annotations match as a subset, each at most once, and no annotation means source(self)`() {
        val tagged = listOf(initialBelief { "p"(1)[source("a"), "tag"("x")] })
        assertEquals(listOf(mapOf("Y" to "x")), tagged.solve { "p"(1)["tag"(Y)] })
        assertEquals(0, tagged.solve { "p"(1)[source(X), source(Y)] }.size)
        assertEquals(listOf(mapOf("S" to "self")), facts.solve { "ready"(1)[source(S)] })
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
    fun `a belief told by two agents is found with both sources`() {
        val beliefBase = BeliefBaseFactory.of(
            UnlimitedChannelQueue(),
            listOf(initialBelief { "refuse"(3)[source("bob")] }, initialBelief { "refuse"(3)[source("carol")] }),
        )
        val beliefs = beliefBase.snapshot().toList()
        assertEquals(1, beliefs.size)
        assertEquals(
            listOf(mapOf("S" to "bob"), mapOf("S" to "carol")),
            beliefs.solve { "refuse"(3)[source(S)] },
        )
    }

    @Test
    fun `rules calling built-ins keep the bindings of their head`() {
        // with 2p-kt's default unificator, X \= Y loses the bindings of sibling(X, Y)
        val family = listOf(
            initialBelief { "parent"("alice", "bob") },
            initialBelief { "parent"("alice", "charlie") },
            inferenceRule { "sibling"(X, Y) impliedBy ("parent"(Z, X) and "parent"(Z, Y) and (X neq Y)) },
        )
        assertEquals(
            listOf(mapOf("X" to "bob", "Y" to "charlie"), mapOf("X" to "charlie", "Y" to "bob")),
            family.solve { "sibling"(X, Y) },
        )
        assertEquals(
            listOf(mapOf("L" to "[alice, bob]")),
            facts.solve { "findall"(S, "propose"(1, `_`)[source(S)], L) },
        )
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

    @Test
    fun `annotations survive formatting and parsing, at any depth`() {
        val formatter = TermFormatter.of(TermFormatter.VarFormat.COMPLETE_NAME, TermFormatter.OpFormat.COLLECTIONS)
        fun roundTrip(term: Struct) =
            Struct.parse(formatter.format(term.withAnnotationsAsTerms())).withAnnotationsFromTerms()
        fun Struct.formatted() = formatter.format(withAnnotationsAsTerms())
        val ground = with(JaktaLogicProgrammingScope()) {
            "reply"("propose"(1, 5)[source("bob")], PrologList.of("p"(2)["tag"(2)]))[source("alice")]
        }
        assertTrue("\$annotated" in ground.formatted())
        assertEquals(ground.formatted(), roundTrip(ground).castToStruct().formatted())
        // a variable in the term and in its annotations is still one variable once parsed
        val parsed = roundTrip(with(JaktaLogicProgrammingScope()) { "p"(Y)["tag"(Y)] }).castToStruct()
        val tag = parsed.getTag<Set<Struct>>(JAKTA_ANNOTATIONS_TAG).orEmpty().single()
        assertEquals(parsed[0], tag[0])
    }
}
