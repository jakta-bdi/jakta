---
sidebar_label: Vacuum World
sidebar_position: 3
hide_table_of_contents: true
---

# Vacuum World

import ExampleApp from '@site/src/components/ExampleApp/ExampleApp';

The [vacuum-world example](https://github.com/jakta-bdi/jakta/tree/main/examples/vacuum-world) is a robot that
keeps a map clean forever, in the style of the EIS Vacuum World. It perceives its `location`, its `direction` and
the squares around it, and acts by moving forward, turning and cleaning. It explores the least recently visited
free square, remembered with `visited/3` beliefs.

Dust respawns at a configurable rate, or you can click a square to drop some.

It runs below, right in your browser: the *agent trace* on the right shows what the agents print while they reason.

<ExampleApp name="vacuum-world" title="Vacuum World" />

```bash
./gradlew :examples:vacuum-world:run                     # desktop
./gradlew :examples:vacuum-world:jsBrowserDevelopmentRun # browser
```
