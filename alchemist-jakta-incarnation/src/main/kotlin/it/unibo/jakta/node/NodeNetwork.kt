package it.unibo.jakta.node

import it.unibo.jakta.event.EventQueue
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.event.UnlimitedChannelQueue

/**
 * Shared queue of events that are managed by [JaktaForAlchemistNode]s executing in the simulation.
 * Each [JaktaForAlchemistNode] holds its subscription to the [NodeNetwork].
 */
val NodeNetwork = LocalNodeConnection()
