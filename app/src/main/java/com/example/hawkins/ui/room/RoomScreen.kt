package com.example.hawkins.ui.room

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hawkins.data.repository.AssetRoomRepository
import com.example.hawkins.ui.room.components.RoomScene
import com.example.hawkins.ui.room.components.RoomToolbar
import kotlinx.coroutines.isActive

@Composable
fun RoomScreen() {
    val context = LocalContext.current.applicationContext
    val factory = remember(context) { object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RoomViewModel(AssetRoomRepository(context.assets)) as T
    } }
    val vm: RoomViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, vm) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) vm.onEvent(RoomEvent.Suspend) }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); vm.onEvent(RoomEvent.Suspend) }
    }
    LaunchedEffect(state.room.ready) {
        if (state.room.ready) {
            var previous = withFrameNanos { it }
            while (isActive) { val now = withFrameNanos { it }; vm.onEvent(RoomEvent.Tick((now - previous) / 1_000_000_000f)); previous = now }
        }
    }
    RoomContent(state.room, vm::onEvent)
}

@Composable
internal fun RoomContent(state: com.example.hawkins.domain.model.RoomState, onEvent: (RoomEvent) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        state.contract?.let { config ->
            key(state.retry) { RoomScene(config, state, { onEvent(RoomEvent.Ready) }, { onEvent(RoomEvent.Failed(it)) }) }
        }
        RoomToolbar(state, onEvent, maxWidth)
    }
}
