package it.unibo.jakta.dsl

interface Builder<T> {
    fun build(): T
}
