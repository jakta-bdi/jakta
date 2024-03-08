package it.unibo.alchemist.jakta

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution
import org.apache.commons.math3.random.RandomGenerator

class JaktaIncarnation<P> : Incarnation<Any?, P> where P : Position<P> {
    override fun getProperty(p0: Node<Any?>?, p1: Molecule?, p2: String?): Double {
        TODO("Not yet implemented")
    }

    override fun createMolecule(p0: String?): Molecule {
        TODO("Not yet implemented")
    }

    override fun createConcentration(p0: String?): Any? {
        TODO("Not yet implemented")
    }

    override fun createConcentration(): Any? {
        TODO("Not yet implemented")
    }

    override fun createAction(
        p0: RandomGenerator?,
        p1: Environment<Any?, P>?,
        p2: Node<Any?>?,
        p3: TimeDistribution<Any?>?,
        p4: Actionable<Any?>?,
        p5: Any?,
    ): Action<Any?> {
        TODO("Not yet implemented")
    }

    override fun createCondition(
        p0: RandomGenerator?,
        p1: Environment<Any?, P>?,
        p2: Node<Any?>?,
        p3: TimeDistribution<Any?>?,
        p4: Actionable<Any?>?,
        p5: Any?,
    ): Condition<Any?> {
        TODO("Not yet implemented")
    }

    override fun createReaction(
        p0: RandomGenerator?,
        p1: Environment<Any?, P>?,
        p2: Node<Any?>?,
        p3: TimeDistribution<Any?>?,
        p4: Any?,
    ): Reaction<Any?> {
        TODO("Not yet implemented")
    }

    override fun createTimeDistribution(
        p0: RandomGenerator?,
        p1: Environment<Any?, P>?,
        p2: Node<Any?>?,
        p3: Any?,
    ): TimeDistribution<Any?> {
        TODO("Not yet implemented")
    }

    override fun createNode(p0: RandomGenerator?, p1: Environment<Any?, P>?, p2: Any?): Node<Any?> {
        TODO("Not yet implemented")
    }
}
