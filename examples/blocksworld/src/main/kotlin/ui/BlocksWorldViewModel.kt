package ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import model.BlocksWorld

class BlocksWorldViewModel(private val world: BlocksWorld) {

    private val _state = MutableStateFlow(runBlocking{ world.getState()})
    val state = _state.asStateFlow()

    suspend fun refresh() {
        _state.value = world.getState()
    }
}
