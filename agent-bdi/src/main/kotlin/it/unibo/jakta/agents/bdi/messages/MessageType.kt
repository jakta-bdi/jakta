package it.unibo.jakta.agents.bdi.messages

sealed interface MessageType

object Achieve : MessageType

object Tell : MessageType
