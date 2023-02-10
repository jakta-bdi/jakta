package io.github.anitvam.agents.bdi.dsl.beliefs

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.dsl.Builder
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.LogicProgrammingScope

class BeliefsScope : Builder<BeliefBase>, LogicProgrammingScope by LogicProgrammingScope.empty() {

    private val beliefs = mutableListOf<Belief>()

    private val lp = LogicProgrammingScope.empty()

    fun fact(struct: Struct) {
        val belief: Belief = Belief.fromSelfSource(struct)
        beliefs.add(belief)
    }

    fun fact(atom: String) = fact(atomOf(atom))

    fun rule(rule: Rule) {
        val belief: Belief = Belief.fromSelfSource(rule.head, rule.bodyItems)
        beliefs.add(belief)
    }

    override fun build(): BeliefBase = BeliefBase.of(beliefs)
}
