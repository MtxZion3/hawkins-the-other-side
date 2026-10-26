package com.example.hawkins.data.repository

import android.content.res.AssetManager
import com.example.hawkins.domain.model.*
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AssetRoomRepository(private val assets: AssetManager) : RoomRepository {
    override suspend fun load(): RoomContract = withContext(Dispatchers.IO) {
        val json = JSONObject(assets.open("bindings.json").bufferedReader().use { it.readText() })
        require(json.getString("schemaVersion") == "threejs-spatial-bindings/v1")
        require(json.getString("units") == "meters" && json.getString("upAxis") == "+Y")
        val digest = MessageDigest.getInstance("SHA-256")
        assets.open("scene.glb").use { stream ->
            val buffer = ByteArray(8192)
            while (true) { val count = stream.read(buffer); if (count < 0) break; digest.update(buffer, 0, count) }
        }
        require(digest.digest().joinToString("") { "%02x".format(it) } == json.getString("assetSha256")) { "Asset revision mismatch" }
        val nodes = json.getJSONObject("nodes")
        val names = nodes.keys().asSequence().associateWith { nodes.getString(it) }
        require(names.values.toSet().size == names.size)
        val statesJson = json.getJSONObject("states")
        val states = statesJson.keys().asSequence().associateWith { key ->
            val state = statesJson.getJSONObject(key)
            Visibility(state.getJSONArray("visible").strings(), state.getJSONArray("hidden").strings())
        }
        val rotation = json.getJSONObject("rotation")
        require(rotation.getString("mode") == "bounded-sweep")
        val signal = json.getJSONObject("signal")
        val text = JSONObject(assets.open("worlds.json").bufferedReader().use { it.readText() })
        val worlds = text.keys().asSequence().associateWith { key ->
            val w = text.getJSONObject(key)
            World(w.getString("label"), w.getString("eyebrow"), w.getString("status"), w.getString("button"), w.getString("description"))
        }
        val ui = JSONObject(assets.open("receiver-design.json").bufferedReader().use { it.readText() })
        val graph = ui.getJSONArray("nodes")
        val panel = (0 until graph.length()).map { graph.getJSONObject(it) }.first { it.getString("id") == "control-panel" }
        val puzzle = panel.getJSONObject("props").getJSONObject("puzzle")
        val puzzleConfig = PuzzleConfig(puzzle.getDouble("minFrequency").toFloat(), puzzle.getDouble("maxFrequency").toFloat(),
            puzzle.getDouble("initialFrequency").toFloat(), puzzle.getDouble("targetFrequency").toFloat(),
            puzzle.getDouble("tolerance").toFloat(), puzzle.getDouble("noiseStepSeconds").toFloat(),
            puzzle.getJSONArray("keys").strings(), puzzle.getInt("replyLength"))
        require(puzzleConfig.min < puzzleConfig.max && puzzleConfig.target in puzzleConfig.min..puzzleConfig.max)
        require(puzzleConfig.initial in puzzleConfig.min..puzzleConfig.max && puzzleConfig.tolerance > 0 && puzzleConfig.noiseStep >= .5f)
        require(puzzleConfig.replyLength == signal.getJSONArray("letters").length())
        require(signal.getJSONArray("letters").strings().all { it.uppercase() in puzzleConfig.keys })
        RoomContract(names, states, json.getString("initialState"), rotation.getString("target"),
            rotation.getDouble("amplitudeDegrees").toFloat(), rotation.getDouble("degreesPerSecond").toFloat(),
            rotation.getDouble("fitScale").toFloat(), signal.getJSONArray("targets").strings(),
            signal.getJSONArray("letters").strings(), signal.getLong("stepMs"), worlds, puzzleConfig).also { c ->
            require(c.initialState in c.states && c.rotationTarget in names)
            require(c.amplitude in 1f..14f && c.speed > 0 && c.fitScale > 0 && c.stepMs > 0)
            require(c.letters.isNotEmpty() && c.signalTargets.all { it in names })
            require(c.states.values.all { (it.visible + it.hidden).all(names::containsKey) })
        }
    }
    private fun JSONArray.strings() = (0 until length()).map(::getString)
}
