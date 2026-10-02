import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.docker.runDistributed
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.beliefQuery
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.belief.matchingBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.goal.replyOne
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.get
import it.unibo.jakta.kqml.KQMLPayload
import it.unibo.jakta.kqml.Tell
import it.unibo.jakta.kqml.askOneTo
import it.unibo.jakta.kqml.broadcastAchieve
import it.unibo.jakta.kqml.handleKQMLPayload
import it.unibo.jakta.kqml.kqmlSerializersModule
import it.unibo.jakta.kqml.tellTo
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.logic.unifiesWith
import it.unibo.jakta.mqtt.AgentIDSerializer
import it.unibo.jakta.node.MessageFilter
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.NodeID
import it.unibo.jakta.print
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.send
import it.unibo.jakta.source
import it.unibo.jakta.value
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.solve.Solution
import kotlin.math.hypot
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic

/**
 * The body of the agents: where they are, at coordinates ([x], [y]).
 * @property x the horizontal coordinate.
 * @property y the vertical coordinate.
 */
@Serializable
data class Position(val x: Double, val y: Double) {
    /**
     * The distance from the [other] position.
     */
    fun distanceTo(other: Position) = hypot(x - other.x, y - other.y)
}

/**
 * A [MessageFilter] selecting the agents, but the [sender], within [radius] from [center]:
 * being serializable, it travels to the other nodes, which evaluate it against the bodies of their agents.
 */
@Serializable
data class Around(
    @Serializable(with = AgentIDSerializer::class) val sender: AgentID,
    val center: Position,
    val radius: Double,
) : MessageFilter<Any> {
    override fun accept(node: Node<out Any>, agent: AgentID, body: Any) =
        agent != sender && body is Position && body.distanceTo(center) <= radius
}

// Each container builds the whole MAS on its own, so the ids of the agents talking to each other must be stable.
private val ping = BaseAgentID("ping", "ping")
private val pong = BaseAgentID("pong", "pong")
private val far = BaseAgentID("far", "far")

private const val ROUNDS = 3
private val start = Atom.of("start")
private val stop = Atom.of("stop")

private fun NodeBuilder<Any, *>.kqmlAgent(
    id: AgentID,
    position: Position,
    block: AgentBuilder<PrologBelief, PrologGoal, Any>.() -> Unit,
) = agent(id) {
    embodiedAs { position }
    handlesMessageEvents { message ->
        (message.payload as? KQMLPayload)?.let { handleKQMLPayload(it, message.sender) }
    }
    hasPlanLibrary {
        prologPlan {
            adding.goal {
                matchingGoal { stop[source(S)] }
            } triggers {
                agent.print("Stopping, as requested by ", S)
                node.terminateNode()
            }
        }
    }
    block()
}

/**
 * Three nodes, one agent each: ping greets the agents nearby, plays a few rounds of ping-pong with pong,
 * asks pong how far the ball went, and then stops everyone. The far agent never hears the greeting.
 *
 * Run without arguments to launch each node in its own Docker container.
 */
fun main(args: Array<String>) = runBlocking {
    Logger.setMinSeverity(Severity.Warn)
    mas(NodeBuilders.baseNode<Any>()) {
        node(NodeID("ping")) {
            context(MessagingSkill(node)) {
                kqmlAgent(ping, Position(0.0, 0.0)) {
                    hasInitialGoals { !initialGoal { start } }
                    hasPlanLibrary {
                        prologPlan {
                            adding.goal {
                                matchingGoal { start }
                            } triggers {
                                agent.print("Greeting the agents within 10 meters")
                                agent.send(
                                    Tell(
                                        setOf(
                                            belief {
                                                "greeting"("hello")
                                            },
                                        ),
                                    ),
                                    Around(ping, Position(0.0, 0.0), 10.0),
                                )
                            }
                        }
                        prologPlan {
                            adding.belief {
                                matchingBelief { "ball"(N)[source(S)] }
                            } triggers {
                                val rounds = N.value<Int>()
                                agent.print("Received ball ", rounds, " from ", S)
                                if (rounds < ROUNDS) {
                                    agent.tellTo(pong, belief { "ball"(rounds + 1) })
                                } else {
                                    agent.askOneTo(pong, beliefQuery { "ball"(X) })
                                    agent.print("Pong says the ball reached ", X)
                                    agent.broadcastAchieve(goal { stop })
                                    node.terminateNode()
                                }
                            }
                        }
                    }
                }
            }
        }
        node(NodeID("pong")) {
            context(MessagingSkill(node)) {
                kqmlAgent(pong, Position(1.0, 0.0)) {
                    hasPlanLibrary {
                        prologPlan {
                            adding.belief {
                                matchingBelief { "greeting"(G)[source(S)] }
                            } triggers {
                                agent.print("Received greeting ", G, " from ", S, ", serving the ball")
                                agent.tellTo(ping, belief { "ball"(1) })
                            }
                        }
                        prologPlan {
                            adding.belief {
                                matchingBelief { "ball"(N)[source(S)] }
                            } triggers {
                                agent.print("Received ball ", N, " from ", S)
                                agent.tellTo(ping, belief { "ball"(N.value<Int>() + 1) })
                            }
                        }
                        prologPlan {
                            adding.goal {
                                matchingGoal { replyOne(Q, M)[source(S)] }
                            } triggers {
                                val solution = agent.beliefs.unifiesWith(Q.value())
                                if (solution is Solution.Yes) {
                                    agent.tellTo(
                                        BaseAgentID(id = S.value()),
                                        M.value<String>(),
                                        belief {
                                            solution.solvedQuery
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        node(NodeID("far")) {
            kqmlAgent(far, Position(100.0, 0.0)) {
                hasPlanLibrary {
                    prologPlan {
                        adding.belief {
                            matchingBelief { "greeting"(G) }
                        } triggers {
                            agent.print("This should not happen: I am too far to hear ", G)
                        }
                    }
                }
            }
        }
    }.runDistributed(
        args,
        appName = "mqtt-ping-pong",
        serializers = kqmlSerializersModule +
            SerializersModule { polymorphic(MessageFilter::class) { subclass(Around::class, Around.serializer()) } },
    )
}
