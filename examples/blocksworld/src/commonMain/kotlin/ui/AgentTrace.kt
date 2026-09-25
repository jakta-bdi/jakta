package ui

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val MAX_TRACE_LINES = 500

/**
 * Collects what agents print (JaKtA logs `print` at [Severity.Assert]) so that the UI can show their reasoning.
 */
object AgentTrace : LogWriter() {
    private val mutableLines = MutableStateFlow<List<String>>(emptyList())

    /**
     * The most recent lines printed by the agents, oldest first.
     */
    val lines: StateFlow<List<String>> = mutableLines.asStateFlow()

    override fun isLoggable(tag: String, severity: Severity): Boolean = severity == Severity.Assert

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        mutableLines.update { (it + "[$tag] $message").takeLast(MAX_TRACE_LINES) }
    }

    /**
     * Forgets all the collected lines.
     */
    fun clear() {
        mutableLines.value = emptyList()
    }
}
