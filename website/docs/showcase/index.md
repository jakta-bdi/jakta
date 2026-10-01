---
sidebar_label: Overview
sidebar_position: 4
---

# Showcase

These examples show JaKtA agents doing more than printing a first message. They are runnable projects
in the main repository and each one highlights a different part of the framework.

## Hello world

The [hello-world example](https://github.com/jakta-bdi/jakta/tree/main/examples/hello-world) is the smallest
complete JaKtA application: one Prolog agent, one goal, and one plan.

Run it with:

```bash
./gradlew :examples:hello-world:run
```

The [Hello World guide](../tutorials/hello-world.md) explains the example line by line.

## Blocks world

The [blocks-world example](https://github.com/jakta-bdi/jakta/tree/main/examples/blocksworld) is a larger
planner with a Compose Desktop UI. It combines Prolog inference rules, recursive sub-goals, custom skills,
and perceptions in a world agents can change.

Run it with:

```bash
./gradlew :examples:blocksworld:run
```

Follow the [Blocks World guide](../tutorials/blocks-world.md) for the model, agent, skills, and UI.

## Simulations

The [Alchemist integration](../how-to/alchemist.md) places JaKtA nodes in simulated devices and lets agents
communicate over a simulated network while time advances under the simulator's control.

For a catalogue of smaller focused examples, see the [quick start](../tutorials/quick-start.md) and the
example suites linked there.