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

Replace `hello-world` with the name of any example below. The examples with a user interface use Compose Multiplatform
and also run in the browser with `./gradlew :examples:<name>:jsBrowserDevelopmentRun`.

| Example | Description |
|---|---|
| [`hello-world`](https://github.com/jakta-bdi/jakta/tree/main/examples/hello-world) | A single agent with one goal and one plan that prints a message and stops the node. See [Basic: Hello world](../tutorials/hello-world.md). |
| [`ping-pong`](https://github.com/jakta-bdi/jakta/tree/main/examples/ping-pong) | Two agents on two nodes exchanging messages turned into beliefs. See [Intermediate: Ping-pong agents](../tutorials/ping-pong.md) and [Create a JaKtA Multi-Agent System](../how-to/create-mas.md). |
| [`thermostat`](https://github.com/jakta-bdi/jakta/tree/main/examples/thermostat) (with UI) | A thermostat keeping a changing room at its target temperature through perceptions, a skill and guarded plans. See [Advanced: Thermostat](../tutorials/thermostat.md) and [Connect agents to an external environment](../how-to/connect-environment.md). |
| [`blocksworld`](https://github.com/jakta-bdi/jakta/tree/main/examples/blocksworld) (with UI) | An agent building towers of blocks with Prolog inference rules, recursive sub-goals and custom skills. See [Blocks World](./blocksworld.md), [Skills](../explanation/basic-concepts/skills.md) and [Beliefs](../explanation/basic-concepts/beliefs.md). |
| [`tictactoe`](https://github.com/jakta-bdi/jakta/tree/main/examples/tictactoe) (with UI) | A tic-tac-toe player whose Prolog rules and plans are generated in Kotlin for any board size. See [Tic-tac-toe](./tictactoe.md) and [Turn perceptions into Prolog facts](../how-to/prolog/perceptions.md). |
| [`vacuum-world`](https://github.com/jakta-bdi/jakta/tree/main/examples/vacuum-world) (with UI) | One to four embodied robots that keep a map clean forever. See [Vacuum World](./vacuum-world.md), [Give agents a body](../how-to/custom-body.md) and [Turn perceptions into Prolog facts](../how-to/prolog/perceptions.md). |
| [`contract-net`](https://github.com/jakta-bdi/jakta/tree/main/examples/contract-net) | KQML messaging with `askOne`, delegating an `achieve`, and `tell`. See [Ask and answer with KQML](../how-to/prolog/kqml.md) and [KQML Messaging](../explanation/incarnations/prolog/kqml.md). |
| [`runtime-agents`](https://github.com/jakta-bdi/jakta/tree/main/examples/runtime-agents) | Adding and removing agents while the MAS runs. See [Add and remove agents at runtime](../how-to/runtime-agents.md). |
| [`wait-for-events`](https://github.com/jakta-bdi/jakta/tree/main/examples/wait-for-events) | Waiting for events, delays and concurrent intentions. See [Wait for events and use time](../how-to/wait-for-events.md). |
| [`custom-incarnation`](https://github.com/jakta-bdi/jakta/tree/main/examples/custom-incarnation) | Beliefs as entries of a key-value memory and goals as tasks, with trigger and guard helpers. See [Write your own incarnation](../how-to/custom-incarnation.md). |
| [`failure-handling`](https://github.com/jakta-bdi/jakta/tree/main/examples/failure-handling) | A failure plan recovering a goal that failed two levels below, with the Prolog incarnation. |

## Dig deeper in the unit tests

If you want to have a look, there are less polished, minimal examples in the unit tests of
[`jakta-core`](https://github.com/jakta-bdi/jakta/tree/main/jakta-core/src/commonTest/kotlin/it/unibo/jakta/dsl/examples),
[`jakta-prolog-incarnation`](https://github.com/jakta-bdi/jakta/tree/main/jakta-prolog-incarnation/src/commonTest/kotlin/it/unibo/jakta)
and [`alchemist-jakta-incarnation`](https://github.com/jakta-bdi/jakta/tree/main/alchemist-jakta-incarnation/src/test/kotlin/it/unibo/jakta/test).
