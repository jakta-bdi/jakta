package io.github.anitvam.agents.bdi.dsl.beliefs

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.dsl.Builder
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.LogicProgrammingScope

class BeliefsScope(
    private val lp: LogicProgrammingScope = LogicProgrammingScope.empty()
) : Builder<BeliefBase>, LogicProgrammingScope by lp {

    private val beliefs = mutableListOf<Belief>()

    fun fact(struct: Struct) {
        val belief: Belief = Belief.wrap(struct.freshCopy())
        beliefs.add(belief)
    }

    override fun rule(function: LogicProgrammingScope.() -> Any): Rule {
        return lp.rule(function).also { rule(it) }
    }

    fun fact(atom: String) = fact(atomOf(atom))

    override fun fact(function: LogicProgrammingScope.() -> Any): Fact {
        return lp.fact(function).also { fact(it) }
    }

    fun rule(rule: Rule) {
        @Suppress("NAME_SHADOWING")
        val rule = rule.freshCopy()
        val belief: Belief = Belief.wrap(rule.head, rule.bodyItems)
        beliefs.add(belief)
    }

    override fun build(): BeliefBase = BeliefBase.of(beliefs)
}
