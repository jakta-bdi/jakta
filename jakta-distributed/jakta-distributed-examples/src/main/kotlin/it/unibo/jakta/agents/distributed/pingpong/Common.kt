package it.unibo.jakta.agents.distributed.pingpong

import it.unibo.jakta.agents.bdi.Jakta
import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.jakta.agents.bdi.goals.Act
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.jakta.agents.bdi.plans.Plan

val sendAction = object : AbstractExternalAction("send", 3) {
    override fun action(request: ExternalRequest) {
        val receiver = request.arguments[0].castToAtom()
        val type = request.arguments[1].castToAtom()
        val message = request.arguments[2].castToStruct()
        when (type.value) {
            "tell" -> sendMessage(receiver.value, Message(request.sender, Tell, message))
            "achieve" -> sendMessage(
                receiver.value,
                Message(request.sender, it.unibo.jakta.agents.bdi.messages.Achieve, message),
            )
        }
    }
}

val sendPlan = Plan.ofAchievementGoalInvocation(
    value = Jakta.parseStruct("sendMessageTo(Message, Receiver)"),
    goals = listOf(
        ActInternally.of(Jakta.parseStruct("print(\"Sending \", Message)")),
        Act.of(Jakta.parseStruct("send(Receiver, tell, Message)")),
    ),
)
