package it.unibo.jakta.agents.distributed.network.impl

import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.actions.effects.SendMessage
import it.unibo.jakta.agents.distributed.RemoteService
import it.unibo.jakta.agents.distributed.client.Client
import it.unibo.jakta.agents.distributed.network.Network

class WebsocketNetwork(host: String, port: Int) : Network {
    private val client: Client = Client.webSocketClient(host, port)
    override fun subscribe(remoteService: RemoteService) {
        client.subscribe(remoteService.serviceName)
    }

    override fun send(event: SendMessage) {
        client.publish(event.recipient, event)
    }
    override fun getMessagesAsEnvironmentChanges(): Iterable<EnvironmentChange> =
        client.incomingData.values.asIterable().map { it as EnvironmentChange }
}
