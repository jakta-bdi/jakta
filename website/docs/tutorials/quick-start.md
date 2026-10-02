---
sidebar_position: 2
---

# Quickstart

The JaKtA repository ships runnable examples in the [`examples`](https://github.com/jakta-bdi/jakta/tree/main/examples) folder.
Clone it and run them with Gradle:

```bash
git clone https://github.com/jakta-bdi/jakta.git
cd jakta
./gradlew :examples:hello-world:run
```

## Available examples

| Example | Command | What it shows |
|---|---|---|
| [`hello-world`](https://github.com/jakta-bdi/jakta/tree/main/examples/hello-world) | `./gradlew :examples:hello-world:run` | A single agent with one goal and one plan that prints a message and stops the node, using `jakta-core` only. Walked through in [Hello world](./hello-world.md). |
| [`ping-pong`](https://github.com/jakta-bdi/jakta/tree/main/examples/ping-pong) | `./gradlew :examples:ping-pong:run` | Two agents on two nodes, one reusable and one inline, exchanging messages turned into beliefs, with plain Kotlin types. Built in the [intermediate tutorial](./ping-pong.md); the program of [Create a MAS](../how-to/create-mas.md). |
| [`thermostat`](https://github.com/jakta-bdi/jakta/tree/main/examples/thermostat) | desktop: `./gradlew :examples:thermostat:run`<br/>browser: `./gradlew :examples:thermostat:jsBrowserDevelopmentRun` | A thermostat keeping a changing room at its target temperature: an environment with its own dynamics, perceptions, a skill, and plans chosen by guards, with plain Kotlin types and a Compose Multiplatform UI. Built in the [advanced tutorial](./thermostat.md). |
| [`blocksworld`](https://github.com/jakta-bdi/jakta/tree/main/examples/blocksworld) | desktop: `./gradlew :examples:blocksworld:run`<br/>browser: `./gradlew :examples:blocksworld:jsBrowserDevelopmentRun` | The classic blocks-world planner (ported from Jason) with a Compose Multiplatform UI: Prolog inference rules, guards, recursive sub-goals, custom [skills](../explanation/basic-concepts/skills.md) and perceptions. Explained in the [Blocks World showcase](../showcase/blocksworld.md). |
| [`tictactoe`](https://github.com/jakta-bdi/jakta/tree/main/examples/tictactoe) | desktop: `./gradlew :examples:tictactoe:run`<br/>browser: `./gradlew :examples:tictactoe:jsBrowserDevelopmentRun` | A tic-tac-toe player whose Prolog rules and plans are generated in Kotlin for any board size; it never loses on 3×3. |
| [`vacuum-world`](https://github.com/jakta-bdi/jakta/tree/main/examples/vacuum-world) | desktop: `./gradlew :examples:vacuum-world:run`<br/>browser: `./gradlew :examples:vacuum-world:jsBrowserDevelopmentRun` | A robot that keeps a map clean forever, in the style of the EIS Vacuum World: reacting to percepts, and a rule-based memory of the visited squares. |
| [`failure-handling`](https://github.com/jakta-bdi/jakta/tree/main/examples/failure-handling) | `./gradlew :examples:failure-handling:run` | A failure plan recovering a goal that failed two levels below, with the Prolog incarnation. |
| [`contract-net`](https://github.com/jakta-bdi/jakta/tree/main/examples/contract-net) | `./gradlew :examples:contract-net:run` | [KQML messaging](../explanation/incarnations/prolog/kqml.md): `askOne`, delegating an `achieve`, and `tell`. |

The UI examples use Compose Multiplatform and run both on the desktop and in the browser (Kotlin/JS).

## Examples of the how-to guides

Every [how-to guide](../how-to/create-mas.md) with a complete program has a matching example:

| Example | Command | What it shows |
|---|---|---|
| [`runtime-agents`](https://github.com/jakta-bdi/jakta/tree/main/examples/runtime-agents) | `./gradlew :examples:runtime-agents:run` | Adding and removing agents while the MAS runs: [Add and remove agents at runtime](../how-to/runtime-agents.md). |
| [`wait-for-events`](https://github.com/jakta-bdi/jakta/tree/main/examples/wait-for-events) | `./gradlew :examples:wait-for-events:run` | Waiting for events, delays and concurrent intentions: [Wait for events and use time](../how-to/wait-for-events.md). |
| [`custom-incarnation`](https://github.com/jakta-bdi/jakta/tree/main/examples/custom-incarnation) | `./gradlew :examples:custom-incarnation:run` | Beliefs and goals as your own types, with trigger and guard helpers: [Write your own incarnation](../how-to/custom-incarnation.md). |

## More examples in the test suites

The test suites double as a catalogue of small, focused examples:

- [`jakta-core/src/commonTest/.../dsl/examples`](https://github.com/jakta-bdi/jakta/tree/main/jakta-core/src/commonTest/kotlin/it/unibo/jakta/dsl/examples):
  ping-pong messaging, belief addition and removal plans, plan failure, delays and concurrent intentions,
  waiting for events, custom agent bodies and skills (`TestSpatialRobot`), adding and removing agents at runtime,
  multi-node systems.
- [`jakta-prolog-incarnation/src/commonTest`](https://github.com/jakta-bdi/jakta/tree/main/jakta-prolog-incarnation/src/commonTest/kotlin/it/unibo/jakta):
  Prolog matching, guards, inference rules and [KQML messaging](../explanation/incarnations/prolog/kqml.md).
- [`alchemist-jakta-incarnation/src/test`](https://github.com/jakta-bdi/jakta/tree/main/alchemist-jakta-incarnation/src/test):
  JaKtA agents running inside [Alchemist](../how-to/alchemist.md) simulations.
