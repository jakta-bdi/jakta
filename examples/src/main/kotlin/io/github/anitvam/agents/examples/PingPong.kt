package io.github.anitvam.agents.examples

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.Jacop
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.Message
import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.actions.ExternalRequest
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.Act
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.goals.RemoveBelief
import io.github.anitvam.agents.bdi.goals.UpdateBelief
import io.github.anitvam.agents.bdi.messages.Tell
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

fun main() {

    val sendAction = object : ExternalAction("send", 3) {
        override fun ExternalRequest.action() {
            val receiver = arguments[0].castToAtom()
            val type = arguments[1].castToAtom()
            val message = arguments[2].castToStruct()
            when (type.value) {
                "tell" -> sendMessage(receiver.value, Message(this.sender, Tell, message))
                "achieve" -> sendMessage(
                    receiver.value,
                    Message(this.sender, io.github.anitvam.agents.bdi.messages.Achieve, message)
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
        value = Jacop.parseStruct("sendMessageTo(Message, Receiver)"),
        goals = listOf(
            ActInternally.of(Jacop.parseStruct("print(\"Sending \", Message)")),
            Act.of(Jacop.parseStruct("send(Receiver, tell, Message)")),
        ),
    )

    val pinger = Agent.of(
        name = "pinger",
        beliefBase = BeliefBase.of(
            Belief.fromSelfSource(Jacop.parseStruct("turn(me)")),
            Belief.fromSelfSource(Jacop.parseStruct("other(ponger)")),
        ),
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(Jacop.parseStruct("send_ping")))),

        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jacop.parseStruct("send_ping"),
                guard = Jacop.parseStruct("turn(source(self), me) & other(source(self), Receiver)"),
                goals = listOf(
                    UpdateBelief.of(Belief.fromSelfSource(Jacop.parseStruct("turn(other)"))),
                    Achieve.of(Jacop.parseStruct("sendMessageTo(ball, Receiver)")),
                ),
            ),
            Plan.ofBeliefBaseAddition(
                belief = Belief.from(Jacop.parseStruct("ball(source(Sender))")),
                guard = Jacop.parseStruct("turn(source(self), other) & other(source(self), Sender)"),
                goals = listOf(
                    UpdateBelief.of(Belief.fromSelfSource(Jacop.parseStruct("turn(me)"))),
                    ActInternally.of(Jacop.parseStruct("print(\"Received ball from\", Sender)")),
                    RemoveBelief.of(Belief.from(Jacop.parseStruct("ball(source(Sender))"))),
                    ActInternally.of(Jacop.parseStruct("print(\"Pinger hasDone\")")),
                ),
            ),
            sendPlan,
        ),

    )

    val ponger = Agent.of(
        name = "ponger",
        beliefBase = BeliefBase.of(
            Belief.fromSelfSource(Jacop.parseStruct("turn(other)")),
            Belief.fromSelfSource(Jacop.parseStruct("other(pinger)")),
        ),
        planLibrary = PlanLibrary.of(
            Plan.ofBeliefBaseAddition(
                belief = Belief.from(Jacop.parseStruct("ball(source(Sender))")),
                guard = Jacop.parseStruct("turn(source(self), other) & other(source(self), Sender)"),
                goals = listOf(
                    UpdateBelief.of(Belief.fromSelfSource(Jacop.parseStruct("turn(me)"))),
                    RemoveBelief.of(Belief.from(Jacop.parseStruct("ball(source(Sender))"))),
                    ActInternally.of(Jacop.parseStruct("print(\"Received ball from\", Sender)")),
                    Achieve.of(Jacop.parseStruct("sendMessageTo(ball, Sender)")),
                    Achieve.of(Jacop.parseStruct("handle_ping")),
                ),
            ),
            Plan.ofAchievementGoalInvocation(
                value = Jacop.parseStruct("handle_ping"),
                goals = listOf(
                    UpdateBelief.of(Belief.fromSelfSource(Jacop.parseStruct("turn(other)"))),
                    ActInternally.of(Jacop.parseStruct("print(\"Ponger has Done\")")),
                ),
            ),
            sendPlan,
        ),
    )

    Mas.of(ExecutionStrategy.oneThreadPerAgent(), env, pinger, ponger).start()
}
