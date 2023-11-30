package it.unibo.jakta.agents.distributed.broker.model

import it.unibo.jakta.agents.distributed.broker.model.impl.SubscriptionManagerImpl

typealias Topic = String

interface SubscriptionManager<T> {

    fun addPublisher(publisher: T, topic: Topic)

    fun removePublisher(publisher: T, topic: Topic)

    fun availableTopics(): Set<Topic>

    fun addSubscriber(subscriber: T, topic: Topic)

    fun removeSubscriber(subscriber: T, topic: Topic)

    fun subscribers(topic: Topic): Set<T>

    companion object {
        operator fun <T> invoke(): SubscriptionManager<T> = SubscriptionManagerImpl()
    }
}
