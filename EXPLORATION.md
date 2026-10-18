# Exploration: JaKtA agents controlling robots in Webots

Branch `feat/webots`, cut from `origin/develop` at `3c1310ca`. This is the first iteration (v1).
The document is review material, to be removed before merging.

The question: can a JaKtA agent control a robot in a physics-driven simulation, and what does the bridge take?
The showcase is the Blocks World agent of `examples/blocksworld`. Its plans are unchanged, and it now drives the arm of a
KUKA youBot in [Webots](https://cyberbotics.com) to stack boxes. Its beliefs come from where the boxes physically are,
not from the effects the agent expects.

This document covers:

- what Webots is and how an external controller drives a robot;
- how the Webots step model fits JaKtA's execution model;
- the designs considered, and the one implemented;
- how to run it, the current limitations and the open questions.

## TL;DR

- **No change to `jakta-api`, `jakta-dsl` or `jakta-core`.**
  - A robot is an environment like any other: skills for the actions, and `node.publishEvent` for the percepts.
  - The only edit to existing code is in the Blocks World example. `blocksWorldNode` gains an overload that takes the
    skills from a factory, so the same agent acts on the in-memory world or on the robot.
- **Webots runs the robot's controller as an external process.**
  - The robot has `controller "<extern>"`, and a plain Gradle `run` task connects to it.
  - The Webots Java API (`Controller.jar` with its JNI libraries) is enough: no launcher, no ROS.
- **One thread owns Webots.**
  - It steps the simulation, runs the arm motions the agent asks for, and perceives the world between them.
  - The agent runs on the usual `CoroutineNodeRunner`. A `move` suspends until the motion ends, about 15 s of
    simulated time.
- **It works in Webots R2025a.** The agent reached every goal tried, from the same start (`C` on `A`, `B` alone):
  - `A,B,C`, twice: 3 moves;
  - `B,C,A`: 2 moves;
  - `C,A B`: 2 moves.
  - `move` checks where each box landed, so every move was verified physically.
  - Calibration took two fixes to the port of `hanoi.c`; see *Calibration*.

## Webots in short

- **Webots** is an open-source (Apache 2.0) robot simulator by Cyberbotics, on top of the ODE physics engine.
  - The latest release is R2025a.
  - It models many commercial robots with their sensors and actuators: e-puck, TurtleBot3, Pioneer 3-DX, TIAGo,
    youBot, Spot, Mavic, Nao, UR and ABB arms...
- **Controllers** can be written in C, C++, Python, Java or MATLAB, or connected through ROS 2 (`webots_ros2`).
  - The Java API, `com.cyberbotics.webots.controller`, is generated with SWIG from the C++ one.
  - It ships with Webots as `$WEBOTS_HOME/lib/controller/java/Controller.jar` and `libJavaController.so`. It is not
    on Maven.
  - Any JVM language can use it.
- **Extern controllers.** A robot whose `controller` field is `"<extern>"` waits for a process started outside Webots.
  - `libController` reads `WEBOTS_CONTROLLER_URL`: `ipc://1234/<robot>` locally, `tcp://<host>:1234/<robot>` remotely.
  - When the variable is unset, it connects to the only robot waiting.
  - It retries for 50 s, so the controller can start before Webots.
  - The `webots-controller` launcher only sets the environment. The example's `build.gradle.kts` does the same:
    - `LD_LIBRARY_PATH=$WEBOTS_HOME/lib/controller`
    - `-Djava.library.path=$WEBOTS_HOME/lib/controller/java`
    - `--enable-native-access=ALL-UNNAMED`
- **The step model.** `robot.step(ms)` is the only point where commands and sensor values move between the controller
  and the simulation.
  - Actuator commands are buffered until the next `step`.
  - With `synchronization TRUE` (the default), the simulation waits for each `step`. Simulated time stops while the
    controller computes, so runs are reproducible.
- **Supervisor.** A robot with `supervisor TRUE` can also read and change the whole world.
  - It can read the pose of any node with a `DEF` name, move or add nodes, reset the simulation, and draw labels.
- **Also available:**
  - Emitter and Receiver devices for messages between robots;
  - `webots --stream`, which shows the simulation in a browser;
  - recording a simulation as an HTML animation.

## The execution model

JaKtA agents are event-driven.

- `CoroutineNodeRunner` gives each agent a coroutine that waits for the next event: a perception, a message, a goal or
  an intention step.
- Plans call skills, which are plain suspending Kotlin functions.
- The environment feeds perceptions in with `node.publishEvent`, and `handlesPerceptionEvents` turns them into belief
  updates.

Webots is step-driven, and its API expects one control loop. The bridge reconciles the two with **one thread that owns
the robot** (`Webots.kt`):

```kotlin
thread(name = "webots", isDaemon = true) {
    val robot = YouBot()
    while (true) {
        val task = tasks.poll()
        if (task == null) {
            robot.step()   // the world goes on while the agent reasons
            robot.onIdle() // e.g. perceive the stacks
        } else {
            robot.task()   // a motion, which steps the simulation until it ends
        }
    }
}

suspend fun <T> run(block: YouBot.() -> T): T // queues block, suspends until it ends
```

- **Sequential motions.** A motion is plain sequential code: hover, descend, grip, lift. It calls `step()` in its loops,
  as a C controller would. No state machine.
- **Suspending skills.** `WebotsBlocksWorldSkills.move` queues the motion with `webots.run { ... }` and suspends. The
  agent stays responsive meanwhile: other intentions and events go on.
- **Perceptions from physics.** Between motions, each step reads the box poses through the supervisor. It publishes a
  `BlocksWorldPerception` when the stacks change, so a box moved by hand in Webots updates the beliefs.
  - During a motion it does not perceive: the held box is in the air.
  - `stacksFrom` maps poses to stacks: by slot, then by height. A box off every slot is in no stack.
- **Free-running timing.** Simulated time does not wait for the agent. Reasoning takes milliseconds of wall-clock time,
  while the simulation idles at whatever speed Webots runs.

## Designs considered

1. **A time-driven runner that steps the agent with the simulation.** This mirrors the Alchemist incarnation, where
   `JaktaAgentAction` calls `tryStep` at each simulation event.
   - It gives lockstep and reproducible runs: reasoning takes zero simulated time.
   - But it needs a new `NodeRunner` and a dispatcher bound to simulated time, like `AlchemistDispatcher`. A motion that
     lasts seconds would need to be split into per-step state machines.
   - Possible later; see open question 2.
2. **One thread that owns Webots, with skills that suspend until a motion ends.** *Implemented.*
   - It needs nothing new in JaKtA, and motions are sequential code.
   - The price is that the runs are not exactly reproducible: the timing of reasoning against the simulation varies.
3. **ROS 2 with `webots_ros2`.** This would make the bridge independent of the simulator, and real robots would then be
   one step away.
   - It brings in a ROS 2 installation and a JVM ROS client (e.g. `rcljava`, poorly maintained) or rosbridge.
   - Too heavy for a first exploration.
4. **Percepts from the robot's sensors only,** e.g. the Kinect camera and range finder, instead of the supervisor.
   - More realistic, but it turns the example into a vision problem.
   - The supervisor stands in for a perception module, which is a common simplification in BDI robotics.

## The example

`examples/webots-blocksworld`, a JVM module:

| File | What it does |
|---|---|
| `worlds/blocksworld.wbt` | A youBot with `controller "<extern>"` and `supervisor TRUE` at the origin, facing a platform with four slots. Three 5 cm boxes, `DEF A/B/C`, start as `C` on `A` and `B` alone. Adapted from Webots' `tower_of_hanoi.wbt`. |
| `YouBot.kt` | Arm inverse kinematics and box gripping, ported from `youbot_control/arm.c` and the Tower of Hanoi contest controller `hanoi.c`. The base stays still. Its constants are calibration knobs. |
| `Webots.kt` | The thread that owns Webots: stepping, motions on request, `onIdle` between them. |
| `WebotsBlocksWorldSkills.kt` | `BlocksWorldSkills` on the robot, plus `stacksFrom`. `move` picks the box from its slot and drops it on another, then checks where it landed and fails if it fell. `"table"` means an empty slot. |
| `Main.kt` | `blocksWorldNode(goalOf(goal)) { WebotsBlocksWorldSkills(webots, it) }`. The goal comes from the arguments, e.g. `--args="C,A B"`. |
| `StacksFromTest.kt` | Poses to stacks: by slot and height, and a fallen box left out. |

`settings.gradle.kts` includes the module only when `WEBOTS_HOME` is set. CI and machines without Webots never see it,
because `Controller.jar` is not on Maven. The flip side is that CI never compiles it.

### Calibration

The calibration ran from a temporary controller, not committed. It moved the arm directly, logged the box poses, and
saved views of the scene with the supervisor's `exportImage`.

- **The lateral sign.** `arm_ik` measures `x` to the robot's right: a positive `x` turns joint `arm1` clockwise seen
  from above. The first run picked from the mirrored slot. `gripBox` now takes metres to the left, like the world's
  `y`, and negates them.
- **`hanoi.c`'s unexplained 0.9.** `hanoi.c` scales lateral offsets by 0.9, "a small offset that I cannot explain". It
  approximates the 0.033 m between the axis of joint `arm1` and joint `arm2` (`Youbot.proto`), which `arm_ik` ignores.
  - With the scale, a box landed 5 mm off its slot.
  - The port now computes the rotation and the reach from the `arm1` axis, and boxes land within 3 mm.
- **PROTO URLs.** Some installs, e.g. the Arch `webots-bin` package, ship without the PROTO files and resolve
  `webots://` URLs to missing local paths. The world uses `https://raw.githubusercontent.com/cyberbotics/webots/R2025a/`
  URLs, which work everywhere; Webots caches what it downloads.

The rest of `hanoi.c` worked as is: the hover height, the descent speed, the gripper gap, and the friction of the
`box` contact material. Fingers closing along the arm grip a box 1 cm from its neighbours without touching them.

### Run it

```bash
export WEBOTS_HOME=/path/to/webots          # R2025a
$WEBOTS_HOME/webots examples/webots-blocksworld/worlds/blocksworld.wbt &
./gradlew :examples:webots-blocksworld:run  # builds the tower A,B,C
```

## Limitations

- **Disturbances are untested.** The tested runs had no disturbance, so the perceptions published while the arm is idle
  carried nothing new. Moving a box by hand in the Webots GUI mid-run should update the beliefs; it has not been tried.
- **Failures end the run.** When a box falls, `move` fails, the failure climbs to `start`, and the agent gives up.
  - The Blocks World plans have no recovery: they were written for a world where moves always succeed.
  - A failure plan that re-adopts the goal from the new beliefs would show replanning; see open question 3.
- **Beliefs update, but the agent does not re-check finished towers.** If you knock a tower down after the agent built
  it, the beliefs change, but the `state` goal has already moved past that tower.
- **No base navigation.** All slots are within reach of a still arm, so with three boxes and four slots the table never
  fills.
  - Driving between platforms, as `hanoi.c` does, needs `base.c` (mecanum odometry, GPS and compass).
- **Linux only.** The library path setup in `build.gradle.kts` covers Linux; macOS and Windows need other paths.
- **One robot, one JVM process.** Webots accepts one controller per extern robot. Several robots means one process per
  robot, or one JVM connecting to several robots.

## Open questions

1. **A `jakta-webots` library?**
   - What is generic is small: the owning thread, the `run` bridge and the library path setup, about 100 lines.
   - Proposal: keep it in the example until a second robot shows what is reusable. Then make a JVM module in the
     monorepo, like `alchemist-jakta-incarnation`, with `Controller.jar` as `compileOnly` and the user supplying
     Webots. A separate repository makes sense only if it has to follow Webots releases rather than JaKtA's.
2. **Lockstep or free-running?**
   - Free-running is closer to a real robot.
   - Lockstep makes experiments reproducible: step only when the agent is idle, or a runner driven by simulated time
     as in Alchemist.
   - Proposal: free-running for demos; revisit lockstep when someone wants to measure.
3. **Should robot failures be modelled in the plans?**
   - The example's plans assume moves succeed.
   - Proposal: a Webots-specific failure plan, added next to the shared plan library, that re-achieves the goal from the
     current beliefs. This is the BDI story worth showing: the plan fails, then the agent replans.
4. **Multi-robot?**
   - Two youBots, each run by an agent, sharing the boxes, or e-pucks or TurtleBots splitting deliveries through
     contract-net. Both need the base driving.
   - Proposal: after the single-robot demo works.
5. **The website.**
   - `webots --stream` serves the simulation to a browser, and a supervisor can record an HTML animation.
   - A recorded run of the agent next to its trace would make a showcase page.
