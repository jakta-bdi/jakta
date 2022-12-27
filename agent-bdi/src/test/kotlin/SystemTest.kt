import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.SingleThreadedExecutionStrategy
import io.github.anitvam.agents.bdi.environment.Environment

fun main() {
    val e = Environment.of()

    val alice = Agent.of()
    val bob = Agent.of()

    val agentSystem = Mas.of(SingleThreadedExecutionStrategy(), e, alice, bob)

    agentSystem.start()
}
