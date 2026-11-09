import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.node.Node
import it.unibo.jakta.skills.Skill

/**
 * The agent of a vacuum robot, as its plans see it: the type of `agent` in a plan body, with its Prolog beliefs and
 * goals. The actions of [VacuumSkill] extend it.
 */
typealias VacuumAgent = MutableAgentState<PrologBelief, PrologGoal>

/**
 * The skill of a vacuum robot: what its plans can do in the world, through the robot's body.
 *
 * An action returns when the body is done and the agent received what the robot sees then, so that its next
 * decision is taken on what the robot sees once the action is over.
 *
 * @param node the node of the robots, which knows the body of each agent.
 */
class VacuumSkill(node: Node<VacuumBody>) : Skill<VacuumBody>(node) {
    private val VacuumAgent.body get() = node.agents.getValue(id)

    /** Moves one cell forward, if it is free. */
    suspend fun VacuumAgent.forward() {
        body.forward()
        report()
    }

    /** Turns left. */
    suspend fun VacuumAgent.turnLeft() {
        body.turnLeft()
        report()
    }

    /** Turns right. */
    suspend fun VacuumAgent.turnRight() {
        body.turnRight()
        report()
    }

    /** Cleans the cell of the robot. */
    suspend fun VacuumAgent.clean() {
        body.clean()
        report()
    }

    /** Switches the robot on: delivers a first reading of its sensors to the agent. */
    suspend fun VacuumAgent.switchOn() = report()

    /** Delivers what the robot's sensors read now to the agent, and waits until the agent received it. */
    private suspend fun VacuumAgent.report() {
        val body = body
        val reading = body.sense()
        node.publishEvent(reading) { it === body }
        wait({ event: AgentEvent -> event.takeIf { it === reading } })
    }
}
