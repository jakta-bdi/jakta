package it.unibo.alchemist.jakta

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist.Companion.BROKER_MOLECULE
import it.unibo.alchemist.model.Position
import it.unibo.jakta.distributed.MessageBroker
import it.unibo.jakta.messages.Message
import it.unibo.alchemist.model.Environment as AlchemistEnvironment

class JaktaForAlchemistMessageBroker<P : Position<P>>(
    private val alchemistEnvironment: AlchemistEnvironment<Any?, P>,
    // For incoming messages
    private val messageBoxes: MutableMap<String, MutableList<Message>> = mutableMapOf(),
) : MessageBroker {
    override fun putInMessageBox(receiver: String, message: Message) {
        if (messageBoxes.contains(receiver)) {
            messageBoxes[receiver]!!.add(message)
        } else {
            messageBoxes += receiver to mutableListOf(message)
        }
    }

    override fun send(receiver: String, host: String, message: Message) {
        val mbox =
            alchemistEnvironment.nodes
                .find { it.id.toString() == host }
                ?.getConcentration(BROKER_MOLECULE)
        if (mbox != null) (mbox as JaktaForAlchemistMessageBroker<*>).putInMessageBox(receiver, message)
    }

    fun send(receiverWithHost: String, message: Message) {
        val receiverAndHost = receiverWithHost.split("@")
        send(receiverAndHost.first(), receiverAndHost[1], message)
    }

    override fun broadcast(message: Message) {
        messageBoxes.forEach {
            if (it.key != message.from) it.value += message
        }
    }

    override fun pop(receiver: String, host: String): Message? = messageBoxes[receiver]?.removeFirst()

    override fun nextMessage(receiver: String, host: String): Message? = messageBoxes[receiver]?.getOrNull(0)
}
