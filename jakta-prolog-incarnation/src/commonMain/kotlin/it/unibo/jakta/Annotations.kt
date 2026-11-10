package it.unibo.jakta

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.utils.setTag

/**
 * Tag used to store annotations on Prolog terms.
 */
const val JAKTA_ANNOTATIONS_TAG = "jakta.annotations"

/**
 * Creates a [Struct] for the source annotation using the passed [AgentID.displayName] as the source.
 */
fun source(id: AgentID): Struct = source(id.toString())

/**
 * Creates a [Struct] for the source annotation using the passed string as the source.
 */
fun source(source: String): Struct = source(Atom.of(source))

/**
 * Creates a [Struct] for the source annotation using the passed [Atom] as the source.
 */
fun source(source: Term): Struct = Struct.of("source", source)

/**
 * An [Atom] representing the "self" annotation.
 */
val self = Atom.of("self")

/*
 * Annotations, as Jason's `p(1)[source(alice)]`, are metadata of a term: equal terms with different annotations are
 * equal. The functions below read and change them on any term (a belief, a query, a goal); on a belief, a [Rule] or a
 * [Fact], they act on its head.
 *
 * A belief without annotations counts as `[source(self)]`: matching treats it so, and the annotated belief revision
 * stores it so. Sources other than an agent, such as `source(percept)` for what the agent perceives, are up to the
 * perception handler, which annotates the beliefs it produces.
 */

/**
 * The annotations of this term, as written: none if it has none. On a belief, those of its head.
 */
val Term.annotations: Set<Struct>
    get() = annotatedPart.getTag<Set<Struct>>(JAKTA_ANNOTATIONS_TAG).orEmpty()

/**
 * This term with [annotations] added to those it has: `p(1)[source(alice)]` with `certain` added is
 * `p(1)[certain, source(alice)]`. In the DSL, `p(1)[certain]` does the same.
 */
fun <T : Term> T.addAnnotations(vararg annotations: Struct): T = withAnnotations(setOf(*annotations) + this.annotations)

/**
 * This term without [annotations]: `p(1)[certain, source(alice)]` without `source(alice)` is `p(1)[certain]`.
 */
fun <T : Term> T.removeAnnotations(vararg annotations: Struct): T =
    withAnnotations(this.annotations - setOf(*annotations))

/**
 * This term without any annotation: `p(1)[certain, source(alice)]` becomes `p(1)`.
 */
fun <T : Term> T.removeAllAnnotations(): T = withAnnotations(emptySet())

/**
 * This term with exactly [annotations], replacing those it has: `p(1)[certain, source(alice)]` with only
 * `source(alice)` is `p(1)[source(alice)]`. A replacement scope uses it to say which annotations of a belief it means.
 */
fun <T : Term> T.setAnnotations(vararg annotations: Struct): T = withAnnotations(setOf(*annotations))

/**
 * Annotates a term with the operator syntax `p(1)[annotation, ...]`, as [addAnnotations].
 */
operator fun <T : Term> T.get(annotation: Struct, vararg otherAnnotations: Struct): T =
    addAnnotations(annotation, *otherAnnotations)

private val Term.annotatedPart: Term
    get() = if (this is Rule) head else this

private fun <T : Term> T.withAnnotations(annotations: Set<Struct>): T {
    @Suppress("UNCHECKED_CAST")
    return when (this) {
        is Rule -> {
            val annotatedHead = head.setTag(JAKTA_ANNOTATIONS_TAG, annotations)
            if (isFact) Fact.of(annotatedHead) else Rule.of(annotatedHead, body)
        }

        else -> setTag(JAKTA_ANNOTATIONS_TAG, annotations)
    } as T
}

/**
 * The annotations of this belief, where a belief without annotations counts as `[source(self)]`.
 */
internal val PrologBelief.effectiveAnnotations: Set<Struct>
    get() = annotations.ifEmpty { setOf(source(self)) }
