---
sidebar_position: 3
---

# Basic: Hello world

This is the [`hello-world`](https://github.com/jakta-bdi/jakta/tree/main/examples/hello-world) example.
It only needs `jakta-core`: beliefs and goals are plain strings, matched with ordinary Kotlin code.

<details>
<summary>Imports</summary>

```kotlin
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import kotlinx.coroutines.runBlocking
```

</details>

```kotlin title="Main.kt"
val helloWorldAgent = agent<String, String, Any> {
    embodiedAs { Any() }
    hasInitialGoals {
        !"sayHello"
    }
    hasPlanLibrary {
        adding.goal {
            takeIf { it == "sayHello" }
        } triggers {
            agent.print("Hello, world!")
            node.terminateNode()
        }
    }
}

fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node { withAgents(helloWorldAgent) }
    }.runLocally()
}
```

## Step by step

### The agent

`agent<Belief, Goal, Body> { ... }` defines an agent specification. The three type parameters are:

- `String` and `String`, the belief and goal types. The engine accepts any type: an
  [incarnation](../explanation/incarnations/index.md) such as the Prolog one gives you richer ones;
- `Any`, the type of the agent **body**: the agent's embodiment in the node it lives in.
  `embodiedAs { Any() }` creates it. This agent needs no body, so any object will do.

`agent { }` at the top level returns a *factory*: the agent is created only when it is added to a node.

### Goals

`hasInitialGoals { !"sayHello" }` gives the agent the goal `sayHello` when it starts.
The `!` operator adds a goal. See [Goals](../explanation/basic-concepts/goals.md).

### Plans

`hasPlanLibrary { }` contains the agent's [plans](../explanation/basic-concepts/plans.md). A plan has:

- a **trigger**: `adding.goal { ... }` receives each new goal and returns `null` if the plan is not relevant,
  or a **context** for the rest of the plan. `takeIf { it == "sayHello" }` makes the plan relevant for `sayHello` only;
- an optional **guard**: `onlyWhen { ... }`, omitted here;
- a **body**: the `triggers { }` block, a `suspend` lambda of ordinary Kotlin code.

Inside the body, `agent` is the agent's mutable state (`print`, `believe`, `achieve`, ...),
and `node` is the node the agent lives in: `node.terminateNode()` stops it.

### Running the MAS

`mas(NodeBuilders.baseNode()) { node { withAgents(helloWorldAgent) } }` describes a multi-agent system
with a single node containing a single agent. `.runLocally()` executes it on coroutines, with all nodes in the
same process. Since it suspends, `main` uses `runBlocking`.
`Logger.setMinSeverity(Severity.Assert)` hides the engine logs, so that only what agents print is shown
(see [Logging](../how-to/create-mas.md#logging)).

`runLocally()` is a shorthand for running the MAS with a `CoroutineNodeRunner` over a `SharedMemoryNetwork`:

```kotlin
mas(NodeBuilders.baseNode()) {
    node { withAgents(helloWorldAgent) }
}.run(CoroutineNodeRunner(SharedMemoryNetwork()))
```

Run it with:

```bash
./gradlew :examples:hello-world:run
```

Next: learn about [beliefs](../explanation/basic-concepts/beliefs.md), or continue with [Intermediate: Ping-pong agents](./ping-pong.md).
