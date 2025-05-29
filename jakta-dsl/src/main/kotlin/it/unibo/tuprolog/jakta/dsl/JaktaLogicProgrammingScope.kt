package it.unibo.tuprolog.jakta.dsl

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

interface JaktaLogicProgrammingScope :
    MinimalLogicProgrammingScope<JaktaLogicProgrammingScope>,
    LogicProgrammingScopeWithSubstitutions<JaktaLogicProgrammingScope>,
    LogicProgrammingScopeWithPrologStandardLibrary<JaktaLogicProgrammingScope>,
    LogicProgrammingScopeWithOperators<JaktaLogicProgrammingScope>,
    LogicProgrammingScopeWithVariables<JaktaLogicProgrammingScope>,
    LogicProgrammingScopeWithUnification<JaktaLogicProgrammingScope>,
    LogicProgrammingScopeWithTheories<JaktaLogicProgrammingScope>,
    LogicProgrammingScopeWithJaktaExtensions<JaktaLogicProgrammingScope> {
    companion object {
        internal val defaultUnificator = Unificator.default

        fun empty(): JaktaLogicProgrammingScope = of()

        fun of(
            scope: Scope = Scope.empty(),
            termificator: Termificator = Termificator.default(scope),
            variablesProvider: VariablesProvider = VariablesProvider.of(scope),
            unificator: Unificator = defaultUnificator,
            theoryFactory: TheoryFactory = IndexedTheoryFactory(unificator),
        ): JaktaLogicProgrammingScope = JaktaLogicProgrammingScopeImpl(
            scope,
            if (termificator.scope === scope) termificator else termificator.copy(scope),
            if (variablesProvider.scope === scope) variablesProvider else variablesProvider.copy(scope),
            unificator,
            if (theoryFactory.unificator === unificator) theoryFactory else theoryFactory.copy(unificator),
        )

        fun of(
            unificator: Unificator,
            theoryFactory: TheoryFactory = IndexedTheoryFactory(unificator),
            scope: Scope = Scope.empty(),
            termificator: Termificator = Termificator.default(scope),
            variablesProvider: VariablesProvider = VariablesProvider.of(scope),
        ): JaktaLogicProgrammingScope = of(scope, termificator, variablesProvider, unificator, theoryFactory)
    }
}
