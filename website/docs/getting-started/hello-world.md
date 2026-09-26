---
sidebar_position: 3
---

# Writing a simple agent

This is the [`hello-world`](https://github.com/jakta-bdi/jakta/tree/main/examples/hello-world) example,
using the [Prolog incarnation](../explanation/incarnations/index.md).

```kotlin title="Main.kt"
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.tuprolog.core.Atom
import kotlinx.coroutines.runBlocking

val helloGoal = Atom.of("sayHello")

val helloWorldAgent = agent<PrologBelief, PrologGoal, Any> {
    embodiedAs { Any() }
    hasInitialGoals {
        !initialGoal { helloGoal }
    }
    hasPlanLibrary {
        prologPlan {
            adding.goal {
                matchingGoal { helloGoal }
            } triggers {
                agent.print("Hello, world!")
                node.terminateNode()
            }
        }
    }
}

fun main(): Unit = runBlocking {
    mas(NodeBuilders.baseNode()) {
        node {
            withAgents(helloWorldAgent)
        }
    }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
}
```

## Step by step

### The agent

`agent<Belief, Goal, Body> { ... }` defines an agent specification. The three type parameters are:
- `PrologBelief` and `PrologGoal`, the belief and goal types fixed by the Prolog incarnation;
- `Any`, the type of the agent **body** — the agent's embodiment in the node it lives in.
  `embodiedAs { Any() }` creates it. This agent needs no body, so any object will do.

`agent { }` at the top level returns a *factory*: the agent is created only when it is added to a node.

### Goals

`hasInitialGoals { !initialGoal { helloGoal } }` gives the agent the goal `sayHello` when it starts.
The `!` operator adds a goal, like `!` in AgentSpeak. See [Goals](../basic-concepts/goals.md).

### Plans

`hasPlanLibrary { }` contains the agent's [plans](../basic-concepts/plans.md). A plan has:
- a **trigger** — `adding.goal { matchingGoal { helloGoal } }` fires when the goal `sayHello` is added;
- an optional **guard** — `onlyWhen { ... }`, omitted here;
- a **body** — the `triggers { }` block, a `suspend` lambda of ordinary Kotlin code.

`prologPlan { }` wraps plans that use Prolog variables and unification, so `matchingGoal` can bind variables
that the body can then read.

Inside the body, `agent` is the agent's mutable state (`print`, `believe`, `achieve`, ...),
and `node` is the node the agent lives in: `node.terminateNode()` stops it.

### Running the MAS

`mas(NodeBuilders.baseNode()) { node { withAgents(helloWorldAgent) } }` describes a multi-agent system
with a single node containing a single agent. `.run(CoroutineNodeRunner(SharedMemoryNetwork()))` executes it
on coroutines, with all nodes in the same process. Since `run` suspends, `main` uses `runBlocking`.

`runLocally()` is a shorthand for exactly that runner:

```kotlin
mas(NodeBuilders.baseNode()) {
    node { withAgents(helloWorldAgent) }
}.runLocally()
```

Run it with:

```bash
./gradlew :examples:hello-world:run
```

Next: learn about [beliefs](../basic-concepts/beliefs.md), or continue with the [intermediate tutorial](./tutorial.md).
