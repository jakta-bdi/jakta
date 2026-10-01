---
sidebar_label: Blocks World
sidebar_position: 1
hide_table_of_contents: true
---

# Blocks World

import ExampleApp from '@site/src/components/ExampleApp/ExampleApp';

The [blocksworld example](https://github.com/jakta-bdi/jakta/tree/main/examples/blocksworld) is the classic
blocks-world planner, ported from Jason: an agent rearranges stacks of blocks until they match a goal configuration.
It combines Prolog inference rules, recursive sub-goals, plan selection by context, custom skills and perceptions.

Drag the blocks to set the goal (shown first) and the starting world, then let the agent work.
It stops by itself when it is done, or gives up through a failure plan.

It runs below, right in your browser: the *agent trace* on the right shows what the agents print while they reason.

<ExampleApp name="blocksworld" title="Blocks World" />

```bash
./gradlew :examples:blocksworld:run                     # desktop
./gradlew :examples:blocksworld:jsBrowserDevelopmentRun # browser
```

Follow the [Blocks World guide](../tutorials/blocks-world.md) for the model, agent, skills, and UI.
