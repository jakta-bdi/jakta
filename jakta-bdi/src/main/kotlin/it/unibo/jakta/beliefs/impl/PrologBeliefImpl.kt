package it.unibo.jakta.beliefs.impl

import it.unibo.jakta.beliefs.PrologBelief
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

internal class PrologBeliefImpl(override val content: Rule) : PrologBelief {

    override fun applySubstitution(substitution: Substitution): PrologBelief =
        PrologBeliefImpl(content.apply(substitution).castToRule())

    override fun hashCode(): Int = content.hashCode()

    override fun toString(): String {
        val escaped = Struct.escapeFunctorIfNecessary(content.head.functor)
        val quoted = Struct.enquoteFunctorIfNecessary(escaped)
        return "$quoted[${content.head.args.first()}]" +
            (if (content.head.arity > 1) "(${content.head.args.drop(1).joinToString(", ")})" else "") +
            if (content.body.isTrue) "." else " ${content.functor} ${content.body}."
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PrologBeliefImpl
        return content == other.content
    }
}
