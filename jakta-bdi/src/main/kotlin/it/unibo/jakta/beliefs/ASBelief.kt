package it.unibo.jakta.beliefs

import it.unibo.jakta.beliefs.impl.ASBeliefImpl
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var

interface ASBelief {

    val content: Rule

    fun applySubstitution(substitution: Substitution): ASBelief

    companion object {

        val SOURCE_PERCEPT: Term = Struct.of("source", Atom.of("percept"))
        val SOURCE_SELF: Term = Struct.of("source", Atom.of("self"))
        val SOURCE_UNKNOWN: Term = Struct.of("source", Var.of("Source"))

        fun wrap(head: Struct, body: Iterable<Term> = emptyList(), wrappingTag: Term = SOURCE_UNKNOWN): ASBelief {
            if (head.arity >= 1 && head[0].let { it is Struct && it.arity == 1 && it.functor == "source" }) {
                return ASBeliefImpl(Rule.of(head, body))
            }
            return ASBeliefImpl(Rule.of(head.addFirst(wrappingTag), body))
        }

        fun of(head: Struct, body: Iterable<Term>, isFromPerceptSource: Boolean): ASBelief {
            val headArguments = (if (isFromPerceptSource) listOf(SOURCE_PERCEPT) else listOf(SOURCE_SELF)) + head.args
            return ASBeliefImpl(
                Rule.of(
                    Struct.of(head.functor, headArguments),
                    body,
                ),
            )
        }

        fun of(head: Struct, body: Iterable<Term>, from: String): ASBelief {
            val headArguments = listOf(Struct.of("source", Atom.of(from))) + head.args
            return ASBeliefImpl(
                Rule.of(
                    Struct.of(head.functor, headArguments),
                    body,
                ),
            )
        }

        fun fromSelfSource(head: Struct, vararg body: Term): ASBelief = fromSelfSource(head, body.asIterable())

        fun fromSelfSource(head: Struct, body: Sequence<Term>): ASBelief = fromSelfSource(head, body.asIterable())

        fun fromSelfSource(head: Struct, body: Iterable<Term>): ASBelief = of(head, body, false)

        fun fromPerceptSource(head: Struct, vararg body: Term): ASBelief = fromPerceptSource(head, body.asIterable())

        fun fromPerceptSource(head: Struct, body: Sequence<Term>): ASBelief = fromPerceptSource(head, body.asIterable())

        fun fromPerceptSource(head: Struct, body: Iterable<Term>): ASBelief = of(head, body, true)

        fun fromMessageSource(from: String, head: Struct, vararg body: Term): ASBelief =
            fromMessageSource(from, head, body.asIterable())

        fun fromMessageSource(from: String, head: Struct, body: Sequence<Term>): ASBelief =
            fromMessageSource(from, head, body.asIterable())

        fun fromMessageSource(from: String, head: Struct, body: Iterable<Term>): ASBelief = of(head, body, from)

        fun from(rule: Rule): ASBelief {
            if (rule.head.args.isNotEmpty() &&
                rule.head.args.first() is Struct &&
                rule.head.args.first().castToStruct().functor == "source"
            ) {
                return ASBeliefImpl(rule)
            }
            throw IllegalArgumentException("The rule is not a belief: $rule")
        }

        fun from(struct: Struct): ASBelief = from(Rule.of(struct))
    }
}
