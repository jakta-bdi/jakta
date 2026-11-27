# JaKtA examples

| Example | What it shows | Run |
|---|---|---|
| `hello-world` | The smallest agent: one goal, one plan (basic tutorial) | `./gradlew :examples:hello-world:run` |
| `ping-pong` | Reusable and inline agents on two nodes; messages turned into beliefs, with plain Kotlin types (intermediate tutorial) | `./gradlew :examples:ping-pong:run` |
| `thermostat` | A thermostat keeping a changing room at its target: perceptions, a skill, plans chosen by guards; drag the temperatures while it works (advanced tutorial) | desktop: `./gradlew :examples:thermostat:run`<br>web: `./gradlew :examples:thermostat:jsBrowserDevelopmentRun` |
| `blocksworld` | Recursive goals and plan selection by context; drag blocks to set the world and the goal | desktop: `./gradlew :examples:blocksworld:run`<br>web: `./gradlew :examples:blocksworld:jsBrowserDevelopmentRun` |
| `tictactoe` | Rules and plans generated in Kotlin for any board size (n−1 in a row wins on 4×4 and 5×5); a strategy that never loses on 3×3; play against it or watch two agents | desktop: `./gradlew :examples:tictactoe:run`<br>web: `./gradlew :examples:tictactoe:jsBrowserDevelopmentRun` |
| `vacuum-world` | One to four robots that keep a map clean forever: embodied robots, timed actions, a rule-based memory of visited cells and dust seen; the world gets new dust every second, or where you click | desktop: `./gradlew :examples:vacuum-world:run`<br>web: `./gradlew :examples:vacuum-world:jsBrowserDevelopmentRun` |
| `contract-net` | A contract net in a restaurant: a waiter gives each order to the chef that can cook it the soonest, with KQML messages among agents on their own nodes; order dishes, switch stations off, and send chefs to nap, offline, while they work | desktop: `./gradlew :examples:contract-net:run`<br>web: `./gradlew :examples:contract-net:jsBrowserDevelopmentRun` |
| `runtime-agents` | Adding and removing agents while the MAS runs | `./gradlew :examples:runtime-agents:run` |
| `sub-goals` | Sub-goals, their results, concurrent intentions and recovering from a failed sub-goal | `./gradlew :examples:sub-goals:run` |
| `wait-for-events` | Waiting for an event with a timeout, and delays | `./gradlew :examples:wait-for-events:run` |
| `alchemist-ping-pong` | Alice and Bob playing ping-pong from two devices of an Alchemist simulation, in simulated time, shown in Alchemist's viewer | `./gradlew :examples:alchemist-ping-pong:run` |
| `custom-incarnation` | Plain numbers as beliefs and goals: a Collatz agent and a prime sieve | `./gradlew :examples:custom-incarnation:run` |

The UI examples use Compose Multiplatform and show an *agent trace* with what agents print while they reason.
They run in the browser through Kotlin/JS: tuProlog does not support WebAssembly yet.
`contract-net` draws its dishes with six emoji of [Noto Color Emoji](https://github.com/googlefonts/noto-emoji),
bundled under the SIL Open Font License 1.1
([`noto-emoji-OFL.txt`](contract-net/src/commonMain/composeResources/files/noto-emoji-OFL.txt)).

`hello-world`, `ping-pong` and `thermostat` are the programs of the website's tutorials; the console examples from
`runtime-agents` on are those of the how-to guides.
