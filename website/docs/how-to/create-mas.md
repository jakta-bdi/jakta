---
sidebar_position: 1
---

# Create a JaKtA Multi-Agent System

After [setting up your project](../tutorials/index.mdx), a MAS is defined and run from a `main` function.
The [`ping-pong`](https://github.com/jakta-bdi/jakta/tree/main/examples/ping-pong) example puts all the steps below
together, two agents on two nodes, and [Intermediate: Ping-pong agents](../tutorials/ping-pong.md) walks through it;
run it with `./gradlew :examples:ping-pong:run`.

## Define a reusable agent

The top-level `agent<Belief, Goal, Body>(id) { ... }` returns a factory, which can be added to any node.
A function returning it can ask for the [skills](#give-agents-skills) the agent needs as context parameters.
See [Beliefs](../explanation/basic-concepts/beliefs.md), [Goals](../explanation/basic-concepts/goals.md) and
[Plans](../explanation/basic-concepts/plans.md).

```kotlin
context(_: MessagingSkill)
private fun ponger() = agent<ToldBelief, String, Any>(BaseAgentID("Bob")) {
    embodiedAs { Any() }
    hasPlanLibrary { /* plans can call agent.sendTo(...) */ }
}
```

## Group agents in nodes

Each `node { }` block inside `mas { }` is a node. Add reusable agents with `withAgents(...)`, or define agents
inline with `agent(id) { ... }`. See [Nodes](../explanation/nodes.md).

```kotlin
mas(NodeBuilders.baseNode()) {
    node {
        context(MessagingSkill(node)) {
            withAgents(ponger())
        }
    }
    node {
        agent<ToldBelief, String>(BaseAgentID("Alice")) { /* ... */ }
    }
}
```

Nodes can also be built on their own, and combined with `withNodes(...)`:

```kotlin
val home = node(NodeBuilders.baseNode()) {
    context(MessagingSkill(node)) {
        withAgents(ponger())
    }
}
mas(NodeBuilders.baseNode()) {
    withNodes(home)
}
```

## Give agents skills

Skills are given at the node level: wrap the agents of a node in `context(SomeSkill(node)) { ... }`, both those
defined inline and those added with `withAgents`. A reusable agent asks for the skills it needs as context parameters,
like `ponger()` above. See [Skills](../explanation/basic-concepts/skills.md).

```kotlin
node {
    context(MessagingSkill(node)) {
        withAgents(ponger())
        agent<ToldBelief, String>(BaseAgentID("Alice")) { /* plans can call agent.sendTo(...) */ }
    }
}
```

## Run the MAS

`runLocally()` runs every node in the current process. It suspends, so call it from a coroutine:

```kotlin
fun main(): Unit = runBlocking {
    mas(NodeBuilders.baseNode()) { /* ... */ }.runLocally()
}
```

`run(CoroutineNodeRunner(SharedMemoryNetwork()))` does the same, choosing the runner and the network explicitly.
The MAS runs until all its nodes terminate, e.g. when an agent calls `node.terminateNode()`.

## Multiple nodes

Each node has its own agents and delivers its own perceptions, while **messages** travel across nodes through
the network that connects them:

- the runner runs every node concurrently, and `run` returns when **all** nodes have terminated.
  Each node stops independently, with `node.terminateNode()`;
- `SharedMemoryNetwork` connects nodes living in the same process. Nodes publish their system events — messages,
  agent additions and removals, shutdown requests — on the network, and every node receives them;
- `sendTo(receiver, payload)` delivers the message to the agent with that ID on whichever node it lives;
  `broadcast(payload)` reaches all the other agents, on all nodes;
- **perceptions stay local**: `node.publishEvent(perception)` only reaches the agents of that node.

To distribute nodes over a simulated network, with positions and connectivity, see
[Simulate a MAS with Alchemist](./alchemist.md).

## Logging

JaKtA logs through [Kermit](https://kermit.touchlab.co/). To silence the engine logs and only see what agents print:

```kotlin
Logger.setMinSeverity(Severity.Assert)
```

## Next steps

- [Connect agents to an external environment](./connect-environment.md)
- [Give agents a body](./custom-body.md)
- [Add and remove agents at runtime](./runtime-agents.md)
- [Wait for events and use time](./wait-for-events.md)
- [Write your own incarnation](./custom-incarnation.md)
- [Simulate a MAS with Alchemist](./alchemist.md)
