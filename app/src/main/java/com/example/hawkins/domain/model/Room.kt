package com.example.hawkins.domain.model

data class World(val label: String, val eyebrow: String, val status: String, val button: String, val description: String)
data class Visibility(val visible: List<String>, val hidden: List<String>)
data class RoomContract(
    val names: Map<String, String>, val states: Map<String, Visibility>,
    val initialState: String, val rotationTarget: String, val amplitude: Float,
    val speed: Float, val fitScale: Float, val signalTargets: List<String>,
    val letters: List<String>, val stepMs: Long, val worlds: Map<String, World>, val puzzle: PuzzleConfig = PuzzleConfig(),
)
data class RoomState(
    val contract: RoomContract? = null, val world: String = "normal",
    val ready: Boolean = false, val error: String? = null, val retry: Int = 0,
    val rotating: Boolean = false, val angle: Float = 0f, val direction: Float = 1f,
    val frequency: Float = 74f, val reply: String = "", val solved: Boolean = false,
    val wrong: Boolean = false, val scanSeconds: Float = 0f,
    val signalIndex: Int = -1, val busy: Boolean = false, val received: Boolean = false,
)

data class PuzzleConfig(
    val min: Float = 70f, val max: Float = 100f, val initial: Float = 74f,
    val target: Float = 83f, val tolerance: Float = .8f, val noiseStep: Float = .65f,
    val keys: List<String> = listOf("N", "A", "R", "S", "U", "O"), val replyLength: Int = 3,
)
