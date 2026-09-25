# JaKtA examples

| Example | What it shows | Run |
|---|---|---|
| `hello-world` | The smallest agent: one goal, one plan | `./gradlew :examples:hello-world:run` |
| `blocksworld` | Recursive goals and plan selection by context; drag blocks to set the world and the goal | desktop: `./gradlew :examples:blocksworld:run`<br>web: `./gradlew :examples:blocksworld:jsBrowserDevelopmentRun` |
| `tictactoe` | Rules and plans generated in Kotlin for any board size; a strategy that never loses on 3×3; a human player that is an agent too | desktop: `./gradlew :examples:tictactoe:run`<br>web: `./gradlew :examples:tictactoe:jsBrowserDevelopmentRun` |
| `cleaning-robots` | Port of Jason's Mars robots: two cooperating agents driven by perception, with retries of an unreliable action | desktop: `./gradlew :examples:cleaning-robots:run`<br>web: `./gradlew :examples:cleaning-robots:jsBrowserDevelopmentRun` |
| `failure-handling` | A failure plan recovering a goal that failed two levels below | `./gradlew :examples:failure-handling:run` |
| `contract-net` | KQML messaging: `askOne`, delegating an `achieve`, `tell` | `./gradlew :examples:contract-net:run` |

The UI examples use Compose Multiplatform and show an *agent trace* with what agents print while they reason.
They run in the browser through Kotlin/JS: tuProlog does not support WebAssembly yet.
