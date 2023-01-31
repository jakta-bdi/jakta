import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.Jacop
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.Message
import io.github.anitvam.agents.bdi.actions.ExternalAction
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
    val broadcastAction = object : ExternalAction("broadcast", 2) {
        override fun ExternalRequest.action() {
            val type = arguments[0].castToAtom()
            val message = arguments[1].castToStruct()
            when (type.value) {
                "tell" -> broadcastMessage(Message(this.sender, Tell, message))
                "achieve" -> broadcastMessage(
                    Message(this.sender, io.github.anitvam.agents.bdi.messages.Achieve, message)
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
            Event.ofAchievementGoalInvocation(Achieve.of(Jacop.parseStruct("broadcast")))
        ),

        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jacop.parseStruct("broadcast"),
                goals = listOf(
                    ActInternally.of(Jacop.parseStruct("print(\"Broadcast message\")")),
                    Act.of(Jacop.parseStruct("broadcast(tell, greetings)"))
                )
            )
        ),
    )

    val alice = Agent.of(
        name = "alice",
        planLibrary = PlanLibrary.of(
            Plan.ofBeliefBaseAddition(
                belief = Belief.from(Jacop.parseStruct("greetings(source(Sender))")),
                goals = listOf(
                    ActInternally.of(Jacop.parseStruct("print(\"Received message from: \", Sender)"))
                )
            )
        ),
    )

    Mas.of(ExecutionStrategy.oneThreadPerAgent(), env, sender, alice, alice.copy()).start()
}
