package it.unibo.jakta.agents.distributed.broadcast

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.Jakta
import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary
import it.unibo.jakta.agents.distributed.dmas.DMas

fun main() {
    val broadcastAction = object : AbstractExternalAction("broadcast", 2) {
        override fun action(request: ExternalRequest) {
            val type = request.arguments[0].castToAtom()
            val message = request.arguments[1].castToStruct()
            when (type.value) {
                "tell" -> broadcastMessage(Message(request.sender, Tell, message))
                "achieve" -> broadcastMessage(
                    Message(request.sender, it.unibo.jakta.agents.bdi.messages.Achieve, message),
                )
            }
        }
    }

    val bob = Agent.of(
        name = "bob",
        planLibrary = PlanLibrary.of(
            Plan.ofBeliefBaseAddition(
                belief = Belief.from(Jakta.parseStruct("greetings(source(Sender))")),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("print(\"Received message from: \", Sender)")),
                ),
            ),
        ),
    )

    val env = Environment.of(
        externalActions = mapOf(
            broadcastAction.signature.name to broadcastAction,
        ),
    )

    DMas.withEmbeddedBroker(ExecutionStrategy.oneThreadPerAgent(), env, listOf(bob), emptyList()).start()
}
