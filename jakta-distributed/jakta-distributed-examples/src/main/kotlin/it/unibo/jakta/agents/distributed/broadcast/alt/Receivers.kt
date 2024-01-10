package it.unibo.jakta.agents.distributed.broadcast.alt

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.Jakta
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary
import it.unibo.jakta.agents.distributed.dmas.DMas

fun main() {
    val env = Environment.of()

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

    DMas.withEmbeddedBroker(ExecutionStrategy.oneThreadPerMas(), env, listOf(alice, bob), emptyList()).start()
}
