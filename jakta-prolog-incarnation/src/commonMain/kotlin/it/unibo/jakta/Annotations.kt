package it.unibo.jakta

import it.unibo.jakta.agent.AgentID
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
 * The source of the agent's own beliefs, as in `source(self)`: a belief without a source is the agent's own.
 */
val self = Atom.of("self")

/**
 * The source of what the agent perceives, as in `source(percept)`: a perception handler marks its beliefs with it,
 * e.g. by `replaceFrom(percept, queries, beliefs)`.
 */
val percept = Atom.of("percept")

/*
 * Annotations, as Jason's `p(1)[source(alice)]`, are metadata of a term: equal terms with different annotations are
 * equal. The functions below read and change them on any term (a belief, a query, a goal); on a belief, a [Rule] or a
 * [Fact], they act on its head. Matching reads them as written: a query without annotations matches any, and one with
 * annotations matches the beliefs that have them.
 *
 * What annotations mean, e.g. that a belief without a source is the agent's own, is up to the belief revision: see
 * `AnnotatedBeliefRevision`. Without it, beliefs are a plain set and keep the annotations they are written with.
 */

/**
 * The annotations of this term, as written: none if it has none. On a belief, those of its head.
 */
val Term.annotations: Set<Struct>
    get() = (if (this is Rule) head else this).getTag<Set<Struct>>(JAKTA_ANNOTATIONS_TAG).orEmpty()

/**
 * This term with [annotations] added to those it has: `p(1)[source(alice)]` with `certain` added is
 * `p(1)[certain, source(alice)]`. In the DSL, `p(1)[certain]` does the same.
 */
fun <T : Term> T.addAnnotations(vararg annotations: Struct): T = setAnnotations(setOf(*annotations) + this.annotations)

/**
 * This term without [annotations]: `p(1)[certain, source(alice)]` without `source(alice)` is `p(1)[certain]`.
 */
fun <T : Term> T.removeAnnotations(vararg annotations: Struct): T =
    setAnnotations(this.annotations - setOf(*annotations))

/**
 * This term without any annotation: `p(1)[certain, source(alice)]` becomes `p(1)`.
 */
fun <T : Term> T.removeAllAnnotations(): T = setAnnotations(emptySet())

/**
 * Annotates a term with the operator syntax `p(1)[annotation, ...]`, as [addAnnotations].
 */
operator fun <T : Term> T.get(annotation: Struct, vararg otherAnnotations: Struct): T =
    addAnnotations(annotation, *otherAnnotations)

/**
 * This term with exactly [annotations], replacing those it has: `p(1)[certain, source(alice)]` with only
 * `setOf(source(alice))` is `p(1)[source(alice)]`. A replacement scope uses it to say which annotations of a belief it
 * means.
 */
fun <T : Term> T.setAnnotations(annotations: Set<Struct>): T {
    // tuProlog's setTag returns a Term, and a belief is rebuilt from its head: both stay of the receiver's own type
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
 * Whether this annotation is a source, `source(S)`.
 */
internal val Struct.isSource: Boolean
    get() = functor == "source" && arity == 1
