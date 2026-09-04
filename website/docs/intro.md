---
sidebar_position: 1
---

# What is JaKtA?

JaKtA is a full fledged, [AgentSpeak(L)](https://link.springer.com/chapter/10.1007/BFb0031845)-compliant [BDI](https://cdn.aaai.org/ICMAS/1995/ICMAS95-042.pdf) technology. 
As such, it comes with its own BDI execution engine, giving semantics to the DSL. 
The choice of realising a fresh implementation of a BDI execution engine instead of reusing an existing one was driven by two major design goals:
1. to explore paradigm blending of AOP – and in particular BDI – with mainstream programming languages, and
2. to support modularity and pluggability of any aspect involving the execution of BDI systems—there including reasoning capabilities, message passing mechanisms, concurrency models, and the like.

Accordingly, the execution engine of JaKtA was designed and implemented from scratch to decouple agent specifications and their execution.

Architecturally, the JaKtA framework is composed by three main modules, namely:
1. the DSL module, which defines the syntax of the language;
2. the BDI interpreter, which governs the execution of agents and environments, regardless of the particular syntax used to define them; and
3. the concurrency management module, which regulates runtime, concurrency, and scheduling aspects for any system run by the BDI interpreter.


The three modules are inter-dependent in a layered way: 
the DSL module is built on top of the BDI interpreter, which in turn is built on top of the concurrency management module.
The DSL module is separate from the BDI interpreter module as it implements one possible syntax of many for BDI MAS specification. 
Other languages could be plugged in the same BDI interpreter, for instance, the Jason’s parser can be, in principle, plugged on top of JaKtA’s BDI interpreter, using the latter as engine. 
Similarly, Scala developers may design a different internal DSL and plug it on top of the existing BDI interpreter, realising in shorter time a way to write BDI agents in Scala. 

---

Source reference: [Blending BDI Agents with Object-Oriented and Functional Programming with JaKtA](https://link.springer.com/article/10.1007/s42979-024-03244-y)    