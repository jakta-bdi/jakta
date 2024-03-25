package it.unibo.alchemist.jakta
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.AgentID
import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.MessageQueue
import it.unibo.jakta.agents.bdi.perception.Perception
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import org.apache.commons.math3.random.RandomGenerator
import kotlin.reflect.KProperty
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

    /**
     * Generates the [Pair] containing the external action name and the [ExternalAction] object itself.
     */
    private val ExternalAction.named: Pair<String, ExternalAction> get() = signature.name to this

    override val agentIDs: Map<String, AgentID> get() =
        node.reactions.asSequence()
            .flatMap { it.actions }
            .filterIsInstance<JaktaAgentForAlchemist<*>>()
            .associate { it.agent.name to it.agent.agentID }

    /**
     * Object that represents an Alchemist empty molecule.
     */
    object EmptyMolecule

    fun Any?.valueOrEmptyMolecule(): Any = this ?: EmptyMolecule

    override val data: Map<String, Any> get() =
        alchemistEnvironment.nodes
            .flatMap { it.contents.toList() }
            .associate { (name, content) ->
                name.name to content.valueOrEmptyMolecule()
            }

    override val perception: Perception
        get() = Perception.of(
            data.map {
                Belief.fromPerceptSource(Struct.of(it.key, ObjectRef.of(it.value)))
            },
        )

    override fun getNextMessage(agentName: String): Message? = null // TODO()

    override fun popMessage(agentName: String): Environment {
        TODO("Not yet implemented")
    }

    override fun submitMessage(agentName: String, message: Message): Environment {
        TODO("Not yet implemented")
    }

    override fun broadcastMessage(message: Message): Environment {
        TODO("Not yet implemented")
    }

    override fun addAgent(agent: Agent): Environment = TODO()

    override fun removeAgent(agentName: String): Environment = TODO()

    override fun addData(key: String, value: Any): Environment = error("Not existing in Alchemist simulation")

    override fun removeData(key: String): Environment = also {
        node.contents.keys.firstOrNull { it.name.equals(key) }?.let { node.removeConcentration(it) }
    }

    override fun updateData(newData: Map<String, Any>): Environment {
        newData.forEach { node.setConcentration(SimpleMolecule(it.key), it.value) }
        return this
    }

    override fun toString(): String {
        return "T"
    }

    override fun copy(
        agentIDs: Map<String, AgentID>,
        externalActions: Map<String, ExternalAction>,
        messageBoxes: Map<AgentID, MessageQueue>,
        perception: Perception,
        data: Map<String, Any>,
    ): Environment = this

    override fun cloneOnNewNode(node: Node<Any?>): NodeProperty<Any?> =
        JaktaEnvironmentForAlchemist(alchemistEnvironment, randomGenerator, node)

    // EXTERNAL ACTIONS FOR ALCHEMIST

    /**
     * Delegate to help implementing Jakta External Actions.
     */
    class ExternalActionFor(
        val arity: Int,
        val body: ExternalRequest.() -> Unit,
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): ExternalAction {
            return object : AbstractExternalAction(property.name, arity) {
                override fun action(request: ExternalRequest) {
                    request.body()
                }
            }
        }
    }

    /**
     * External action that writes information into the Alchemist environment.
     */
    val writeData = object : AbstractExternalAction("writeData", 2) {
        override fun action(request: ExternalRequest) {
            println("Before: $data")
            val name = request.arguments[0].castToAtom().value
            println(name)
            val concentration = request.arguments[1].`as`<ObjectRef>()?.`object`.valueOrEmptyMolecule()
            node.setConcentration(SimpleMolecule(name), concentration)
            println("After: $data")
        }
    }

//    val sendTo = object : AbstractExternalAction("sendTo", 2) {
//        override fun action(request: ExternalRequest) {
//            val agent = request.arguments[0].castToAtom().value
//            val destinationNode = alchemistEnvironment.nodes.filter { it.id }
//        }
//
//    }

    /**
     * External action that runs arbitrary lambdas defined into the Jakta DSL.
     */
    @Suppress("UNCHECKED_CAST")
    val run by ExternalActionFor(1) {
        (arguments[0].`as`<ObjectRef>()?.`object` as () -> Unit)()
    }

    override var externalActions: Map<String, ExternalAction> = mapOf(
        run.named,
        writeData.named,
        // sendTo.named
    )
}
