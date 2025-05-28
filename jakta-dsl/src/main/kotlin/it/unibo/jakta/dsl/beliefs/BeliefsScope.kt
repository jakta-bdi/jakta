package it.unibo.jakta.dsl.beliefs

import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.dsl.Builder
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.jakta.dsl.JaktaLogicProgrammingScope

/**
 * Builder for Jakta Agents's [ASBeliefBase].
 */
class BeliefsScope(
    private val lp: JaktaLogicProgrammingScope = JaktaLogicProgrammingScope.empty(),
) : Builder<ASBeliefBase>, JaktaLogicProgrammingScope by lp {

    private val beliefs = mutableListOf<Belief>()

    /**
     * Handler for the addition of a fact [Belief] into the agent's [ASBeliefBase].
     * @param struct the [Struct] that represents the [Belief].
     */
    fun fact(struct: Struct) =
        beliefs.add(Belief.wrap(struct.freshCopy(), wrappingTag = Belief.SOURCE_SELF))

    /**
     * Handler for the addition of a fact [Belief] into the agent's [ASBeliefBase].
     * @param function executed in the [JaktaLogicProgrammingScope] context to describe agent's [Belief].
     */
    override fun fact(function: JaktaLogicProgrammingScope.() -> Any): Fact =
        lp.fact { function() }.also { fact(it.head) }

    /**
     * Handler for the addition of a fact [Belief] into the agent's [ASBeliefBase].
     * @param atom the [String] representing the [Atom] the agent is going to believe.
     */
    fun fact(atom: String) = fact(atomOf(atom))

    /**
     * Handler for the addition of a rule [Belief] into the agent's [ASBeliefBase].
     * @param function executed in the [JaktaLogicProgrammingScope] context to describe agent's [Belief].
     */
    override fun rule(function: JaktaLogicProgrammingScope.() -> Any): Rule =
        lp.rule(function).also { rule(it) }

    /**
     * Handler for the addition of a rule [Belief] into the agent's [ASBeliefBase].
     * @param rule the [Rule] the agent is going to believe.
     */
    fun rule(rule: Rule) {
        val freshRule = rule.freshCopy()
        val belief: Belief = Belief.wrap(freshRule.head, freshRule.bodyItems, wrappingTag = Belief.SOURCE_SELF)
        beliefs.add(belief)
    }

    override fun build(): ASBeliefBase = ASBeliefBase.of(beliefs)
}
