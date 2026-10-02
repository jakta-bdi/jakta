---
sidebar_position: 3
---

# Give agents a body

Every agent has a **body**: its representation inside the node, through which it is situated in the environment.
The body owns what belongs to the agent in the world, such as its position, and its sensors and actuators.
Skills act through it, and perceptions can be delivered to specific bodies only.
Agents that don't need one use `Any`.

The snippets below come from the robot of the
[`vacuum-world`](https://github.com/jakta-bdi/jakta/tree/main/examples/vacuum-world) example
([`VacuumBody.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/vacuum-world/src/commonMain/kotlin/VacuumBody.kt)),
which you can also [try in the browser](../showcase/vacuum-world.md).

## Define the body type

The body keeps the robot's own state, separate from the environment it is placed in:

```kotlin
data class RobotState(val position: Pos, val facing: Direction, val cleaned: Int = 0)

class VacuumBody(val world: VacuumWorld, start: Pos = ROBOT_START) {
    private val mutableState = MutableStateFlow(RobotState(start, Direction.EAST))
    val state: StateFlow<RobotState> = mutableState.asStateFlow()
    // ...
}
```

Exposing the state as a `StateFlow` lets a UI draw the robot from its body.

## Give it actuators

Actions change the body, and the world only where the robot affects it:

```kotlin
fun forward() = mutableState.update { robot ->
    val ahead = robot.cellOf(Square.FORWARD)
    if (world.state.value.contentAt(ahead) == Content.OBSTACLE) robot else robot.copy(position = ahead)
}

fun clean() {
    if (world.removeDust(state.value.position)) mutableState.update { it.copy(cleaned = it.cleaned + 1) }
}
```

## Give it sensors

A perception is what the sensors read, not the whole world:

```kotlin
data class VacuumPerception(
    val location: Pos,
    val facing: Direction,
    val time: Int,
    val squares: Map<Square, Content>,
) : Perception

fun sense(): VacuumPerception {
    val robot = state.value
    val world = world.state.value
    val squares = Square.entries.associateWith { world.contentAt(robot.cellOf(it)) }
    return VacuumPerception(robot.position, robot.facing, world.time, squares)
}
```

## Use it in the node and in the agent

The body type is fixed by the node builder, and `embodiedAs` gives the agent its body:

```kotlin
mas(NodeBuilders.baseNode<VacuumBody>()) {
    node {
        agent<PrologBelief, PrologGoal>(BaseAgentID("vacuum")) {
            embodiedAs { body }
            // ...
        }
    }
}
```

`embodiedAs` receives the agent's ID, so it can also create a new body for each agent.
`node.agents` maps every agent ID to its body: `node.agents.getValue(agent.id)`.

## Act through the body with a skill

A skill finds the body of the acting agent, acts through it, and delivers what the robot senses afterwards only to
that body, using the body filter of `publishEvent`. Simplified from the example, which also lets dust appear:

```kotlin
class VacuumSkill(node: Node<VacuumBody>, private val stepTime: () -> Duration) : Skill<VacuumBody>(node) {
    private val Agent.body get() = node.agents.getValue(id)

    fun Agent.look() {
        val body = body
        node.publishEvent(body.sense()) { it === body }
    }

    suspend fun Agent.forward() {
        delay(stepTime())
        body.forward()
        look()
    }
}
```

`with(VacuumSkill(node, stepTime)) { agent(...) { ... } }` brings the actions into scope, so that plans can call
`agent.look()` and `agent.forward()`.
To use a skill as a context parameter instead, as `MessagingSkill` is, add top-level functions such as
`context(skill: VacuumSkill) suspend fun Agent.forward() = with(skill) { forward() }`.

## Turn readings into beliefs

`handlesPerceptionEvents` maps each reading to beliefs, replacing the previous readings:

```kotlin
handlesPerceptionEvents { if (it is VacuumPerception) handleVacuumPerception(it, beliefs) else null }
```

Here `handleVacuumPerception` turns the reading into `location/2`, `direction/1`, `time/1` and `square/2` facts,
plus the `dust_at/2` memory of the dust seen, and returns an `AgentUpdate.Belief` with only what changed
(see [Turn perceptions into Prolog facts](./prolog/perceptions.md)).

:::note
Built-in skills such as `MessagingSkill(node)` expect a `Node<Any>`. On a node with a custom body type, define your
own skills against `Node<YourBody>`, as above.
:::

For an example with two skills (movement and battery recharging) sharing the same body, see
[`TestSpatialRobot`](https://github.com/jakta-bdi/jakta/blob/main/jakta-core/src/commonTest/kotlin/it/unibo/jakta/dsl/examples/TestSpatialRobot.kt).
