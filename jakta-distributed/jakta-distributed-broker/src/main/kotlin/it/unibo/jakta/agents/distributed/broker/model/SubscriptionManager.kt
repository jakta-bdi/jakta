package it.unibo.jakta.agents.distributed.broker.model

import it.unibo.jakta.agents.distributed.broker.model.impl.SubscriptionManagerImpl

interface SubscriptionManager {

    fun generateUniqueID(): MasID

    fun addPublisher(publisher: MasID)

    fun removePublisher(publisher: MasID)

    fun availablePublishers(): Set<MasID>

    fun addSubscriber(subscriber: MasID, publisher: MasID)

    fun removeSubscriber(subscriber: MasID, publisher: MasID)

    fun subscribers(publisher: MasID): Set<MasID>

    companion object {
        operator fun invoke(): SubscriptionManager = SubscriptionManagerImpl()
    }
}
