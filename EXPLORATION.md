# Alchemist incarnation: exploration notes

Branch `feat/alchemist-communication`, cut from `origin/main` (1.1.24).

## TL;DR

- **Neighborhood-only messaging**: a JaKtA program can declare `messaging: neighborhood` in the simulation file.
  Messages then reach only agents on the same Alchemist node or on its neighbors (linking rule).
  The default is still `global`.
- **Event routing through the Alchemist environment**: the JVM-wide `NodeNetwork` singleton is gone. This also fixes
  message cross-talk between simulations running in the same JVM, e.g. Alchemist batches, which run in parallel by default.
- **Runtime rework** (one `JaktaStepAction` per Alchemist node). Agents added at runtime now run, agent removal no longer
  crashes the simulation, `terminateNode()` stops its agents, and `device { node {} node {} }` builds two JaKtA nodes.
- **Bug fixes**: sub-second `delay`s were rounded down to whole seconds; molecule exporters crashed with an NPE.
- **New example** `examples:alchemist-gossip`: rumor flooding on a 10x10 grid. Runs headless with a CSV export, or in
  the Alchemist Swing GUI.
- **`AlchemistNodeRunner` prototype**: runs a normal `mas { }` inside an Alchemist simulation. It is feasible, works,
  and has tests. Its limits are listed in the NodeRunner section.

## How to run the example

```bash
# headless: prints when each agent is informed, exports the number of informed nodes over time
./gradlew :examples:alchemist-gossip:run
cat examples/alchemist-gossip/build/exports/gossip_messaging-neighborhood.csv

# Alchemist Swing GUI (press P to play/pause, R for real time, L to show links); informed nodes turn red
./gradlew :examples:alchemist-gossip:runGui

# batch: both messaging modes, in parallel in the same JVM -> two CSV files
./gradlew :examples:alchemist-gossip:run --args="run gossip.yml --verbosity error \
  --override '{launcher: {type: DefaultLauncher, parameters: [[messaging], true, true, 2]}}'"
```

Files: `examples/alchemist-gossip/src/main/kotlin/Gossip.kt` (entrypoint),
`src/main/resources/gossip.yml` (simulation), `gui.yml` (GUI override), `effects.json` (GUI effects).
The source node is marked with a `source` molecule, set through a YAML `contents` filter.
The agent reads it with `alchemistNode.contains(SOURCE)`. When an agent learns the rumor, it sets the `informed`
molecule, which feeds the exporter and the GUI, and broadcasts the rumor once.

Results with seeds 0/0 (runs are deterministic: two runs gave identical CSV files and logs):

| messaging | informed at t=18 | at t=33 | at t=48 |
|---|---|---|---|
| `neighborhood` | 61 | 100 | 100 |
| `global` | 49 | 72 | 89 |

Global messaging is *slower*. With global broadcasts, every informed agent reaches all 100 agents, which makes
N² messages. An agent handles one event per reasoning step, so these duplicate messages clog every agent's queue.
This follows from the execution model; it is not a bug.

**Not verified**: I did not open the GUI on screen (no virtual display was available, and the session was
unattended). I checked that the GUI override loads in headless mode (the `SwingGUI` monitor is built and only warns
about headless mode) and that `effects.json` deserializes with `EffectSerializationFactory`.

## What changed, and design decisions

### Execution model of a device (Alchemist node)

`JaktaIncarnation.createReaction` builds one `Event` reaction per Alchemist node, with the program's time distribution
and a single `JaktaStepAction`. Each firing calls `JaktaForAlchemistRuntime.step()`, which:

1. handles **all** system events received since the last step: messages go to the local agents' inboxes, and
   agent additions, removals and node shutdowns are applied;
2. runs **one reasoning step** (`tryStep`, one event) of every hosted agent, after resuming due `delay`s;
3. sends **all** system events the hosted nodes produced.

A message sent during a step is therefore handled by the receiver at the receiver's next step.
Before this change, each node forwarded and received at most one system event per step. That added an artificial
per-node bottleneck and made latency depend on the order of reactions. Agents still process one event per step,
so the rate of the time distribution is the number of reasoning steps per simulated second.

Scalar `time-distribution` values are still a `DiracComb` rate. Typed ones already worked and now have a documented
example: `{ type: ExponentialTime, parameters: [1] }` is used by the gossip example, and Alchemist builds it with the
seeded RNG.

### Neighborhood messaging (task 2)

- **Where**: routing happens in `JaktaForAlchemistRuntime.route`. `AgentMessage` events in `NEIGHBORHOOD` mode go to
  `environment.getNeighborhood(node).neighbors` plus the node itself. Everything else goes to all
  `environment.nodes` plus the node itself, as before. Agent additions, removals and shutdowns are always global,
  because they are addressed by id.
- **When the neighborhood is evaluated**: when the message leaves the node, at the end of the sender's step.
  After that, the message is in the receiver's buffer even if the receiver moves away.
- **Recipient not in range: the message is dropped silently.** A debug log reports how many Alchemist nodes a message
  reached. Why drop:
  1. It matches Alchemist's own semantics: out of range means not heard, as in the Protelis and ScaFi incarnations.
  2. `sendTo`/`broadcast` are fire-and-forget in JaKtA. Sending to a non-existent agent is already a silent no-op with
     `SharedMemoryNetwork`.
  3. The sender cannot know who is reachable: the receiver is chosen by a filter evaluated *at the receiving node*.
     Failing the action would mean changing the `MessagingSkill`/`Node` API to report deliveries synchronously.

  Agents that need reliable delivery can use acks and timeouts (`agent.wait(filter, timeout)`).
- **Configuration**: the YAML `program` can be a map, mirroring the unused `_pool` sketch that was in the old test
  files. A plain string still works and means `global`. YAML variables work, so the mode can be batched, as in the
  gossip example.

  ```yaml
  programs:
    - time-distribution: 1
      program:
        entrypoint: my.Simulation.entrypoint
        messaging: neighborhood   # or global (default)
  ```

  An entrypoint can also set `messaging = Messaging.NEIGHBORHOOD` in Kotlin: it runs after the YAML value is applied.
  `AlchemistNodeRunner` takes it as a constructor parameter.
- **Tests**: `TestLoadingWithAlchemist` covers the YAML path: global mode communicates out of range, neighborhood mode
  does not, and neighborhood mode communicates in range. `TestAlchemistNodeRunner` covers broadcast on an A–B–C line:
  global reaches B and C, neighborhood reaches only B.

### Other fixes and additions

- `AlchemistDispatcher`: `timeMillis / 1000` became `timeMillis / 1000.0`. Covered by the runner test, which fails
  without the fix.
- `JaktaIncarnation.getProperty` now accepts a `null` property. Alchemist exporters pass `null`, so every `export` of
  a molecule crashed. Booleans are exported as 0/1.
- `JaktaForAlchemistRuntime` now exposes:
  - `randomGenerator`: the seeded simulation RNG, for reproducible agent randomness;
  - `alchemistNode`: the Alchemist node. Inside `node { }` / `agent { }`, the name `node` refers to the JaKtA node, so
    the Alchemist one was only reachable as `this@entrypoint.node`;
  - a `messaging` var.
- Loading a second JaKtA program on the same Alchemist node now fails with a clear message. Before, its nodes were
  silently never run and the first program's agents were stepped twice.
- The bounds of `RuntimeNodes`/`device` were relaxed to any `ExecutableNode`, so `NodeBuilders.baseNode()` works too.
  `JaktaForAlchemistNode` is now a plain `BaseNode` subclass, kept for compatibility.

## Gap analysis

| Feature | Status | Notes |
|---|---|---|
| Neighbor-aware messaging | **done** | `messaging: neighborhood` (see above) |
| Isolation between simulations in one JVM (batches, tests) | **done** | Routing through the environment. The old global network leaked messages across parallel batch runs, and its `tryLock` could silently drop events under contention. |
| Agent removal (`agent.terminate()`, `node.removeAgent`) | **done** | It was `TODO()`, which crashed the simulation |
| Agents added at runtime (`node.addAgent`) | **done** | Before, they were registered but never stepped |
| `terminateNode()` stops the node's agents | **done** | Before, it only closed the subscription and agents kept running. A shutdown carrying an error stops the simulation with that error. |
| Several JaKtA nodes per device | **done** | `DeviceBuilder` reused one builder for every `node { }` |
| Sub-second delays | **done** | |
| Export molecules to Alchemist exporters and charts | **done** (fix) | Agents write molecules with `alchemistNode.setConcentration(...)`. `getProperty` handles Number, String and Boolean. |
| Deterministic seeding | **done** | Alchemist seeds drive time distributions, and `randomGenerator` is exposed to agents. The example is verified reproducible. Only UUIDs (agent and node ids) stay random; no behaviour depends on them. |
| Non-DiracComb time distributions | **already worked** | Typed `time-distribution`, now documented in the KDoc and used in the example |
| Alchemist GUI | **done** (example) | `SwingGUI` output monitor plus effects on molecules; see `runGui` |
| Run a plain `mas { }` in Alchemist | **prototype** | `AlchemistNodeRunner`, see below |
| Perceive position and neighbors as beliefs (pushed perceptions) | not done | Pull access already works from plans (`alchemistEnvironment.getPosition(alchemistNode)`, `getNeighborhood`). Pushing perceptions needs a design choice: when to emit (every step, on change) and in which belief format. The incarnation does not fix the belief type. |
| List the agents reachable in the neighborhood | not done | Useful for `sendTo` in neighborhood mode. The runtime's hosted nodes are private. A `neighborAgents(): Set<AgentID>` on the runtime would be about 5 lines; it was left out because nothing needs it yet (broadcast covers gossip). |
| Movement actions | not done (no helper) | `alchemistEnvironment.moveNodeToPosition(alchemistNode, p)` works from a plan body, because plans run on the simulation thread inside the reaction. Typed Alchemist movement reactions (e.g. `BrownianMove`) can be added in YAML as separate programs. Neither combination was tested. |
| Typed Alchemist actions or conditions inside a JaKtA program | untested | By reading the loader code, typed `actions:` are appended to the JaKtA reaction. String-based `createAction` still errors; `createCondition` is always true. |
| Per-agent time distributions | not done | All agents of a device share its reaction. Would need one reaction per agent (or per program), which changes the runtime structure. |
| Message latency and loss models | not done | Messages arrive at the receiver's next step. A delay or loss distribution could be applied in `route`. |
| Molecule writes as reaction dependencies | not done | `JaktaStepAction` declares no outbound dependencies, so Alchemist reactions conditioned on molecules written by agents are not rescheduled. Fixable by declaring dependencies (costly) or documenting it. |
| Parallel `BatchEngine` safety | not checked | The step declares `Context.LOCAL` but writes into neighbors' inboxes (thread-safe channels) and reads neighborhoods. It is fine with the default `Engine`; unknown with `BatchEngine`. |
| Node cloning (`cloneNode`) | not supported | The cloned runtime hosts no JaKtA nodes, as before |
| Agent crash handling | differs | An exception outside plan bodies, e.g. in a perception handler, stops the simulation. `CoroutineNodeRunner` removes the agent and logs instead. Fail-fast suits simulations, but it is a choice. |
| Logging | not done | JaKtA logs through Kermit to stdout and ignores Alchemist's `--verbosity`. The example calls `Logger.setMinSeverity(Warn)` in its entrypoint. A Kermit-to-SLF4J `LogWriter` would unify them. |
| Scalable routing of non-message events | ponytail | Additions, removals and shutdowns are broadcast to all nodes (O(N) each, O(N²) at load). This is fine for thousands of nodes; they could be addressed by `NodeID` instead. |
| Website docs | not done | This branch's `website/` does not have the new `explanation/incarnations/alchemist.md` and `how-to/alchemist.md` (they are on the unmerged branch checked out in `../jakta`). Their caveats about delays, agent removal and YAML-only fixed rates are now outdated. |

## Alchemist as a `NodeRunner` (task 5)

**Verdict: feasible, prototyped** in
`alchemist-jakta-incarnation/src/main/kotlin/it/unibo/alchemist/jakta/AlchemistNodeRunner.kt`, tested in
`TestAlchemistNodeRunner`.

```kotlin
val environment = Continuous2DEnvironment(JaktaIncarnation<Euclidean2DPosition>())
environment.linkingRule = ConnectWithinDistance(5.0)
environment.addTerminator(AfterTime(DoubleTime(100.0)))
val simulation = Engine(environment) // or LoadAlchemist.from(yaml).getDefault() to get exporters, GUI monitors, ...
val runner = AlchemistNodeRunner<Euclidean2DPosition, BaseNode<Any>>(simulation, Messaging.NEIGHBORHOOD, rate = 10.0) {
    environment.makePosition(4.0 * it, 0.0) // position of the it-th node
}
mas(NodeBuilders.baseNode<Any>()) { node { /* agents */ } node { /* agents */ } }.run(runner)
```

How the `NodeRunner` contract maps onto Alchemist:

- **`run(node)`** registers the node and suspends until it terminates. Registration is a `simulation.schedule { }`
  command that creates a `GenericNode` with a `JaktaForAlchemistRuntime` and an `Event(DiracComb(rate))` running a
  `JaktaStepAction`, then adds it to the environment at `position(index)`. Nodes can only be added from the simulation
  thread once an `Engine` exists, hence `schedule`. `run` returns when the runtime reports the node's shutdown, or when
  the simulation finishes (an `OutputMonitor`). If the simulation failed, `run` rethrows the simulation error.
- **Time**: the same step as the YAML path. `delay` runs on simulated time through `AlchemistDispatcher`. Agent
  coroutines run on the simulation thread; the caller of `mas.run` only awaits.
- **Start and stop**: the first `run` starts the engine on a daemon thread, after a `yield()` so that the other nodes
  of the MAS register first. When all nodes have terminated, the runner terminates the simulation; otherwise the
  simulation's terminators end it.
- **`nodes`**: the nodes currently running.

Limitations, and a path forward:

1. **Unknown number of nodes.** `MasBuilder.run` does not tell the runner how many nodes will come. Starting after a
   `yield()` works on single-threaded dispatchers (`runBlocking`, `runTest`). On multi-threaded ones, a node could
   register after all the others have already finished (marked `ponytail:` in the code). A clean fix needs the API:
   e.g. a `NodeRunner.runAll(nodes)` hook, or a start that the builder triggers.
2. **Portable agents cannot reach Alchemist.** A plain `mas { }` has no `JaktaForAlchemistRuntime` receiver: no
   molecules, positions or RNG. Path forward: a `Skill` resolved through the runner (JaKtA `NodeID` → Alchemist node).
   Nodes are built before `run`, so the lookup has to be lazy.
3. **One rate for all nodes**, and positions given by index. Enough for a prototype. A `(N) -> TimeDistribution` and a
   `Deployment` could replace them.
4. **YAML features**: variables and batches need the YAML loader. Passing a YAML-loaded `Simulation` with no
   deployments gives exporters, GUI monitors, seeds, linking rule and terminators. By reading the code this should
   work, but I did not test it.
5. `NodeRunner<N>` is invariant, so the runner is generic in `N`
   (`AlchemistNodeRunner<Euclidean2DPosition, BaseNode<Any>>`).

## API changes (not marked as breaking in the commits)

- Removed: `JaktaAgentAction`, `NodeEventsAction` (replaced by `JaktaStepAction`), the global
  `it.unibo.jakta.node.NodeNetwork` value of the incarnation module, and `JaktaForAlchemistNode.subscription`.
- `JaktaForAlchemistRuntime` constructor: `(environment, node, randomGenerator, messaging = GLOBAL, onShutdown = {})`.
  `randomGenerator` is required.
- `DeviceBuilder(builderFactory: () -> NB)`, previously `DeviceBuilder(builder: NB)`.
  `loadEntrypointFromClasspath` returns `RuntimeNodes<*>`.

## Open questions for the user

1. **Breaking changes**: the commits are `feat`/`fix`, not `!`. Should they be marked breaking (major release), or are
   these classes internal enough to count as a minor release? Should `JaktaForAlchemistNode`/`alchemistNode()` be
   deprecated in favour of `NodeBuilders.baseNode()`?
2. **Default messaging mode**: I kept `global` for compatibility. For Alchemist users, `neighborhood` is arguably the
   natural default.
3. **Unreachable recipients are dropped silently.** Would you rather have a warning, or a delivery report in the
   `MessagingSkill` API?
4. **Step semantics**: system events are now drained on every step, where before it was one per step. Timings of
   existing simulations shift slightly. Is that acceptable?
5. **A shutdown with an error stops the whole simulation.** Is that right, or should only the node stop, with the error
   logged?
6. **Docs**: should I port the changes to the website docs? They live on the branch in `../jakta`, which this branch
   does not include.
7. **`AlchemistNodeRunner`**: keep it as a public API, or leave it as a prototype? If kept, which of limitations 1–3
   matter to you?
8. `examples:alchemist-gossip` depends on `alchemist-swingui`, which is deprecated upstream ("must be replaced by a
   web UI"). Is that acceptable for an example?
