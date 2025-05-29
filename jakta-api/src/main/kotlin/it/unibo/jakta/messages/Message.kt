package it.unibo.jakta.messages

data class Message<Payload : Any>(val from: String, val type: MessageType, val value: Payload)
