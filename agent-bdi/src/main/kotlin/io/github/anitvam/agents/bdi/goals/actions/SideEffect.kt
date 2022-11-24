package io.github.anitvam.agents.bdi.goals.actions

sealed interface SideEffect

sealed interface AgentChange : SideEffect

class AddBelief : AgentChange

class RemoveBelief : AgentChange

sealed interface EnvironmentChange : SideEffect

class SpawnAgent : EnvironmentChange

class SendMessage : EnvironmentChange
