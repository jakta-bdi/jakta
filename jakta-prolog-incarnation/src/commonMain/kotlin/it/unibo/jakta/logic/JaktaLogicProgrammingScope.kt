package it.unibo.jakta.logic

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.agent.GoalBuilder
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.plan.PlanLibraryBuilder
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.VariablesProvider
import it.unibo.tuprolog.dsl.LogicProgrammingScopeWithOperators
import it.unibo.tuprolog.dsl.LogicProgrammingScopeWithPrologStandardLibrary
import it.unibo.tuprolog.dsl.LogicProgrammingScopeWithSubstitutions
import it.unibo.tuprolog.dsl.LogicProgrammingScopeWithVariables
import it.unibo.tuprolog.dsl.MinimalLogicProgrammingScope
import it.unibo.tuprolog.dsl.Termificator
import it.unibo.tuprolog.dsl.theory.LogicProgrammingScopeWithTheories
import it.unibo.tuprolog.dsl.unify.LogicProgrammingScopeWithUnification
import it.unibo.tuprolog.theory.IndexedTheoryFactory
import it.unibo.tuprolog.theory.TheoryFactory
import it.unibo.tuprolog.unify.Unificator

/**
 * A logic programming scope for Jakta, providing a context for defining and manipulating Prolog beliefs and goals
 * through the 2p-kt DSL.
 * This class implements various interfaces to support logic programming features such as substitutions, unification,
 * and the use of Prolog standard library functions.
 *
 * @property scope The current scope for logic programming, defaulting to an empty scope.
 * @property termificator The termificator used for creating terms within the scope.
 * @property variablesProvider The provider for variables within the scope.
 * @property unificator The unificator used for unification operations.
 * @property theoryFactory The factory for creating theories within the scope.
 */
open class JaktaLogicProgrammingScope(
    override val scope: Scope = Scope.empty(),
    override val termificator: Termificator = Termificator.default(scope),
    override val variablesProvider: VariablesProvider = VariablesProvider.of(scope),
    override val unificator: Unificator = Unificator.default,
    override val theoryFactory: TheoryFactory = IndexedTheoryFactory(unificator),
) : MinimalLogicProgrammingScope<JaktaLogicProgrammingScope>,
    LogicProgrammingScopeWithSubstitutions<JaktaLogicProgrammingScope>,
    LogicProgrammingScopeWithPrologStandardLibrary<JaktaLogicProgrammingScope>,
    LogicProgrammingScopeWithOperators<JaktaLogicProgrammingScope>,
    LogicProgrammingScopeWithVariables<JaktaLogicProgrammingScope>,
    LogicProgrammingScopeWithUnification<JaktaLogicProgrammingScope>,
    LogicProgrammingScopeWithTheories<JaktaLogicProgrammingScope>,
    VariablesProvider by variablesProvider,
    Unificator by unificator,
    TheoryFactory by theoryFactory {
    override fun copy(scope: Scope): JaktaLogicProgrammingScope = JaktaLogicProgrammingScope(
        scope,
        termificator.copy(scope),
        variablesProvider.copy(scope),
        unificator,
        theoryFactory,
    )

    override fun copy(unificator: Unificator): JaktaLogicProgrammingScope = JaktaLogicProgrammingScope(
        scope,
        termificator,
        variablesProvider,
        unificator,
        theoryFactory.copy(unificator),
    )

    override fun newScope(): JaktaLogicProgrammingScope = copy(Scope.empty())

    init {
        require(scope === variablesProvider.scope && scope === termificator.scope) {
            "The provided Scope should be the same object for both Termificator and VariablesProvider"
        }
        require(unificator == theoryFactory.unificator) {
            "The provided Unificator should be the same object for both Unificator and TheoryFactory"
        }
    }

    /**
     * Companion object for the [JaktaLogicProgrammingScope] class, providing utility functions for creating
     * Prolog plans within the Jakta DSL.
     */
    companion object {
        /**
         * Creates a Prolog plan within the context of a [GoalBuilder] for [PrologGoal].
         * @param block A lambda function that defines the Prolog plan to be created.
         * @return The result of the scope block.
         */
        @JaktaDSL
        inline fun PlanLibraryBuilder<PrologBelief, PrologGoal>.prologPlan(
            block: JaktaLogicProgrammingScope.() -> Unit,
        ): Unit = with(JaktaLogicProgrammingScope(), block)
    }
}
