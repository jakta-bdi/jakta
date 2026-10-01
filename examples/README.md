# JaKtA examples

| Example | What it shows | Run |
|---|---|---|
| `hello-world` | The smallest agent: one goal, one plan | `./gradlew :examples:hello-world:run` |
| `blocksworld` | Recursive goals and plan selection by context; drag blocks to set the world and the goal | desktop: `./gradlew :examples:blocksworld:run`<br>web: `./gradlew :examples:blocksworld:jsBrowserDevelopmentRun` |
| `tictactoe` | Rules and plans generated in Kotlin for any board size; a strategy that never loses on 3×3; a human player that is an agent too | desktop: `./gradlew :examples:tictactoe:run`<br>web: `./gradlew :examples:tictactoe:jsBrowserDevelopmentRun` |
| `vacuum-world` | A robot that keeps a map clean forever, in the style of the EIS Vacuum World: reacting to percepts, and a rule-based memory of visited cells to explore; dust respawns or is dropped by clicking | desktop: `./gradlew :examples:vacuum-world:run`<br>web: `./gradlew :examples:vacuum-world:jsBrowserDevelopmentRun` |
| `failure-handling` | A failure plan recovering a goal that failed two levels below | `./gradlew :examples:failure-handling:run` |
| `contract-net` | KQML messaging: `askOne`, delegating an `achieve`, `tell` | `./gradlew :examples:contract-net:run` |
| `ping-pong` | Messages turned into beliefs, with plain Kotlin types (intermediate tutorial) | `./gradlew :examples:ping-pong:run` |
| `create-mas` | A node with reusable and inline agents | `./gradlew :examples:create-mas:run` |
| `connect-environment` | A thermostat perceiving a room and acting on it through a skill | `./gradlew :examples:connect-environment:run` |
| `custom-body` | A robot with a body moving on a grid | `./gradlew :examples:custom-body:run` |
| `multi-node` | Ping-pong between agents on two nodes | `./gradlew :examples:multi-node:run` |
| `runtime-agents` | Adding and removing agents while the MAS runs | `./gradlew :examples:runtime-agents:run` |
| `wait-for-events` | Waiting for events, delays and concurrent intentions | `./gradlew :examples:wait-for-events:run` |
| `custom-incarnation` | Beliefs and goals as your own types, with trigger and guard helpers | `./gradlew :examples:custom-incarnation:run` |

The UI examples use Compose Multiplatform and show an *agent trace* with what agents print while they reason.
They run in the browser through Kotlin/JS: tuProlog does not support WebAssembly yet.

The console examples from `ping-pong` on are the complete programs of the website's tutorials and how-to guides.
