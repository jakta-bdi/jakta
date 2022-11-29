package io.github.anitvam.agents.bdi.goals.actions

sealed interface SideEffect

sealed interface AgentChange : SideEffect

object AddBelief : AgentChange

object RemoveBelief : AgentChange

sealed interface EnvironmentChange : SideEffect

object SpawnAgent : EnvironmentChange

object SendMessage : EnvironmentChange
