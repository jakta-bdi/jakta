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

## Read the view

- **Agents**, on the left, grouped by node. The number is how many snapshots the agent recorded.
- **Event**, **Beliefs**, **Intentions** and **Plans** of the selected agent, after the event selected in its
  **History**. An intention lists the goals it pursues, from the one it was created for to its current sub-goal.
- The buttons step through the history; `>|` goes back to following the newest event. Steps of intentions are hidden
  unless you tick *intention steps*.
- **Messages**: sender, receivers, kind and payload of each message, coloured by kind. A message nobody handled
  (yet) is greyed out. While you look at a past event, the log stops at that moment; tick *only ...'s* to see the
  messages of the selected agent only.

## Tune it

```kotlin
val inspector = MindInspector(
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

The `mind-inspector` example runs a small contract net on two nodes, a manager auctioning jobs to three workers:

```shell
./gradlew :examples:mind-inspector:run
```
