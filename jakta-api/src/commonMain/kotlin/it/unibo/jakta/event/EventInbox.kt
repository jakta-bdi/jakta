package it.unibo.jakta.event

interface EventInbox<in E: Event> {
    fun send(event: E)
}


