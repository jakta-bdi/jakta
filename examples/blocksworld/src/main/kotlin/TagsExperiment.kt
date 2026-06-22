import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.unify.AbstractUnificator
import it.unibo.tuprolog.unify.Unificator
import it.unibo.tuprolog.utils.setTag


operator fun Term.get(annotation: Struct, vararg otherAnnotations: Struct): Term {
    return setTag("jakta.annotations", setOf(annotation, *otherAnnotations))
}

val annotatedStruct = with(JaktaLogicProgrammingScope()) {
    "f"(X)["g"(1), "g"(2)]
}

val unannotatedStruct = with(JaktaLogicProgrammingScope()) {
    "f"("a")["g"(A), "g"(2), "g"(B)]
}

val unificator = object : AbstractUnificator() {
    override fun checkTermsEquality(
        first: Term,
        second: Term
    ): Boolean = first == second

    @Suppress("UNCHECKED_CAST")
    override fun mgu(term1: Term, term2: Term, occurCheckEnabled: Boolean): Substitution {
        val mguWithoutAnnotations = super.mgu(term1, term2, occurCheckEnabled)
        if (mguWithoutAnnotations is Substitution.Fail) {
            return mguWithoutAnnotations
        }
        if ("jakta.annotations" !in term1.tags && "jakta.annotations" !in term2.tags) {
            return mguWithoutAnnotations
        }
        val annotations1 = (term1.tags["jakta.annotations"] as? Set<Struct> ?: emptySet()).toMutableList()
        val annotations2 = (term2.tags["jakta.annotations"] as? Set<Struct> ?: emptySet()).toMutableList()
        if (annotations1.size < annotations2.size) {
            return Substitution.failed()
        }
        var result = mguWithoutAnnotations
        for (annotation in annotations2) {
            val i = annotations1.listIterator()
            while (i.hasNext()) {
                val candidate = i.next()
                val mguForAnnotation = super.mgu(candidate, annotation, occurCheckEnabled)
                if (mguForAnnotation is Substitution.Fail) {
                    return mguForAnnotation
                }
                result += mguForAnnotation
                i.remove()
                break
            }
        }
        return result
    }
}

fun main(args: Array<String>) {
    println(unificator.mgu(annotatedStruct, unannotatedStruct))
}
