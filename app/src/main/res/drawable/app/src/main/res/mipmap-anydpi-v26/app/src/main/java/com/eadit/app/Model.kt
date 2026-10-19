package com.eadit.app

import kotlin.math.abs

enum class LayerType { TEXT, RECT, CIRCLE }

/** Position is normalized: x,y in 0..1 where (0,0) is the top-left of the video frame. */
data class LayerState(
    val x: Float,
    val y: Float,
    val scale: Float,
    val opacity: Float,
)

data class Keyframe(val timeMs: Long, val state: LayerState)

data class Layer(
    val id: Int,
    val type: LayerType,
    val text: String = "",
    val colorArgb: Int = 0xFFFFFFFF.toInt(),
    /** Visible range, in the timeline of the original video. */
    val startMs: Long,
    val endMs: Long,
    val keyframes: List<Keyframe>,
) {
    fun isVisibleAt(timeMs: Long): Boolean = timeMs in startMs..endMs

    /** Interpolated state at [timeMs] (smoothstep easing between keyframes). */
    fun stateAt(timeMs: Long): LayerState {
        val ks = keyframes
        if (ks.isEmpty()) return LayerState(0.5f, 0.5f, 1f, 1f)
        if (timeMs <= ks.first().timeMs) return ks.first().state
        if (timeMs >= ks.last().timeMs) return ks.last().state
        val i = ks.indexOfLast { it.timeMs <= timeMs }
        val a = ks[i]
        val b = ks[i + 1]
        val span = (b.timeMs - a.timeMs).coerceAtLeast(1L)
        val f = (timeMs - a.timeMs).toFloat() / span
        val e = f * f * (3f - 2f * f)
        return LayerState(
            x = lerp(a.state.x, b.state.x, e),
            y = lerp(a.state.y, b.state.y, e),
            scale = lerp(a.state.scale, b.state.scale, e),
            opacity = lerp(a.state.opacity, b.state.opacity, e),
        )
    }

    /** Adds or replaces the keyframe at [timeMs] (within 50 ms counts as the same keyframe). */
    fun withKeyframe(timeMs: Long, state: LayerState): Layer {
        val rest = keyframes.filterNot { abs(it.timeMs - timeMs) < 50 }
        return copy(keyframes = (rest + Keyframe(timeMs, state)).sortedBy { it.timeMs })
    }

    /** Removes the keyframe nearest to [timeMs] (within 150 ms), always keeping at least one. */
    fun withoutKeyframeNear(timeMs: Long): Layer {
        if (keyframes.size <= 1) return this
        val nearest = keyframes.minByOrNull { abs(it.timeMs - timeMs) } ?: return this
        if (abs(nearest.timeMs - timeMs) > 150) return this
        return copy(keyframes = keyframes - nearest)
    }

    fun label(): String = when (type) {
        LayerType.TEXT -> "T: " + text.take(10).ifBlank { "Text" }
        LayerType.RECT -> "Rect $id"
        LayerType.CIRCLE -> "Circle $id"
    }
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
