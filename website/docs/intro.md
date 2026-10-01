---
sidebar_position: 1
---

# What is JaKtA?

JaKtA is a full fledged, [AgentSpeak(L)](https://link.springer.com/chapter/10.1007/BFb0031845)-compliant [BDI](https://cdn.aaai.org/ICMAS/1995/ICMAS95-042.pdf) technology
for [Kotlin](https://kotlinlang.org). It comes with its own BDI execution engine and an internal Kotlin DSL
to write agents, their beliefs, goals and plans, side by side with ordinary Kotlin code.

JaKtA 1.x is a multiplatform, coroutine-based BDI framework with pluggable knowledge representations,
skills, and nodes for connecting agents to their surroundings. Start with the
[Getting Started](./tutorials/index.mdx) guide, explore the [Showcase](./showcase/index.md),
or read the [JaKtA 1.x redesign](./history/jakta-1-x.md)
for the main ideas behind the current architecture.

The choice of realising a fresh implementation of a BDI execution engine instead of reusing an existing one was driven by two major design goals:
1. to explore paradigm blending of AOP – and in particular BDI – with mainstream programming languages, and
2. to support modularity and pluggability of any aspect involving the execution of BDI systems—there including reasoning capabilities, message passing mechanisms, concurrency models, and the like.

:::info[Coming from JaKtA 0.x?]
The 1.x DSL is not source-compatible with 0.x: `mas { ... }.start()`, `environment { }`, `actions { }`,
`+achieve(...) onlyIf { } then { }` and friends are gone. The pages in this documentation describe the new API only;
see [JaKtA 0.x](/docs/jakta-0x) for the old version and how its concepts map to 1.x.
:::

## Architecture

JaKtA is split into a set of modules, described in detail in [Modules](./reference/modules.md):

| Module | Role |
|---|---|
| `jakta-api` | The representation-agnostic contracts: agents, events, plans, nodes. |
| `jakta-dsl` | The builder interfaces that make up the DSL. |
| `jakta-core` | The reference implementation of the engine and the DSL entry points (`mas`, `node`, `agent`, `plans`). |
| `jakta-prolog-incarnation` | Beliefs and goals as [2P-Kt](https://github.com/tuProlog/2p-kt) Prolog terms, with unification and KQML messaging. |
| `jakta-string-incarnation` | Beliefs and goals as plain strings — the smallest possible incarnation. |
| `alchemist-jakta-incarnation` | Runs JaKtA nodes inside the [Alchemist](https://alchemistsimulator.github.io/) simulator. |

The DSL is separate from the engine, since it implements one possible syntax of many for BDI MAS specification:
other languages could be plugged on top of the same engine.

---

Source reference: [Blending BDI Agents with Object-Oriented and Functional Programming with JaKtA](https://link.springer.com/article/10.1007/s42979-024-03244-y)
