package it.unibo.jakta.intentions

interface Intention<Belief : Any, Query : Any, Result> {
    //    val stack: List<ActivationRecord<Belief, Query, Result>>
    val id: IntentionID

    fun isEmpty(): Boolean

    fun pop(): Popped<Belief, Query, Result>

    fun push(record: ActivationRecord<Belief, Query, Result>): Intention<Belief, Query, Result>

    data class Popped<Belief : Any, Query : Any, Result>(
        val intention: Intention<Belief, Query, Result>?,
        val activationRecord: ActivationRecord<Belief, Query, Result>,
    )

    companion object {

        private data class IntentionImpl<Belief : Any, Query : Any, Result>(
            override val id: IntentionID,
            private val queue: List<ActivationRecord<Belief, Query, Result>>,
        ) : Intention<Belief, Query, Result> {
            override fun isEmpty(): Boolean = queue.isEmpty()

            override fun pop(): Popped<Belief, Query, Result> {
                if (queue.isEmpty()) {
                    throw NoSuchElementException("Cannot pop from an empty intention")
                }
                return Popped(IntentionImpl(id, queue.dropLast(1)), queue.last())
            }

            override fun push(record: ActivationRecord<Belief, Query, Result>): Intention<Belief, Query, Result> =
                IntentionImpl(id, queue + record)
        }

        @JvmStatic
        operator fun <Belief : Any, Query : Any, Result> invoke(record: ActivationRecord<Belief, Query, Result>): Intention<Belief, Query, Result> =
            IntentionImpl(IntentionID(), listOf(record))
    }
}
