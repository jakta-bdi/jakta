package io.github.anitvam.agents.bdi.beliefs.impl

import io.github.anitvam.agents.bdi.beliefs.Belief
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

internal class BeliefImpl(override val rule: Rule) : Belief {

    override fun applySubstitution(substitution: Substitution): Belief =
        BeliefImpl(rule.apply(substitution).castToRule())

    override fun hashCode(): Int = rule.hashCode()

    override fun toString(): String {
        val escaped = Struct.escapeFunctorIfNecessary(rule.head.functor)
        val quoted = Struct.enquoteFunctorIfNecessary(escaped)
        return "$quoted[${rule.head.args.first()}]" +
            (if (rule.head.arity > 1) "(${rule.head.args.drop(1).joinToString(", ")})" else "") +
            if (rule.body.isTrue) "." else " ${rule.functor} ${rule.body}."
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BeliefImpl
        return rule == other.rule
    }
}
