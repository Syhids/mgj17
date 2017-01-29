package io.syhids.mgj17

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite

class PositionComponent(
        var x: Float = 0f,
        var y: Float = 0f
) : Component {
    fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun set(position: PositionComponent) {
        set(position.x, position.y)
    }
}

class VelocityComponent : Component {
    var x: Float = 0f
    var y: Float = 0f

    val MAX_VELOCITY = 640f
    val POWER = 6000f
}

class MexicanComponent : Component {
}

class TrumpComponent : Component {
}

class TrumpRelativeComponent : Component {
    var timeLeftToShoot: Float = 1f + Math.random().toFloat()
}

class WigMovementComponent : Component {
    lateinit var parent: Entity
    var state: WigState = WigState.Invisible

    sealed class WigState {
        object Invisible : WigState()
        object InTrumpsHands : WigState()
        object Falling : WigState()
    }
}

class SpriteComponent(
        var img: Texture? = null,
        var scale: Float = 1f,
        var visible: Boolean = true,
        var depth: Int = 0
) : Component {
    val sprite: Sprite
        get() = Sprite(img)

    val midWidth: Int
        get() = width / 2
    val midHeight: Int
        get() = height / 2

    val width: Int
        get() = img?.let { it.width * scale }?.toInt() ?: 0
    val height: Int
        get() = img?.let { it.height * scale }?.toInt() ?: 0

    var alpha: Float = 1f
    var rotation: Float = 0f
}

class AnimationComponent(
        var speed: Float = 1f,
        val animation: Animation
) : Component {

    var state: State = State.Playing()
        set(value) {
            field = value
            if (value is State.PlayUntilFrame && currentAnimationIndex == value.frameIndex) {
                value.loopUntilNextFrame = true
            }
        }

    val isLastFrame: Boolean
        get() = animation.lastFrameIndex == currentAnimationIndex

    val isFirstFrame: Boolean
        get() = currentAnimationIndex == 0

    sealed class State {
        class Playing(val times: Int = -1) : State() {
            var currentTimes: Int = 0
        }

        object Paused : State()
        class PlayUntilFrame(val frameIndex: Int) : State()

        var loopUntilNextFrame: Boolean = false
    }

    private var accDelta: Float = 0f

    var currentAnimationIndex: Int = 0

    fun updateCurrentAnimation() {
        var deltaMs = accDelta * 1000 * speed

        while (deltaMs > animation.totalDuration) {
            deltaMs -= animation.totalDuration
        }

        animation.frames.forEachIndexed { frameIndex, frame ->
            deltaMs -= frame.duration

            if (deltaMs <= 0) {
                currentAnimationIndex = frameIndex

                val curState = state
                if (curState is State.PlayUntilFrame) {
                    if (frameIndex == curState.frameIndex && !curState.loopUntilNextFrame) {
                        state = State.Paused
                    } else {
                        val isNextFrame = curState.frameIndex == frameIndex - 1 || (frameIndex == 0 && curState.frameIndex == animation.lastFrameIndex)

                        if (isNextFrame && curState.loopUntilNextFrame)
                            curState.loopUntilNextFrame = false
                    }
                }

                return
            }
        }

        throw RuntimeException("Should not happen")
    }

    fun getCurrentAnimation(): Texture {
        return animation.frames[currentAnimationIndex].texture
    }

    fun step(deltaTime: Float) {
        if (state is State.Paused)
            return

        accDelta += deltaTime
    }

    fun reset() {
        accDelta = 0f
    }
}

data class Animation(val frames: List<Frame>) {
    constructor(vararg frames: Frame) : this(frames.toList())

    init {
        preload()
    }

    val totalDuration by lazy { frames.map { it.duration }.reduce { i, i2 -> i + i2 } }

    fun preload() {
        frames.forEach { it.texture }
    }

    val lastFrameIndex: Int
        get() = frames.size - 1
}

data class Frame(val imageName: String, val duration: Int) {
    val texture by lazy { Texture(imageName) }
}

class MovableComponent : Component {
    var shouldMoveRight: Boolean = false
    var shouldMoveLeft: Boolean = false
}

class ColliderComponent(
        var width: Float = 0f,
        var height: Float = 0f
) : Component {
    val midWidth: Float
        get() = width / 2

    val midHeight: Float
        get() = height / 2
}

class MenuComponent : Component {

}