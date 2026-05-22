package it.unibo.jakta.dsl

import it.unibo.jakta.belief.PrologBelief
import it.unibo.jakta.dsl.agent.GoalBuilder
import it.unibo.jakta.dsl.plan.PlanLibraryBuilder
import it.unibo.jakta.goal.PrologGoal
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

    companion object {
        inline fun <reified R> GoalBuilder<PrologGoal>.logicProgram(block: JaktaLogicProgrammingScope.() -> R): R =
            with(JaktaLogicProgrammingScope(), block)

        inline fun <reified R> PlanLibraryBuilder<PrologBelief, PrologGoal, *>.logicProgram(
            block: JaktaLogicProgrammingScope.() -> R,
        ): R = with(JaktaLogicProgrammingScope(), block)
    }
}
