package io.github.anitvam.agents.bdi.messages

sealed interface MessageType

object Achieve : MessageType

object Tell : MessageType
