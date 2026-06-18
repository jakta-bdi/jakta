package examples

import ifGoalMatch
import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.node
import it.unibo.jakta.node.LocalNode
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.NodeBuilder
import it.unibo.jakta.plan.PlanScope
import it.unibo.jakta.plan.triggers


interface Skill<Body: Any, out N : Node<Body>> {
    val node: N
}


data class StopSkill(override val node: LocalNode<Any>) : Skill<Any, LocalNode<Any>> {

    fun stopNode() {
        node.terminateNode()
    }
}

context(skill: StopSkill)
val PlanScope<*, *, *>.stopSkill: StopSkill
    get() = skill


@JaktaDSL
fun <T : Skill<*, *>, R> NodeBuilder<*, *, *, *>.withSkills(
    skill: T,
    block: context(T) () -> R
): R {
    return with(skill) {
        block()
    }
}

class TestWithSkillSyntax {


    val helloWorld =
        node {
            withSkills(StopSkill(node)) {
                agent {
                    embodiedAs { Any() }
                    believes {
                        +"testBelief"
                    }
                    hasInitialGoals {
                        !"removeBelief"
                    }
                    hasPlans {
                        adding.goal {
                            ifGoalMatch("removeBelief")
                        } triggers {
                            agent.forget("testBelief")
                        }
                        removing.belief {
                            this.takeIf { it == "testBelief" }
                        } triggers {
                            agent.print("Belief removed: $context")
                            stopSkill.stopNode()
                        }
                    }
                }
            }
        }



}
