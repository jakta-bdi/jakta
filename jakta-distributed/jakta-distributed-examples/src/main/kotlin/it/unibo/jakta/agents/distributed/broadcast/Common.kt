package it.unibo.jakta.agents.distributed.broadcast

import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.messages.Achieve
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell

object Common {
    private val broadcastAction = object : AbstractExternalAction("broadcast", 2) {
        override fun action(request: ExternalRequest) {
            val type = request.arguments[0].castToAtom()
            val message = request.arguments[1].castToStruct()
            when (type.value) {
                "tell" -> broadcastMessage(Message(request.sender, Tell, message))
                "achieve" -> broadcastMessage(
                    Message(request.sender, Achieve, message),
                )
            }
        }
    }

    val env = Environment.of(
        externalActions = mapOf(
            broadcastAction.signature.name to broadcastAction,
        ),
    )
}
