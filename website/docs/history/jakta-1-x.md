---
sidebar_label: JaKtA 1.x redesign
sidebar_position: 1
---

# JaKtA 1.x redesign

Starting from version 1.0.0 JaKtA was rewritten from the ground up. The BDI model remains central,
but the engine, DSL, and module structure were redesigned for Kotlin Multiplatform and more flexible
agent systems.

## What's new in JaKtA 1.x

- **Kotlin Multiplatform**: the core libraries run on the JVM, in JavaScript (browser and Node.js, also published on npm) and natively.
- **Coroutines-based engine**: every intention is a coroutine, and plan bodies are plain `suspend` Kotlin code.
- **Generic knowledge representation**: the engine does not force a belief or goal type.
  An [incarnation](../explanation/incarnations/index.md) fixes it: use Prolog terms, plain strings, or your own Kotlin types.
- **Skills instead of actions**: what an agent can do is modelled by ordinary Kotlin objects made available to plans
  through [context parameters](https://kotlinlang.org/docs/context-parameters.html). See [Skills](../explanation/basic-concepts/skills.md).
- **Nodes instead of environments**: agents live in [nodes](../explanation/nodes.md) that deliver perceptions and messages,
  and can be simulated with [Alchemist](../how-to/alchemist.md).

The 1.x DSL is not source-compatible with 0.x. For the old syntax and a concept-by-concept comparison,
see [JaKtA 0.x](/docs/jakta-0x).