---
sidebar_label: Jason-like Kotlin Agents
sidebar_position: 3
---

# Jason-like Kotlin Agents

JaKtA started as a new implementation of [Jason](https://jason-lang.github.io/), the interpreter for an extended
version of [AgentSpeak(L)](https://link.springer.com/chapter/10.1007/BFb0031845), written in Kotlin.
The name was originally an acronym for **Jason-like Kotlin Agents**.

## JaKtA 0.x: Jason-like agents in Kotlin

[JaKtA 0.x](./jakta-0x.md) brought AgentSpeak-style agents into Kotlin, as an internal DSL instead of a separate
language. It followed Jason closely: beliefs and goals were always Prolog terms, goals were either achievement or
test goals, and agents acted on an environment through actions. The aim was to explore paradigm blending:
writing BDI agents with the same syntax and tools as ordinary object-oriented and functional Kotlin code.

## JaKtA 1.x: moving away from Jason

[JaKtA 1.x](./jakta-1-x.md) keeps the BDI model, but is no longer an AgentSpeak(L) implementation:

- **Knowledge representation is generic.** Beliefs and goals are not tied to Prolog terms: an
  [incarnation](../explanation/incarnations/index.md) chooses them, and also defines how plan triggers and guards match.
- **Plan bodies are Kotlin.** They are ordinary `suspend` functions instead of AgentSpeak body formulas,
  and test goals are replaced by guards.
- **The engine is coroutine-based.** Each intention is a coroutine, see the
  [execution model](../explanation/execution-model.md).
- **Agents live in nodes**, not in a Jason-style environment, and act through [skills](../explanation/basic-concepts/skills.md)
  instead of environment actions.

Jason is still an influence. The [Prolog incarnation](../explanation/incarnations/prolog/index.md) supports belief
sources and KQML messaging in the style of Jason, which makes porting Jason programs straightforward
(see the [Blocks World](../tutorials/blocks-world.md) tutorial). But it is one incarnation among others, and today
JaKtA is just a name rather than an acronym.
