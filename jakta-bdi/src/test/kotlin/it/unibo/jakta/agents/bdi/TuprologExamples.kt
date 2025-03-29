package it.unibo.jakta.agents.bdi

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.operators.Operator
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.core.operators.Specifier
import it.unibo.tuprolog.core.parsing.TermParser
import it.unibo.tuprolog.core.visitors.DefaultTermVisitor

class TuprologExamples :
    DescribeSpec({

        val customOperators =
            OperatorSet(
                Operator("&", Specifier.XFY, 1000),
                Operator("|", Specifier.XFY, 1100),
                Operator("~", Specifier.FX, 900),
            )

        describe("parsing custom fixing operators") {
            it("is possibly by customising a term parser") {
                val parser = TermParser.withOperators(OperatorSet.DEFAULT + customOperators)
                val actual = parser.parseStruct("~a | b & c")
                val expected =
                    Struct.of(
                        "|",
                        Struct.of("~", Atom.of("a")),
                        Struct.of("&", Atom.of("b"), Atom.of("c")),
                    )
                println(actual)
                actual shouldBe expected
            }
        }

        val jason2Prolog =
            object : DefaultTermVisitor<Term>() {
                override fun defaultValue(term: Term): Term = term

                override fun visitStruct(term: Struct): Term =
                    when {
                        term.arity == 2 && term.functor == "&" ->
                            Struct.of(",", term.args.map { it.accept(this) })
                        term.arity == 2 && term.functor == "|" ->
                            Struct.of(";", term.args.map { it.accept(this) })
                        term.arity == 1 && term.functor == "~" ->
                            Struct.of("not", term.args.map { it.accept(this) })
                        else -> super.visitStruct(term)
                    }
            }

        describe("passing from jason syntax to prolog syntax") {
            it("requires a term visitor") {
                val struct =
                    Struct.of(
                        "|",
                        Struct.of("~", Atom.of("a")),
                        Struct.of("&", Atom.of("b"), Atom.of("c")),
                    )
                val actual = struct.accept(jason2Prolog)
                val expected =
                    Struct.of(
                        ";",
                        Struct.of("not", Atom.of("a")),
                        Struct.of(",", Atom.of("b"), Atom.of("c")),
                    )
                println(actual)
                actual shouldBe expected
            }
        }
    })
