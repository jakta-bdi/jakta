package it.unibo.jakta.agents.distributed.broker.model.impl

import io.ktor.websocket.DefaultWebSocketSession
import it.unibo.jakta.agents.distributed.broker.model.SubscriptionManager
import it.unibo.jakta.agents.distributed.broker.model.Topic
import java.util.*

class SubscriptionManagerImpl : SubscriptionManager {

    private val subscribers: MutableMap<Topic, MutableSet<DefaultWebSocketSession>> =
        Collections.synchronizedMap(LinkedHashMap())

    private val publishers: MutableMap<Topic, MutableSet<DefaultWebSocketSession>> =
        Collections.synchronizedMap(LinkedHashMap())

    override fun addPublisher(publisher: DefaultWebSocketSession, topic: Topic) {
        if (publishers[topic].isNullOrEmpty()) publishers[topic] = Collections.synchronizedSet(LinkedHashSet())
        publishers[topic]?.add(publisher)
    }

    override fun removePublisher(publisher: DefaultWebSocketSession, topic: Topic) {
        publishers[topic]?.remove(publisher)
    }

    override fun availableTopics(): Set<Topic> {
        return publishers.keys
    }

    override fun addSubscriber(subscriber: DefaultWebSocketSession, topic: Topic) {
        if (subscribers[topic].isNullOrEmpty()) subscribers[topic] = Collections.synchronizedSet(LinkedHashSet())
        subscribers[topic]?.add(subscriber)
    }

    override fun removeSubscriber(subscriber: DefaultWebSocketSession, topic: Topic) {
        subscribers[topic]?.remove(subscriber)
    }

    override fun subscribers(topic: Topic): Set<DefaultWebSocketSession> {
        return subscribers[topic]?.toSet() ?: emptySet()
    }
}
