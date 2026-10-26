package com.example.hawkins.domain.usecase

import com.example.hawkins.roomFixture
import com.example.hawkins.domain.model.RoomState
import org.junit.Assert.*
import org.junit.Test

class AdvanceRoomUseCaseTest {
    private val useCase = AdvanceRoomUseCase()
    @Test fun motionIsFrameRateIndependentAndReflectsAtBoundary() {
        val initial = RoomState(contract = roomFixture(), ready = true, rotating = true)
        val coarse = useCase(initial, 4f)
        val fine = (1..40).fold(initial) { state, _ -> useCase(state, .1f) }
        assertEquals(coarse.angle, fine.angle, .001f)
        assertTrue(coarse.angle in -14f..14f)
        assertEquals(-1f, coarse.direction)
    }
    @Test fun pausedAndInvalidDeltasNeverMove() {
        val initial = RoomState(contract = roomFixture(), ready = true)
        assertEquals(initial, useCase(initial, 1f))
        assertEquals(initial, useCase(initial, Float.NaN))
        assertEquals(initial, useCase(initial, -1f))
    }
}
