package it.unibo.jakta.agents.distributed.broker.model.impl

import it.unibo.jakta.agents.distributed.broker.model.ReservedTopics
import it.unibo.jakta.agents.distributed.broker.model.SubscriptionManager
import it.unibo.jakta.agents.distributed.broker.model.Topic
import java.util.*

internal class SubscriptionManagerImpl<T> : SubscriptionManager<T> {

    private val subscribers: MutableMap<Topic, MutableSet<T>> =
        Collections.synchronizedMap(LinkedHashMap())

    private val publishers: MutableMap<Topic, MutableSet<T>> =
        Collections.synchronizedMap(LinkedHashMap())

    override fun addPublisher(publisher: T, topic: Topic) {
        if ((publishers[topic]?.size ?: 0) > 0 && !ReservedTopics.topics.contains(topic)) {
            throw PublisherAlreadyPresentException()
        } else {
            publishers[topic] = Collections.synchronizedSet(LinkedHashSet())
            publishers[topic]?.add(publisher)
        }
    }

    override fun removePublisher(publisher: T, topic: Topic) {
        publishers[topic]?.remove(publisher)
        if (publishers[topic] == emptySet<T>()) publishers.remove(topic)
    }

    override fun availableTopics(): Set<Topic> {
        return publishers.keys
    }

    override fun addSubscriber(subscriber: T, topic: Topic) {
        if (subscribers[topic].isNullOrEmpty()) subscribers[topic] = Collections.synchronizedSet(LinkedHashSet())
        subscribers[topic]?.add(subscriber)
    }

    override fun removeSubscriber(subscriber: T, topic: Topic) {
        subscribers[topic]?.remove(subscriber)
        if (subscribers[topic] == emptySet<T>()) subscribers.remove(topic)
    }

    override fun subscribers(topic: Topic): Set<T> {
        return subscribers[topic]?.toSet() ?: emptySet()
    }
}
