package it.unibo.jakta.context

sealed interface ContextUpdate

data object Addition : ContextUpdate

data object Removal : ContextUpdate
