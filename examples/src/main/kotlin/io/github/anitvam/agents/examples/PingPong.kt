package io.github.anitvam.agents.examples

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.JasonParser
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.Message
import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.actions.ExternalRequest
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.executionstrategies.OneThreadPerMas
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.Act
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.goals.RemoveBelief
import io.github.anitvam.agents.bdi.goals.UpdateBelief
import io.github.anitvam.agents.bdi.messages.Tell
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

fun main() {

    val sendAction = object : ExternalAction("send", 4) {
        override fun ExternalRequest.action() {
            val sender = arguments[0].castToAtom()
            val receiver = arguments[1].castToAtom()
            val type = arguments[2].castToAtom()
            val message = arguments[3].castToStruct()

            when (type.value) {
                "tell" -> sendMessage(receiver.value, Message(sender.value, Tell, message))
                "achieve" -> sendMessage(
                    receiver.value,
                    Message(sender.value, io.github.anitvam.agents.bdi.messages.Achieve, message)
                )
            }
        }
    }

    val env = Environment.of(
        externalActions = mapOf(
            sendAction.signature.name to sendAction
        )
    )

    val pinger = Agent.of(
        name = "pinger",
        beliefBase = BeliefBase.of(
            Belief.fromSelfSource(JasonParser.parser.parseStruct("turn(me)")),
            Belief.fromSelfSource(JasonParser.parser.parseStruct("other(ponger)")),
        ),
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(JasonParser.parser.parseStruct("send_ping")))),

        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = JasonParser.parser.parseStruct("send_ping"),
                guard = JasonParser.parser.parseStruct("turn(source(self), me) & other(source(self), Receiver)"),
                goals = listOf(
                    UpdateBelief.of(Belief.fromSelfSource(JasonParser.parser.parseStruct("turn(other)"))),
                    Achieve.of(JasonParser.parser.parseStruct("sendMessageTo(ball, Receiver)")),
                ),
            ),
            Plan.ofBeliefBaseAddition(
                belief = Belief.from(JasonParser.parser.parseStruct("ball(source(Sender))")),
                guard = JasonParser.parser.parseStruct("turn(source(self), other) & other(source(self), Sender)"),
                goals = listOf(
                    UpdateBelief.of(Belief.fromSelfSource(JasonParser.parser.parseStruct("turn(me)"))),
                    ActInternally.of(JasonParser.parser.parseStruct("print(\"Received ball from\", Sender)")),
                    RemoveBelief.of(Belief.from(JasonParser.parser.parseStruct("ball(source(Sender))"))),
                    ActInternally.of(JasonParser.parser.parseStruct("print(\"Pinger hasDone\")")),
                ),
            ),
            Plan.ofAchievementGoalInvocation(
                value = JasonParser.parser.parseStruct("sendMessageTo(Message, Receiver)"),
                goals = listOf(
                    ActInternally.of(JasonParser.parser.parseStruct("print(\"Sending \", Message)")),
                    Act.of(JasonParser.parser.parseStruct("send(pinger, Receiver, tell, Message)")),
                ),
            ),

        ),

    )

    val ponger = Agent.of(
        name = "ponger",
        beliefBase = BeliefBase.of(
            Belief.fromSelfSource(JasonParser.parser.parseStruct("turn(other)")),
            Belief.fromSelfSource(JasonParser.parser.parseStruct("other(pinger)")),
        ),
        planLibrary = PlanLibrary.of(
            Plan.ofBeliefBaseAddition(
                belief = Belief.from(JasonParser.parser.parseStruct("ball(source(Sender))")),
                guard = JasonParser.parser.parseStruct("turn(source(self), other) & other(source(self), Sender)"),
                goals = listOf(
                    UpdateBelief.of(Belief.fromSelfSource(JasonParser.parser.parseStruct("turn(me)"))),
                    RemoveBelief.of(Belief.from(JasonParser.parser.parseStruct("ball(source(Sender))"))),
                    ActInternally.of(JasonParser.parser.parseStruct("print(\"Received ball from\", Sender)")),
                    Achieve.of(JasonParser.parser.parseStruct("sendMessageTo(ball, Sender)")),
                    Achieve.of(JasonParser.parser.parseStruct("handle_ping")),
                ),
            ),
            Plan.ofAchievementGoalInvocation(
                value = JasonParser.parser.parseStruct("handle_ping"),
                goals = listOf(
                    UpdateBelief.of(Belief.fromSelfSource(JasonParser.parser.parseStruct("turn(other)"))),
                    ActInternally.of(JasonParser.parser.parseStruct("print(\"Ponger has Done\")")),
                ),
            ),
            Plan.ofAchievementGoalInvocation(
                value = JasonParser.parser.parseStruct("sendMessageTo(Message, Receiver)"),
                goals = listOf(
                    ActInternally.of(JasonParser.parser.parseStruct("print(\"Sending \", Message)")),
                    Act.of(JasonParser.parser.parseStruct("send(ponger, Receiver, tell, Message)")),
                ),
            ),
        ),
    )

    Mas.of(OneThreadPerMas(), env, pinger, ponger).start()
}
