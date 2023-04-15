package it.unibo.jakta.agents.bdi.dsl.examples.blocksworld

import it.unibo.jakta.agents.bdi.AgentID
import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.environment.impl.EnvironmentImpl
import it.unibo.jakta.agents.bdi.messages.MessageQueue
import it.unibo.jakta.agents.bdi.perception.Perception
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.utils.addFirst

class BlocksWorldEnvironment(
    agentIDs: Map<String, AgentID> = emptyMap(),
    externalActions: Map<String, ExternalAction> = emptyMap(),
    messageBoxes: Map<AgentID, MessageQueue> = emptyMap(),
    perception: Perception = Perception.empty(),
    data: Map<String, Any> = mapOf(
        "tower1" to listOf("z", "x", "c"),
        "tower2" to listOf("y", "b"),
        "tower3" to listOf("a"),
    ),
) : EnvironmentImpl(externalActions, agentIDs, messageBoxes, perception, data) {

    private fun moveBlock(upper: String, lower: String): List<List<String>> {
        val towers = getTableState()
        print("$towers -> MOVING $upper OVER $lower -> ")
        for (tower in towers) {
            if (tower.first() == upper) {
                tower.removeFirst()
                if (tower.isEmpty()) {
                    towers.remove(tower)
                }
                if (lower == "table") {
                    towers.add(mutableListOf(upper))
                } else {
                    for (otherTower in towers) {
                        if (otherTower.first() == lower) {
                            otherTower.addFirst(upper)
                            break
                        }
                    }
                }

                break
            }
        }
        return towers
    }

    private fun getTableState(): MutableList<MutableList<String>> = data.values.map {
        (it as List<*>).filterIsInstance<String>().toMutableList()
    }.toMutableList()

    override fun updateData(newData: Map<String, Any>): Environment {
        val towers = this.moveBlock(newData["upper"].toString(), newData["lower"].toString())
        var updatedData: Map<String, List<String>> = emptyMap()
        for ((index, tower) in towers.withIndex()) {
            updatedData = updatedData + ("tower$index" to tower)
        }
        println(towers)
        return copy(data = updatedData)
    }

    override fun percept() = BeliefBase.of(
        getTableState().flatMap { tower ->
            val stack = tower.zipWithNext().map { (above, below) ->
                Belief.fromPerceptSource(Struct.of("on", Atom.of(above), Atom.of(below)))
            }
            val top = Belief.fromPerceptSource(Struct.of("on", Atom.of(tower.last()), Atom.of("table")))
            stack + top
        }
    )

    override fun copy(
        agentIDs: Map<String, AgentID>,
        externalActions: Map<String, ExternalAction>,
        messageBoxes: Map<AgentID, MessageQueue>,
        perception: Perception,
        data: Map<String, Any>
    ): BlocksWorldEnvironment =
        BlocksWorldEnvironment(agentIDs, externalActions, messageBoxes, perception, data)
}
