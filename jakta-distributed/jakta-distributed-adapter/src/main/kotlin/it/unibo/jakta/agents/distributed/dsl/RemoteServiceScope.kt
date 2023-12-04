package it.unibo.jakta.agents.distributed.dsl

import it.unibo.jakta.agents.bdi.dsl.Builder
import it.unibo.jakta.agents.distributed.RemoteService

class RemoteServiceScope : Builder<RemoteService> {
    private var name: String = ""

    fun name(name: String): RemoteServiceScope {
        this.name = name
        return this
    }

    override fun build(): RemoteService = RemoteService(name)
}
