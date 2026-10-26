package com.example.hawkins.domain.usecase

import com.example.hawkins.domain.model.RoomState
import kotlin.math.abs

/** Pure puzzle rules; timing and lifecycle are owned by RoomViewModel. */
class ReceiverPuzzleUseCase {
    fun locked(s: RoomState): Boolean = s.contract?.puzzle?.let { abs(s.frequency - it.target) <= it.tolerance } ?: false
    fun strength(s: RoomState): Int = s.contract?.puzzle?.let { ((1f - abs(s.frequency - it.target) / 12f).coerceIn(0f, 1f) * 100).toInt() } ?: 0
    fun tune(s: RoomState, value: Float): RoomState {
        if (!s.ready || s.busy || s.solved || !value.isFinite()) return s
        val config = s.contract ?: return s
        val next = value.coerceIn(config.puzzle.min, config.puzzle.max)
        if (next == s.frequency) return s
        return s.copy(frequency = next, received = false, reply = "", wrong = false)
    }
    fun enter(s: RoomState, letter: String): RoomState {
        val config = s.contract ?: return s
        if (!s.ready || !s.received || s.busy || s.solved || s.reply.length >= config.puzzle.replyLength || letter !in config.puzzle.keys) return s
        return s.copy(reply = s.reply + letter, wrong = false)
    }
    fun submit(s: RoomState): RoomState {
        val config = s.contract ?: return s
        if (!s.ready || !s.received || s.busy || s.solved || s.reply.length != config.puzzle.replyLength) return s
        val correct = s.reply == config.letters.joinToString("").uppercase()
        return if (correct) s.copy(solved = true, wrong = false, rotating = false, angle = 0f, world = config.initialState)
        else s.copy(wrong = true)
    }
}
