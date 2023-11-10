package it.unibo.jakta.agents.distributed.broker.model.impl

import it.unibo.jakta.agents.distributed.broker.model.InvalidIDException
import it.unibo.jakta.agents.distributed.broker.model.MasID
import it.unibo.jakta.agents.distributed.broker.model.PublisherAlreadyPresentException
import it.unibo.jakta.agents.distributed.broker.model.PublisherNotPresentException
import it.unibo.jakta.agents.distributed.broker.model.SubscriberAlreadyPresentException
import it.unibo.jakta.agents.distributed.broker.model.SubscriberNotPresentException
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class SubscriptionManagerImplTest {

    @Test
    fun generateUniqueID() {
        var subscriptionManager = SubscriptionManagerImpl()
        assertNotEquals(subscriptionManager.generateUniqueID(), subscriptionManager.generateUniqueID())
    }

    @Test
    fun addPublisher() {
        var subscriptionManager = SubscriptionManagerImpl()
        val myId = subscriptionManager.generateUniqueID()
        subscriptionManager.addPublisher(myId)
        assertThrows<PublisherAlreadyPresentException> {
            subscriptionManager.addPublisher(myId)
        }
        assertThrows<InvalidIDException> {
            subscriptionManager.addPublisher(MasID("test"))
        }
    }

    @Test
    fun removePublisher() {
        var subscriptionManager = SubscriptionManagerImpl()
        val myId = subscriptionManager.generateUniqueID()
        subscriptionManager.addPublisher(myId)
        subscriptionManager.removePublisher(myId)
        assertThrows<PublisherNotPresentException> {
            subscriptionManager.removePublisher(myId)
        }
    }

    @Test
    fun availablePublishers() {
        var subscriptionManager = SubscriptionManagerImpl()
        val myId = subscriptionManager.generateUniqueID()
        assertEquals(emptySet(), subscriptionManager.availablePublishers())
        subscriptionManager.addPublisher(myId)
        assertEquals(setOf(myId), subscriptionManager.availablePublishers())
    }

    @Test
    fun addSubscriber() {
        var subscriptionManager = SubscriptionManagerImpl()
        val publisherId = subscriptionManager.generateUniqueID()
        val subscriberId = subscriptionManager.generateUniqueID()
        assertThrows<InvalidIDException> {
            subscriptionManager.addSubscriber(MasID("test"), publisherId)
        }
        assertThrows<InvalidIDException> {
            subscriptionManager.addSubscriber(subscriberId, MasID("test"))
        }
        subscriptionManager.addPublisher(publisherId)
        subscriptionManager.addSubscriber(subscriberId, publisherId)
        assertThrows<SubscriberAlreadyPresentException> {
            subscriptionManager.addSubscriber(subscriberId, publisherId)
        }
    }

    @Test
    fun removeSubscriber() {
        var subscriptionManager = SubscriptionManagerImpl()
        val publisherId = subscriptionManager.generateUniqueID()
        val subscriberId = subscriptionManager.generateUniqueID()
        assertThrows<InvalidIDException> {
            subscriptionManager.removeSubscriber(MasID("test"), publisherId)
        }
        assertThrows<InvalidIDException> {
            subscriptionManager.removeSubscriber(subscriberId, MasID("test"))
        }
        subscriptionManager.addPublisher(publisherId)
        subscriptionManager.addSubscriber(subscriberId, publisherId)
        subscriptionManager.removeSubscriber(subscriberId, publisherId)
        assertThrows<SubscriberNotPresentException> {
            subscriptionManager.removeSubscriber(subscriberId, publisherId)
        }
    }

    @Test
    fun subscribers() {
        var subscriptionManager = SubscriptionManagerImpl()
        val publisherId = subscriptionManager.generateUniqueID()
        val subscriberId = subscriptionManager.generateUniqueID()
        assertThrows<PublisherNotPresentException> {
            subscriptionManager.subscribers(publisherId)
        }
        subscriptionManager.addPublisher(publisherId)
        subscriptionManager.addSubscriber(subscriberId, publisherId)
        assertEquals(setOf(subscriberId), subscriptionManager.subscribers(publisherId))
    }
}
