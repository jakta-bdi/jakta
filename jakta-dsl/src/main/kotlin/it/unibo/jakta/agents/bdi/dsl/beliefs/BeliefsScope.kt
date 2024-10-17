package it.unibo.jakta.agents.bdi.dsl.beliefs

import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.dsl.Builder
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.jakta.JaktaLogicProgrammingScope

/**
 * Builder for Jakta Agents's [BeliefBase].
 */
class BeliefsScope(
    private val lp: JaktaLogicProgrammingScope = JaktaLogicProgrammingScope.empty(),
) : Builder<BeliefBase>, JaktaLogicProgrammingScope by lp {

    private val beliefs = mutableListOf<Belief>()

    /**
     * Handler for the addition of a fact [Belief] into the agent's [BeliefBase].
     * @param struct the [Struct] that represents the [Belief].
     */
    fun fact(struct: Struct) =
        beliefs.add(Belief.wrap(struct.freshCopy(), wrappingTag = Belief.SOURCE_SELF))

    /**
     * Handler for the addition of a fact [Belief] into the agent's [BeliefBase].
     * @param function executed in the [JaktaLogicProgrammingScope] context to describe agent's [Belief].
     */
    override fun fact(function: JaktaLogicProgrammingScope.() -> Any): Fact =
        lp.fact { function() }.also { fact(it.head) }

    /**
     * Handler for the addition of a fact [Belief] into the agent's [BeliefBase].
     * @param atom the [String] representing the [Atom] the agent is going to believe.
     */
    fun fact(atom: String) = fact(atomOf(atom))

    /**
     * Handler for the addition of a rule [Belief] into the agent's [BeliefBase].
     * @param function executed in the [JaktaLogicProgrammingScope] context to describe agent's [Belief].
     */
    override fun rule(function: JaktaLogicProgrammingScope.() -> Any): Rule =
        lp.rule(function).also { rule(it) }

    /**
     * Handler for the addition of a rule [Belief] into the agent's [BeliefBase].
     * @param rule the [Rule] the agent is going to believe.
     */
    fun rule(rule: Rule) {
        val freshRule = rule.freshCopy()
        val belief: Belief = Belief.wrap(freshRule.head, freshRule.bodyItems, wrappingTag = Belief.SOURCE_SELF)
        beliefs.add(belief)
    }

    override fun build(): BeliefBase = BeliefBase.of(beliefs)
}
