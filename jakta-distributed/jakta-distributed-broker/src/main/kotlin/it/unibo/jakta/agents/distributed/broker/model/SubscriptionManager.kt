package it.unibo.jakta.agents.distributed.broker.model

import io.ktor.websocket.DefaultWebSocketSession
import it.unibo.jakta.agents.distributed.broker.model.impl.SubscriptionManagerImpl

typealias Topic = String

interface SubscriptionManager {

    fun addPublisher(publisher: DefaultWebSocketSession, topic: Topic)

    fun removePublisher(publisher: DefaultWebSocketSession, topic: Topic)

    fun availableTopics(): Set<Topic>

    fun addSubscriber(subscriber: DefaultWebSocketSession, topic: Topic)

    fun removeSubscriber(subscriber: DefaultWebSocketSession, topic: Topic)

    fun subscribers(topic: Topic): Set<DefaultWebSocketSession>

    companion object {
        operator fun invoke(): SubscriptionManager = SubscriptionManagerImpl()
    }
}
