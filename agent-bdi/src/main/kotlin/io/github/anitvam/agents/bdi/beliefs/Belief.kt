package io.github.anitvam.agents.bdi.beliefs

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import io.github.anitvam.agents.bdi.beliefs.impl.BeliefImpl

interface Belief {
    val rule: Rule

    fun applySubstitution(substitution: Substitution): Belief

    companion object {

        val SOURCE_PERCEPT: Term = Struct.of("source", Atom.of("percept"))
        val SOURCE_SELF: Term = Struct.of("source", Atom.of("self"))

        fun of(head: Struct, body: Iterable<Term>, isFromPerceptSource: Boolean): Belief {
            val headArguments = (if (isFromPerceptSource) listOf(SOURCE_PERCEPT) else listOf(SOURCE_SELF)) + head.args
            return BeliefImpl(
                Rule.of(
                    Struct.of(head.functor, headArguments),
                    body,
                )
            )
        }

        fun fromSelfSource(head: Struct, vararg body: Term): Belief =
            fromSelfSource(head, body.asIterable())

        fun fromSelfSource(head: Struct, body: Sequence<Term>): Belief =
            fromSelfSource(head, body.asIterable())

        fun fromSelfSource(head: Struct, body: Iterable<Term>): Belief =
            of(head, body, false)

        fun fromPerceptSource(head: Struct, vararg body: Term): Belief =
            fromPerceptSource(head, body.asIterable())

        fun fromPerceptSource(head: Struct, body: Sequence<Term>): Belief =
            fromPerceptSource(head, body.asIterable())

        fun fromPerceptSource(head: Struct, body: Iterable<Term>): Belief =
            of(head, body, true)

        fun from(rule: Rule): Belief {
            if (rule.head.args.first() != SOURCE_PERCEPT && rule.head.args.first() != SOURCE_SELF)
                throw IllegalArgumentException("The rule is not a belief.")
            return BeliefImpl(rule)
        }
    }
}
