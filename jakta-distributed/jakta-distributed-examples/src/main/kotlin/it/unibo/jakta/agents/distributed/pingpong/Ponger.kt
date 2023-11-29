package it.unibo.jakta.agents.distributed.pingpong

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.Jakta
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.goals.RemoveBelief
import it.unibo.jakta.agents.bdi.goals.UpdateBelief
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary
import it.unibo.jakta.agents.distributed.RemoteService
import it.unibo.jakta.agents.distributed.dmas.DMas

fun main() {
    val env = Environment.of(
        externalActions = mapOf(
            sendAction.signature.name to sendAction,
        ),
    )

    val ponger = Agent.of(
        name = "ponger",
        beliefBase = BeliefBase.of(
            Belief.fromSelfSource(Jakta.parseStruct("turn(other)")),
            Belief.fromSelfSource(Jakta.parseStruct("other(pinger)")),
        ),
        planLibrary = PlanLibrary.of(
            Plan.ofBeliefBaseAddition(
                belief = Belief.from(Jakta.parseStruct("ball(source(Sender))")),
                guard = Jakta.parseStruct("turn(source(self), other) & other(source(self), Sender)"),
                goals = listOf(
                    UpdateBelief.of(Belief.fromSelfSource(Jakta.parseStruct("turn(me)"))),
                    RemoveBelief.of(Belief.from(Jakta.parseStruct("ball(source(Sender))"))),
                    ActInternally.of(Jakta.parseStruct("print(\"Received ball from\", Sender)")),
                    Achieve.of(Jakta.parseStruct("sendMessageTo(ball, Sender)")),
                    Achieve.of(Jakta.parseStruct("handle_ping")),
                ),
            ),
            Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("handle_ping"),
                goals = listOf(
                    UpdateBelief.of(Belief.fromSelfSource(Jakta.parseStruct("turn(other)"))),
                    ActInternally.of(Jakta.parseStruct("print(\"Ponger has Done\")")),
                ),
            ),
            sendPlan,
        ),
    )

    val pinger = RemoteService("pinger")

    DMas.withEmbeddedBroker(
        ExecutionStrategy.oneThreadPerAgent(),
        env,
        listOf(ponger),
        listOf(pinger),
    ).start()
}
