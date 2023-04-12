package io.github.anitvam.agents.bdi.dsl.examples.tris

import io.github.anitvam.agents.bdi.AgentID
import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.environment.impl.EnvironmentImpl
import io.github.anitvam.agents.bdi.messages.MessageQueue
import io.github.anitvam.agents.bdi.perception.Perception
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct

class GridEnvironment(
    private val n: Int,
    agentIDs: Map<String, AgentID> = emptyMap(),
    externalActions: Map<String, ExternalAction> = emptyMap(),
    messageBoxes: Map<AgentID, MessageQueue> = emptyMap(),
    perception: Perception = Perception.empty(),
    data: Map<String, Any> = mapOf("grid" to createGrid(n)),
) : EnvironmentImpl(externalActions, agentIDs, messageBoxes, perception, data) {

    companion object {
        internal fun createGrid(n: Int): Array<CharArray> {
            Array(n) { CharArray(n) { 'e' } }
            return arrayOf(
                charArrayOf('e', 'e', 'e'),
                charArrayOf('x', 'x', 'e'),
                charArrayOf('e', 'e', 'e')
            )
        }

        internal fun Array<CharArray>.copy() =
            Array(size) { i -> CharArray(this[i].size) { j -> this[i][j] } }
    }

    @Suppress("UNCHECKED_CAST")
    internal val grid: Array<CharArray>
        get() = data["grid"] as Array<CharArray>

    @Suppress("UNCHECKED_CAST")
    override fun updateData(newData: Map<String, Any>): Environment {
        if ("cell" in newData) {
            val cell = newData["cell"] as Triple<Int, Int, Char>
            val result = copy(data = mapOf("grid" to grid.copy()))
            result.grid[cell.first][cell.second] = cell.third
            return result
        }
        return this
    }

    override fun percept(): BeliefBase =
        BeliefBase.of(
            buildList {
                for (i in grid.indices) {
                    for (j in grid[i].indices) {
                        add(
                            Belief.fromPerceptSource(
                                Struct.of("cell", Integer.of(i), Integer.of(j), Atom.of("${grid[i][j]}"))
                            )
                        )
                    }
                }
            }
        )

    override fun copy(
        agentIDs: Map<String, AgentID>,
        externalActions: Map<String, ExternalAction>,
        messageBoxes: Map<AgentID, MessageQueue>,
        perception: Perception,
        data: Map<String, Any>
    ) = GridEnvironment(n, agentIDs, externalActions, messageBoxes, perception, data)
}
