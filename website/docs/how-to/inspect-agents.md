# Inspect the minds of your agents

The `jakta-inspector` module records, for every agent of a running multi-agent system, what it has in mind after
each event it handles (beliefs, intentions with their sub-goals, plans), and every message the agents exchange.
It shows them in a Compose Multiplatform view, on the desktop and in the browser, in the spirit of Jason's
*Mind Inspector*: pick an agent, follow its mind live, or step back through its history.

## Add the module

```kotlin title="build.gradle.kts"
dependencies {
    implementation("it.unibo.jakta:jakta-inspector:<version>")
}
```

## Run the MAS with an inspector

Create a `MindInspector` and run your MAS with its runner, instead of a `CoroutineNodeRunner`:

```kotlin
val inspector = MindInspector()

mas(NodeBuilders.baseNode()) {
    // your nodes and agents
}.run(inspector.runner())
```

Nothing changes in your agents: the inspector observes their reasoning cycles and the network their nodes share.

## Show it in a desktop window

`MindInspectorWindow` opens the inspector in its own window, next to the windows of your application.
Run the agents off the UI thread:

<details>
<summary>Imports</summary>

```kotlin
import androidx.compose.ui.window.application
import it.unibo.jakta.inspector.MindInspector
import it.unibo.jakta.inspector.MindInspectorWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
```

</details>

```kotlin
fun main() {
    val inspector = MindInspector()
    CoroutineScope(Dispatchers.Default).launch { myMas().run(inspector.runner()) }
    application {
        MindInspectorWindow(inspector, onCloseRequest = ::exitApplication)
    }
}
```

## Show it in a page, or inside your own UI

`MindInspectorView` is a composable: put it in a `ComposeViewport` in the browser, or anywhere in your own layout.

```kotlin
ComposeViewport(checkNotNull(document.body)) {
    MaterialTheme { MindInspectorView(inspector) }
}
```

Compose for the web needs `html, body { height: 100%; margin: 0; }` in your `index.html`.

## Compare several runs, one tab each

An inspector records one run of a MAS. To compare several, such as two configurations, or the same MAS on coroutines
and in a simulation, give each run its own inspector, with a name, and show them together: the view has a tab for
each.

```kotlin
val small = MindInspector("3 workers")
val large = MindInspector("30 workers")
scope.launch { myMas(workers = 3).run(small.runner()) }
scope.launch { myMas(workers = 30).run(large.runner()) }

MindInspectorView(small, large) // or MindInspectorWindow(small, large, onCloseRequest = ...)
```

## Inspect an Alchemist simulation

In a simulation, time is the simulation's: pass it to the inspector as its `clock`. The Alchemist incarnation tells
`JaktaIncarnation.agentObserver` about every event its agents handle; set it once the simulation is loaded:

```kotlin
val simulation = LoadAlchemist.from(yaml).getDefault<Any, Euclidean2DPosition>()
val inspector = MindInspector("alchemist", clock = { simulation.time.toDouble().seconds })
JaktaIncarnation.agentObserver = inspector
```

The Alchemist nodes share a network of their own, which the inspector does not tap: it shows each message when an
agent handles it, so a lost message does not appear, and the agents are listed without their node.

## Read the view

- **Agents**, on the left, grouped by node. The number is how many snapshots the agent recorded.
- **Event**, **Beliefs**, **Intentions** and **Plans** of the selected agent, after the event selected in its
  **History**. An intention lists the goals it pursues, from the one it was created for to its current sub-goal.
- The buttons step through the history; `>|` goes back to following the newest event. Steps of intentions are hidden
  unless you tick *intention steps*.
- **Interaction flow**: the messages as a sequence diagram, one lifeline per agent, time flowing down. Each arrow
  goes from the sender to a receiver, labelled with the payload and coloured by kind: a broadcast is one arrow per
  receiver, a message to oneself loops back, and a message nobody handled (yet) ends in a cross. Click an arrow to
  see the mind of its receiver when it handled the message. While you look at a past event, the flow stops at that
  moment; tick *only ...'s* to see the messages of the selected agent only.

## Tune it

```kotlin
val inspector = MindInspector(
    name = "contract net", // on its tab
    clock = { simulatedTime() }, // the time of each event and message: by default, since the inspector was created
    historySize = 200, // snapshots kept per agent
    messageLogSize = 500, // messages kept
    messageKind = { payload -> (payload as? KQMLPayload)?.let { it::class.simpleName } ?: "other" },
)
```

`messageKind` names the kind of a message from its payload, which colours it: by default, the payload's class name.

The inspector keeps references to beliefs and payloads: use immutable ones (data classes, Prolog terms) to see each
past state as it was.

If you build your runner yourself, pass the inspector as its observer and let it tap the network:

```kotlin
CoroutineNodeRunner(inspector.tap(SharedMemoryNetwork()), inspector)
```

## See it working

The `mind-inspector` example runs a small contract net on two nodes, a manager auctioning jobs to three workers,
twice, with different random choices, one tab each:

```shell
./gradlew :examples:mind-inspector:run
```
