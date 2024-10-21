package it.unibo.jakta.beliefs

import it.unibo.jakta.beliefs.impl.PrologBeliefImpl
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var

interface PrologBelief : Rule {

    fun applySubstitution(substitution: Substitution): PrologBelief

    companion object {

        val SOURCE_PERCEPT: Term = Struct.of("source", Atom.of("percept"))
        val SOURCE_SELF: Term = Struct.of("source", Atom.of("self"))
        val SOURCE_UNKNOWN: Term = Struct.of("source", Var.of("Source"))

        fun wrap(head: Struct, body: Iterable<Term> = emptyList(), wrappingTag: Term = SOURCE_UNKNOWN): PrologBelief {
            if (head.arity >= 1 && head[0].let { it is Struct && it.arity == 1 && it.functor == "source" }) {
                return PrologBeliefImpl(Rule.of(head, body))
            }
            return PrologBeliefImpl(Rule.of(head.addFirst(wrappingTag), body))
        }

        fun of(head: Struct, body: Iterable<Term>, isFromPerceptSource: Boolean): PrologBelief {
            val headArguments = (if (isFromPerceptSource) listOf(SOURCE_PERCEPT) else listOf(SOURCE_SELF)) + head.args
            return PrologBeliefImpl(
                Rule.of(
                    Struct.of(head.functor, headArguments),
                    body,
                ),
            )
        }

        fun of(head: Struct, body: Iterable<Term>, from: String): PrologBelief {
            val headArguments = listOf(Struct.of("source", Atom.of(from))) + head.args
            return PrologBeliefImpl(
                Rule.of(
                    Struct.of(head.functor, headArguments),
                    body,
                ),
            )
        }

        fun fromSelfSource(head: Struct, vararg body: Term): PrologBelief =
            fromSelfSource(head, body.asIterable())

        fun fromSelfSource(head: Struct, body: Sequence<Term>): PrologBelief =
            fromSelfSource(head, body.asIterable())

        fun fromSelfSource(head: Struct, body: Iterable<Term>): PrologBelief =
            of(head, body, false)

        fun fromPerceptSource(head: Struct, vararg body: Term): PrologBelief =
            fromPerceptSource(head, body.asIterable())

        fun fromPerceptSource(head: Struct, body: Sequence<Term>): PrologBelief =
            fromPerceptSource(head, body.asIterable())

        fun fromPerceptSource(head: Struct, body: Iterable<Term>): PrologBelief =
            of(head, body, true)

        fun fromMessageSource(from: String, head: Struct, vararg body: Term): PrologBelief =
            fromMessageSource(from, head, body.asIterable())

        fun fromMessageSource(from: String, head: Struct, body: Sequence<Term>): PrologBelief =
            fromMessageSource(from, head, body.asIterable())

        fun fromMessageSource(from: String, head: Struct, body: Iterable<Term>): PrologBelief =
            of(head, body, from)

        fun from(rule: Rule): PrologBelief {
            if (rule.head.args.isNotEmpty() &&
                rule.head.args.first() is Struct &&
                rule.head.args.first().castToStruct().functor == "source"
            ) {
                return PrologBeliefImpl(rule)
            }
            throw IllegalArgumentException("The rule is not a belief: $rule")
        }

        fun from(struct: Struct): PrologBelief = from(Rule.of(struct))
    }
}
