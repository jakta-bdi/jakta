package io.github.anitvam.agents.bdi.dsl

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.LogicProgrammingScope

class BeliefsScope : Builder<BeliefBase> {

    private val beliefs = mutableListOf<Belief>()

    private val lp = LogicProgrammingScope.empty()

    fun fact(f: BeliefScope.() -> Struct) {
        val struct = BeliefScope().f()
        val belief: Belief = Belief.fromSelfSource(struct)
        beliefs.add(belief)
    }

    fun rule(f: BeliefScope.() -> Rule) {
        val rule = BeliefScope().f()
        val belief: Belief = Belief.fromSelfSource(rule.head, rule.bodyItems)
        beliefs.add(belief)
    }

    override fun build(): BeliefBase = BeliefBase.of(beliefs)
}
