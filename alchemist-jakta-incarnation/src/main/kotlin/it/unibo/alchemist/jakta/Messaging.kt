package it.unibo.alchemist.jakta

/**
 * Which Alchemist nodes the messages sent by the agents of a device can reach.
 * Messages that cannot reach an agent are silently dropped, as out-of-range radio transmissions.
 */
enum class Messaging {
    /**
     * Messages reach the agents of every Alchemist node, ignoring the linking rule.
     */
    GLOBAL,

    /**
     * Messages reach only the agents of the same Alchemist node and of its neighbors,
     * as defined by the linking rule when the message leaves the node.
     */
    NEIGHBORHOOD,

    ;

    /**
     * Utilities for [Messaging].
     */
    companion object {
        /**
         * Parses a [Messaging] mode from its case-insensitive [name], e.g. from a simulation file.
         */
        fun parse(name: Any?): Messaging = requireNotNull(entries.find { it.name.equals(name.toString(), true) }) {
            "Unknown messaging mode '$name', expected one of ${entries.map { it.name.lowercase() }}"
        }
    }
}
