import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.executionstrategies.OneThreadPerAgent
import io.github.anitvam.agents.bdi.environment.Environment

fun main() {
    val e = Environment.of()

    val alice = Agent.of()
    val bob = Agent.of()

    val agentSystem = Mas.of(OneThreadPerAgent(), e, alice, bob)

    agentSystem.start()
}
