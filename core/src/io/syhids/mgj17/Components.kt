package io.syhids.mgj17

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Texture

class PositionComponent(
        var x: Float = 0f,
        var y: Float = 0f
) : Component {
}

class VelocityComponent : Component {
    var x: Float = 0f
    var y: Float = 0f

    val MAX_VELOCITY = 640f
    val POWER = 6000f
}

class MexicanComponent : Component {
}

class SpriteComponent(
        var img: Texture? = null,
        var scale: Float = 1f
) : Component {

    val midWidth: Int
        get() = width / 2
    val midHeight: Int
        get() = height / 2

    val width: Int
        get() = img?.let { it.width * scale }?.toInt() ?: 0
    val height: Int
        get() = img?.let { it.height * scale }?.toInt() ?: 0
}

class AnimationComponent(
        var speed: Float = 1f,
        val animation: Animation
) : Component {
    private var accDelta: Float = 0f

    fun getCurrentAnimation(): Texture {
        var deltaMs = accDelta * 1000 * speed

        while (deltaMs > animation.totalDuration) {
            deltaMs -= animation.totalDuration
        }

        animation.frames.forEachIndexed { i, frame ->
            deltaMs -= frame.duration

            if (deltaMs <= 0) {
                return frame.texture
            }
        }

        throw RuntimeException("Should not happen")
    }

    fun step(deltaTime: Float) {
        accDelta += deltaTime
    }
}

data class Animation(val frames: List<Frame>) {
    constructor(vararg frames : Frame) : this(frames.toList())

    init {
        preload()
    }

    val totalDuration by lazy { frames.map { it.duration }.reduce { i, i2 -> i + i2 } }

    fun preload() {
        frames.forEach { it.texture }
    }
}

data class Frame(val imageName: String, val duration: Int) {
    val texture by lazy { Texture(imageName) }
}

class MovableComponent : Component {
    var shouldMoveRight: Boolean = false
    var shouldMoveLeft: Boolean = false
}