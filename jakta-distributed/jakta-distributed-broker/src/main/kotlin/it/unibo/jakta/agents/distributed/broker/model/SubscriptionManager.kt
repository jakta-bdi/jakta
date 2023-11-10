package it.unibo.jakta.agents.distributed.broker.model

import it.unibo.jakta.agents.distributed.broker.model.impl.SubscriptionManagerImpl

typealias Topic = MasID

interface SubscriptionManager {

    fun generateUniqueID(): MasID

    fun addTopic(topic: Topic)

    fun removeTopic(topic: Topic)

    fun availableTopics(): Set<Topic>

    fun addSubscriber(id: MasID, topic: Topic)

    fun removeSubscriber(id: MasID, topic: Topic)

    fun getSubscribers(topic: Topic): Set<MasID>

    companion object {
        operator fun invoke(): SubscriptionManager = SubscriptionManagerImpl()
    }
}
