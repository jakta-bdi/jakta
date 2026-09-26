---
sidebar_position: 5
---

# Distribute a MAS with MQTT and Docker

The `jakta-distributed` module (JVM only) runs **each node of a MAS in its own Docker container**,
connecting the nodes through an MQTT broker.
A complete example is in `examples/mqtt-ping-pong`: run it with `./gradlew :examples:mqtt-ping-pong:run`.

```kotlin
dependencies {
    implementation("it.unibo.jakta:jakta-distributed:<version>")
}
```

## One program, many containers

Every container runs **the same application**: it builds the whole MAS, and then runs only one of its nodes.
Hence, the code of the MAS (agents, plans, filters) is available everywhere,
and only data travels on the network: the messages, with their payloads and filters.

This has two consequences:

- **ids must be stable**: each container builds the MAS on its own, so random ids would differ.
  Give an explicit id to every node, and to every agent receiving messages from other nodes:

  ```kotlin
  val pong = BaseAgentID("pong", "pong") // name and id

  mas(NodeBuilders.baseNode<Any>()) {
      node(NodeID("ping")) { ... }
      node(NodeID("pong")) {
          agent<PrologBelief, PrologGoal>(pong) { ... }
      }
  }
  ```

- **payloads and filters must be serializable**, see below.

## Run it

Replace `run(...)` with `runDistributed`, passing the command line arguments and the name of the application:

```kotlin
fun main(args: Array<String>) = runBlocking {
    mas(NodeBuilders.baseNode<Any>()) {
        // ...
    }.runDistributed(args, appName = "mqtt-ping-pong", serializers = kqmlSerializersModule)
}
```

- **Without arguments**, the process is the launcher. It builds a Docker image from the application
  installed by `./gradlew installDist`, then generates `build/jakta-docker/docker-compose.yml`, with a Mosquitto broker
  and one service per node. It runs the containers until every node terminates, and then removes them.
  Make the `run` task depend on `installDist`: `tasks.named("run") { dependsOn("installDist") }`.
- **With `node <id>`**, the process runs only that node, connected to the broker at `JAKTA_MQTT_BROKER`
  (default `tcp://localhost:1883`). This is what each container does.

The nodes wait for each other before starting their agents, so no message is lost while containers start.
A container exits when its node terminates: with `node.terminateNode()`, or when its last agent is removed.

## Serialize payloads and filters

Messages are sent as JSON with [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization).
`String` payloads and the built-in filters used by `sendTo` and `broadcast` work out of the box.
Other types must be registered in a `SerializersModule`:

- **payloads** as polymorphic subclasses of `Any`: the Prolog incarnation provides `kqmlSerializersModule`
  for its KQML performatives (`tellTo`, `askOneTo`, `delegateAchieveTo`, ...);
- **filters** as polymorphic subclasses of `MessageFilter`.

A `MessageFilter` selects the agents that receive a message. The filter is evaluated by each receiving node,
against the ids and bodies of its agents. Write filters as `@Serializable` data classes:

```kotlin
@Serializable
data class Around(
    @Serializable(with = AgentIDSerializer::class) val sender: AgentID,
    val center: Position,
    val radius: Double,
) : MessageFilter<Any> {
    override fun accept(node: Node<out Any>, agent: AgentID, body: Any) =
        agent != sender && body is Position && body.distanceTo(center) <= radius
}

// in a plan
agent.send(Tell(setOf(belief { "greeting"("hello") })), Around(ping, myPosition, 10.0))

// when running the MAS
val serializers = kqmlSerializersModule +
    SerializersModule { polymorphic(MessageFilter::class) { subclass(Around::class, Around.serializer()) } }
```

## Limitations

- A message whose payload or filter cannot be serialized (e.g. a lambda filter) is only delivered to the agents
  of the sending node, and a warning is logged.
- Agents can only be added to the node of the process adding them.
- Annotations of Prolog terms are not serialized: receivers annotate the terms with `source(sender)`,
  but other annotations are lost ([#960](https://github.com/jakta-bdi/jakta/issues/960)).
- Every message reaches every node, which then applies the filter.
