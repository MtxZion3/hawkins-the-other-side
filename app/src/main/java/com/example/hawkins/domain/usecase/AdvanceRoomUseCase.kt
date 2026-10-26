package com.example.hawkins.domain.usecase

import com.example.hawkins.domain.model.RoomState

class AdvanceRoomUseCase {
    operator fun invoke(state: RoomState, seconds: Float): RoomState {
        val config = state.contract ?: return state
        if (!state.ready || !state.rotating || !seconds.isFinite() || seconds <= 0) return state
        val amplitude = config.amplitude
        val distance = config.speed * seconds
        val phase = if (state.direction > 0) state.angle + amplitude else 3 * amplitude - state.angle
        val next = (phase + distance) % (4 * amplitude)
        return if (next <= 2 * amplitude) state.copy(angle = next - amplitude, direction = 1f)
        else state.copy(angle = 3 * amplitude - next, direction = -1f)
    }
}
