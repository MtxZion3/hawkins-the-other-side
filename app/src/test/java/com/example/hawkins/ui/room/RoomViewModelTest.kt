package com.example.hawkins.ui.room

import com.example.hawkins.data.repository.RoomRepository
import com.example.hawkins.roomFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class RoomViewModelTest {
    @Before fun setup() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun teardown() { Dispatchers.resetMain() }
    private fun vm() = RoomViewModel(object : RoomRepository { override suspend fun load() = roomFixture() })
    @Test fun loadsContractAndWaitsForNativeReady() = runTest {
        val vm = vm(); runCurrent()
        assertNotNull(vm.state.value.room.contract); assertFalse(vm.state.value.room.ready)
        vm.onEvent(RoomEvent.Tune(83f)); vm.onEvent(RoomEvent.PlaySignal); runCurrent(); assertFalse(vm.state.value.room.busy)
        vm.onEvent(RoomEvent.Ready); runCurrent(); assertTrue(vm.state.value.room.ready)
    }
    @Test fun sendsRunAt650msIntervals() = runTest {
        val vm = vm(); runCurrent(); vm.onEvent(RoomEvent.Ready); vm.onEvent(RoomEvent.Tune(83f)); vm.onEvent(RoomEvent.PlaySignal); runCurrent()
        assertEquals(0, vm.state.value.room.signalIndex)
        advanceTimeBy(650); runCurrent(); assertEquals(1, vm.state.value.room.signalIndex)
        advanceTimeBy(650); runCurrent(); assertEquals(2, vm.state.value.room.signalIndex)
        advanceTimeBy(650); runCurrent(); assertTrue(vm.state.value.room.received); assertFalse(vm.state.value.room.busy)
    }
    @Test fun worldSwitchCancelsSignalAndPreservesAngle() = runTest {
        val vm = vm(); runCurrent(); vm.onEvent(RoomEvent.Ready); vm.onEvent(RoomEvent.ToggleMotion); vm.onEvent(RoomEvent.Tick(1f))
        vm.onEvent(RoomEvent.Tune(83f)); vm.onEvent(RoomEvent.PlaySignal); runCurrent(); vm.onEvent(RoomEvent.ToggleWorld); runCurrent()
        advanceTimeBy(3000); runCurrent()
        assertEquals("alternate", vm.state.value.room.world); assertEquals(8f, vm.state.value.room.angle)
        assertFalse(vm.state.value.room.busy); assertFalse(vm.state.value.room.received)
    }
    @Test fun resetAndStopCancelPendingWork() = runTest {
        val vm = vm(); runCurrent(); vm.onEvent(RoomEvent.Ready); vm.onEvent(RoomEvent.ToggleWorld); vm.onEvent(RoomEvent.ToggleMotion)
        vm.onEvent(RoomEvent.Tune(83f)); vm.onEvent(RoomEvent.PlaySignal); runCurrent(); vm.onEvent(RoomEvent.Reset); advanceTimeBy(3000); runCurrent()
        assertEquals("normal", vm.state.value.room.world); assertEquals(0f, vm.state.value.room.angle); assertFalse(vm.state.value.room.rotating)
        vm.onEvent(RoomEvent.Tune(83f)); vm.onEvent(RoomEvent.PlaySignal); runCurrent(); vm.onEvent(RoomEvent.Suspend); advanceTimeBy(3000); runCurrent()
        assertEquals(-1, vm.state.value.room.signalIndex); assertFalse(vm.state.value.room.received)
    }
    @Test fun repositoryFailureCanBeRetried() = runTest {
        var attempts = 0
        val vm = RoomViewModel(object : RoomRepository { override suspend fun load() = if (++attempts == 1) error("Missing asset") else roomFixture() })
        runCurrent(); assertEquals("Missing asset", vm.state.value.room.error)
        vm.onEvent(RoomEvent.Reset); runCurrent()
        assertEquals(2, attempts); assertNull(vm.state.value.room.error); assertNotNull(vm.state.value.room.contract); assertEquals(1, vm.state.value.room.retry)
    }
    @Test fun selectingCurrentWorldDoesNotCancelSignal() = runTest {
        val vm = vm(); runCurrent(); vm.onEvent(RoomEvent.Ready)
        vm.onEvent(RoomEvent.Tune(83f)); vm.onEvent(RoomEvent.PlaySignal); runCurrent()
        vm.onEvent(RoomEvent.SelectWorld("normal")); runCurrent()
        assertTrue(vm.state.value.room.busy)
        vm.onEvent(RoomEvent.SelectWorld("alternate")); runCurrent()
        assertEquals("alternate", vm.state.value.room.world); assertFalse(vm.state.value.room.busy)
        vm.onEvent(RoomEvent.SelectWorld("missing")); runCurrent()
        assertEquals("alternate", vm.state.value.room.world)
    }
    @Test fun switchUsesExplicitStateAndResetClearsIt() = runTest {
        val vm = vm(); runCurrent(); vm.onEvent(RoomEvent.SetMotion(true)); runCurrent()
        assertFalse(vm.state.value.room.rotating)
        vm.onEvent(RoomEvent.Ready); vm.onEvent(RoomEvent.SetMotion(true)); vm.onEvent(RoomEvent.SetMotion(true)); runCurrent()
        assertTrue(vm.state.value.room.rotating)
        vm.onEvent(RoomEvent.Reset); runCurrent(); assertFalse(vm.state.value.room.rotating)
    }

    @Test fun completePuzzleRequiresChannelObservationAndReply() = runTest {
        val vm = vm(); runCurrent(); vm.onEvent(RoomEvent.Ready)
        vm.onEvent(RoomEvent.PlaySignal); runCurrent(); assertFalse(vm.state.value.room.busy)
        vm.onEvent(RoomEvent.Tune(83f)); vm.onEvent(RoomEvent.PlaySignal); runCurrent()
        vm.onEvent(RoomEvent.EnterLetter("R")); runCurrent(); assertEquals("", vm.state.value.room.reply)
        vm.onEvent(RoomEvent.Tune(70f)); runCurrent(); assertEquals(83f, vm.state.value.room.frequency)
        advanceTimeBy(1950); runCurrent(); assertTrue(vm.state.value.room.received)
        listOf("N", "U", "R").forEach { vm.onEvent(RoomEvent.EnterLetter(it)) }
        vm.onEvent(RoomEvent.SubmitReply); runCurrent(); assertTrue(vm.state.value.room.wrong); assertFalse(vm.state.value.room.solved)
        repeat(3) { vm.onEvent(RoomEvent.EraseReply) }
        listOf("R", "U", "N").forEach { vm.onEvent(RoomEvent.EnterLetter(it)) }
        vm.onEvent(RoomEvent.SubmitReply); runCurrent(); assertTrue(vm.state.value.room.solved)
        vm.onEvent(RoomEvent.Tune(70f)); runCurrent(); assertEquals(83f, vm.state.value.room.frequency)
        vm.onEvent(RoomEvent.Reset); runCurrent(); assertFalse(vm.state.value.room.solved); assertEquals(74f, vm.state.value.room.frequency)
    }
    @Test fun retuningAndReplayInvalidateOldAnswerAndSuspendCancelsReception() = runTest {
        val vm = vm(); runCurrent(); vm.onEvent(RoomEvent.Ready); vm.onEvent(RoomEvent.Tune(83f)); vm.onEvent(RoomEvent.PlaySignal)
        advanceTimeBy(1950); runCurrent(); vm.onEvent(RoomEvent.EnterLetter("R")); vm.onEvent(RoomEvent.Tune(84f)); runCurrent()
        assertFalse(vm.state.value.room.received); assertEquals("", vm.state.value.room.reply)
        vm.onEvent(RoomEvent.Tune(83f)); vm.onEvent(RoomEvent.PlaySignal); runCurrent(); vm.onEvent(RoomEvent.Suspend)
        advanceTimeBy(3000); runCurrent(); assertFalse(vm.state.value.room.received); assertFalse(vm.state.value.room.busy)
        vm.onEvent(RoomEvent.PlaySignal); advanceTimeBy(1950); runCurrent(); assertTrue(vm.state.value.room.received)
        vm.onEvent(RoomEvent.EnterLetter("R")); vm.onEvent(RoomEvent.PlaySignal); runCurrent(); assertEquals("", vm.state.value.room.reply)
    }
}
