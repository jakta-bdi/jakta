package it.unibo.jakta.docker

import it.unibo.jakta.dsl.mas.MasBuilder
import it.unibo.jakta.mqtt.MqttNetwork
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.ExecutableNode
import java.io.File
import kotlin.uuid.Uuid
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

private const val BROKER_ENV = "JAKTA_MQTT_BROKER"
private const val DEFAULT_BROKER = "tcp://localhost:1883"
private val serviceName = Regex("[a-z0-9][a-z0-9_-]*")

/**
 * Runs each node of the MAS in its own Docker container, connecting the nodes through an MQTT broker.
 *
 * Every container runs the same application, which evaluates the whole MAS and then runs only one node:
 * - without [args], the process is the launcher: it builds a Docker image from the application installed by
 *   `./gradlew installDist` (in `build/install/<appName>`), generates a docker-compose file with an MQTT broker and
 *   one service per node, runs it until all the nodes terminate, and then tears it down;
 * - with `node <id>` as [args], the process runs the node with that id,
 *   connected to the broker at the `JAKTA_MQTT_BROKER` environment variable (default `tcp://localhost:1883`).
 *
 * Since each container builds the MAS on its own, node ids (and the ids of agents receiving messages from other
 * nodes) must be set explicitly, e.g. `node(NodeID("ping")) { ... }` and `BaseAgentID("pong", "pong")`.
 *
 * @param appName the name of the application, as installed by Gradle's `installDist`.
 * @param serializers the serializers of the message payloads and filters, see [MqttNetwork].
 */
suspend fun <B : Any, N : ExecutableNode<B>> MasBuilder<N, *>.runDistributed(
    args: Array<String>,
    appName: String,
    serializers: SerializersModule = EmptySerializersModule(),
) {
    val ids = nodes.map { it.id.id }
    ids.forEach { id ->
        require(Uuid.parseOrNull(id) == null) {
            "A node has the random id $id, which changes in every container: set it with node(NodeID(\"name\")) { }"
        }
        require(serviceName.matches(id) && id != "broker") { "The node id $id is not a valid Docker service name" }
    }
    when {
        args.isEmpty() -> DockerLauncher(appName, ids).launch()

        args.size == 2 && args[0] == "node" -> {
            val node = requireNotNull(nodes.find { it.id.id == args[1] }) { "No node ${args[1]} in the MAS: $ids" }
            MqttNetwork(System.getenv(BROKER_ENV) ?: DEFAULT_BROKER, args[1], ids.toSet(), serializers).use {
                CoroutineNodeRunner<B, N>(it).run(node)
            }
        }

        else -> error("Run without arguments to launch the MAS on Docker, or with `node <id>` to run a single node")
    }
}

/**
 * Builds the Docker image of the application [appName] and runs one container per node in [nodes].
 */
private class DockerLauncher(private val appName: String, private val nodes: List<String>) {

    private val installDir = File("build/install/$appName")
    private val dockerDir = File("build/jakta-docker")
    private val image = "jakta-$appName"

    fun launch() {
        require(serviceName.matches(appName)) { "The app name $appName is not a valid Docker project name" }
        require(installDir.resolve("bin/$appName").exists()) {
            "${installDir.absolutePath} not found, install the application first with `./gradlew installDist`"
        }
        dockerDir.mkdirs()
        val dockerfile = dockerDir.resolve("Dockerfile").apply { writeText(dockerfile()) }
        val composeFile = dockerDir.resolve("docker-compose.yml").apply { writeText(compose()) }
        val compose = listOf("docker", "compose", "-f", composeFile.path, "-p", appName)
        Runtime.getRuntime().addShutdownHook(Thread { exec(compose + "down") })
        exec(listOf("docker", "build", "-t", image, "-f", dockerfile.path, installDir.path))
        exec(compose + listOf("up", "-d"))
        val logs = ProcessBuilder(compose + listOf("logs", "-f") + nodes).inheritIO().start()
        exec(compose + "wait" + nodes)
        logs.destroy()
    }

    private fun dockerfile() =
        """
        |FROM eclipse-temurin:17-jre
        |COPY . /app
        |ENTRYPOINT ["/app/bin/$appName"]
        |
        """.trimMargin()

    private fun compose() = buildString {
        appendLine("services:")
        appendLine("  broker:")
        appendLine("    image: eclipse-mosquitto:2")
        appendLine("    command: mosquitto -c /mosquitto-no-auth.conf")
        nodes.forEach { node ->
            appendLine("  $node:")
            appendLine("    image: $image")
            appendLine("    command: [\"node\", \"$node\"]")
            appendLine("    environment:")
            appendLine("      $BROKER_ENV: tcp://broker:1883")
            appendLine("    depends_on: [broker]")
        }
    }

    private fun exec(command: List<String>) {
        val exitCode = ProcessBuilder(command).inheritIO().start().waitFor()
        check(exitCode == 0) { "`${command.joinToString(" ")}` failed with exit code $exitCode" }
    }
}
