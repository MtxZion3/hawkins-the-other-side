package com.example.hawkins.domain.usecase

import com.example.hawkins.domain.model.RoomState
import com.example.hawkins.roomFixture
import org.junit.Test
import org.junit.Assert.*

class ReceiverPuzzleUseCaseTest {
    private val rules = ReceiverPuzzleUseCase()
    private fun state() = RoomState(contract = roomFixture(), ready = true)
    @Test fun tuningClampsAndRejectsInvalidAndBlockedInput() {
        val s = state()
        assertEquals(100f, rules.tune(s, 150f).frequency)
        assertEquals(70f, rules.tune(s, 0f).frequency)
        assertEquals(s, rules.tune(s, Float.NaN))
        assertEquals(s.copy(busy = true), rules.tune(s.copy(busy = true), 83f))
        assertFalse(rules.locked(s)); assertTrue(rules.locked(s.copy(frequency = 83f)))
        assertTrue(rules.strength(s.copy(frequency = 82f)) > rules.strength(s))
    }
    @Test fun cannotGuessBeforeReceivingOrOverflowAndWrongAnswerCanRecover() {
        val s = state(); assertEquals(s, rules.enter(s, "R"))
        var received = s.copy(received = true)
        assertEquals(received, rules.enter(received, "X"))
        for (letter in listOf("N", "U", "R")) received = rules.enter(received, letter)
        assertEquals(received, rules.enter(received, "A"))
        assertTrue(rules.submit(received).wrong)
        val solved = rules.submit(received.copy(reply = "RUN", world = "alternate", rotating = true))
        assertTrue(solved.solved); assertFalse(solved.rotating); assertEquals("normal", solved.world)
        assertFalse(rules.submit(received.copy(reply = "RU")).solved)
    }
}
