package io.github.anitvam.agents.bdi.goals.actions.effects

sealed interface SideEffect

sealed interface EnvironmentChange : SideEffect

object SpawnAgent : EnvironmentChange

object SendMessage : EnvironmentChange
