package io.github.anitvam.agents.bdi.actions.effects

sealed interface SideEffect

sealed interface EnvironmentChange : SideEffect

object SpawnAgent : EnvironmentChange

object SendMessage : EnvironmentChange
