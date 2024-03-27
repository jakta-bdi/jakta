package it.unibo.jakta.agents.bdi.dsl.beliefs

import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.dsl.Builder
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.LogicProgrammingScope
import it.unibo.tuprolog.solve.stdlib.rule.SetPrologFlag.head

/**
 * Builder for Jakta Agents's [BeliefBase].
 */
class BeliefsScope(
    private val lp: LogicProgrammingScope = LogicProgrammingScope.empty(),
) : Builder<BeliefBase>, LogicProgrammingScope by lp {

    private val beliefs = mutableListOf<Belief>()

    /**
     * Handler for the addition of a fact [Belief] into the agent's [BeliefBase].
     * @param struct the [Struct] that represents the [Belief].
     */
    fun fact(struct: Struct) =
        beliefs.add(Belief.fromSelfSource(struct.freshCopy()))

    /**
     * Handler for the addition of a fact [Belief] into the agent's [BeliefBase].
     * @param function executed in the [LogicProgrammingScope] context to describe agent's [Belief].
     */
    override fun fact(function: LogicProgrammingScope.() -> Any): Fact =
        lp.fact { function() }.also { fact(it.head) }

    /**
     * Handler for the addition of a fact [Belief] into the agent's [BeliefBase].
     * @param atom the [String] representing the [Atom] the agent is going to believe.
     */
    fun fact(atom: String) = fact(atomOf(atom))

    /**
     * Handler for the addition of a rule [Belief] into the agent's [BeliefBase].
     * @param function executed in the [LogicProgrammingScope] context to describe agent's [Belief].
     */
    override fun rule(function: LogicProgrammingScope.() -> Any): Rule =
        lp.rule(function).also { rule(it) }

    /**
     * Handler for the addition of a rule [Belief] into the agent's [BeliefBase].
     * @param rule the [Rule] the agent is going to believe.
     */
    fun rule(rule: Rule) {
        val freshRule = rule.freshCopy()
        val belief: Belief = Belief.fromSelfSource(freshRule.head, freshRule.bodyItems)
        beliefs.add(belief)
    }

    override fun build(): BeliefBase = BeliefBase.of(beliefs)
}
