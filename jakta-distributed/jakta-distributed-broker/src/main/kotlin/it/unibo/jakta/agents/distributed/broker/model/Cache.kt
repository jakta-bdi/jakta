package it.unibo.jakta.agents.distributed.broker.model

import it.unibo.jakta.agents.distributed.broker.model.impl.CacheImpl

interface Cache<T> {
    fun register(data: T, topic: Topic)

    fun free(topic: Topic)

    fun read(topic: Topic): T?

    companion object {
        operator fun <T> invoke(): Cache<T> = CacheImpl()
    }
}
