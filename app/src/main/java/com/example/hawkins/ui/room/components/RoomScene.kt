package com.example.hawkins.ui.room.components

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.example.hawkins.domain.model.RoomContract
import com.example.hawkins.domain.model.RoomState
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.ecs.DirectionalLightComponent
import com.pico.spatial.core.ecs.ViewCoordinateSpace
import com.pico.spatial.core.math.Color4
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.ui.foundation.content.SpatialView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.math.abs
import com.example.hawkins.domain.usecase.ReceiverPuzzleUseCase

private class SceneHolder {
    var owner: Entity? = null
    var fit: Entity? = null
    var nodes = emptyMap<String, Entity>()
    var disposed = false
    var previous: Pair<String, Int>? = null
    fun apply(state: RoomState) {
        val config = state.contract ?: return
        val visibility = config.states.getValue(state.world)
        visibility.visible.forEach { nodes[it]?.enabled = true }
        visibility.hidden.forEach { nodes[it]?.enabled = false }
        val letter = config.letters.getOrNull(state.signalIndex)
        val locked = ReceiverPuzzleUseCase().locked(state)
        val scan = (state.scanSeconds / config.puzzle.noiseStep).toInt() % (config.letters.size + 2)
        config.signalTargets.forEach { id ->
            val glow = when {
                state.solved -> true
                state.busy -> letter != null && id.endsWith("_" + letter)
                state.received -> false
                locked -> true
                else -> config.letters.getOrNull(scan)?.let { id.endsWith("_" + it) } ?: false
            }
            nodes[id]?.enabled = glow
        }
        fit?.components?.get(TransformComponent::class.java)?.eulerAngles = EulerAngles(0f, state.angle, 0f)
        val marker = state.world to state.signalIndex
        if (previous != marker) { Log.i("HawkinsScene", "STATE world=" + state.world + " signal=" + letter); previous = marker }
    }
    fun close() { disposed = true; owner?.destroy(); owner = null; fit = null; nodes = emptyMap() }
}

@Composable
fun RoomScene(config: RoomContract, state: RoomState, onReady: () -> Unit, onFailure: (String) -> Unit) {
    val holder = remember { SceneHolder() }
    val latest by rememberUpdatedState(state)
    DisposableEffect(holder) { onDispose { holder.close(); Log.i("HawkinsScene", "DISPOSED") } }
    SpatialView(modifier = Modifier.fillMaxSize(), update = { content, _ ->
        val corner = content.convertPosition(Vector3.ZERO, ViewCoordinateSpace.Local, content.localSpatialCoordinateSpace)
        val resize = minOf(abs(corner.x) / .7f, abs(corner.y) / .4f, abs(corner.z) / .4f)
        if (resize > 0 && resize.isFinite()) {
            val scale = config.fitScale * resize
            holder.fit?.components?.get(TransformComponent::class.java)?.scaleVector = Vector3(scale, scale, scale)
        }
        holder.apply(state)
    }, initial = { content, _ ->
        var imported: Entity? = null
        try {
            imported = Entity.loadSuspend("asset://scene.glb")
            currentCoroutineContext().ensureActive()
            if (holder.disposed) { imported.destroy(); return@SpatialView }
            val all = mutableListOf<Entity>()
            fun visit(node: Entity) { all.add(node); node.getChildren().forEach(::visit) }
            visit(imported)
            holder.nodes = config.names.mapValues { (_, name) ->
                all.filter { it.getName() == name }.also { require(it.size == 1) { "Ambiguous or missing binding: $name" } }.single()
            }
            val owner = Entity().also { it.setName("placementOwner") }
            holder.owner = owner
            val fit = Entity().also { it.setName("fittedSceneRoot") }
            holder.fit = fit
            owner.addChild(fit); fit.addChild(imported)
            fit.components[TransformComponent::class.java] = TransformComponent().apply { scaleVector = Vector3(config.fitScale, config.fitScale, config.fitScale) }
            listOf(EulerAngles(-40f, -25f, 0f), EulerAngles(-20f, 80f, 0f)).forEachIndexed { index, angle ->
                val light = Entity()
                light.components[TransformComponent::class.java] = TransformComponent().apply { eulerAngles = angle }
                light.components[DirectionalLightComponent::class.java] = DirectionalLightComponent(
                    if (index == 0) Color4(1f, .87f, .7f, 1f) else Color4(.55f, .74f, 1f, 1f),
                    if (index == 0) 1400f else 650f, index == 0)
                owner.addChild(light)
            }
            content.addEntity(owner)
            val corner = content.convertPosition(Vector3.ZERO, ViewCoordinateSpace.Local, content.localSpatialCoordinateSpace)
            val safe = Vector3(abs(corner.x), abs(corner.y), abs(corner.z))
            require(safe.x > 0 && safe.y > 0 && safe.z > 0) { "SpatialView bounds unavailable" }
            for (yaw in listOf(-config.amplitude, 0f, config.amplitude)) {
                fit.components[TransformComponent::class.java]?.eulerAngles = EulerAngles(0f, yaw, 0f)
                val bounds = imported.getVisualBounds(owner, recursive = true, enabledOnly = false)
                Log.i("HawkinsScene", "BOUNDS yaw=$yaw min=${bounds.min} max=${bounds.max} safe=$safe")
                require(bounds.halfExtent.x > 0 && bounds.halfExtent.y > 0 && bounds.halfExtent.z > 0) { "Empty model bounds" }
                require(bounds.min.x >= -safe.x && bounds.max.x <= safe.x && bounds.min.y >= -safe.y && bounds.max.y <= safe.y && bounds.min.z >= -safe.z && bounds.max.z <= safe.z) { "Scene exceeds SpatialView bounds" }
            }
            holder.apply(latest)
            Log.i("HawkinsScene", "READY bindings=${holder.nodes.size} entities=${all.size} fit=${config.fitScale}")
            onReady()
        } catch (e: CancellationException) { imported?.destroy(); throw e }
        catch (e: Exception) { holder.owner?.destroy(); holder.owner = null; imported?.destroy(); Log.e("HawkinsScene", "LOAD_FAILED", e); onFailure(e.message ?: "Scene unavailable") }
    })
}
