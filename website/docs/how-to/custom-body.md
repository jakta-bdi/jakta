---
sidebar_position: 3
---

# Give agents a body

Every agent has a **body**: its representation inside the node, through which it is situated in the external environment.
The body owns what belongs to the agent in the world, such as its position, and its sensors and actuators.
Skills act through it, and perceptions can be delivered to specific bodies only.
Agents that don't need one use `Any`.

The snippets below come from the robot of the
[`vacuum-world`](https://github.com/jakta-bdi/jakta/tree/main/examples/vacuum-world) example
([`VacuumBody.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/vacuum-world/src/commonMain/kotlin/VacuumBody.kt), [`VacuumPerception.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/vacuum-world/src/commonMain/kotlin/VacuumPerception.kt)
and [`VacuumSkill.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/vacuum-world/src/commonMain/kotlin/VacuumSkill.kt)),
which you can also [try in the browser](../showcase/vacuum-world.md).

## Define the body type

The body keeps the robot's own state, separate from the external environment it is placed in:

```kotlin
data class RobotState(val position: Pos, val facing: Direction, val cleaned: Int = 0)

class VacuumBody(private val world: VacuumWorld, start: Pos, facing: Direction) {
    private val mutableState = MutableStateFlow(RobotState(start, facing))
    private val busy = Mutex()
    val state: StateFlow<RobotState> = mutableState.asStateFlow()
    // ...
}
```

Exposing the state as a `StateFlow` lets a UI draw the robot from its body.

## Give it actuators

Actions change the body, and the world only where the robot affects it. In the example an action takes time: it is
a `suspend` function that returns when the robot is done, and a mutex makes the body safe to share, as two actions
never overlap:

```kotlin
suspend fun clean() = busy.withLock {
    world.passTime(TIME_TO_CLEAN) // a delay, scaled by the world's speed
    val removed = world.removeDust(state.value.position)
    mutableState.update { it.copy(cleaned = it.cleaned + if (removed) 1 else 0) }
}
```

A simpler body can just change its state at once, as soon as it is asked.

## Give it sensors

A perception is what the sensors read, not the whole world:

```kotlin
data class VacuumPerception(val position: Pos, val facing: Direction, val squares: Map<Square, Content>) : Perception
```

`sense()` reads it from the robot's state and the cells around it.

## Use it in the node and in the agent

The body type is fixed by the node builder, and `embodiedAs` gives the agent its body:

```kotlin
node(NodeBuilders.baseNode<VacuumBody>()) {
    with(VacuumSkill(node)) {
        bodies.forEachIndexed { index, body ->
            agent<PrologBelief, PrologGoal>(BaseAgentID(RobotColour.entries[index].name.lowercase())) {
                embodiedAs { body }
                // ...
            }
        }
    }
}
```

`embodiedAs` receives the agent's ID, so it can also create a new body for each agent.
`node.agents` maps every agent ID to its body: `node.agents.getValue(agent.id)`.

## Act through the body with a skill

A skill finds the body of the acting agent and acts through it. Here each action, once the body is done, publishes
what the robot sees then on the node, delivering it only to that body with the body filter of `publishEvent`, and
waits until the agent received it, so that the plan goes on with what the robot sees once the action is over:

```kotlin
// the type of `agent` in a plan body
typealias VacuumAgent = MutableAgentState<PrologBelief, PrologGoal>

class VacuumSkill(node: Node<VacuumBody>) : Skill<VacuumBody>(node) {
    private val VacuumAgent.body get() = node.agents.getValue(id)

    suspend fun VacuumAgent.clean() {
        body.clean()
        report()
    }

    suspend fun VacuumAgent.switchOn() = report()

    private suspend fun VacuumAgent.report() {
        val body = body
        val reading = body.sense()
        node.publishEvent(reading) { it === body }
        wait({ event: AgentEvent -> event.takeIf { it === reading } })
    }
    // forward(), turnLeft() and turnRight() alike
}
```

In this example the robot perceives only at the end of its actions, and its first plan calls `switchOn` to get a
first reading. A body whose sensors work on their own can instead publish a reading whenever it changes, from a
coroutine that runs beside the node; a perception published before its agent joins the node is lost.

`with(VacuumSkill(node)) { agent(...) { ... } }` brings the actions into scope, so that plans can call
`agent.switchOn()` and `agent.clean()`.
To use a skill as a context parameter instead, as `MessagingSkill` is, add top-level functions such as
`context(skill: VacuumSkill) suspend fun VacuumAgent.clean() = with(skill) { clean() }`.

## Turn readings into beliefs

`handlesPerceptionEvents` maps each reading to beliefs, replacing the previous readings:

```kotlin
handlesPerceptionEvents { if (it is VacuumPerception) handleVacuumPerception(it, beliefs) else null }
```

Here `handleVacuumPerception` turns the reading into `position/2`, `facing/1` and `square/2` facts,
plus the `dust_at/2` memory of the dust seen, and returns an `AgentUpdate.Belief` with only what changed
(see [Turn perceptions into Prolog facts](./prolog/perceptions.md)).

:::note
Built-in skills such as `MessagingSkill(node)` expect a `Node<Any>`. On a node with a custom body type, define your
own skills against `Node<YourBody>`, as above.
:::

For an example with two skills (movement and battery recharging) sharing the same body, see
[`TestSpatialRobot`](https://github.com/jakta-bdi/jakta/blob/main/jakta-core/src/commonTest/kotlin/it/unibo/jakta/dsl/examples/TestSpatialRobot.kt).
