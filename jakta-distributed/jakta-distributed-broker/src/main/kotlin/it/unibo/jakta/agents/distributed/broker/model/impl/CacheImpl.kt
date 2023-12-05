package it.unibo.jakta.agents.distributed.broker.model.impl

import it.unibo.jakta.agents.distributed.broker.model.Cache
import it.unibo.jakta.agents.distributed.broker.model.Topic
import java.util.*

internal class CacheImpl<T> : Cache<T> {
    private val cache: MutableMap<Topic, T> = Collections.synchronizedMap(LinkedHashMap())

    override fun register(data: T, topic: Topic) {
        cache[topic] = data
    }

    override fun free(topic: Topic) {
        cache.remove(topic)
    }

    override fun read(topic: Topic): T? {
        return cache[topic]
    }
}
