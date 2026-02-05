package it.unibo.jakta.event

interface EventBus<E> : EventInbox<E> , EventStream<E>
