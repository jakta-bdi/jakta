package it.unibo.jakta

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.logic.MutableSubstitutionPlanContext
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.serialize.TermObjectifier
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

/**
 * Extension method to tag terms with JaKtA annotations.
 */
fun <T : Term> T.tag(vararg annotation: Struct): T {
    val previousAnnotations = this.getTag<Set<Struct>>(JAKTA_ANNOTATIONS_TAG)
    val struct = this.setTag(
        JAKTA_ANNOTATIONS_TAG,
        setOf(*annotation) + (previousAnnotations.orEmpty()),
    )
    return struct
}

/**
 * Extension function to annotate a Prolog struct with one or more annotations
 * using the operator syntax struct(...)[ann, ann2, ...].
 */
operator fun <T : Term> T.get(annotation: Struct, vararg otherAnnotations: Struct): T =
    tag(annotation, *otherAnnotations)

/**
 * Extension function to convert a variable to a Kotlin type using the provided substitution.
 * @receiver The [Var] to be converted.
 * @return The value of the variable as the specified Kotlin type [T].
 * @throws IllegalArgumentException if the variable cannot be cast to the expected type.
 */
@Suppress("UNCHECKED_CAST")
context(context: MutableSubstitutionPlanContext)
inline fun <reified T : Any> Var.value(): T {
    val term = context.substitution[this]
        ?: error { "Variable $this is not ground in the substitution $context.substitution" }

    val result = term as? T
        ?: term.accept(TermObjectifier.default) as? T
        ?: error { "Term $this cannot be cast to the expected type" }
    return result
}

/**
 * Extension function to print 2p-kt logic variables substituted with their values from the current substitution.
 * @receiver The mutable agent state containing the current substitution.
 * @param parts The parts to be printed, which can include variables and other objects.
 */
context(context: MutableSubstitutionPlanContext)
fun MutableAgentState<PrologBelief, PrologGoal>.print(vararg parts: Any?) {
    val text = buildString {
        for (part in parts) {
            append(
                when (part) {
                    is Var -> part.value()
                    else -> part
                },
            )
        }
    }
    print(text)
}
