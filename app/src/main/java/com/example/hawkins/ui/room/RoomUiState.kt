package com.example.hawkins.ui.room

data class RoomUiState(val room: com.example.hawkins.domain.model.RoomState = com.example.hawkins.domain.model.RoomState())

sealed interface RoomEvent {
    data class Tune(val frequency: Float) : RoomEvent
    data class EnterLetter(val letter: String) : RoomEvent
    data object EraseReply : RoomEvent
    data object SubmitReply : RoomEvent
    data class SelectWorld(val world: String) : RoomEvent
    data class SetMotion(val enabled: Boolean) : RoomEvent
    data object ToggleWorld : RoomEvent
    data object PlaySignal : RoomEvent
    data object ToggleMotion : RoomEvent
    data object Reset : RoomEvent
    data object Ready : RoomEvent
    data object Suspend : RoomEvent
    data class Failed(val message: String) : RoomEvent
    data class Tick(val seconds: Float) : RoomEvent
}
