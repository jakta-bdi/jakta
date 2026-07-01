package it.unibo.jakta.dsl.examples

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.examples.TemperatureSensorSkill.Events.Factory.temperature
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.dsl.plan.PlanLibraryBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.PlanScope
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

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


// TODO Should skills have "active behavior" or should all active behavior be delegated externally?
//  where does the active behavior of a skill run with respect to a node? inside the node? outside?
//  what happens when we move to distributed settings?

class TemperatureSensor {

    val observers: MutableList<(Double) -> Unit> = mutableListOf()

    fun observe(handler : (Double) -> Unit) {
        observers.add(handler)
    }

    // TODO somebody will remember to invoke start in the main when creating the sensor.
    suspend fun start() {
        while(true) {
            delay(1.seconds)
            observers.forEach { it.invoke(Random.nextDouble() * 100)}
        }
    }
}

class TemperatureSensorSkill(node: Node<Any>, temperatureSensor: TemperatureSensor) : BaseSkill<Any>(node) {

    init {
        temperatureSensor.observe { t ->
            node.sendEvent(temperature(t))
        }
    }

    object Events {
        class Temperature internal constructor(val temp: Double) : AgentEvent.External.Perception

        object Factory {
            fun TemperatureSensorSkill.temperature(temp: Double): Temperature = Temperature(temp)
        }
    }
}
