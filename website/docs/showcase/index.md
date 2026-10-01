---
sidebar_label: Overview
sidebar_position: 4
hide_table_of_contents: true
---

# Showcase

import ExampleApp from '@site/src/components/ExampleApp/ExampleApp';

These examples show JaKtA agents doing more than printing a first message. They are runnable projects
in the main repository, and each one highlights a different part of the framework.

The interactive ones use [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/): the same code
runs as a desktop app and, through Kotlin/JS, in the browser, right on this page.
Each panel shows the *agent trace*: what agents print while they reason.

## Blocks world

The [blocksworld example](https://github.com/jakta-bdi/jakta/tree/main/examples/blocksworld) is the classic
blocks-world planner, ported from Jason: an agent rearranges stacks of blocks until they match a goal configuration.
It combines Prolog inference rules, recursive sub-goals, plan selection by context, custom skills and perceptions.

Drag the blocks to set the goal (shown first) and the starting world, then let the agent work.
It stops by itself when it is done, or gives up through a failure plan.

<ExampleApp name="blocksworld" title="Blocks World" />

```bash
./gradlew :examples:blocksworld:run                     # desktop
./gradlew :examples:blocksworld:jsBrowserDevelopmentRun # browser
```

Follow the [Blocks World guide](../tutorials/blocks-world.md) for the model, agent, skills, and UI.

## Tic-tac-toe

The [tictactoe example](https://github.com/jakta-bdi/jakta/tree/main/examples/tictactoe) is driven by the board
state (`cell/3`, `turn/1` beliefs), with the environment acting as referee. Alignment, threat and fork rules, and the
plans to win or block, are generated in Kotlin for any board size. The strategy (Newell and Simon) never loses on 3×3:
a test plays the agent against every possible opponent.

The human player is an agent too, whose plan suspends until you click a cell. Lower the difficulty to make the agent
sometimes distracted, so that it plays a random cell.

<ExampleApp name="tictactoe" title="Tic-tac-toe" />

```bash
./gradlew :examples:tictactoe:run                     # desktop
./gradlew :examples:tictactoe:jsBrowserDevelopmentRun # browser
```

## Vacuum world

The [vacuum-world example](https://github.com/jakta-bdi/jakta/tree/main/examples/vacuum-world) is a robot that
keeps a map clean forever, in the style of the EIS Vacuum World. It perceives its `location`, its `direction` and
the squares around it, and acts by moving forward, turning and cleaning. It explores the least recently visited
free square, remembered with `visited/3` beliefs.

Dust respawns at a configurable rate, or you can click a square to drop some.

<ExampleApp name="vacuum-world" title="Vacuum World" />

```bash
./gradlew :examples:vacuum-world:run                     # desktop
./gradlew :examples:vacuum-world:jsBrowserDevelopmentRun # browser
```

## Console examples

| Example | What it shows | Run |
|---|---|---|
| [`hello-world`](https://github.com/jakta-bdi/jakta/tree/main/examples/hello-world) | The smallest complete application: one agent, one goal, one plan, with `jakta-core` only. See [Hello, world!](../tutorials/hello-world.md). | `./gradlew :examples:hello-world:run` |
| [`failure-handling`](https://github.com/jakta-bdi/jakta/tree/main/examples/failure-handling) | A failure plan recovering a goal that failed two levels below. | `./gradlew :examples:failure-handling:run` |
| [`contract-net`](https://github.com/jakta-bdi/jakta/tree/main/examples/contract-net) | [KQML messaging](../explanation/communication.md): `askOne`, delegating an `achieve`, and `tell`. | `./gradlew :examples:contract-net:run` |

## Simulations

The [Alchemist integration](../how-to/alchemist.md) places JaKtA nodes in simulated devices and lets agents
communicate over a simulated network while time advances under the simulator's control.

For the smaller examples that go with each guide, see the [quick start](../tutorials/quick-start.md).
