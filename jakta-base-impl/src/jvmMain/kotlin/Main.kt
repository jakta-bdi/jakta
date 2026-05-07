import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.mas.mas
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.node.CoroutineNodeRunner
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    fun <Goal : Any, Skills : Any> LocalNodeBuilder<
        Any,
        Goal,
        Skills,
        Any,
        >.simpleAgentBuilder(
        name: String,
        skills: Skills,
        block: AgentBuilder<Any, Goal, Skills, Any>.() -> Unit,
    ) {
        agent(name) {
            embodiedAs { object {} }
            withSkills { skills }
            believes {
                +"testBelief"
            }
            hasPlans {
                adding.belief {
                    this.takeIf { it == "testBelief" }
                } triggers {
                    agent.print(Thread.currentThread().toString() + "Belief added: $context")
                    delay(1000)
                    agent.print(Thread.currentThread().toString() + "dopo delay")
                }
            }
            block()
        }
    }

    launch {
        Logger.setMinSeverity(Severity.Info)
        mas(LocalNodeBuilder()) {
            node {
                val skills = object {
                    val node = this@node
                }

                simpleAgentBuilder("Pippo", skills) { }
                simpleAgentBuilder("Pluto", skills) { }
            }
            node {
                val skills = object {
                    val node = this@node
                }

                simpleAgentBuilder("Paperino", skills) { }
            }
        }.run(CoroutineNodeRunner())
    }
}
