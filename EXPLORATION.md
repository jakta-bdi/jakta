# Alchemist incarnation: exploration notes

Branch `feat/alchemist-communication`, cut from `origin/main` (1.1.24).
There were two iterations: v1 (communication, fixes, example, NodeRunner prototype), then v2
(generic situated skills, a reworked `AlchemistNodeRunner`, the Alchemist upgrade and GUI check).

## TL;DR

- **Generic situated skills** (v2), in the new multiplatform module `jakta-situated`: `SpatialSkill` (position,
  `moveTo`, `moveBy`, `moveTowards`), `NeighborhoodSkill` and `PropertySkill`, plus `Situated` bodies.
  There are two implementations: `InMemorySpace` for the default runner, and `AlchemistSkills` (positions, linking
  rule and molecules of the Alchemist node). Agent code written against the interfaces runs unchanged under both.
  This is shown by a test (`TestSameAgentCode`) and by the gossip example.
- **`AlchemistNodeRunner`** (v2 rework) runs a plain `mas { }` with one Alchemist node per JaKtA node:
  - nodes are placed at the initial position of their `Situated` agents;
  - each node gets its own time distribution;
  - Alchemist features are available to agents through skills;
  - there is no start-up race any more, thanks to `NodeRunner.runAll`: a small default method in `jakta-api`.
- **Neighborhood-only messaging** (v1): `messaging: neighborhood` in the YAML program. Messages then reach only agents
  on the same or neighboring Alchemist nodes. The default is still `global`.
- **Runtime rework and fixes** (v1). Events are routed through the simulation, so parallel batches no longer
  cross-talk. Fixed: agent removal crashed, runtime-added agents never ran, `terminateNode()` didn't stop agents,
  `device { node {} node {} }` built a single node, sub-second `delay`s were truncated, molecule export threw an NPE.
- **Alchemist version** (v2): already on the latest release, 43.1.5. Later git tags exist but were never published.
- **GUI** (v2): kept the Swing GUI. The newer Compose and web UIs are not usable yet (details below).
- **Example** `examples:alchemist-gossip`: a rumor floods a 10x10 grid. It runs in Alchemist headless (CSV export) or
  in the GUI, and the same agents run in memory with the default runner.

## How to run the example

```bash
# Alchemist, headless: exports the number of informed nodes over time
./gradlew :examples:alchemist-gossip:run
cat examples/alchemist-gossip/build/exports/gossip.csv

# Alchemist Swing GUI (press P to play/pause, R for real time, L to show links); informed nodes turn red
./gradlew :examples:alchemist-gossip:runGui

# the same agents without Alchemist: default runner, InMemorySpace, one node per agent on the same grid
./gradlew :examples:alchemist-gossip:runInMemory
```

Files in `examples/alchemist-gossip`:

- `src/main/kotlin/Gossip.kt`: the agent. It only uses `NeighborhoodSkill`, `PropertySkill` and `MessagingSkill`. It
  reads the `source` property, then sets `informed`, tells the rumor to each neighbor, and stops its node.
- `src/main/kotlin/AlchemistGossip.kt`: the YAML entrypoint, which installs `skills` (the Alchemist skills of the node).
- `src/main/kotlin/InMemoryGossip.kt`: the in-memory main.
- `src/main/resources/gossip.yml`, `gui.yml` (GUI override), `effects.json` (GUI effects).

In Alchemist, `source` is a molecule set through a YAML `contents` filter; in memory, it is an initial property of
the `SituatedBody`. All 100 nodes are informed by t≈33 (seeds 0/0). Two runs gave identical CSV files and logs.

**Not verified: I could not view the GUI.** No virtual display was available, and the session was unattended. I
checked that the GUI override loads headless (the `SwingGUI` monitor is built and only warns about headless mode) and
that `effects.json` deserializes with `EffectSerializationFactory`.

v1 finding, measured with v1's broadcast version of the gossip: with `global` messaging, every agent heard every
broadcast, which makes N² messages. At t=48 only 89 nodes were informed, against 100 by t=33 with `neighborhood`.
Agents handle one event per step, so duplicate messages clog their queues. The v2 agent addresses its neighbors
explicitly, so the messaging mode no longer changes its result.

## Design

### Execution model of an Alchemist node

`JaktaIncarnation.createReaction` (YAML) and `AlchemistNodeRunner` both build one `Event` reaction per Alchemist node,
with a single `JaktaStepAction`. Each firing calls `JaktaForAlchemistRuntime.step()`, which:

1. handles all the system events received since the last step;
2. runs one reasoning step (one event) of each hosted agent, after resuming the `delay`s that are due;
3. sends all the system events produced.

A message is handled at the receiver's next step. Rates are therefore reasoning steps per simulated second.
Scalar `time-distribution` values are `DiracComb` rates; typed ones (e.g. `{ type: ExponentialTime, parameters: [1] }`)
are built by Alchemist with the seeded RNG.

### Messaging (v1)

- Routing happens in `JaktaForAlchemistRuntime.route`:
  - `AgentMessage` events in `NEIGHBORHOOD` mode go to the node and its neighbors, evaluated when the message leaves
    the node;
  - everything else goes to all Alchemist nodes.
- **Unreachable recipients are dropped silently**, with a debug log of how many nodes the message reached. This matches
  Alchemist's radio semantics, and JaKtA's fire-and-forget `sendTo`. The sender also cannot know who is reachable,
  because receivers are chosen by a filter at the receiving node.
- Configuration: `program: { entrypoint: ..., messaging: neighborhood | global }`. A plain string program still means
  global. The entrypoint can also set `messaging` in Kotlin, and `AlchemistNodeRunner` takes it as a parameter.

### Situated skills (v2)

Module `jakta-situated` (common code, depends only on `jakta-api`, package `it.unibo.jakta.situated`). It follows the
`MessagingSkill` pattern: an interface with member extensions on `Agent`, plus top-level `context(skill: ...)` wrappers,
so plans write `agent.position`, `agent.moveTowards(target, step)`, `agent.neighbors` and
`agent.setProperty("informed", true)`.

| Interface | Members | `InMemorySpace` | `AlchemistSkills` |
|---|---|---|---|
| `SpatialSkill` | `position`, `moveTo`; helpers `moveBy`, `moveTowards(target, maxDistance)` | per-agent map | position of the Alchemist node; `moveNodeToPosition` |
| `NeighborhoodSkill` | `neighbors: Set<AgentID>` | agents within `range` | agents of the same and neighboring Alchemist nodes (linking rule) |
| `PropertySkill` | `property(name)`, `setProperty(name, value)` (null removes it) | per-agent map | molecule `name` of the Alchemist node |

Decisions:

- **Where**: a new small module, not `jakta-core`, because core is being changed concurrently on other branches. The
  interfaces are general, so they don't belong in the Alchemist module either.
- **Embodiment**: `Situated` bodies declare the *initial* state, `initialPosition` and `initialProperties`.
  Implementations read it when they first meet the agent, then track state themselves, so the body is never updated.
  This gives both implementations the same semantics; use the skills for current values.
  - `SituatedBody` is deliberately not a data class: bodies identify agents (`getAgentIDfromBody`), so equal positions
    must not make bodies equal.
  - Bodies stay typed `Any` at the node level (`NodeBuilders.baseNode<Any>()`). That keeps `MessagingSkill`, which
    needs a `Node<Any>`, usable.
- **One combined object, several requirements**: a skills object implements all three interfaces. `context(skills)`
  satisfies any `context(_: SpatialSkill, _: NeighborhoodSkill, ...)`, and agent code declares only what it uses.
- **Getting the skills**:
  - YAML entrypoints use `skills`, a property of `JaktaForAlchemistRuntime`;
  - a runner MAS uses `runner.skillsFor(node)`, which resolves the Alchemist node lazily, since nodes are built before
    they run;
  - in memory, `space.skillsFor(node)`.
- **Movement** is immediate (`moveTo`). To move at a speed, call `moveTowards` repeatedly with `delay`; the delay is
  simulated in Alchemist and virtual under `runTest`.
- `Coordinates` is an N-dimensional Euclidean point; `moveTowards` uses Euclidean math.

### `AlchemistNodeRunner` (v1 prototype, v2 rework)

```kotlin
val environment = Continuous2DEnvironment(JaktaIncarnation<Euclidean2DPosition>())
environment.linkingRule = ConnectWithinDistance(5.0)
environment.addTerminator(AfterTime(DoubleTime(100.0)))
val simulation = Engine(environment)
val runner = AlchemistNodeRunner<Euclidean2DPosition, BaseNode<Any>>(
    simulation,
    messaging = Messaging.NEIGHBORHOOD,
    timeDistribution = { node -> DiracComb(1.0) }, // per JaKtA node
    // position = { node -> ... }                 // optional: by default, from the node's Situated bodies
)
mas(NodeBuilders.baseNode<Any>()) {
    node {
        context(runner.skillsFor(node), MessagingSkill(node)) {
            agent<String, String> { embodiedAs { SituatedBody(Coordinates(0.0, 0.0)) } /* ... */ }
        }
    }
}.run(runner)
```

How it fixes the three v1 limitations:

1. **Unknown node count / racy `yield()`**. `jakta-api`'s `NodeRunner` gained
   `suspend fun runAll(nodes: Collection<N>)`. Its default launches `run(node)` for each node, which is exactly what
   `BaseMasBuilder.run` did, and `BaseMasBuilder.run` now delegates to it. The Alchemist runner overrides it to schedule
   the creation of every node *before* starting the engine thread, so the yield heuristic is gone. `run(node)` still
   works on its own, and joins a running simulation.
2. **No Alchemist access**. `skillsFor(node)` gives agents position, movement, neighbors and molecules through the
   generic interfaces. `JaktaForAlchemistRuntime.randomGenerator` and `alchemistNode` remain for Alchemist-specific code.
3. **One rate**. `timeDistribution: (N) -> TimeDistribution` gives each node its own distribution. The default is
   `DiracComb(1.0)`.

Mapping and other behaviour:

- One Alchemist node per JaKtA node.
- Position: `position(node)` if given. Otherwise the `initialPosition` of the first `Situated` agent of the node; if
  there is none, a clear error stops the simulation.
- `initialProperties` of `Situated` agents become molecules when the agent is added. This happens in the YAML path
  too, where positions come from the deployment instead.
- The simulation runs on a daemon thread and is terminated when all nodes have terminated. `runAll` returns then, or
  when the simulation's terminators end it, rethrowing the simulation error if there was one.

**Tests**:

- `TestAlchemistNodeRunner`: ping-pong in simulated time, including sub-second delays and the stop when all nodes have
  terminated, plus global vs. neighborhood broadcast.
- `TestSameAgentCode`: the same MAS (a walker moving towards a target until it is a neighbor, then greeting it) runs
  with `CoroutineNodeRunner` + `InMemorySpace` and with `AlchemistNodeRunner`. Both give the same outcome, and in
  Alchemist the property is checked as a molecule.

### Alchemist version and GUI (v2)

- **Version**: the latest Alchemist release is **43.1.5**, which this branch already uses. That is GitHub's "latest
  release" (2026-06-16) and the latest `it.unibo.alchemist:alchemist` on Maven Central. Git tags go up to 43.1.38
  (2026-09-30) but have no GitHub release and no Maven Central artifacts; the only exception is a stray
  `alchemist-composeui-jvm:43.1.18`. Nothing to upgrade to.
- **GUI**: kept `alchemist-swingui`'s `SwingGUI`, which upstream marks `@Deprecated("must be replaced by a web UI")`. I
  inspected the replacements' jars and sources but did not run them:
  - `alchemist-composeui` (43.1.5, and still on the 43.1.38 tag) is a placeholder window with a "Click me!" button;
  - `alchemist-web-renderer` (`WebRendererLauncher`) draws nodes as plain circles, with no molecules, effects or links.
    Its JVM artifact on Maven Central also lacks the browser client: it serves `index.html` from the classpath, and
    the file is not there.

  Neither can show the gossip.

### Other v1 changes

- `JaktaForAlchemistRuntime` exposes:
  - `randomGenerator`: the seeded simulation RNG;
  - `alchemistNode`: plain `node` is shadowed in the DSL;
  - `messaging`;
  - `skills` and `hostedAgents` (v2).
- A second JaKtA program on the same Alchemist node fails with a clear message.
- `RuntimeNodes`/`device` accept any `ExecutableNode`, so `NodeBuilders.baseNode()` works too (the gossip example uses
  it). `JaktaForAlchemistNode` is now a plain `BaseNode`.

## API changes (not marked as breaking in the commits)

- `jakta-api`: new `NodeRunner.runAll`, with a default implementation. `jakta-core`: `BaseMasBuilder.run` delegates to it.
- New module `jakta-situated`. The incarnation depends on it through `api`.
- Incarnation:
  - removed `JaktaAgentAction`, `NodeEventsAction`, the global `NodeNetwork` value and
    `JaktaForAlchemistNode.subscription`;
  - the `JaktaForAlchemistRuntime` constructor is now
    `(environment, node, randomGenerator, messaging = GLOBAL, onShutdown = {})`;
  - `DeviceBuilder(builderFactory)`;
  - `loadEntrypointFromClasspath` returns `RuntimeNodes<*>`.

  `AlchemistNodeRunner` is new on this branch: `rate` became `timeDistribution`, and `position` became optional,
  `(N) -> P`.

## Current limitations

Still open from the gap analysis (none of these were implemented):

| Limitation | Notes / path forward |
|---|---|
| Position and neighbors are not *pushed* as perceptions/beliefs | Only pull, through the skills. Pushing needs a policy (on change only: one event per step per agent would starve the agents), and a belief format (the incarnation does not fix belief types). |
| Per-agent time distributions | Per node with the runner, per Alchemist node in YAML. Per agent would mean one reaction per agent in the runtime. |
| No message latency or loss models | Messages arrive at the receiver's next step. A seeded loss probability or a delay could be applied in `route`. |
| Molecule writes don't wake dependent reactions | `JaktaStepAction` declares no outbound dependencies, so Alchemist reactions conditioned on molecules written by agents (`setProperty`) are not rescheduled. |
| Parallel `BatchEngine` safety unknown | The step declares `Context.LOCAL`, but reads neighborhoods, writes into neighbors' inboxes (thread-safe), and can now move its node. It is fine with the default `Engine`. |
| Node cloning | A cloned Alchemist node hosts no JaKtA nodes. |
| Agent crash handling | An exception outside plan bodies stops the simulation, whereas `CoroutineNodeRunner` removes the agent and logs. |
| Logging | Kermit logs to stdout and ignores Alchemist's `--verbosity`; the examples set the Kermit severity themselves. |
| Routing scalability | Non-message system events are broadcast to all Alchemist nodes (O(N) each). |
| `InMemorySpace` | Not thread-safe (use a single-threaded dispatcher such as `runBlocking`/`runTest`); neighbors are a linear scan. |
| Shared device state | Agents on the same Alchemist node share its position and molecules. The node's placement is decided by its first `Situated` agent, and in the YAML path `initialPosition` is ignored. |
| No generic clock or RNG skill | Agent code written only against the generic skills cannot read the simulated time or the seeded RNG; the gossip agent prints without a time. |
| A late `run(node)` after all the other nodes terminated fails | The simulation is already terminated. Use `runAll`, i.e. `mas.run`. |
| Untested by me | Typed Alchemist `actions:` inside a JaKtA program (should be appended, per the loader code); the runner on a YAML-loaded `Simulation` (should give exporters and monitors); the GUI on screen. |
| Website docs not updated | The newer docs live on the unmerged branch in `../jakta`. Their Alchemist caveats (delays, agent removal, fixed rates) are now outdated. |

## Open questions for the user

1. **Breaking changes**: the commits are `feat`/`fix`, not `!`. Mark them breaking (major release)? Deprecate
   `JaktaForAlchemistNode`/`alchemistNode()` in favour of `NodeBuilders.baseNode()`?
2. **Default messaging mode**: kept `global`. Should Alchemist default to `neighborhood`?
3. **Unreachable recipients** are dropped silently. Do you prefer a warning, or a delivery report in `MessagingSkill`?
4. **Step semantics**: system events are drained every step, where before it was one per step. Timings shift
   slightly.
5. **A shutdown with an error** stops the whole simulation. Would you rather only stop the node?
6. **Docs**: port the changes to the website branch?
7. **`NodeRunner.runAll`** is a small API addition in `jakta-api`/`jakta-core`. `feat/mqtt-messaging` and
   `feat/distributed-communication` also change `BaseMasBuilder`, so expect a small merge conflict there. OK, or would
   you rather expose the nodes from `MasBuilder` (as `feat/mqtt-messaging` does) and run them from there?
8. **`jakta-situated`**: is the module name and placement right, or should the interfaces move to `jakta-core` once
   the other branches land? Should it also offer a clock and an RNG skill, so that fully generic agents can be
   time-aware and reproducible?
9. **Embodiment as an initial-state descriptor** (the body is never updated), rather than live state in the body as
   in the custom-body how-to. Is that the right trade-off for portability?
10. **Example**: the gossiper stops its node after relaying, which the in-memory run needs in order to end. Is that
    fine?
11. **GUI**: keep the deprecated Swing GUI until upstream publishes a usable web/Compose UI?
12. **Keep `AlchemistNodeRunner` as public API?**
