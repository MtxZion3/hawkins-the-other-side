package com.example.hawkins.ui.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hawkins.data.repository.RoomRepository
import com.example.hawkins.domain.usecase.ReceiverPuzzleUseCase
import com.example.hawkins.domain.usecase.AdvanceRoomUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import com.example.hawkins.domain.model.RoomState
import kotlinx.coroutines.launch

class RoomViewModel(private val repository: RoomRepository, private val advance: AdvanceRoomUseCase = AdvanceRoomUseCase()) : ViewModel() {
    private val puzzle = ReceiverPuzzleUseCase()
    private val mutable = MutableStateFlow(RoomState())
    val state = mutable.map { RoomUiState(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, RoomUiState())
    private var signal: Job? = null
    private var loading: Job? = null
    init { load() }
    private fun load() {
        loading?.cancel()
        loading = viewModelScope.launch {
            try { val config = repository.load(); mutable.update { it.copy(contract = config, world = config.initialState, frequency = config.puzzle.initial, error = null) } }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutable.update { it.copy(error = e.message ?: "Scene unavailable", ready = false) } }
        }
    }
    fun onEvent(event: RoomEvent) {
        when (event) {
            is RoomEvent.Tune -> mutable.update { puzzle.tune(it, event.frequency) }
            is RoomEvent.EnterLetter -> mutable.update { puzzle.enter(it, event.letter) }
            RoomEvent.EraseReply -> mutable.update { if (it.received && !it.busy && !it.solved) it.copy(reply = it.reply.dropLast(1), wrong = false) else it }
            RoomEvent.SubmitReply -> mutable.update { puzzle.submit(it) }
            RoomEvent.Ready -> mutable.update { it.copy(ready = true, error = null) }
            is RoomEvent.Failed -> { cancelSignal(); mutable.update { it.copy(ready = false, rotating = false, error = event.message) } }
            is RoomEvent.SelectWorld -> if (mutable.value.ready && !mutable.value.solved && event.world in mutable.value.contract!!.states && event.world != mutable.value.world) {
                cancelSignal(); mutable.update { it.copy(world = event.world, received = false, reply = "", wrong = false) }
            }
            is RoomEvent.SetMotion -> if (mutable.value.ready && !mutable.value.busy && !mutable.value.solved) mutable.update { it.copy(rotating = event.enabled) }
            RoomEvent.ToggleWorld -> if (mutable.value.ready) {
                cancelSignal(); mutable.update { s -> s.copy(world = s.contract!!.states.keys.first { it != s.world }, received = false) }
            }
            RoomEvent.ToggleMotion -> if (mutable.value.ready) mutable.update { it.copy(rotating = !it.rotating) }
            RoomEvent.PlaySignal -> if (mutable.value.ready && !mutable.value.busy && !mutable.value.solved && puzzle.locked(mutable.value)) {
                val config = mutable.value.contract!!
                signal = viewModelScope.launch {
                    mutable.update { it.copy(busy = true, received = false, reply = "", wrong = false, rotating = false) }
                    config.letters.indices.forEach { index ->
                        mutable.update { it.copy(signalIndex = index) }; delay(config.stepMs)
                    }
                    mutable.update { it.copy(signalIndex = -1, busy = false, received = true) }
                }
            }
            RoomEvent.Reset -> {
                cancelSignal(); val old = mutable.value
                mutable.value = RoomState(contract = old.contract, world = old.contract?.initialState ?: "normal",
                    frequency = old.contract?.puzzle?.initial ?: 74f, ready = old.ready && old.error == null, retry = old.retry + if (old.error != null) 1 else 0)
                if (old.error != null) load()
            }
            RoomEvent.Suspend -> { cancelSignal(); mutable.update { it.copy(rotating = false) } }
            is RoomEvent.Tick -> if (event.seconds.isFinite() && event.seconds > 0) mutable.update {
                val next = advance(it, event.seconds)
                if (next.ready && !next.busy && !next.received && !next.solved) next.copy(scanSeconds = (next.scanSeconds + event.seconds.coerceAtMost(.1f)) % 60f) else next
            }
        }
    }
    private fun cancelSignal() { signal?.cancel(); signal = null; mutable.update { it.copy(busy = false, signalIndex = -1) } }
}
