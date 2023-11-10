package it.unibo.jakta.agents.distributed.broker.model.impl

import it.unibo.jakta.agents.distributed.broker.model.InvalidIDException
import it.unibo.jakta.agents.distributed.broker.model.MasID
import it.unibo.jakta.agents.distributed.broker.model.PublisherAlreadyPresentException
import it.unibo.jakta.agents.distributed.broker.model.PublisherNotPresentException
import it.unibo.jakta.agents.distributed.broker.model.SubscriberAlreadyPresentException
import it.unibo.jakta.agents.distributed.broker.model.SubscriberNotPresentException
import it.unibo.jakta.agents.distributed.broker.model.SubscriptionManager
import java.util.*

class SubscriptionManagerImpl : SubscriptionManager {

    private val subscriptions: MutableMap<MasID, MutableSet<MasID>> =
        Collections.synchronizedMap(LinkedHashMap())
    private val assignedIDs = Collections.synchronizedSet<MasID>(LinkedHashSet())

    override fun generateUniqueID(): MasID {
        val newID = MasID()
        assignedIDs.add(newID)
        return newID
    }

    override fun addPublisher(publisher: MasID) {
        when {
            !assignedIDs.contains(publisher) -> throw InvalidIDException()
            subscriptions.containsKey(publisher) -> throw PublisherAlreadyPresentException()
            else -> subscriptions[publisher] = Collections.synchronizedSet(LinkedHashSet())
        }
    }

    override fun removePublisher(publisher: MasID) {
        when {
            !assignedIDs.contains(publisher) -> throw InvalidIDException()
            !subscriptions.containsKey(publisher) -> throw PublisherNotPresentException()
            else -> subscriptions.remove(publisher)
        }
    }

    override fun availablePublishers(): Set<MasID> {
        return subscriptions.keys
    }

    override fun addSubscriber(subscriber: MasID, publisher: MasID) {
        when {
            !assignedIDs.contains(subscriber) -> throw InvalidIDException()
            !assignedIDs.contains(publisher) -> throw InvalidIDException()
            !subscriptions.containsKey(publisher) -> throw PublisherNotPresentException()
            subscriptions[publisher]?.contains(subscriber) == true -> throw SubscriberAlreadyPresentException()
            else -> subscriptions[publisher]?.add(subscriber)
        }
    }

    override fun removeSubscriber(subscriber: MasID, publisher: MasID) {
        when {
            !assignedIDs.contains(subscriber) -> throw InvalidIDException()
            !assignedIDs.contains(publisher) -> throw InvalidIDException()
            !subscriptions.containsKey(publisher) -> throw PublisherNotPresentException()
            subscriptions[publisher]?.contains(subscriber) == false -> throw SubscriberNotPresentException()
            else -> subscriptions[publisher]?.remove(subscriber)
        }
    }

    override fun subscribers(publisher: MasID): Set<MasID> {
        when {
            !assignedIDs.contains(publisher) -> throw InvalidIDException()
            !subscriptions.containsKey(publisher) -> throw PublisherNotPresentException()
            else -> return subscriptions[publisher]?.toSet() ?: emptySet()
        }
    }
}
