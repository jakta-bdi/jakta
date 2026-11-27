package it.unibo.jakta.artifact

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.matchingBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.get
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.jakta.tag
import it.unibo.jakta.value
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class PrologCounter(name: String) : Artifact(name) {
    var count by observable(0)

    val inc by operation { count++ }
}

// Observable properties become facts annotated with their artifact, as in JaCaMo: count(1)[artifact_name(c)]
fun AgentState<PrologBelief, PrologGoal>.prologBeliefsFrom(event: Perception): AgentUpdate<*>? =
    (event as? ArtifactEvent.PropertyChanged)?.let { changed ->
        val value = Struct.of(changed.property, Integer.of(changed.value as Int))
        AgentUpdate.Belief(
            setOf(Fact.of(value.tag(Struct.of("artifact_name", Atom.of(changed.artifact))))),
            beliefs.filter { it.head.functor == changed.property }.toSet(),
        )
    }

class TestPrologArtifacts {

    @Test
    fun observablePropertiesBecomePrologBeliefs() = runTest {
        Logger.setMinSeverity(Severity.Error)
        val counts = mutableListOf<Int>()
        mas(NodeBuilders.artifactNode<Any>()) {
            node {
                val counter = node.makeArtifact(PrologCounter("counter"))
                context(ArtifactSkill(node)) {
                    agent<PrologBelief, PrologGoal> {
                        embodiedAs { Any() }
                        handlesPerceptionEvents { prologBeliefsFrom(it) }
                        hasInitialGoals { !initialGoal { "count"(2) } }
                        hasPlanLibrary {
                            prologPlan {
                                adding.goal { matchingGoal { "count"(N) } } triggers {
                                    agent.focus(counter)
                                    repeat(N.value<Int>()) { counter.inc() }
                                }
                            }
                            prologPlan {
                                adding.belief { matchingBelief { "count"(N)["artifact_name"("counter")] } } triggers {
                                    counts += N.value<Int>()
                                    if (N.value<Int>() == 2) node.terminateNode()
                                }
                            }
                        }
                    }
                }
            }
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        assertEquals(listOf(0, 1, 2), counts)
    }
}
