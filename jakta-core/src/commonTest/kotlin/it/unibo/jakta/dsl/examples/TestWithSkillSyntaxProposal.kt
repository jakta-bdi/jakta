package it.unibo.jakta.dsl.examples

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.dsl.plan.PlanLibraryBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.PlanScope

interface Skill<Body : Any>

open class BaseSkill<Body : Any>(protected val node: Node<Body>) : Skill<Body>

class StopSkill(node: Node<Any>) : BaseSkill<Any>(node) {

    fun stopNode() {
        node.terminateNode()
    }
}

interface WhateverSkill : Skill<Any> {
    fun doSomething()
}

class WhateverFoo(node: Node<Any>) : BaseSkill<Any>(node), WhateverSkill {
    override fun doSomething() {
        print("foo")
    }
}

class WhateverBar(node: Node<Any>) : BaseSkill<Any>(node), WhateverSkill {
    override fun doSomething() {
        print("foo")
    }
}

context(skill: WhateverSkill)
val PlanScope<*, *, *>.whateverSkill: WhateverSkill
    get() = skill

context(skill: StopSkill)
val PlanScope<*, *, *>.stopSkill: StopSkill
    get() = skill

// TODO this requires the overload with multiple parameters just like `context` does.
//  consider using generated signatures
@JaktaDSL
fun <T : Skill<*>, R> NodeBuilder<*, *>.withSkills(skill: T, block: context(T) () -> R): R = context(skill) {
    block()
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
                        customPlan()
                    }
                }
            }
        }
}

context(skill: StopSkill)
fun PlanLibraryBuilder<*, *>.customPlan() {
    removing.belief {
        this.takeIf { it == "testBelief" }
    } triggers {
        agent.print("Belief removed: $context")
        stopSkill.stopNode()
    }
}
