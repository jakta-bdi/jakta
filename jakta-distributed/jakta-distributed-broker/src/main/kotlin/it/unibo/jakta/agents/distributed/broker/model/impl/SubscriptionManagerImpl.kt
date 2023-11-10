package it.unibo.jakta.agents.distributed.broker.model.impl

import it.unibo.jakta.agents.distributed.broker.model.IDNotPresentException
import it.unibo.jakta.agents.distributed.broker.model.InvalidTopicException
import it.unibo.jakta.agents.distributed.broker.model.MasID
import it.unibo.jakta.agents.distributed.broker.model.SubscriberAlreadyPresentException
import it.unibo.jakta.agents.distributed.broker.model.SubscriberNotPresentException
import it.unibo.jakta.agents.distributed.broker.model.SubscriptionManager
import it.unibo.jakta.agents.distributed.broker.model.Topic
import it.unibo.jakta.agents.distributed.broker.model.TopicAlreadyPresentException
import it.unibo.jakta.agents.distributed.broker.model.TopicNotPresentException
import java.util.*

class SubscriptionManagerImpl : SubscriptionManager {

    private val topicsSubscribers: MutableMap<Topic, MutableSet<MasID>> =
        Collections.synchronizedMap(LinkedHashMap())
    private val assignedIDs = Collections.synchronizedSet<MasID>(LinkedHashSet())

    override fun generateUniqueID(): MasID {
        val newID = MasID()
        assignedIDs.add(newID)
        return newID
    }

    override fun addTopic(topic: Topic) {
        when {
            !assignedIDs.contains(topic) -> throw InvalidTopicException()
            topicsSubscribers.containsKey(topic) -> throw TopicAlreadyPresentException()
            else -> topicsSubscribers[topic] = Collections.synchronizedSet(LinkedHashSet())
        }
    }

    override fun removeTopic(topic: Topic) {
        when {
            !assignedIDs.contains(topic) -> throw InvalidTopicException()
            !topicsSubscribers.containsKey(topic) -> throw TopicNotPresentException()
            else -> topicsSubscribers.remove(topic)
        }
    }

    override fun availableTopics(): Set<Topic> {
        return topicsSubscribers.keys
    }

    override fun addSubscriber(id: MasID, topic: Topic) {
        when {
            !assignedIDs.contains(id) -> throw IDNotPresentException()
            !topicsSubscribers.containsKey(topic) -> throw TopicNotPresentException()
            topicsSubscribers[topic]?.contains(id) == true -> throw SubscriberAlreadyPresentException()
            else -> topicsSubscribers[topic]?.add(id)
        }
    }

    override fun removeSubscriber(id: MasID, topic: Topic) {
        when {
            !assignedIDs.contains(id) -> throw IDNotPresentException()
            !topicsSubscribers.containsKey(topic) -> throw TopicNotPresentException()
            topicsSubscribers[topic]?.contains(id) == false -> throw SubscriberNotPresentException()
            else -> topicsSubscribers[topic]?.remove(id)
        }
    }

    override fun getSubscribers(topic: Topic): Set<MasID> {
        when {
            !topicsSubscribers.containsKey(topic) -> throw TopicNotPresentException()
            else -> return topicsSubscribers[topic]?.toSet() ?: emptySet()
        }
    }
}
