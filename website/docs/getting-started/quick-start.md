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
| [`hello-world`](https://github.com/jakta-bdi/jakta/tree/main/examples/hello-world) | `./gradlew :examples:hello-world:run` | A single Prolog agent with one goal and one plan that prints a message and stops the node. Walked through in [Writing a simple agent](./hello-world.md). |
| [`blocksworld`](https://github.com/jakta-bdi/jakta/tree/main/examples/blocksworld) | `./gradlew :examples:blocksworld:run` | The classic blocks-world planner (ported from Jason) with a Compose Desktop UI: Prolog inference rules, guards, recursive sub-goals, custom [skills](../basic-concepts/skills.md) and perceptions. |

## More examples in the test suites

The test suites double as a catalogue of small, focused examples:

- [`jakta-core/src/commonTest/.../dsl/examples`](https://github.com/jakta-bdi/jakta/tree/main/jakta-core/src/commonTest/kotlin/it/unibo/jakta/dsl/examples):
  ping-pong messaging, belief addition and removal plans, plan failure, delays and concurrent intentions,
  waiting for events, custom agent bodies and skills (`TestSpatialRobot`), adding and removing agents at runtime,
  multi-node systems.
- [`jakta-prolog-incarnation/src/commonTest`](https://github.com/jakta-bdi/jakta/tree/main/jakta-prolog-incarnation/src/commonTest/kotlin/it/unibo/jakta):
  Prolog matching, guards, inference rules and [KQML messaging](../explanation/communication.md#kqml-messaging-prolog-incarnation).
- [`alchemist-jakta-incarnation/src/test`](https://github.com/jakta-bdi/jakta/tree/main/alchemist-jakta-incarnation/src/test):
  JaKtA agents running inside [Alchemist](../how-to/alchemist.md) simulations.
