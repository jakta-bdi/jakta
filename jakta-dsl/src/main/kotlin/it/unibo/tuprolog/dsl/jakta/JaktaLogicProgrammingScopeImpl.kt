package it.unibo.tuprolog.dsl.jakta

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.VariablesProvider
import it.unibo.tuprolog.dsl.Termificator
import it.unibo.tuprolog.theory.TheoryFactory
import it.unibo.tuprolog.unify.Unificator

internal class JaktaLogicProgrammingScopeImpl(
    override val scope: Scope,
    override val termificator: Termificator,
    override val variablesProvider: VariablesProvider,
    override val unificator: Unificator,
    override val theoryFactory: TheoryFactory,
) : JaktaLogicProgrammingScope,
    VariablesProvider by variablesProvider,
    Unificator by unificator,
    TheoryFactory by theoryFactory {
    init {
        require(scope === variablesProvider.scope && scope === termificator.scope) {
            "The provided Scope should be the same object for both Termificator and VariablesProvider"
        }
        require(unificator == theoryFactory.unificator) {
            "The provided Unificator should be the same object for both Unificator and TheoryFactory"
        }
    }

    override fun copy(scope: Scope): JaktaLogicProgrammingScope =
        JaktaLogicProgrammingScopeImpl(
            scope,
            termificator.copy(scope),
            variablesProvider.copy(scope),
            unificator,
            theoryFactory,
        )

    override fun copy(unificator: Unificator): JaktaLogicProgrammingScope =
        JaktaLogicProgrammingScopeImpl(
            scope,
            termificator,
            variablesProvider,
            unificator,
            theoryFactory.copy(unificator),
        )

    override fun newScope(): JaktaLogicProgrammingScope = copy(Scope.empty())
}
