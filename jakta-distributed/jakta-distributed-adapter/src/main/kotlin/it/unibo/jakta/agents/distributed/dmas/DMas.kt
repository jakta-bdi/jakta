package it.unibo.jakta.agents.distributed.dmas

import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.distributed.network.Network
import it.unibo.jakta.agents.distributed.remoteagent.RemoteAgent

/**
 * A DistributedMas is a Mas with networking capabilities: it can send and receive messages from other DMas' over the
 * network.
 */
interface DMas : Mas {
    val remoteAgents: Iterable<RemoteAgent>
    val network: Network
}
