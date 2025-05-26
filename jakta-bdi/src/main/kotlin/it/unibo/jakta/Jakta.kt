package it.unibo.jakta

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.TermFormatter
import it.unibo.tuprolog.core.Var
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
    fun parseVar(string: String): Var = parser.parseVar(string)
    fun parseClause(string: String): Clause = parser.parseClause(string)

    fun printAslSyntax(agent: ASAgent, prettyFormatted: Boolean = true) {
        println("% ${agent.context.agentName}")
        for (belief in agent.context.beliefBase.filterIsInstance<ASBelief>()) {
            if (prettyFormatted) {
                val formatter = TermFormatter.prettyExpressions(operatorSet = OperatorSet.DEFAULT + Jakta.operators)
                println(formatter.format(belief.content))
            } else {
                println(belief.content)
            }
        }
        for (plan in agent.context.plans) {
            var trigger = plan.trigger.value.toString()
            var guard = plan.guard.toString()
            var body = plan.toActivationRecord().taskQueue.joinToString("; ") { it.toString() }
            if (prettyFormatted) {
                val formatter = TermFormatter.prettyExpressions(operatorSet = OperatorSet.DEFAULT + Jakta.operators)
                trigger = formatter.format(plan.trigger.value)
                guard = formatter.format(plan.guard)
                // body = plan.tasks.joinToString("; ") { formatter.format(it.value) }
                // TODO(Missing generator of ASL syntax from Jakta spec)
            }
            println("+!$trigger : $guard <- $body")
        }
    }
}
