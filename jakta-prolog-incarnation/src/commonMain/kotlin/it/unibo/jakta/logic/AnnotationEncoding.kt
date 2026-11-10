package it.unibo.jakta.logic

import it.unibo.jakta.JAKTA_ANNOTATIONS_TAG
import it.unibo.jakta.self
import it.unibo.jakta.source
import it.unibo.jakta.tag
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.List as PrologList
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Tuple
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown
import it.unibo.tuprolog.solve.prolog.PrologSolverFactory
import it.unibo.tuprolog.unify.AbstractUnificator
import it.unibo.tuprolog.utils.setTag

/*
 * Annotations are tags outside the term, which the solver never sees: it does not substitute, rename or report the
 * variables in them. So the solver is given plain terms instead, with the annotations as an extra last argument:
 *
 * - a belief `p(t1, ..., tn)[a1, ..., ak]` is the clause `p(t1, ..., tn, [a1, ..., ak])`, and a belief without
 *   annotations is `p(t1, ..., tn, [source(self)])`;
 * - a goal `p(x1, ..., xn)` is `p(x1, ..., xn, _)`, any annotations; a goal `p(x1, ..., xn)[q1, ..., qm]` is
 *   `p(x1, ..., xn, A), '$jakta_select'(q1, A, R1), ..., '$jakta_select'(qm, Rm-1, Rm)`: each annotation of the goal
 *   matches a different annotation of the belief, with backtracking;
 * - a rule `h :- b` is `h'(..., annotations of h, or [source(self)]) :- b'`, its body rewritten as goals.
 *
 * Only the predicates whose annotations matter are rewritten: those of a belief or a rule with annotations other than
 * `source(self)`, and those of a goal with annotations, in the query or in a rule. The others, built-ins and library
 * predicates included, are solved as they are, which gives the same solutions, faster.
 */

private const val SELECT = "\$jakta_select"

/** `'$jakta_select'(X, L, R)`: R is L without one element unifying with X. */
private val selectClauses: List<Clause> = with(JaktaLogicProgrammingScope()) {
    listOf(
        Fact.of(Struct.of(SELECT, X, consOf(X, T), T)),
        Rule.of(Struct.of(SELECT, X, consOf(H, T), consOf(H, R)), Struct.of(SELECT, X, T, R)),
    )
}

// the goal arguments of the control constructs, rewritten in place
private val controlArguments: Map<Signature, List<Int>> = mapOf(
    Signature(",", 2) to listOf(0, 1),
    Signature(";", 2) to listOf(0, 1),
    Signature("->", 2) to listOf(0, 1),
    Signature("*->", 2) to listOf(0, 1),
    Signature("\\+", 1) to listOf(0),
    Signature("not", 1) to listOf(0),
    Signature("call", 1) to listOf(0),
    Signature("once", 1) to listOf(0),
    Signature("findall", 3) to listOf(1),
    Signature("bagof", 3) to listOf(1),
    Signature("setof", 3) to listOf(1),
    Signature("^", 2) to listOf(1),
    Signature("forall", 2) to listOf(0, 1),
)

/*
 * Looking clauses up, the solver unifies whole clauses: only their heads matter, and unifying their bodies too makes
 * solving about three times slower. The stored term goes first: binding the clause's fresh variables to the query's
 * terms keeps the substitutions short, which makes solving about twice as fast.
 */
private val storedFirst = object : AbstractUnificator() {
    override fun checkTermsEquality(first: Term, second: Term): Boolean = first == second

    private fun headOf(term: Term): Term = if (term is Rule) term.head else term

    override fun mgu(term1: Term, term2: Term, occurCheckEnabled: Boolean): Substitution =
        super.mgu(headOf(term2), headOf(term1), occurCheckEnabled)
}

// referenced directly: the by-name lookup behind Solver.prolog fails in bundled JS apps (webpack)
private fun solverOf(theory: Iterable<Clause>): Solver = PrologSolverFactory
    .newBuilder()
    .unificator(storedFirst)
    .staticKb(theory)
    .flag(Unknown, Unknown.FAIL)
    // without it, a rule loses the bindings of its head before a built-in in its body, as `is` or `\=`
    .flag(TrackVariables) { ON }
    .build()

/** The annotations of this term, none if it has none. */
internal val Term.rawAnnotations: Set<Struct>
    get() = getTag<Set<Struct>>(JAKTA_ANNOTATIONS_TAG).orEmpty()

private fun Struct.withLast(argument: Term): Struct = Struct.of(functor, args + argument)

private fun Struct.signature() = Signature(functor, arity)

private val selfOnly = setOf(source(self))

// the signatures of the terms with annotations in this term, at any depth
private fun Term.annotatedSignatures(): Sequence<Signature> = when (this) {
    is Struct -> args.asSequence().flatMap { it.annotatedSignatures() } +
        if (rawAnnotations.isEmpty()) emptySequence() else sequenceOf(signature())

    else -> emptySequence()
}

// the predicates whose annotations matter, which the solver must see with their annotations as last argument
private fun annotatedPredicates(theory: Collection<Clause>, query: Struct): Set<Signature> = theory.flatMap { clause ->
    val head = clause.head ?: return@flatMap emptyList()
    val own = head.rawAnnotations.isEmpty() || head.rawAnnotations == selfOnly
    listOfNotNull(head.signature().takeUnless { own }) + clause.body.annotatedSignatures()
}.toSet() + query.annotatedSignatures()

private fun encodeGoal(term: Term, annotated: Set<Signature>): Term {
    if (term !is Struct) return term
    val signature = term.signature()
    val control = controlArguments[signature]
    return when {
        control != null -> term.setArgs(
            term.args.mapIndexed { i, argument -> if (i in control) encodeGoal(argument, annotated) else argument },
        )

        signature !in annotated -> term

        term.rawAnnotations.isEmpty() -> term.withLast(Var.anonymous())

        else -> {
            val annotations = Var.of("Annotations")
            val selections = term.rawAnnotations.fold(annotations to listOf<Term>()) { (rest, goals), annotation ->
                val left = Var.of("Rest")
                left to goals + Struct.of(SELECT, annotation, rest, left)
            }.second
            Tuple.of(listOf(term.withLast(annotations)) + selections)
        }
    }
}

private fun encodeClause(clause: Clause, annotated: Set<Signature>): Clause {
    val head = clause.head ?: return clause
    val encodedHead = when (head.signature()) {
        in annotated -> head.withLast(PrologList.of(head.rawAnnotations.ifEmpty { selfOnly }))
        else -> head
    }
    return if (clause is Fact) Fact.of(encodedHead) else Rule.of(encodedHead, encodeGoal(clause.body, annotated))
}

/** The variables of this term, those in its annotations included. */
internal fun Term.allVariables(): Set<Var> = when (this) {
    is Var -> setOf(this)
    is Struct -> args.flatMap { it.allVariables() }.toSet() + rawAnnotations.flatMap { it.allVariables() }
    else -> emptySet()
}

private fun Term.hasAnnotations(): Boolean =
    this is Struct && (rawAnnotations.isNotEmpty() || args.any { it.hasAnnotations() })

/**
 * This term with [substitution] applied to it, and to its annotations too, which [Term.apply] leaves untouched.
 */
fun Term.applyWithAnnotations(substitution: Substitution): Term = when {
    !hasAnnotations() -> apply(substitution)

    else -> {
        val struct = this as Struct
        val applied = struct.setArgs(struct.args.map { it.applyWithAnnotations(substitution) })
        val annotations = struct.rawAnnotations.map { it.apply(substitution).castToStruct() }.toSet()
        if (annotations.isEmpty()) applied else applied.setTag(JAKTA_ANNOTATIONS_TAG, annotations)
    }
}

/**
 * The solutions of [query] on [theory], with annotations solved as plain terms: each binds the variables of the query,
 * those in its annotations included. Lazily, so that [solveOnce] stops at the first one.
 */
private fun solve(theory: Collection<Clause>, query: Struct): Sequence<Solution> {
    val annotated = annotatedPredicates(theory, query)
    val variables = query.allVariables()
    val solver = when {
        annotated.isEmpty() -> solverOf(theory)
        else -> solverOf(theory.map { encodeClause(it, annotated) } + selectClauses)
    }
    return solver.solve(encodeGoal(query, annotated).castToStruct()).map { solution ->
        when (solution) {
            is Solution.Yes -> Solution.yes(
                query,
                Substitution.unifier(solution.substitution.filterKeys { it in variables }),
            )

            is Solution.No -> Solution.no(query)

            is Solution.Halt -> Solution.halt(query, solution.exception)
        }
    }
}

/** The first solution of [query] on these beliefs and rules, annotations included. */
internal fun Collection<Clause>.solveOnce(query: Struct): Solution = solve(this, query).first()

/** Every solution of [query] on these beliefs and rules, annotations included. */
internal fun Collection<Clause>.solveAll(query: Struct): List<Solution> = solve(this, query).toList()

private const val ANNOTATED = "\$annotated"

/**
 * This term with its annotations written as plain terms, to format it as text: every annotated term `t[a1, ..., ak]`,
 * at any depth, is `'$annotated'(t, [a1, ..., ak])`. [withAnnotationsFromTerms] restores them.
 */
fun Term.withAnnotationsAsTerms(): Term = when {
    !hasAnnotations() -> this

    else -> {
        val struct = this as Struct
        val plain = struct.setArgs(struct.args.map { it.withAnnotationsAsTerms() })
        val annotations = struct.rawAnnotations
        if (annotations.isEmpty()) {
            plain
        } else {
            Struct.of(ANNOTATED, plain, PrologList.of(annotations.map { it.withAnnotationsAsTerms() }))
        }
    }
}

/**
 * This term with the annotations written by [withAnnotationsAsTerms] restored, as parsed from text.
 */
fun Term.withAnnotationsFromTerms(): Term = when {
    this !is Struct || arity == 0 -> this

    functor == ANNOTATED && arity == 2 && this[1] is PrologList -> {
        val annotations = (this[1] as PrologList).toList().map { it.withAnnotationsFromTerms().castToStruct() }
        this[0].withAnnotationsFromTerms().tag(*annotations.toTypedArray())
    }

    else -> setArgs(args.map { it.withAnnotationsFromTerms() })
}
