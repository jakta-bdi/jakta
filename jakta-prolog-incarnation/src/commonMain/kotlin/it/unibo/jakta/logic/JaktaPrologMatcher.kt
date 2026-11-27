package it.unibo.jakta.logic

import it.unibo.jakta.annotations
import it.unibo.jakta.removeAllAnnotations
import it.unibo.jakta.setAnnotations
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.List as PrologList
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown
import it.unibo.tuprolog.solve.primitive.BinaryRelation
import it.unibo.tuprolog.solve.primitive.Solve
import it.unibo.tuprolog.solve.prolog.PrologSolverFactory
import it.unibo.tuprolog.unify.AbstractUnificator
import it.unibo.tuprolog.unify.Unificator

/**
 * Every way the [query] annotations match the [data] ones, each with a different one, lazily: the substitutions
 * extending [result]. Plan triggers take the first, the solver backtracks over all of them.
 */
private fun annotationMatches(
    query: List<Term>,
    data: List<Term>,
    result: Substitution,
    mgu: (Term, Term) -> Substitution,
): Sequence<Substitution> {
    if (query.isEmpty()) return sequenceOf(result)
    val annotation = query.first().apply(result)
    return data.asSequence().flatMap { candidate ->
        val matched = mgu(annotation, candidate)
        val combined = if (matched is Substitution.Fail) matched else result + matched
        when (combined) {
            is Substitution.Fail -> emptySequence()
            else -> annotationMatches(query.drop(1), data - candidate, combined, mgu)
        }
    }
}

/**
 * Matches a term against annotated data, as plan triggers do: [term1] is the query (e.g. a plan trigger) and [term2]
 * the annotated data it is matched against (e.g. a belief, or the goal of an event). Annotations are read as written:
 * each annotation of the query matches a different annotation of the data, and a query without annotations matches
 * data with any.
 */
private val annotationUnificator = object : AbstractUnificator() {
    override fun checkTermsEquality(first: Term, second: Term): Boolean = first == second

    override fun mgu(term1: Term, term2: Term, occurCheckEnabled: Boolean): Substitution {
        val query = annotatedTerm(term1)
        val data = annotatedTerm(term2)
        // the data first, as in solving: its variables are bound to the query's terms
        val result = super.mgu(data, query, occurCheckEnabled)
        // the annotations of the terms as given: a rule's are those of its clause, not of its head
        return when {
            result is Substitution.Fail || term1.annotations.isEmpty() -> result

            else -> annotationMatches(term1.annotations.toList(), term2.annotations.toList(), result) { a, b ->
                super.mgu(a, b, occurCheckEnabled)
            }.firstOrNull() ?: Substitution.failed()
        }
    }

    private fun annotatedTerm(term: Term): Term = when (term) {
        is Rule -> term.head
        else -> term
    }
}

/**
 * `'$jakta_annotated'(Query, Patterns)`: for each belief unifying with `Query`, every way each of the `Patterns`
 * matches a different annotation of the belief. A `Query` of the form `(H :- B)` looks at the rules instead, whose
 * annotations are those of the whole clause.
 */
private object AnnotatedBelief : BinaryRelation.WithoutSideEffects<ExecutionContext>("\$jakta_annotated") {
    override fun Solve.Request<ExecutionContext>.computeAllSubstitutions(
        first: Term,
        second: Term,
    ): Sequence<Substitution> {
        val query = first as? Struct ?: return emptySequence()
        val patterns = second.castToList().toList()
        val ofRules = query.functor == ":-" && query.arity == 2
        val clauses = when {
            ofRules -> (context.staticKb.clauses + context.dynamicKb.clauses).asSequence().filter { it.isRule }
            else -> (context.staticKb[query] + context.dynamicKb[query]).filter { it.isFact }
        }
        return clauses.filterIsInstance<Rule>().flatMap { clause ->
            val term = if (ofRules) Struct.of(":-", clause.head, clause.body) else clause.head.removeAllAnnotations()
            // the clause and its annotations as a single term, so that a copy keeps their variables shared
            val copy = Struct.of("annotated", term, PrologList.of(clause.annotations)).freshCopy().castToStruct()
            when (val matched = Unificator.default.mgu(copy[0], query)) {
                is Substitution.Fail -> emptySequence()

                else -> annotationMatches(patterns, copy[1].castToList().toList(), matched) { x, y ->
                    Unificator.default.mgu(x, y)
                }
            }
        }
    }
}

/**
 * An object representing a way to access a [Solver] configured with JaKtA specific options.
 */
object JaktaSolver {
    /**
     * Factory method to create a new solver instance with a loaded theory.
     */
    // referenced directly: the by-name lookup behind Solver.prolog fails in bundled JS apps (webpack)
    fun get(theory: Iterable<Clause>): Solver = PrologSolverFactory
        .newBuilder()
        .unificator(annotationUnificator)
        .library(AnnotatedBelief)
        .staticKb(theory)
        .flag(Unknown, Unknown.FAIL)
        // without it, a rule loses the bindings of its head before a built-in in its body, as `is` or `\=`
        .flag(TrackVariables) { ON }
        .build()

    /**
     * The arguments of this term that are queries, if it is a control construct such as `(A, B)` or `findall(T, G, L)`:
     * annotated queries are looked for there, and not in data such as the template and the list of `findall`.
     */
    private val Struct.queryArguments: List<Int>
        get() = when ("$functor/$arity") {
            ",/2", ";/2", "->/2", "*->/2", "forall/2" -> listOf(0, 1)
            "\\+/1", "not/1", "call/1", "once/1" -> listOf(0)
            "findall/3", "bagof/3", "setof/3", "^/2" -> listOf(1)
            else -> emptyList()
        }

    /*
     * Annotations are tags, whose variables the solver never substitutes, renames or reports, and a unificator returns
     * a single substitution, so it cannot backtrack over them. So an annotated query `p(X)[q1, ..., qm]` is solved as
     * `'$jakta_annotated'(p(X), [q1, ..., qm])`, with its annotations as a plain list. Queries without annotations are
     * left as they are.
     */
    private fun rewriteAnnotatedQueries(term: Term): Term {
        if (term !is Struct) return term
        val queries = term.queryArguments
        return when {
            queries.isNotEmpty() -> term.setArgs(
                term.args.mapIndexed { i, argument ->
                    if (i in
                        queries
                    ) {
                        rewriteAnnotatedQueries(argument)
                    } else {
                        argument
                    }
                },
            )

            term.annotations.isEmpty() -> term

            else -> Struct.of(
                AnnotatedBelief.signature.name,
                term.removeAllAnnotations(),
                PrologList.of(term.annotations),
            )
        }
    }

    /**
     * The solutions of [query] on [theory], lazily. Annotations are solved like the rest of the terms, with
     * backtracking: each solution binds the variables of the query, those in its annotations included.
     *
     * Annotations belong to beliefs and to whole rules: a query with annotations, as `p(X)[source(S)]`, looks at the
     * beliefs only, while `p(X)` looks at the rules too; `(p(X) :- B)[source(S)]` looks at the rules, as clauses.
     *
     * A query known only when solving, as `G` in `G = p(X)[source(S)], call(G)`, still filters by constant annotations,
     * but does not bind the variables in them.
     */
    fun solve(theory: Collection<Clause>, query: Struct): Sequence<Solution> {
        fun Clause.hasAnnotatedQueries() = this is Rule && body.hasAnnotations()
        if (!query.hasAnnotations() && theory.none { it.hasAnnotatedQueries() }) return get(theory).solve(query)
        val clauses = theory.map {
            if (it is Rule &&
                it.hasAnnotatedQueries()
            ) {
                Rule.of(it.head, rewriteAnnotatedQueries(it.body))
            } else {
                it
            }
        }
        val rewritten = rewriteAnnotatedQueries(query).castToStruct()
        val variables = query.allVariables()
        return get(clauses).solve(rewritten).map {
            when (it) {
                is Solution.Yes -> Solution.yes(
                    query,
                    Substitution.unifier(it.substitution.filterKeys(variables::contains)),
                )

                is Solution.No -> Solution.no(query)

                is Solution.Halt -> Solution.halt(query, it.exception)
            }
        }
    }
}

/**
 * The first solution of [query] on these beliefs and rules, as by [JaktaSolver.solve]: a variable in an annotation,
 * such as `S` in `p(X)[source(S)]`, is bound by the solution.
 * @param query The [Struct] to be solved on the collection of rules.
 * @return The [Solution] of the query, which can be a success or failure.
 */
fun Collection<Rule>.unifiesWith(query: Struct): Solution = JaktaSolver.solve(this, query).first()

/**
 * Computes the most general unifier (MGU) between this annotated data, as a belief or a goal, and the [query] of a plan
 * trigger: each annotation of the query must match a different annotation of the data, read as written.
 */
fun Struct.annotatedMguWith(query: Struct): Substitution = annotationUnificator.mgu(query, this)

/**
 * Every solution of [query] on these beliefs and rules, as for [unifiesWith].
 * @param query The [Struct] to be solved on the collection of rules.
 * @return The [Solution]s of the query.
 */
fun Collection<Rule>.allSolutionsOf(query: Struct): List<Solution> = JaktaSolver.solve(this, query).toList()

/**
 * List of special functors in Prolog that are not considered predicates.
 */
val specialFunctors = listOf(".", "{}", ":-", "/", "[]", ",", ";", "\\+", "&", "|")

/**
 * Checks if the given [Struct] is a predicate by ensuring its functor is not in the list of special functors.
 * @param struct The [Struct] to be checked.
 * @param message A lambda function that generates an error message if the check fails.
 */
fun requirePredicate(struct: Struct, message: (Struct) -> String = { "Struct $it must be a predicate" }) =
    require(struct.functor !in specialFunctors) { message(struct) }

/**
 * Checks if the given [Struct] is ground (i.e., contains no variables, in its annotations neither).
 * @param struct The [Struct] to be checked.
 * @param message A lambda function that generates an error message if the check fails.
 */
fun requireGround(struct: Struct, message: (Struct) -> String = { "Struct $it must be ground" }) =
    require(struct.allVariables().isEmpty()) { message(struct) }

/** The variables of this term, those in its annotations included. */
internal fun Term.allVariables(): Set<Var> = when (this) {
    is Var -> setOf(this)
    is Struct -> args.flatMap { it.allVariables() }.toSet() + annotations.flatMap { it.allVariables() }
    else -> emptySet()
}

private fun Term.hasAnnotations(): Boolean =
    this is Struct && (annotations.isNotEmpty() || args.any { it.hasAnnotations() })

/**
 * This term with [substitution] applied to it, and to its annotations too, which [Term.apply] leaves untouched.
 */
fun Term.applyToTermAndAnnotations(substitution: Substitution): Term = when {
    !hasAnnotations() -> apply(substitution)

    else -> {
        val struct = this as Struct
        val applied = struct.setArgs(struct.args.map { it.applyToTermAndAnnotations(substitution) })
        applied.setAnnotations(struct.annotations.map { it.apply(substitution).castToStruct() }.toSet())
    }
}
