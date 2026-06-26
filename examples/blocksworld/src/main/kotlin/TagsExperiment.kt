import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.unify.AbstractUnificator
import it.unibo.tuprolog.unify.Unificator
import it.unibo.tuprolog.utils.setTag

/**
 * Extension function to annotate a Prolog term with one or more annotations
 * using the operator syntax term[ann, ann2, ...].
 */
operator fun Term.get(annotation: Struct, vararg otherAnnotations: Struct): Term =
    setTag("jakta.annotations", setOf(annotation, *otherAnnotations))

/**
 * test val.
 */
val annotatedStruct = with(JaktaLogicProgrammingScope()) {
    "f"(X)["g"(1), "g"(2)]
}

/**
 * test val.
 */
val unannotatedStruct = with(JaktaLogicProgrammingScope()) {
    "f"("a")["g"(A), "g"(2), "g"(B)]
}

/**
 * Custom unificator that handles annotations in Prolog terms.
 * It extends the AbstractUnificator and overrides the mgu method to consider annotations during unification.
 */
val unificator = object : AbstractUnificator() {
    override fun checkTermsEquality(first: Term, second: Term): Boolean = first == second

    // TODO this is a temporary solution to make detekt happy, but we should refactor the whole thing
    @Suppress("UNCHECKED_CAST", "NestedBlockDepth", "LoopWithTooManyJumpStatements")
    override fun mgu(term1: Term, term2: Term, occurCheckEnabled: Boolean): Substitution {
        var result = super.mgu(term1, term2, occurCheckEnabled)

        if (result !is Substitution.Fail) {
            if ("jakta.annotations" in term1.tags || "jakta.annotations" in term2.tags) {
                val annotations1 =
                    (term1.tags["jakta.annotations"] as? Set<Struct>).orEmpty().toMutableList()
                val annotations2 =
                    (term2.tags["jakta.annotations"] as? Set<Struct>).orEmpty().toMutableList()

                if (annotations1.size < annotations2.size) {
                    result = Substitution.failed()
                } else {
                    for (annotation in annotations2) {
                        val i = annotations1.listIterator()
                        while (i.hasNext()) {
                            val candidate = i.next()
                            val mguForAnnotation = super.mgu(candidate, annotation, occurCheckEnabled)
                            if (mguForAnnotation is Substitution.Fail) {
                                result = mguForAnnotation
                                break
                            }
                            result += mguForAnnotation
                            i.remove()
                            break
                        }
                        if (result is Substitution.Fail) break
                    }
                }
            }
        }
        return result
    }
}

/**
 * Main function to test the custom unificator with annotated and unannotated Prolog terms.
 */
fun main(args: Array<String>) {
    println(unificator.mgu(annotatedStruct, unannotatedStruct))
}
