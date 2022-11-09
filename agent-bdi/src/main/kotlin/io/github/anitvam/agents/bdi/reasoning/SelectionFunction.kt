package io.github.anitvam.agents.bdi.reasoning

/** A selection function chose element of type T from a collection of them */
fun interface SelectionFunction<T> {
    /** Method that select an element of type [T] from a collection of them */
    fun select(collection: Iterable<T>): T
}
