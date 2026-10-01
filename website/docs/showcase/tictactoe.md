---
sidebar_label: Tic-tac-toe
sidebar_position: 2
hide_table_of_contents: true
---

# Tic-tac-toe

import ExampleApp from '@site/src/components/ExampleApp/ExampleApp';

The [tictactoe example](https://github.com/jakta-bdi/jakta/tree/main/examples/tictactoe) is driven by the board
state (`cell/3`, `turn/1` beliefs), with the environment acting as referee. Alignment, threat and fork rules, and the
plans to win or block, are generated in Kotlin for any board size. The strategy (Newell and Simon) never loses on 3×3:
a test plays the agent against every possible opponent.

The human player is an agent too, whose plan suspends until you click a cell. Lower the difficulty to make the agent
sometimes distracted, so that it plays a random cell.

It runs below, right in your browser: the *agent trace* on the right shows what the agents print while they reason.

<ExampleApp name="tictactoe" title="Tic-tac-toe" />

```bash
./gradlew :examples:tictactoe:run                     # desktop
./gradlew :examples:tictactoe:jsBrowserDevelopmentRun # browser
```
