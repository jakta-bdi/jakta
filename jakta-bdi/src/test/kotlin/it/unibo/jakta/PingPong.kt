package it.unibo.jakta

import it.unibo.jakta.actions.requests.ExternalRequest
import it.unibo.jakta.actions.AbstractExternalAction
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.goals.Achieve
import it.unibo.jakta.goals.Act
import it.unibo.jakta.goals.ActInternally
import it.unibo.jakta.goals.RemoveBelief
import it.unibo.jakta.goals.UpdateBelief
import it.unibo.jakta.messages.Message
import it.unibo.jakta.messages.Tell
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary

fun main() {
    val sendAction = object : AbstractExternalAction("send", 3) {
        override suspend fun action(request: ExternalRequest) {
            val receiver = request.arguments[0].castToAtom()
            val type = request.arguments[1].castToAtom()
            val message = request.arguments[2].castToStruct()
            when (type.value) {
                "tell" -> sendMessage(receiver.value, Message(request.sender, Tell, message))
                "achieve" -> sendMessage(
                    receiver.value,
                    Message(request.sender, it.unibo.jakta.messages.Achieve, message),
                )
            }
        }
    }

    val env = Environment.of(
        externalActions = mapOf(
            sendAction.signature.name to sendAction,
        ),
    )

    val sendPlan = Plan.ofAchievementGoalInvocation(
        value = Jakta.parseStruct("sendMessageTo(Message, Receiver)"),
        goals = listOf(
            ActInternally.of(Jakta.parseStruct("print(\"Sending \", Message)")),
            Act.of(Jakta.parseStruct("send(Receiver, tell, Message)")),
        ),
    )

    val pinger = ASAgent.of(
        name = "pinger",
        beliefBase = ASBeliefBase.of(
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

    val ponger = ASAgent.of(
        name = "ponger",
        beliefBase = ASBeliefBase.of(
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

    Mas.of(it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(), env, pinger, ponger).start()
}
