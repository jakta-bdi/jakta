# Embodiment

Agents are situated in an environment, and many are **embodied** in it: they have a physical or simulated presence,
such as a robot in a room, through which they perceive and act. Behaviour-based robotics argued that intelligence
emerges from this interaction with the world rather than from abstract reasoning alone[^brooks], and agent platforms
connected to an environment commonly keep the agent apart from the **entity** it controls in it[^eis]. The agent's
mind deliberates; its body is what the environment sees of the agent, and what the agent senses and acts through.

## Bodies in JaKtA

Each agent has a **body**: its representation inside the [node](../nodes.md). The body owns what belongs to the
agent in the world, such as its position, the direction it faces, or its sensors and actuators, while the agent's
mind — its [beliefs](../basic-concepts/beliefs.md), [goals](../basic-concepts/goals.md) and
[plans](../basic-concepts/plans.md) — stays apart from it.

The separation matters because the two can disagree: the body holds where the robot *is*, the beliefs where the
robot *thinks* it is, and the agent only learns about its body by perceiving it. It also lets the same mind be
embodied in different ways, e.g. a simulated robot in tests and a real one in production.

The body type is the third type parameter of `agent<Belief, Goal, Body>`, and of the node builder,
`NodeBuilders.baseNode<Body>()`. `embodiedAs { id -> ... }` gives the agent its body when it joins the node.
Agents that need no body use `Any`, with `embodiedAs { Any() }`, as in the tutorials.

## What a body is for

- **Acting**: a [skill](./acting.md) finds the body of the acting agent and acts through it, e.g. moving the robot it
  belongs to.
- **Perceiving**: `publishEvent(perception) { body -> ... }` delivers a [perception](./perception.md) only to the agents
  whose body passes the filter, e.g. only to the robot that made a sensor reading.
- **Being addressed**: [messages](./communication.md) reach their receiver through its body.
- **Being perceived**: a body can expose data about its agent, such as its position, to the rest of the world.
  Since `node.agents` maps every agent to its body, a skill or the external environment can read the bodies of the
  other agents and publish what it finds as perceptions, e.g. the robots close to the one that looks around:
  agents then perceive each other through their bodies.

:::tip
Give each agent a distinct body instance (`embodiedAs { Any() }` does), so that bodies identify agents unambiguously.
:::

:::info
The how-to [Give agents a body](../../how-to/custom-body.md) builds the body of the vacuum robot step by step, and the
[Vacuum World showcase](../../showcase/vacuum-world.md#embodiment-in-a-simulated-world) explains why the world, the
body, the skill and the agent are kept apart.
:::

[^brooks]: Rodney A. Brooks. [Intelligence without representation](https://doi.org/10.1016/0004-3702(91)90053-M). Artificial Intelligence 47(1–3), 1991.
[^eis]: Tristan M. Behrens, Koen V. Hindriks, Jürgen Dix. [Towards an environment interface standard for agent platforms](https://doi.org/10.1007/s10472-010-9215-9). Annals of Mathematics and Artificial Intelligence 61(4), 2011.
