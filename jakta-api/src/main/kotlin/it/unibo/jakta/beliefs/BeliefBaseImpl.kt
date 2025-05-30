package it.unibo.jakta.beliefs

import it.unibo.jakta.events.Event

internal data class BeliefBaseImpl<Belief : Any>(val believes: MutableSet<Belief> = mutableSetOf()) :
    MutableBeliefBase<Belief>,
    Set<Belief> by believes {

    private val eventQueue: MutableList<Event.Internal.Belief<Belief>> = mutableListOf()

    override fun poll(): Event.Internal.Belief<Belief>? = eventQueue.removeLastOrNull()

    override fun snapshot(): BeliefBase<Belief> = this.copy()

//    override fun update(belief: Belief): Boolean {
//        val element = matcher.query()
//        return if (element != null) {
//            beliefs.remove(element)
//            events.add(BeliefBaseRemoval(element))
//            beliefs.add(belief)
//            events.add(BeliefBaseAddition(belief))
//            true
//        } else {
//            false
//        },
//    }
//
//    override fun update(beliefBase: ASBeliefBase): Boolean {
//        when (beliefs == beliefBase) {
//            false -> {
//                // 1. each literal l in p not currently in b is added to b
//                beliefs.addAll(beliefBase.filterIsInstance<ASBelief>())
//
//                // 2. each literal l in b no longer in p is deleted from b
//                beliefs.forEach { belief: ASBelief ->
//                    if (!beliefBase.contains(belief) && belief.content.head.args.first() == ASBelief.SOURCE_PERCEPT) {
//                        remove(belief)
//                    }
//                }
//                return true
//            }
//            // TODO("Can be done better. Different lists for percepts and self sourced beliefs;
//            //  update events are not curr. generated)
//            else -> return false
//        }
//    }

    override fun remove(element: Belief): Boolean = believes.remove(element).alsoWhenTrue {
        eventQueue.add(element.removeEvent())
    }

    override fun addAll(elements: Collection<Belief>): Boolean {
        var result = false
        for (belief in elements) {
            result = add(belief)
        }
        return result
    }

    override fun removeAll(elements: Collection<Belief>): Boolean {
        var result = false
        for (belief in elements) {
            result = remove(belief)
        }
        return result
    }

    override fun retainAll(elements: Collection<Belief>): Boolean {
        var result = false
        val iterator = believes.iterator()
        while (iterator.hasNext()) {
            val belief = iterator.next()
            if (belief !in elements) {
                iterator.remove()
                result = true
            }
        }
        return result
    }

    override fun clear() {
        eventQueue += believes.map { it.removeEvent() }
        believes.clear()
    }

    override fun add(element: Belief): Boolean = believes.add(element).alsoWhenTrue {
        eventQueue.add(object : Event.Internal.Belief.Add<Belief> {
            override val belief: Belief get() = element
        })
    }

    // TODO: verify with a test that events are generated ONCE for every removal via iterator
    override fun iterator(): MutableIterator<Belief> = believes.iterator()

//    override fun toString(): String = beliefs.joinToString { ASBelief.from(it.content.castToRule()).toString() }

    companion object {
        private fun Boolean.alsoWhenTrue(body: () -> Unit): Boolean = if (this) {
            body()
            true
        } else {
            false
        }

        fun <Belief : Any> Belief.removeEvent() = object : Event.Internal.Belief.Remove<Belief> {
            override val belief: Belief get() = this@removeEvent
        }
    }
}
