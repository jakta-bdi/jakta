import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy

fun main() {
    val e = Environment.of()

    val alice = Agent.of()
    val bob = Agent.of()

    val agentSystem = Mas.of(ExecutionStrategy.oneThreadPerAgent(), e, alice, bob)

    agentSystem.start()
}
