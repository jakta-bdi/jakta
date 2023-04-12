package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.actions.impl.AbstractExternalAction
import io.github.anitvam.agents.bdi.actions.ExternalRequest
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.Act
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.messages.Tell
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

fun main() {
    val broadcastAction = object : AbstractExternalAction("broadcast", 2) {
        override fun action(request: ExternalRequest) {
            val type = request.arguments[0].castToAtom()
            val message = request.arguments[1].castToStruct()
            when (type.value) {
                "tell" -> broadcastMessage(Message(request.sender, Tell, message))
                "achieve" -> broadcastMessage(
                    Message(request.sender, io.github.anitvam.agents.bdi.messages.Achieve, message)
                )
            }
        }
    }

    val env = Environment.of(
        externalActions = mapOf(
            broadcastAction.signature.name to broadcastAction
        )
    )

    val sender = Agent.of(
        name = "sender",
        events = listOf(
            Event.ofAchievementGoalInvocation(Achieve.of(Jakta.parseStruct("broadcast")))
        ),

        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("broadcast"),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("print(\"Broadcast message\")")),
                    Act.of(Jakta.parseStruct("broadcast(tell, greetings)"))
                )
            )
        ),
    )

    val alice = Agent.of(
        name = "alice",
        planLibrary = PlanLibrary.of(
            Plan.ofBeliefBaseAddition(
                belief = Belief.from(Jakta.parseStruct("greetings(source(Sender))")),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("print(\"Received message from: \", Sender)"))
                )
            )
        ),
    )

    Mas.of(ExecutionStrategy.oneThreadPerAgent(), env, sender, alice, alice.copy()).start()
}
