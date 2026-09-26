---
sidebar_position: 3
---

# Give agents a body

Every agent has a **body**: its representation inside the node. The body is where the environment keeps
per-agent state — a position, a battery level, a name — that skills can read and change.
Agents that don't need one use `Any`.

This guide builds a robot that moves on a grid.

## 1. Define the body type

```kotlin
class Robot(val name: String) {
    var x = 0
    var y = 0
}
```

## 2. Use it in the node and in the agents

The body type is fixed by the node builder, `NodeBuilders.baseNode<Robot>()`, and each agent creates its own body
with `embodiedAs`, which receives the agent's ID:

```kotlin
mas(NodeBuilders.baseNode<Robot>()) {
    node {
        agent<String, String>(BaseAgentID("R2")) {
            embodiedAs { id -> Robot(id.displayName) }
            // ...
        }
    }
}
```

`node.agents` maps every agent ID to its body, so both skills and plan bodies can find it:
`node.agents.getValue(agent.id)`.

## 3. Write a skill that uses the body

```kotlin
data class Moved(val x: Int, val y: Int) : Perception

class GridMovement(private val node: Node<Robot>) {
    fun Agent.moveTo(x: Int, y: Int) {
        val body = node.agents.getValue(id)
        body.x = x
        body.y = y
        // only the robot that moved perceives its new position
        node.publishEvent(Moved(x, y)) { it === body }
    }
}

context(movement: GridMovement)
fun Agent.moveTo(x: Int, y: Int) = with(movement) { moveTo(x, y) }
```

The skill changes the body and publishes a perception, using the body filter of `publishEvent` so that only
the moving robot receives it. The top-level `moveTo` makes `agent.moveTo(...)` available wherever a `GridMovement`
is in scope, the same way `MessagingSkill` provides `agent.sendTo(...)`.

## 4. Put it together

```kotlin
fun main(): Unit = runBlocking {
    mas(NodeBuilders.baseNode<Robot>()) {
        node {
            context(GridMovement(node)) {
                agent<String, String>(BaseAgentID("R2")) {
                    embodiedAs { id -> Robot(id.displayName) }
                    handlesPerceptionEvents { perception ->
                        when (perception) {
                            is Moved -> AgentUpdate.Belief(
                                setOf("at(${perception.x},${perception.y})"),
                                beliefs.filter { it.startsWith("at(") }.toSet(),
                            )
                            else -> null
                        }
                    }
                    hasInitialGoals { !"patrol" }
                    hasPlanLibrary {
                        adding.goal {
                            takeIf { it == "patrol" }
                        } triggers {
                            agent.moveTo(1, 0)
                            agent.moveTo(1, 1)
                        }
                        adding.belief {
                            takeIf { it == "at(1,1)" }
                        } triggers {
                            val body = node.agents.getValue(agent.id)
                            agent.print("${body.name} reached (${body.x}, ${body.y})")
                            node.terminateNode()
                        }
                    }
                }
            }
        }
    }.runLocally()
}
```

It prints `R2 reached (1, 1)`.

<details>
<summary>Imports</summary>

```kotlin
import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.Node
import kotlinx.coroutines.runBlocking
```

</details>

:::note
Built-in skills such as `MessagingSkill(node)` expect a `Node<Any>`. On a node with a custom body type, define your
own skills against `Node<YourBody>`, as above.
:::

For a larger example, with two skills (movement and battery recharging) sharing the same body, see
[`TestSpatialRobot`](https://github.com/jakta-bdi/jakta/blob/main/jakta-core/src/commonTest/kotlin/it/unibo/jakta/dsl/examples/TestSpatialRobot.kt).
