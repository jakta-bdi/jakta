package it.unibo.jakta.agents.bdi.dsl.beliefs

import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.dsl.Builder
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.LogicProgrammingScope
import it.unibo.tuprolog.solve.stdlib.rule.SetPrologFlag.head

class BeliefsScope(
    private val lp: LogicProgrammingScope = LogicProgrammingScope.empty(),
) : Builder<BeliefBase>, LogicProgrammingScope by lp {

    private val beliefs = mutableListOf<Belief>()

    fun fact(struct: Struct) {
        val s = struct.castToRule().head
        val belief: Belief = Belief.wrap(s.freshCopy())
        beliefs.add(belief)
    }

    override fun fact(function: LogicProgrammingScope.() -> Any): Fact {
        return lp.fact(function).also { fact(it) }
    }

    fun fact(atom: String) = fact(atomOf(atom))

    override fun rule(function: LogicProgrammingScope.() -> Any): Rule {
        return lp.rule(function).also { rule(it) }
    }

    fun rule(rule: Rule) {
        val freshRule = rule.freshCopy()
        val belief: Belief = Belief.wrap(freshRule.head, freshRule.bodyItems)
        beliefs.add(belief)
    }

    fun rule(f: LogicProgrammingScope.() -> Rule) = rule(f())

    override fun build(): BeliefBase = BeliefBase.of(beliefs)
}
