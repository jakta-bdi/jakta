# Blocks World on a youBot in Webots

The [Blocks World agent](../blocksworld), with its plans unchanged, stacks boxes with the arm of a KUKA youBot in
[Webots](https://cyberbotics.com), an open-source robot simulator with physics.
The agent perceives the stacks where the boxes physically are, so if a box falls, or you move one, its beliefs follow.

## Run it

1. Install [Webots R2025a](https://github.com/cyberbotics/webots/releases/tag/R2025a) and point `WEBOTS_HOME` at it,
   e.g. `export WEBOTS_HOME=$HOME/.local/webots` after extracting the tarball there.
   Gradle includes this example only when `WEBOTS_HOME` is set, as the Webots Java API is not on Maven.
2. Open the world: `$WEBOTS_HOME/webots examples/webots-blocksworld/worlds/blocksworld.wbt`.
   The youBot's controller is `<extern>`: the simulation waits for it.
3. Run the controller: `./gradlew :examples:webots-blocksworld:run`.
   The agent builds the tower `A,B,C`, with `A` at the bottom; pass other towers as arguments, e.g.
   `./gradlew :examples:webots-blocksworld:run --args="C,A B"`.

The controller runs on Linux; macOS and Windows need other library paths in `build.gradle.kts`.

## How it works

- `Webots` is the only thread that calls the Webots API: it steps the simulation, runs the arm motions the agent asks
  for, and perceives the world between them.
- `YouBot` ports the arm inverse kinematics and the box gripping of Webots' Tower of Hanoi contest controller; its
  constants are calibration knobs.
- `WebotsBlocksWorldSkills` implements the example's `BlocksWorldSkills`: `move` grips a box and releases it on another
  slot, then checks where it landed; the stacks come from where the boxes are, read as a Webots supervisor.
