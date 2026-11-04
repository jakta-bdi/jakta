---
sidebar_position: 4
---

# All the examples

The JaKtA repository ships runnable examples in the
[`examples`](https://github.com/jakta-bdi/jakta/tree/main/examples) folder. Clone it and run them with Gradle:

```bash
git clone https://github.com/jakta-bdi/jakta.git
cd jakta
./gradlew :examples:hello-world:run
```

| Example | Command | What it shows | Read more |
|---|---|---|---|
| [`hello-world`](https://github.com/jakta-bdi/jakta/tree/main/examples/hello-world) | `./gradlew :examples:hello-world:run` | A single agent with one goal and one plan that prints a message and stops the node. | [Basic: Hello world](../tutorials/hello-world.md) |
| [`ping-pong`](https://github.com/jakta-bdi/jakta/tree/main/examples/ping-pong) | `./gradlew :examples:ping-pong:run` | Two agents on two nodes exchanging messages turned into beliefs. | [Intermediate: Ping-pong agents](../tutorials/ping-pong.md), [Create a JaKtA Multi-Agent System](../how-to/create-mas.md) |
| [`thermostat`](https://github.com/jakta-bdi/jakta/tree/main/examples/thermostat) | desktop: `./gradlew :examples:thermostat:run`<br/>browser: `./gradlew :examples:thermostat:jsBrowserDevelopmentRun` | A thermostat keeping a changing room at its target temperature through perceptions, a skill and guarded plans. | [Advanced: Thermostat](../tutorials/thermostat.md), [Connect agents to an external environment](../how-to/connect-environment.md) |
| [`blocksworld`](https://github.com/jakta-bdi/jakta/tree/main/examples/blocksworld) | desktop: `./gradlew :examples:blocksworld:run`<br/>browser: `./gradlew :examples:blocksworld:jsBrowserDevelopmentRun` | An agent building towers of blocks with Prolog inference rules, recursive sub-goals and custom skills. | [Blocks World](./blocksworld.md), [Skills](../explanation/basic-concepts/skills.md), [Beliefs](../explanation/basic-concepts/beliefs.md) |
| [`tictactoe`](https://github.com/jakta-bdi/jakta/tree/main/examples/tictactoe) | desktop: `./gradlew :examples:tictactoe:run`<br/>browser: `./gradlew :examples:tictactoe:jsBrowserDevelopmentRun` | A tic-tac-toe player whose Prolog rules and plans are generated in Kotlin for any board size. | [Tic-tac-toe](./tictactoe.md), [Turn perceptions into Prolog facts](../how-to/prolog/perceptions.md) |
| [`vacuum-world`](https://github.com/jakta-bdi/jakta/tree/main/examples/vacuum-world) | desktop: `./gradlew :examples:vacuum-world:run`<br/>browser: `./gradlew :examples:vacuum-world:jsBrowserDevelopmentRun` | One to four embodied robots that keep a map clean forever. | [Vacuum World](./vacuum-world.md), [Give agents a body](../how-to/custom-body.md), [Turn perceptions into Prolog facts](../how-to/prolog/perceptions.md) |
| [`contract-net`](https://github.com/jakta-bdi/jakta/tree/main/examples/contract-net) | `./gradlew :examples:contract-net:run` | KQML messaging with `askOne`, delegating an `achieve`, and `tell`. | [Ask and answer with KQML](../how-to/prolog/kqml.md), [KQML Messaging](../explanation/incarnations/prolog/kqml.md) |
| [`runtime-agents`](https://github.com/jakta-bdi/jakta/tree/main/examples/runtime-agents) | `./gradlew :examples:runtime-agents:run` | Adding and removing agents while the MAS runs. | [Add and remove agents at runtime](../how-to/runtime-agents.md) |
| [`wait-for-events`](https://github.com/jakta-bdi/jakta/tree/main/examples/wait-for-events) | `./gradlew :examples:wait-for-events:run` | Waiting for events, delays and concurrent intentions. | [Wait for events and use time](../how-to/wait-for-events.md) |
| [`custom-incarnation`](https://github.com/jakta-bdi/jakta/tree/main/examples/custom-incarnation) | `./gradlew :examples:custom-incarnation:run` | Beliefs as entries of a key-value memory and goals as tasks, with trigger and guard helpers. | [Write your own incarnation](../how-to/custom-incarnation.md) |
| [`failure-handling`](https://github.com/jakta-bdi/jakta/tree/main/examples/failure-handling) | `./gradlew :examples:failure-handling:run` | A failure plan recovering a goal that failed two levels below, with the Prolog incarnation. |  |

The examples with a user interface use Compose Multiplatform and run both on the desktop and in the browser (Kotlin/JS).

## Dig deeper in the unit tests

If you want to have a look, there are less polished, minimal examples in the unit tests of
[`jakta-core`](https://github.com/jakta-bdi/jakta/tree/main/jakta-core/src/commonTest/kotlin/it/unibo/jakta/dsl/examples),
[`jakta-prolog-incarnation`](https://github.com/jakta-bdi/jakta/tree/main/jakta-prolog-incarnation/src/commonTest/kotlin/it/unibo/jakta)
and [`alchemist-jakta-incarnation`](https://github.com/jakta-bdi/jakta/tree/main/alchemist-jakta-incarnation/src/test/kotlin/it/unibo/jakta/test).
