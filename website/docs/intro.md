---
sidebar_position: 1
---

# What is JaKtA?

JaKtA is a [BDI](https://cdn.aaai.org/ICMAS/1995/ICMAS95-042.pdf) agent-oriented programming framework
for [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html).
Agents have **beliefs** about the world, **goals** to achieve, and **plans** that say how to achieve them.
You write all three with a Kotlin DSL, and plan bodies are ordinary `suspend` Kotlin code:

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
    mas(NodeBuilders.baseNode()) {
        node { withAgents(helloWorldAgent) }
    }.runLocally()
}
```

The agent starts with the goal `"sayHello"`. Adding the goal triggers the plan, which prints a message and stops
the node the agent lives in. The [Hello world tutorial](./tutorials/hello-world.md) explains it line by line.

Beliefs and goals here are plain strings, but they can be any type: an
[incarnation](./explanation/incarnations/index.md) decides what they are and how plans match them.
The [Prolog incarnation](./explanation/incarnations/prolog/index.md) makes them Prolog terms, as in Jason.

## Where to go next

- [**Tutorials**](./tutorials/index.mdx): add JaKtA to a project, then write agents in three short tutorials.
- [**Showcase**](./showcase/blocksworld.md): run example multi-agent systems in your browser.
- [**How-to guides**](./how-to/create-mas.md): recipes for specific tasks, such as
  [porting a Jason agent](./how-to/prolog/port-jason.md).
- [**Explanation**](./explanation/basic-concepts/bdi.mdx): the BDI model, incarnations, and how JaKtA runs agents.
- [**Reference**](./reference/dsl.md): the DSL, the modules, and the API docs.

Using `jakta-prolog-incarnation`? Its guides, explanations and reference have [their own section](./explanation/incarnations/prolog/index.md).

:::info[Coming from JaKtA 0.x?]
The 1.x DSL is not source-compatible with 0.x. See [JaKtA 0.x](/docs/jakta-0x) for the old version and how its
concepts map to 1.x, and the [JaKtA 1.x redesign](./history/jakta-1-x.md) for the ideas behind it.
:::

---

Source reference: [Blending BDI Agents with Object-Oriented and Functional Programming with JaKtA](https://link.springer.com/article/10.1007/s42979-024-03244-y)
