package it.unibo.jakta.agents.distributed.broadcast

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.Jakta
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.Act
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary
import it.unibo.jakta.agents.distributed.broadcast.Common.env
import it.unibo.jakta.agents.distributed.dmas.DMas

fun main() {
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

    DMas.withEmbeddedBroker(ExecutionStrategy.oneThreadPerAgent(), env, listOf(sender), emptyList()).start()
}
