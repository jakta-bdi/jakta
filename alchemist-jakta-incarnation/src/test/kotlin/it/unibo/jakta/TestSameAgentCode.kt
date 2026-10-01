package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.alchemist.core.Engine
import it.unibo.alchemist.jakta.AlchemistNodeRunner
import it.unibo.alchemist.jakta.JaktaIncarnation
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.BaseNode
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.jakta.situated.Coordinates
import it.unibo.jakta.situated.InMemorySpace
import it.unibo.jakta.situated.NeighborhoodSkill
import it.unibo.jakta.situated.PropertySkill
import it.unibo.jakta.situated.SituatedBody
import it.unibo.jakta.situated.SpatialSkill
import it.unibo.jakta.situated.moveTowards
import it.unibo.jakta.situated.neighbors
import it.unibo.jakta.situated.position
import it.unibo.jakta.situated.property
import it.unibo.jakta.situated.setProperty
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.sendTo
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest

/**
 * The same MAS, written only against the situated skills, runs in memory and in Alchemist:
 * a walker moves towards a target until it is in range, then greets it.
 */
class TestSameAgentCode {

    private class Outcome {
        var greetedFrom: Coordinates? = null
        var greeted = false
    }

    private val walker = BaseAgentID("walker")
    private val target = BaseAgentID("target")

    context(_: SpatialSkill, _: NeighborhoodSkill, _: MessagingSkill)
    private fun NodeBuilder<Any, *>.walker(outcome: Outcome) = agent<String, String>(walker) {
        embodiedAs { SituatedBody(Coordinates(0.0, 0.0)) }
        hasInitialGoals { !"approach" }
        hasPlanLibrary {
            adding.goal { takeIf { it == "approach" } } triggers {
                while (target !in agent.neighbors) {
                    agent.moveTowards(Coordinates(TARGET_X, 0.0), STEP)
                    delay(1.seconds)
                }
                outcome.greetedFrom = agent.position
                agent.sendTo(target, "hello")
                node.terminateNode()
            }
        }
    }

    context(_: PropertySkill)
    private fun NodeBuilder<Any, *>.target(outcome: Outcome) = agent<String, String>(target) {
        embodiedAs { SituatedBody(Coordinates(TARGET_X, 0.0), mapOf(GREETED to false)) }
        handlesMessageEvents { AgentUpdate.Belief(setOf(it.payload.toString()), emptySet()) }
        hasPlanLibrary {
            adding.belief { takeIf { it == "hello" } } triggers {
                agent.setProperty(GREETED, true)
                outcome.greeted = agent.property(GREETED) == true
                node.terminateNode()
            }
        }
    }

    private fun <S> approach(
        outcome: Outcome,
        skillsFor: (Node<Any>) -> S,
    ) where
            S : SpatialSkill, S : NeighborhoodSkill, S : PropertySkill = mas(NodeBuilders.baseNode<Any>()) {
        node {
            context(skillsFor(node), MessagingSkill(node)) { walker(outcome) }
        }
        node {
            context(skillsFor(node)) { target(outcome) }
        }
    }

    private fun Outcome.assertExpected() {
        // it steps by 1.5 from 0 towards 10, and is in range from 6
        val from = checkNotNull(greetedFrom) { "The walker never greeted" }
        assertTrue(from.distanceTo(Coordinates(6.0, 0.0)) < 1e-9, "The walker greeted from $from")
        assertTrue(greeted)
    }

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Warn)
    }

    @Test
    fun testInMemory() {
        val space = InMemorySpace(RANGE)
        val outcome = Outcome()
        runTest {
            approach(outcome, space::skillsFor).run(CoroutineNodeRunner(SharedMemoryNetwork()))
        }
        outcome.assertExpected()
    }

    @Test
    fun testInAlchemist() {
        val environment = Continuous2DEnvironment(JaktaIncarnation<Euclidean2DPosition>())
        environment.linkingRule = ConnectWithinDistance(RANGE)
        environment.addTerminator(AfterTime(DoubleTime(MAX_TIME)))
        val simulation = Engine(environment)
        val runner = AlchemistNodeRunner<Euclidean2DPosition, BaseNode<Any>>(simulation)
        val outcome = Outcome()
        runBlocking { approach(outcome, runner::skillsFor).run(runner) }
        simulation.error.ifPresent { throw it }
        outcome.assertExpected()
        // properties are molecules of the Alchemist node
        assertEquals(listOf<Any?>(true), environment.nodes.mapNotNull { it.contents[SimpleMolecule(GREETED)] })
    }

    private companion object {
        const val GREETED = "greeted"
        const val RANGE = 5.0
        const val STEP = 1.5
        const val TARGET_X = 10.0
        const val MAX_TIME = 100.0
    }
}
