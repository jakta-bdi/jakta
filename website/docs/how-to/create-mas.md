---
sidebar_position: 1
---

# Create a JaKtA Multi-Agent System

After [setting up your project](../tutorials/index.mdx), a MAS is defined and run from a `main` function.
The complete program is the [`create-mas`](https://github.com/jakta-bdi/jakta/tree/main/examples/create-mas) example; run it with `./gradlew :examples:create-mas:run`.

```kotlin
private fun greeter(name: String) = agent<String, String, Any>(BaseAgentID(name)) {
    embodiedAs { Any() }
    hasInitialGoals { !"greet" }
    hasPlanLibrary {
        adding.goal {
            takeIf { it == "greet" }
        } triggers {
            agent.print("Hello from $name")
        }
    }
}

private val agentA = greeter("agentA")
private val agentB = greeter("agentB")

fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node {
            withAgents(agentA, agentB)
            agent<String, String>(BaseAgentID("agentC")) {
                embodiedAs { Any() }
                hasInitialGoals { !"stop" }
                hasPlanLibrary {
                    adding.goal {
                        takeIf { it == "stop" }
                    } triggers {
                        agent.print("Stopping the node")
                        node.terminateNode()
                    }
                }
            }
        }
    }.runLocally()
}
```

1. **Define agents** with `agent<Belief, Goal, Body> { ... }` (reusable, at top level) or inside a node with
   `agent(id) { ... }`. See [Beliefs](../explanation/basic-concepts/beliefs.md), [Goals](../explanation/basic-concepts/goals.md) and
   [Plans](../explanation/basic-concepts/plans.md).
2. **Group them in nodes** with `node { }`. Add several `node { }` blocks, or build nodes separately with
   `node(NodeBuilders.baseNode()) { }` and combine them with `withNodes(...)`. See [Nodes and environment](../explanation/nodes.md).
3. **Give them skills** by wrapping agents in `context(SomeSkill(node)) { ... }`. See [Skills](../explanation/basic-concepts/skills.md).
4. **Run** the MAS. `run(runner)` is a `suspend` function, so call it from a coroutine (e.g. `runBlocking`):
   - `runLocally()` runs every node in the current process;
   - `run(CoroutineNodeRunner(SharedMemoryNetwork()))` does the same, explicitly choosing the runner and the network.

The MAS runs until all its nodes terminate, e.g. when an agent calls `node.terminateNode()`.

## Logging

JaKtA logs through [Kermit](https://kermit.touchlab.co/). To silence the engine logs and only see what agents print:

```kotlin
Logger.setMinSeverity(Severity.Assert)
```

## Next steps

- [Connect agents to an environment](./connect-environment.md)
- [Give agents a body](./custom-body.md)
- [Run a MAS with multiple nodes](./multi-node.md)
- [Add and remove agents at runtime](./runtime-agents.md)
- [Wait for events and use time](./wait-for-events.md)
- [Write your own incarnation](./custom-incarnation.md)
- [Use JaKtA from Kotlin/JS](./javascript.md)
- [Simulate a MAS with Alchemist](./alchemist.md)
