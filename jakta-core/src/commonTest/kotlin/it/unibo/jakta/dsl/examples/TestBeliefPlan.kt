package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.PlanLibraryBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.skills.NodeTerminationSkill
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestBeliefPlan {

    context(terminator: NodeTerminationSkill)
    fun PlanLibraryBuilder<String, *>.terminateOn(test: String) = adding.belief {
        this.takeIf { it == test }
    } triggers {
        agent.print("Belief added: $context")
        terminator.terminateNode()
    }

    context(terminator: NodeTerminationSkill)
    fun LocalNodeBuilder<Any>.testAgent() = agent(BaseAgentID("TestAgent")) {
        embodiedAs { Any() }
        hasInitialGoals {
            !"testGoal"
        }
        hasPlans {
            adding.goal {
                this.takeIf { it == "testGoal" }
            } triggers {
                agent.print("Adding belief: testBelief")
                agent.believe("testBelief")
            }
            terminateOn("testBelief")
        }
    }

    val helloWorld = node {
        context(NodeTerminationSkill(node)) {
            testAgent()
        }
    }

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Error)
    }

    @Test
    fun testBeliefAddition() {
        executeInTestScope { helloWorld }
    }
}
