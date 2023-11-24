package it.unibo.jakta.agents.bdi

import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.Act
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary

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

    val env = Environment.of(
        externalActions = mapOf(
            broadcastAction.signature.name to broadcastAction,
        ),
    )

    val sender = Agent.of(
        name = "sender",
        events = listOf(),

        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("broadcast"),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("print(\"Broadcast message\")")),
                    Act.of(Jakta.parseStruct("broadcast(tell, greetings)")),
                ),
            ),
        ),
    )

    val alice = Agent.of(
        name = "alice",
        planLibrary = PlanLibrary.of(
            Plan.ofBeliefBaseAddition(
                belief = Belief.from(Jakta.parseStruct("greetings(source(Sender))")),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("print(\"Received message from: \", Sender)")),
                ),
            ),
        ),
    )

    Mas.of(ExecutionStrategy.oneThreadPerAgent(), env, sender, alice, alice.copy()).start()
}
