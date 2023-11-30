package it.unibo.jakta.agents.distributed.broker.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SubscriptionManagerTest {

    private val topic = "test"
    private val id = 0

    @Test
    fun addPublisher() {
        val subscriptionManager = SubscriptionManager<Int>()
        subscriptionManager.addPublisher(id, topic)
        assertEquals(setOf(topic), subscriptionManager.availableTopics())
    }

    @Test
    fun removePublisher() {
        val subscriptionManager = SubscriptionManager<Int>()
        subscriptionManager.addPublisher(id, topic)
        subscriptionManager.removePublisher(id, topic)
        assertEquals(emptySet(), subscriptionManager.availableTopics())
    }

    @Test
    fun addSubscriber() {
        val subscriptionManager = SubscriptionManager<Int>()
        subscriptionManager.addSubscriber(id, topic)
        assertEquals(setOf(id), subscriptionManager.subscribers(topic))
    }

    @Test
    fun removeSubscriber() {
        val subscriptionManager = SubscriptionManager<Int>()
        subscriptionManager.addSubscriber(id, topic)
        subscriptionManager.removeSubscriber(id, topic)
        assertEquals(emptySet(), subscriptionManager.subscribers(topic))
    }
}
