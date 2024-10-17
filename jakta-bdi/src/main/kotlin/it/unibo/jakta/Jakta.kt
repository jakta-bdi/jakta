package it.unibo.jakta

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.TermFormatter
import it.unibo.tuprolog.core.operators.Operator
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.core.operators.Specifier
import it.unibo.tuprolog.core.parsing.TermParser

object Jakta {

    // fun convert1(struct: Struct): Term = struct.accept(jasonTo2p)

    val operators = OperatorSet(
        Operator("&", Specifier.XFY, 1000),
        Operator("|", Specifier.XFY, 1100),
        Operator("~", Specifier.FX, 900),
    )

    private val parser = TermParser.withOperators(OperatorSet.DEFAULT + operators)
    fun parseStruct(string: String): Struct = parser.parseStruct(string)
    fun parseClause(string: String): Clause = parser.parseClause(string)

    fun printAslSyntax(agent: Agent, prettyFormatted: Boolean = true) {
        println("% ${agent.name}")
        for (belief in agent.context.beliefBase) {
            if (prettyFormatted) {
                val formatter = TermFormatter.prettyExpressions(operatorSet = OperatorSet.DEFAULT + Jakta.operators)
                println(formatter.format(belief.rule))
            } else {
                println(belief.rule)
            }
        }
        for (plan in agent.context.planLibrary.plans) {
            var trigger = plan.trigger.value.toString()
            var guard = plan.guard.toString()
            var body = plan.goals.joinToString("; ") { it.value.toString() }
            if (prettyFormatted) {
                val formatter = TermFormatter.prettyExpressions(operatorSet = OperatorSet.DEFAULT + Jakta.operators)
                trigger = formatter.format(plan.trigger.value)
                guard = formatter.format(plan.guard)
                body = plan.goals.joinToString("; ") { formatter.format(it.value) }
            }
            println("+!$trigger : $guard <- $body")
        }
    }
}
