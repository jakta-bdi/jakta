# JaKtA examples

| Example | What it shows | Run |
|---|---|---|
| `hello-world` | The smallest agent: one goal, one plan (basic tutorial) | `./gradlew :examples:hello-world:run` |
| `ping-pong` | Reusable and inline agents on two nodes; messages turned into beliefs, with plain Kotlin types (intermediate tutorial) | `./gradlew :examples:ping-pong:run` |
| `thermostat` | A thermostat keeping a changing room at its target: perceptions, a skill, plans chosen by guards; drag the temperatures while it works (advanced tutorial) | desktop: `./gradlew :examples:thermostat:run`<br>web: `./gradlew :examples:thermostat:jsBrowserDevelopmentRun` |
| `blocksworld` | Recursive goals and plan selection by context; drag blocks to set the world and the goal | desktop: `./gradlew :examples:blocksworld:run`<br>web: `./gradlew :examples:blocksworld:jsBrowserDevelopmentRun` |
| `tictactoe` | Rules and plans generated in Kotlin for any board size; a strategy that never loses on 3×3; play against it or watch two agents | desktop: `./gradlew :examples:tictactoe:run`<br>web: `./gradlew :examples:tictactoe:jsBrowserDevelopmentRun` |
| `vacuum-world` | A robot that keeps a map clean forever, in the style of the EIS Vacuum World: reacting to percepts, and a rule-based memory of visited cells to explore; dust respawns or is dropped by clicking | desktop: `./gradlew :examples:vacuum-world:run`<br>web: `./gradlew :examples:vacuum-world:jsBrowserDevelopmentRun` |
| `failure-handling` | A failure plan recovering a goal that failed two levels below | `./gradlew :examples:failure-handling:run` |
| `contract-net` | KQML messaging: `askOne`, delegating an `achieve`, `tell` | `./gradlew :examples:contract-net:run` |
| `runtime-agents` | Adding and removing agents while the MAS runs | `./gradlew :examples:runtime-agents:run` |
| `wait-for-events` | Waiting for events, delays and concurrent intentions | `./gradlew :examples:wait-for-events:run` |
| `custom-incarnation` | Beliefs and goals as your own types, with trigger and guard helpers | `./gradlew :examples:custom-incarnation:run` |

The UI examples use Compose Multiplatform and show an *agent trace* with what agents print while they reason.
They run in the browser through Kotlin/JS: tuProlog does not support WebAssembly yet.

`hello-world`, `ping-pong` and `thermostat` are the programs of the website's tutorials; the console examples from
`runtime-agents` on are those of the how-to guides.
