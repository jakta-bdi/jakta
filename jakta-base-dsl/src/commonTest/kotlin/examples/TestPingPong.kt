package examples

import NodeTerminationSkill
import NodeTerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.agent.AgentBuilder
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.node
import it.unibo.jakta.node.LocalNodeBuilder
import it.unibo.jakta.plan.triggers
import kotlin.test.Test

class TestPingPong {

    class BodyWithName(val name: String)
    data class SimpleMessage(val payload: String, val sender: String) : AgentEvent.External.Message

    private fun <Belief : Any, Goal : Any> LocalNodeBuilder<Belief, Goal, NodeTerminationSkill, BodyWithName>.messageEnabledAgent(
        name: String,
        block: AgentBuilder<Belief, Goal, NodeTerminationSkill, BodyWithName>.() -> Unit,
    ) {
        agent(name) {
            body = BodyWithName(name)
            withSkills { NodeTerminationSkillImpl(this@messageEnabledAgent.node) }
            messageHandler = { BeliefAddEvent(it) }
            block()
        }
    }

    val node = node {
        messageEnabledAgent("Bob") {
            //withBeliefsType<SimpleMessage>() //TODO(Does this work instead of specifying the believes {}
            believes {
                +SimpleMessage("pippo", "pluto") //TODO(It cannot infer Belief type from plans directly)
            }
            hasPlans {
                adding.belief {
                    this.takeIf { it == SimpleMessage("Message", "Alice") }
                } triggers {
                    agent.print(context.toString())
                    this@node.node.sendEvent(
                        SimpleMessage("Hello Back!", agent.id.displayName),
                    ) { it.name == context.sender }
                }
            }
        }
        messageEnabledAgent("Alice") {
            hasInitialGoals {
                !"sendMessage"
            }
            hasPlans {
                adding.goal {
                    ifGoalMatch("sendMessage")
                } triggers {
                    agent.print("Hello World!")
                    this@node.node.sendEvent( //This needs to be a skill :)
                        SimpleMessage("Message", agent.id.displayName),
                    ) {it.name == "Bob"}
                }
                adding.belief{
                    this.takeIf { it == SimpleMessage("Hello Back!", "Bob") }
                } triggers {
                    agent.print("Terminating!")
                    skills.terminateNode()
                    // node.terminate() TODO(The actual implementation allows this!!!)
                }
            }
        }
    }

    @Test
    fun testPingPong() {
        Logger.setMinSeverity(Severity.Error)
        executeInTestScope { node }
    }
}
