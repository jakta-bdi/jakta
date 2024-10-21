package it.unibo.jakta

import it.unibo.jakta.actions.ExternalRequest
import it.unibo.jakta.actions.impl.AbstractExternalAction
import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.events.Event
import it.unibo.jakta.goals.Achieve
import it.unibo.jakta.goals.Act
import it.unibo.jakta.goals.ActInternally
import it.unibo.jakta.messages.Message
import it.unibo.jakta.messages.Tell
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary

fun main() {
    val broadcastAction = object : AbstractExternalAction("broadcast", 2) {
        override fun action(request: ExternalRequest) {
            val type = request.arguments[0].castToAtom()
            val message = request.arguments[1].castToStruct()
            when (type.value) {
                "tell" -> broadcastMessage(Message(request.sender, Tell, message))
                "achieve" -> broadcastMessage(
                    Message(request.sender, it.unibo.jakta.messages.Achieve, message),
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
        events = listOf(
            Event.ofAchievementGoalInvocation(Achieve.of(Jakta.parseStruct("broadcast"))),
        ),
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

    Mas.of(
        it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(),
        env,
        sender,
        alice,
        alice.copy(),
    ).start()
}
