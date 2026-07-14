package it.unibo.jakta.node

import it.unibo.jakta.event.EventQueue
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.event.UnlimitedChannelQueue
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Represents a local node connection that allows for communication and event handling across nodes on the same JVM.
 * This implementation uses an [UnlimitedChannelQueue] to manage system events.
 */
class LocalNodeConnection : NodeConnection {

    private val mutex = Mutex()

    private val subscribers: MutableSet<EventQueue<SystemEvent>> = mutableSetOf()

    override suspend fun subscribe(): NodeSubscription = mutex.withLock {
        val queue = UnlimitedChannelQueue<SystemEvent>().also {
            subscribers.add(it)
        }
        return object : NodeSubscription {
            override val queue: EventQueue<SystemEvent> = queue

            override suspend fun close() {
                mutex.withLock {
                    subscribers.remove(queue)
                }
            }
        }
    }

    override suspend fun send(event: SystemEvent) = mutex.withLock {
        subscribers.forEach { it.send(event) }
    }
}
