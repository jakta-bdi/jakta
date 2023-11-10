package it.unibo.jakta.agents.distributed.broker.model

import it.unibo.jakta.agents.distributed.broker.model.impl.SubscriptionManagerImpl

typealias Topic = UniqueID

interface SubscriptionManager {

    fun generateUniqueID(): UniqueID

    fun addTopic(topic: Topic)

    fun removeTopic(topic: Topic)

    fun availableTopics(): Set<Topic>

    fun addSubscriber(id: UniqueID, topic: Topic)

    fun removeSubscriber(id: UniqueID, topic: Topic)

    fun getSubscribers(topic: Topic): Set<UniqueID>

    companion object {
        operator fun invoke(): SubscriptionManager = SubscriptionManagerImpl()
    }
}
