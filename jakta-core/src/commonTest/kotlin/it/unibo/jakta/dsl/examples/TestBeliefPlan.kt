package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.PlanLibraryBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.skills.BaseNodeTerminationSkill
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
    fun <Goal : Any> LocalNodeBuilder<Any>.testAgent() = agent<String, Goal>("Hello world agent") {
        embodiedAs { Any() }
        believes {
            +"testBelief"
        }
        hasPlans {
            terminateOn("testBelief")
        }
    }

    val helloWorld = node {
        context(BaseNodeTerminationSkill(node)) {
            testAgent<String>()
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
