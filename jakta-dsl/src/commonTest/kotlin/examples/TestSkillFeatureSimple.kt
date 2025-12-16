package examples

import TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.agent
import it.unibo.jakta.mas
import it.unibo.jakta.plan.triggers
import kotlin.test.Test

interface SkillSimple

interface HttpSkillSimple : SkillSimple {
    fun pippo() = Logger.i { "pippo" }
}
class HttpSkillSimpleImpl : HttpSkillSimple

interface SshSkillSimple : SkillSimple {
    fun baudo() = Logger.i { "baudo" }
}
class SshSkillSimpleImpl : SshSkillSimple

class MyCapabilitiesSimple : HttpSkillSimple by HttpSkillSimpleImpl(), SshSkillSimple by SshSkillSimpleImpl()

class TestSkillFeatureSimple {
    val mas = mas {
        environment { TestEnvironment() }

        agent("HelloAgent") {
            hasInitialGoals {
                !"goal"
            }
            hasPlans {
                adding.goal {
                    ifGoalMatch("goal")
                } triggers {
                    with(MyCapabilitiesSimple()) {
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
