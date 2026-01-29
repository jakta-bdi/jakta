package it.unibo.jakta.event

interface EventInbox<in E> {
    fun send(event: E)
}


