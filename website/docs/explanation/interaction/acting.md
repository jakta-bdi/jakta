---
title: Acting and skills
sidebar_label: Acting and skills
---

# Acting and skills

Agents affect their environment through actuators[^aima]. In the [BDI](../basic-concepts/bdi.mdx) model, actions are
the primitive steps of plans: the means by which an agent pursues its goals, i.e. by which it is **proactive**
rather than only reacting to what happens[^wooldridge-jennings]. An action is an *attempt*: the environment may change
concurrently, or not comply, so an agent knows the outcome of what it did only by [perceiving](./perception.md) it.
Actions may also take time, and fail.

## Any Kotlin code is an action

JaKtA has no fixed set of actions, nor dedicated constructs for them: plan bodies are plain `suspend` Kotlin code, and
whatever they do is the agent acting. A call that reaches outside the agent — switching on a heater, moving a robot,
sending an HTTP request, writing a file — is an action on the external environment:

- an action that takes time is a `suspend` function: the plan waits for it, while the agent keeps reacting to
  everything else;
- an action that fails throws, and the plan, hence its goal, [fails](../basic-concepts/goals.md#failure) with it.

A thermostat could switch the room's heating directly, from the room it was given:

```kotlin
fun thermostat(room: Room) = agent<RoomBelief, String, Any>(BaseAgentID("thermostat")) {
    // ...
    hasPlanLibrary {
        adding.belief {
            takeIf { it.temperature < it.target - TOLERANCE }
        } triggers {
            room.switch(Mode.HEATING)
        }
    }
}
```

This works, and for a small program it may be all you need. The agent, though, is now tied to that `Room`: to run it
on a different room, a simulated one or a real one, its definition has to change.

## Skills

A **skill** is an ordinary Kotlin object whose methods are actions, made available to plan bodies through
[context parameters](https://kotlinlang.org/docs/context-parameters.html). Skills are **optional**: they add no
power to what a plan can do, but they make agent specifications easier to adapt to different execution scenarios,
e.g. running in a simulation before being deployed. A skill:

1. **groups related actions** into a capability, which agents and plans declare they depend on: a plan that calls
   `agent.sendTo(...)` only compiles where a `MessagingSkill` is in scope;
2. **separates what an action does from how**: the agent depends on the skill, and the node it runs on provides an
   implementation suited to the actual environment, simulated or real, without changing the agent;
3. **ties perception to action**: since an action is an attempt, its skill can report what changed as a
   [perception](./perception.md), and even wait until the agent received it, so that the plan goes on with
   up-to-date beliefs (as the [vacuum robots](../../showcase/vacuum-world.md#the-robots-skill) do);
4. **shares resources at the node level**: the node creates a skill once and gives it to the agents it hosts, so they
   all reach the same resource through it, be it the node itself (as `MessagingSkill` does), a shared world model, or
   a connection to an external system.

## Built-in skills

`jakta-core` provides, in `it.unibo.jakta.skills`:

| Skill | Provides |
|---|---|
| `MessagingSkill(node)` | `agent.sendTo(receiver, payload)` and `agent.broadcast(payload)`, see [Communication](./communication.md) |
| `AgentTerminationSkill` | `agent.terminate()` — stop this agent |
| `NodeTerminationSkill(node)` | `terminateNode()` — stop the whole node |

Plan bodies can also always reach the node directly (`node.terminateNode()`, `node.publishEvent(...)`).

:::warning
The built-in skills take a `Node<Any>`, so they work with nodes built with `NodeBuilders.baseNode()`, whose body type
is `Any`. For agents with a custom [body](./embodiment.md), write a skill for your node type
(see [Give agents a body](../../how-to/custom-body.md)).
:::

## Skills are given by the node

A skill acts on the world through a [node](../nodes.md): `MessagingSkill` delivers messages through the node,
`NodeTerminationSkill` stops it, and a custom skill changes the external environment the node is connected to.

JaKtA therefore encourages you to see a skill as a **requirement** of an agent, which the **node** executing the agent
satisfies. The agent states what it needs to be able to do; the node decides how it is done. The same agent can then
run on nodes that provide the skill differently, for instance a simulated world in tests and the real one in
production, without changing its definition.

A reusable agent states the skills it requires as context parameters of the function that defines it:

```kotlin
context(_: MessagingSkill)
fun ponger() = agent<ToldBelief, String, Any>(bob) {
    // plans here can call agent.sendTo(...)
}
```

The node provides them by wrapping its agents in a `context(...)` block. Every agent defined or added inside it can use
the skill:

```kotlin
node {
    context(MessagingSkill(node)) {
        withAgents(ponger())
        agent<ToldBelief, String>(alice) {
            // plans here can call agent.sendTo(...)
        }
    }
}
```

The compiler checks the requirement: `withAgents(ponger())` only compiles where a `MessagingSkill` is in scope,
so an agent cannot be added to a node that does not provide what it needs.
The [ping-pong tutorial](../../tutorials/ping-pong.md) builds a complete example this way.

<details>
<summary>Giving a skill to a single agent</summary>

An agent can also give a skill to itself, since `node` is available inside `agent { }`, or to only some of its plans:

```kotlin
agent<ToldBelief, String, Any>(bob) {
    context(MessagingSkill(node)) {
        hasPlanLibrary {
            // only these plans can call agent.sendTo(...)
        }
    }
}
```

This works, but the agent then decides on its own how the skill is implemented, and it can no longer be reused on a
node that provides the skill differently. Prefer stating the requirement and letting the node satisfy it.

</details>

## Your own skills

A skill is any class. In the [thermostat tutorial](../../tutorials/thermostat.md), the skill switches the heating and
cooling system of a room:

```kotlin
class Hvac(private val room: Room) {
    fun heat() = room.switch(Mode.HEATING)
    fun cool() = room.switch(Mode.COOLING)
    fun off() = room.switch(Mode.OFF)
}
```

The agent requires it instead of a `Room`, and its plans call it like any other object in scope:

```kotlin
context(hvac: Hvac)
fun thermostat() = agent<RoomBelief, String, Any>(BaseAgentID("thermostat")) {
    // ...
    hasPlanLibrary {
        adding.belief {
            takeIf { it.temperature < it.target - TOLERANCE }
        } triggers {
            hvac.heat()
        }
    }
}
```

and the node it runs on provides it, with `context(Hvac(room)) { withAgents(thermostat()) }`. Making `Hvac` an
interface would let a simulated room and a real heating system each provide their own implementation.

<details>
<summary>Exposing a skill as a property of plan bodies</summary>

To make a skill read naturally inside plan bodies, e.g. `blocksWorld.move(...)` as in the
[Blocks World showcase](../../showcase/blocksworld.md), expose it as an extension on `PlanScope` that requires the
skill as a context parameter:

```kotlin
context(skills: BlocksWorldSkills)
val PlanScope<*, *, *>.blocksWorld
    get() = skills
```

</details>

For a skill that acts through the agent's [body](./embodiment.md), see the
[robot's skill](../../showcase/vacuum-world.md#the-robots-skill) in the Vacuum World showcase, where each robot moves on
a grid and cleans the dust.

[^aima]: Stuart Russell, Peter Norvig. *Artificial Intelligence: A Modern Approach*. Pearson, 4th edition, 2020.
[^wooldridge-jennings]: Michael Wooldridge, Nicholas R. Jennings. [Intelligent agents: theory and practice](https://doi.org/10.1017/S0269888900008122). The Knowledge Engineering Review 10(2), 1995.
