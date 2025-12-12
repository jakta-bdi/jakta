package examples

import TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.mas
import it.unibo.jakta.plan.triggers
import kotlin.test.Test
import kotlin.test.assertTrue

interface Skill<Env : Environment> {
    val environment: Env
}

class WebEnvironment : TestEnvironment() {
    fun post() = Logger.i { "WebEnvironment.post" }
    fun get() = Logger.i { "WebEnvironment.get" }
}

interface HttpSkill : Skill<WebEnvironment> {
    fun pippo()
}

class HttpSkillImpl(
    override val environment: WebEnvironment,
) : HttpSkill {
    override fun pippo() = environment.post()
}

interface SshSkill : Skill<WebEnvironment> {
    fun baudo()
}

class SshSkillImpl(
    override val environment: WebEnvironment,
) : SshSkill {
    override fun baudo() = environment.get()
}

data class MyCapabilities(
    override val environment: WebEnvironment,
) : HttpSkill by HttpSkillImpl(environment), SshSkill by SshSkillImpl(environment)

class TestSkillFeatureWithEnvironment {
    val mas = mas {
        // val env = TestEnvironment()
        val env = WebEnvironment()
        environment { env }

        with(MyCapabilities(env)) {
            agent("HelloAgent") {
                hasInitialGoals {
                    !"goal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("Hello World!")
                        pippo()
                        baudo()
                        agent.terminate()
                    }
                }
            }
        }
    }

    @Test
    fun testSkillFeature() {
        Logger.setMinSeverity(Severity.Debug)
        executeInTestScope { mas }
    }
}
