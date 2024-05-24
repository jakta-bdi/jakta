package it.unibo.alchemist.jakta.properties
import it.unibo.alchemist.jakta.JaktaForAlchemistLibrary
import it.unibo.alchemist.jakta.JaktaForAlchemistLibrary.ExternalActionFor
import it.unibo.alchemist.jakta.JaktaForAlchemistMessageBroker
import it.unibo.alchemist.jakta.reactions.JaktaAgentForAlchemist
import it.unibo.alchemist.jakta.util.valueOrEmptyMolecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.AgentID
import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.MessageQueue
import it.unibo.jakta.agents.bdi.perception.Perception
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import org.apache.commons.math3.random.RandomGenerator
import it.unibo.alchemist.model.Environment as AlchemistEnvironment
import it.unibo.jakta.agents.bdi.environment.Environment as JaktaEnvironment

/**
 * Jakta Environment Implementation that connects to Alchemist meta-model.
 */
class JaktaEnvironmentForAlchemist<P : Position<P>>(
    val alchemistEnvironment: AlchemistEnvironment<Any?, P>,
    val randomGenerator: RandomGenerator,
    // Alchemist NodeProperty inheritance
    override val node: Node<Any?>,
    // Jakta Environment inheritance
    override val messageBoxes: Map<AgentID, MessageQueue> = emptyMap(),
) : JaktaEnvironment, NodeProperty<Any?> {

    override val agentIDs: Map<String, AgentID> get() =
        node.reactions.asSequence()
            .flatMap { it.actions }
            .filterIsInstance<JaktaAgentForAlchemist<*>>()
            .associate { it.agent.name to it.agent.agentID }

    private fun neighborhoodData(): Map<String, Any> = alchemistEnvironment.getNeighborhood(node)
        .flatMap { it.contents.toList() }
        .associate { (name, content) ->
            name.name to content.valueOrEmptyMolecule()
        }

    private fun myNodeData(): Map<String, Any> = node.contents.toList()
        .associate { (name, content) ->
            name.name to content.valueOrEmptyMolecule()
        }

    override val data: Map<String, Any> get() = myNodeData()

    override val perception: Perception
        get() = Perception.of(
            neighborhoodData().map {
                Belief.fromPerceptSource(Struct.of(it.key, ObjectRef.of(it.value)))
            },
        )

    // --------------- Messages ---------------

    @Suppress("UNCHECKED_CAST")
    private fun getMessageBroker(): JaktaForAlchemistMessageBroker<P> =
        node.getConcentration(BROKER_MOLECULE) as JaktaForAlchemistMessageBroker<P>

    override fun getNextMessage(agentName: String): Message? {
        return getMessageBroker().nextMessage(agentName, node.id.toString())
    }

    override fun popMessage(agentName: String): Environment = this.also {
        getMessageBroker().pop(agentName, node.id.toString())
    }

    override fun submitMessage(agentName: String, message: Message): Environment = this.also {
        getMessageBroker().send(agentName, message)
    }

    override fun broadcastMessage(message: Message): Environment = this.also {
        getMessageBroker().broadcast(message)
    }

    // ----------------------------------------

    override fun addAgent(agent: Agent): Environment = TODO()

    override fun removeAgent(agentName: String): Environment = TODO()

    // --------------- Data manipulation in node ---------------

    override fun addData(key: String, value: Any): Environment {
        node.setConcentration(SimpleMolecule(key), value)
        return this
    }

    override fun removeData(key: String): Environment {
        node.contents.keys.firstOrNull { it.name.equals(key) }?.let { node.removeConcentration(it) }
        return this
    }

    override fun updateData(newData: Map<String, Any>): Environment {
        newData.forEach { node.setConcentration(SimpleMolecule(it.key), it.value) }
        return this
    }

    // ----------------------------------------

    override fun copy(
        agentIDs: Map<String, AgentID>,
        externalActions: Map<String, ExternalAction>,
        messageBoxes: Map<AgentID, MessageQueue>,
        perception: Perception,
        data: Map<String, Any>,
    ): Environment = this

    override fun cloneOnNewNode(node: Node<Any?>): NodeProperty<Any?> =
        JaktaEnvironmentForAlchemist(alchemistEnvironment, randomGenerator, node)

    /**
     * External action that runs arbitrary lambdas defined into the Jakta DSL.
     */
    @Suppress("UNCHECKED_CAST")
    val run by ExternalActionFor(1) {
        (arguments[0].`as`<ObjectRef>()?.`object` as () -> Unit)()
    }

    override var externalActions: Map<String, ExternalAction> = JaktaForAlchemistLibrary(this).api() +
        ("run" to run)

    companion object {
        val BROKER_MOLECULE = SimpleMolecule("MessageBroker")
    }
}
