package it.unibo.jakta.agents.bdi

import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.Act
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.goals.RemoveBelief
import it.unibo.jakta.agents.bdi.goals.UpdateBelief
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary

fun main() {

    val sendAction = object : AbstractExternalAction("send", 3) {
        override fun action(request: ExternalRequest) {
            val receiver = request.arguments[0].castToAtom()
            val type = request.arguments[1].castToAtom()
            val message = request.arguments[2].castToStruct()
            when (type.value) {
                "tell" -> sendMessage(receiver.value, Message(request.sender, Tell, message))
                "achieve" -> sendMessage(
                    receiver.value,
                    Message(request.sender, it.unibo.jakta.agents.bdi.messages.Achieve, message)
                )
            }
        }
    }

    val env = Environment.of(
        externalActions = mapOf(
            sendAction.signature.name to sendAction
        )
    )

    val sendPlan = Plan.ofAchievementGoalInvocation(
        value = Jakta.parseStruct("sendMessageTo(Message, Receiver)"),
        goals = listOf(
            ActInternally.of(Jakta.parseStruct("print(\"Sending \", Message)")),
            Act.of(Jakta.parseStruct("send(Receiver, tell, Message)")),
        ),
    )

    val pinger = Agent.of(
        name = "pinger",
        beliefBase = BeliefBase.of(
            Belief.fromSelfSource(Jakta.parseStruct("turn(me)")),
            Belief.fromSelfSource(Jakta.parseStruct("other(ponger)")),
        ),
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(Jakta.parseStruct("send_ping")))),

        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("send_ping"),
                guard = Jakta.parseStruct("turn(source(self), me) & other(source(self), Receiver)"),
                goals = listOf(
                    UpdateBelief.of(Belief.fromSelfSource(Jakta.parseStruct("turn(other)"))),
                    Achieve.of(Jakta.parseStruct("sendMessageTo(ball, Receiver)")),
                ),
            ),
            Plan.ofBeliefBaseAddition(
                guard = Jakta.parseStruct("turn(source(self), other) & other(source(self), Sender)"),
                belief = Belief.from(Jakta.parseStruct("ball(source(Sender))")),
                goals = listOf(
                    UpdateBelief.of(Belief.fromSelfSource(Jakta.parseStruct("turn(me)"))),
                    ActInternally.of(Jakta.parseStruct("print(\"Received ball from\", Sender)")),
                    RemoveBelief.of(Belief.from(Jakta.parseStruct("ball(source(Sender))"))),
                    ActInternally.of(Jakta.parseStruct("print(\"Pinger hasDone\")")),
                ),
            ),
            sendPlan,
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

    Mas.of(ExecutionStrategy.oneThreadPerAgent(), env, pinger, ponger).start()
}
