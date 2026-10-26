package com.example.hawkins.ui.room.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hawkins.domain.model.RoomState
import com.example.hawkins.ui.room.RoomEvent
import com.pico.spatial.ui.design.*
import com.pico.spatial.ui.design.windows.Toolbar
import org.json.JSONObject
import com.example.hawkins.domain.usecase.ReceiverPuzzleUseCase
import java.util.Locale

// The bounded receiver renderer consumes the actual design graph; no second copy of its layout or copy.
private class ReceiverDesign(val json: JSONObject) {
    val nodes = json.getJSONArray("nodes").let { a -> (0 until a.length()).map { a.getJSONObject(it) }.associateBy { it.getString("id") } }
    val actions = json.getJSONArray("actions").let { a -> (0 until a.length()).map { a.getJSONObject(it) }.associateBy { it.getString("id") } }
    fun node(id: String) = nodes.getValue(id)
    fun color(key: String): Color {
        val theme = json.getJSONObject("theme")
        val value = theme.getJSONObject("brandColors").optString(key).ifEmpty {
            theme.getJSONObject("colorScheme").optJSONObject(key)?.getString("value") ?: key
        }
        return Color(android.graphics.Color.parseColor(value))
    }
    fun layout(id: String, width: Float): Pair<Map<String, Any>, Boolean> {
        val result = mutableMapOf<String, Any>()
        fun merge(o: JSONObject?) { o?.keys()?.forEach { result[it] = o.get(it) } }
        merge(node(id).optJSONObject("layout"))
        var hidden = false
        val rules = json.getJSONArray("responsiveRules")
        for (i in 0 until rules.length()) {
            val rule = rules.getJSONObject(i); val range = rule.getJSONObject("when")
            if (width < range.optDouble("minWidthDp", 0.0) || width > range.optDouble("maxWidthDp", Double.MAX_VALUE)) continue
            val overrides = rule.getJSONArray("overrides")
            for (j in 0 until overrides.length()) {
                val o = overrides.getJSONObject(j)
                if (o.getString("nodeId") == id) { merge(o.optJSONObject("layout")); if (o.has("hidden")) hidden = o.getBoolean("hidden") }
            }
        }
        return result to hidden
    }
    fun value(key: String, s: RoomState): String {
        val cases = json.getJSONObject("dataCases")
        val initial = cases.getJSONObject("normal")
        val loading = node("control-panel").getJSONObject("props").getJSONObject("loading")
        val sequence = actions.getValue("play-run").getJSONObject("payload")
        val worldKeys = actions.getValue("switch-world").getJSONObject("payload").getJSONArray("nativeStates")
        val index = (0 until worldKeys.length()).firstOrNull { worldKeys.getString(it) == s.world } ?: 0
        val target = actions.getValue("switch-world").getJSONObject("payload").getJSONArray("targetStates").getString(index)
        val puzzle = node("control-panel").getJSONObject("props").getJSONObject("puzzle")
        val labels = puzzle.getJSONObject("labels")
        val rules = ReceiverPuzzleUseCase()
        val locked = rules.locked(s)
        val phase = when { s.solved -> "solved"; s.busy -> "listening"; s.wrong -> "wrong"; s.received -> "reply"; locked -> "locked"; else -> "search" }
        val copy = puzzle.getJSONObject("phaseCopy").getJSONArray(phase)
        val letters = s.contract?.letters ?: emptyList()
        val unavailable = !s.ready || s.error != null
        return when (key) {
            "worldIndex" -> index.toString()
            "controlsDisabled" -> unavailable.toString()
            "worldDisabled" -> (unavailable || s.solved).toString()
            "motionDisabled", "tunerDisabled" -> (unavailable || s.busy || s.solved).toString()
            "listenDisabled" -> (unavailable || s.busy || s.solved || !locked).toString()
            "keyDisabled" -> (unavailable || !s.received || s.busy || s.solved || s.reply.length >= puzzle.getInt("replyLength")).toString()
            "eraseDisabled" -> (unavailable || s.reply.isEmpty() || s.busy || s.solved).toString()
            "submitDisabled" -> (unavailable || !s.received || s.reply.length != puzzle.getInt("replyLength") || s.busy || s.solved).toString()
            "frequency" -> s.frequency.toString()
            "frequencyLabel" -> String.format(Locale.US, "%.1f", s.frequency) + labels.getString("frequencySuffix")
            "strengthLabel" -> rules.strength(s).toString() + labels.getString("strengthSuffix")
            "busy" -> (unavailable || s.busy).toString()
            "rotating" -> s.rotating.toString()
            "playLabel" -> labels.getString(when { s.solved -> "solved"; s.busy -> "listening"; s.received -> "replay"; else -> "listen" })
            "status" -> when { s.error != null -> cases.getJSONObject("error").getString("status"); !s.ready -> loading.getString("status"); else -> copy.getString(1) }
            "signalCaption" -> when { s.error != null -> cases.getJSONObject("error").getString("signalCaption"); !s.ready -> loading.getString("signalCaption"); else -> copy.getString(0) }
            "signalReadout" -> when {
                s.received || s.solved -> (0 until puzzle.getInt("replyLength")).joinToString(" ") { s.reply.getOrNull(it)?.toString() ?: "_" }
                s.busy -> letters.indices.joinToString(" ") { if (it <= s.signalIndex) "●" else "·" }
                else -> String.format(Locale.US, "%.1f", s.frequency)
            }
            else -> error("Unknown receiver binding: $key")
        }
    }
}

@Composable
fun RoomToolbar(state: RoomState, onEvent: (RoomEvent) -> Unit, availableWidth: Dp) {
    val assets = LocalContext.current.assets
    val design = remember(assets) { ReceiverDesign(JSONObject(assets.open("receiver-design.json").bufferedReader().use { it.readText() })) }
    ReceiverNode("control-panel", design, state, onEvent, availableWidth.value)
}

@Composable
private fun ReceiverNode(id: String, d: ReceiverDesign, s: RoomState, send: (RoomEvent) -> Unit, width: Float, modifier: Modifier = Modifier) {
    val n = d.node(id); val (l, hidden) = d.layout(id, width)
    if (hidden) return
    val children = n.optJSONArray("children")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList()
    val gap = (l["gapDp"] as? Number)?.toFloat()?.dp ?: 0.dp
    var m = modifier
    fun dimension(k: String) = (l[k] as? Number)?.toFloat()?.dp
    dimension("widthDp")?.let { m = m.width(it) }; dimension("heightDp")?.let { m = m.height(it) }
    dimension("minWidthDp")?.let { m = m.widthIn(min = it) }; dimension("minHeightDp")?.let { m = m.heightIn(min = it) }
    n.optJSONObject("appearance")?.let { a ->
        if (a.has("fill")) m = m.clip(RoundedCornerShape(a.optDouble("radiusDp", 0.0).toFloat().dp)).background(d.color(a.getString("fill")))
    }
    dimension("paddingDp")?.let { m = m.padding(it) }
    if (n.has("accessibilityLabel")) m = m.semantics { contentDescription = n.getString("accessibilityLabel") }
    fun prop(key: String, default: String = ""): String = n.optJSONObject("bindings")?.optString(key)?.takeIf { it.isNotEmpty() }?.let { d.value(it, s) } ?: n.optJSONObject("props")?.optString(key, default) ?: default
    fun action(event: String, index: Int = 0, checked: Boolean = false) {
        val name = n.optJSONObject("events")?.optString(event) ?: return
        when (name) {
            "switch-world" -> send(RoomEvent.SelectWorld(d.actions.getValue(name).getJSONObject("payload").getJSONArray("nativeStates").getString(index)))
            "play-run" -> send(RoomEvent.PlaySignal)
            "toggle-rotation" -> send(RoomEvent.SetMotion(checked))
            "reset-scene" -> send(RoomEvent.Reset)
            "erase-reply" -> send(RoomEvent.EraseReply)
            "submit-reply" -> send(RoomEvent.SubmitReply)
            else -> d.actions[name]?.optJSONObject("payload")?.let { if (it.optString("operation") == "reply-letter") send(RoomEvent.EnterLetter(it.getString("letter"))) }
        }
    }
    when (n.getString("kind")) {
        "layout" -> if (l["mode"] == "row") {
            Row(m, horizontalArrangement = if (l["justify"] == "space-between") Arrangement.SpaceBetween else Arrangement.spacedBy(gap), verticalAlignment = when (l["align"]) { "center" -> Alignment.CenterVertically; "end" -> Alignment.Bottom; else -> Alignment.Top }) {
                children.forEach { child ->
                    val grow = d.layout(child, width).first["grow"] as? Number
                    ReceiverNode(child, d, s, send, width, if (grow != null) Modifier.weight(grow.toFloat()) else Modifier)
                }
            }
        } else {
            Column(m, verticalArrangement = Arrangement.spacedBy(gap)) { children.forEach { ReceiverNode(it, d, s, send, width, Modifier.fillMaxWidth()) } }
        }
        "text" -> {
            val style = n.getJSONObject("textStyle")
            val type = when (style.getString("typography")) { "display" -> PicoTheme.typography.displaySmall; "readout" -> PicoTheme.typography.headlineLarge; "label" -> PicoTheme.typography.labelSmall; "subtitle" -> PicoTheme.typography.titleSmall; else -> PicoTheme.typography.bodyMedium }
            Text(n.optJSONObject("bindings")?.optString("content")?.takeIf { it.isNotEmpty() }?.let { d.value(it, s) } ?: n.optString("content"), modifier = m, color = d.color(style.getString("color")), style = type)
        }
        "spatialui" -> when (n.getString("component")) {
            "sui-toolbar" -> Toolbar(cornerSize = prop("corner-radius").toFloat().dp) { children.forEach { ReceiverNode(it, d, s, send, width) } }
            "sui-button" -> Button(onClick = { action("click-action") }, modifier = m.heightIn(min = 56.dp), enabled = prop("disabled") != "true", size = if (prop("size") == "max") ButtonDefaults.Max else ButtonDefaults.Regular, colors = ButtonDefaults.buttonColors(containerColor = d.color(prop("container-color")), contentColor = d.color(prop("content-color")))) {
                // design-style: inherited-content-color Button
                Text(prop("text"))
            }
            "sui-segment-control" -> {
                val enabled = prop("disabled") != "true"
                SegmentControl(modifier = m.heightIn(min = 56.dp).alpha(if (enabled) 1f else .45f).semantics { if (!enabled) disabled() }) {
                    prop("options").split(',').forEachIndexed { i, title -> SegmentItem(selected = prop("selected-index").toInt() == i, onClick = { if (enabled) action("select", index = i) }, title = {
                        // design-style: inherited-content-color SegmentItem
                        Text(title)
                    }) }
                }
            }
            "sui-slider" -> Slider(value = prop("value").toFloat(), onValueChange = { send(RoomEvent.Tune(it)) }, valueRange = prop("min").toFloat()..prop("max").toFloat(), enabled = prop("disabled") != "true", modifier = m.heightIn(min = 56.dp))
            "sui-switch" -> Switch(checked = prop("checked") == "true", onCheckedChange = { action("checked-change", checked = it) }, enabled = prop("disabled") != "true", modifier = m)
            else -> error("Unsupported receiver component: ${n.getString("component")}")
        }
    }
}
