---
sidebar_position: 2
---

# Connect agents to an external environment

JaKtA has no environment class: the external environment is **your** Kotlin model of the world.
Agents are connected to it in two directions:

- **perceiving**: the external environment publishes **perceptions** on the node, and each agent turns them into beliefs;
- **acting**: a [skill](../explanation/basic-concepts/skills.md) exposes the operations agents can perform on it.

The snippets come from the [`thermostat`](https://github.com/jakta-bdi/jakta/tree/main/examples/thermostat) example,
walked through in the [thermostat tutorial](../tutorials/thermostat.md).

## Define a perception

Any class implementing `AgentEvent.External.Perception` (from `it.unibo.jakta.event`):

```kotlin
data class TemperatureReading(val degrees: Double) : Perception
```

Keep perceptions separate from the belief type: each agent decides what a perception means to it.

## Publish it

Anything holding the node can publish, e.g. the external environment's own loop:

```kotlin
node.publishEvent(TemperatureReading(state.value.temperature))
```

To get the node from outside the DSL, build it on its own and add it to the MAS with `withNodes`:

```kotlin
val home = node(NodeBuilders.baseNode()) {
    withAgents(thermostat(Hvac(room)))
}
launch { room.simulate(home, stepTime) }
mas(NodeBuilders.baseNode()) { withNodes(home) }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
```

Inside the DSL, `node` is in scope in `node { }` blocks, agent builders and plan bodies.

## Deliver it to some agents only

`publishEvent` takes an optional filter on agent **bodies**:

```kotlin
node.publishEvent(reading) { body -> body === node.agents[agentId] }
```

This is most useful with [custom bodies](./custom-body.md), e.g. to deliver a sensor reading only to the robot that
made it. Perceptions only reach the agents of the node that publishes them; use
[messages](../explanation/communication.md) to reach other nodes.

## Turn perceptions into beliefs

Return an `AgentUpdate.Belief(additions, removals)` from `handlesPerceptionEvents`, or `null` to ignore a perception:

```kotlin
handlesPerceptionEvents { perception ->
    val room = beliefs.single()
    val updated = when (perception) {
        is TemperatureReading -> room.copy(temperature = perception.degrees)
        is TargetSet -> room.copy(target = perception.degrees)
        else -> room
    }
    if (updated == room) null else AgentUpdate.Belief(setOf(updated), setOf(room))
}
```

Beliefs can be objects of your own type: here each perception updates a field of the agent's `RoomBelief`, and the
update replaces the old object with the new one. Returning `null` when nothing changed generates events only when
something did.

## React to them

A belief addition triggers the plans whose trigger accepts the new belief and whose guard holds:

```kotlin
adding.belief {
    this
} onlyWhen {
    context.takeIf { it.temperature < it.target - TOLERANCE }
} triggers {
    hvac.heat()
}
```

## Act through a skill

A skill is an ordinary object wrapping the operations on the external environment:

```kotlin
class Hvac(private val room: Room) {
    fun heat() = room.switch(Mode.HEATING)
    fun off() = room.switch(Mode.OFF)
}
```

Pass it to the agents that use it (`thermostat(Hvac(room))`), or make it available to every agent in a block with
`context(Hvac(room)) { ... }` plus a property such as
`context(hvac: Hvac) val PlanScope<*, *, *>.hvac get() = hvac`, the way `MessagingSkill` provides `agent.sendTo(...)`.

## With the Prolog incarnation

The pattern is the same with Prolog beliefs: the perception handler builds Prolog facts and removes the stale ones.
See [Turn perceptions into Prolog facts](./prolog/perceptions.md).
