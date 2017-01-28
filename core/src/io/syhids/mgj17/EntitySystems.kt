package io.syhids.mgj17

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import java.util.*

class TrumpMovementSystem : IteratingSystem(Family.all(
        TrumpComponent::class.java
).get()) {
    private var accDelta: Float = 0f
    private val difficuty: Float
        get() = accDelta + 1

    var state: State = State.IdlingFor(ms = 2000)

    sealed class State {
        class MovingTo(val x: Float) : State()
        class IdlingFor(val ms: Int, var mustShootAtStart: Boolean = true) : State() {
            var idledTime: Float = 0f

            val seconds: Float
                get() = ms / 1000f

            val idledEnough: Boolean
                get() = idledTime >= seconds
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        accDelta += deltaTime

        val curState = state
        when (curState) {
            is State.MovingTo -> {
                val moveStep = deltaTime * 1f + Math.min(difficuty * 0.2f, 25f)

                if (entity.position.x < curState.x) {
                    entity.velocity.x += moveStep
                } else {
                    entity.velocity.x -= moveStep
                }

                if (Math.abs(Math.abs(entity.position.x) - Math.abs(curState.x)) < 10) {
                    val fixed = 2500 - Math.min(1200f, difficuty * 200f)
                    val variable = Random().nextInt(4400 - Math.min(4399f, difficuty * 400f).toInt())

                    state = State.IdlingFor(ms = (fixed + variable).toInt())
                }
            }
            is State.IdlingFor -> {
                if (curState.mustShootAtStart) {
                    curState.mustShootAtStart = false

                    shootWig(entity)
                }

                curState.idledTime += deltaTime

                if (curState.idledEnough) {
                    val finalTargetPosX = findRandomXInsideWorld(entity)
                    state = State.MovingTo(finalTargetPosX)
                }

                entity.velocity.x *= 0.88f
            }
        }
    }

    private fun findRandomXInsideWorld(entity: Entity): Float {
        var finalTargetPosX: Float

        val leftBounds = -WORLD_WIDTH / 3
        val rightBounds = WORLD_WIDTH / 3

        do {
            val delta = (Math.random() * 800 - 400).toInt()
            finalTargetPosX = entity.position.x + delta
        } while (finalTargetPosX < leftBounds || finalTargetPosX > rightBounds)

        return finalTargetPosX
    }

    private fun shootWig(entity: Entity) {
        val trumpAnim = entity.animation

        trumpAnim.reset()
        trumpAnim.state = AnimationComponent.State.PlayUntilFrame(0)
        engine.addSystem(WigSystem(entity))
    }
}

class SpriteDrawingSystem(
        private val batch: SpriteBatch,
        private val camera: OrthographicCamera
) : IteratingSystem(Family.all(
        PositionComponent::class.java,
        SpriteComponent::class.java
).get()) {

    private val position = component(PositionComponent::class)
    private val sprite = component(SpriteComponent::class)


    override fun update(deltaTime: Float) {
        camera.update()

        batch.enableBlending()
        batch.begin()
        batch.projectionMatrix = camera.combined
        super.update(deltaTime)
        batch.end()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val sprite = sprite.get(entity)

        if (!sprite.visible || sprite.img == null)
            return

        val position = position.get(entity)
        val spriteToDraw = sprite.sprite

        spriteToDraw.setAlpha(sprite.alpha)
        spriteToDraw.setScale(sprite.scale)
        spriteToDraw.setCenter(position.x, position.y)

        spriteToDraw.draw(batch)
    }
}

class WigSystem(private val entity: Entity) : EntitySystem() {
    val wig = Wig()

    var state: WigState = WigState.Invisible

    sealed class WigState {
        object Invisible : WigState()
        object InTrumpsHands : WigState()
        object Falling : WigState()
    }

    override fun addedToEngine(engine: Engine) {
        wig.position.set(entity.position.x, entity.position.y)
        engine.addEntity(wig)
    }

    override fun update(deltaTime: Float) {
        val curState = state
        when (curState) {
            WigState.Invisible -> {
                state = if (entity is Trump) {
                    WigState.InTrumpsHands
                } else {
                    WigState.Falling
                }

                wig.sprite.visible = false
            }
            WigState.InTrumpsHands -> {
                val frameIndex = entity.animation.currentAnimationIndex

                wig.sprite.visible = frameIndex != 0

                wig.sprite.alpha = when (frameIndex) {
                    1 -> 0.5f
                    2 -> 0.75f
                    else -> 1f
                }

                applyPositionOfFrame(frameIndex)

                if (entity.animation.currentAnimationIndex >= entity.animation.animation.lastFrameIndex - 1) {
                    state = WigState.Falling
                    wig.sprite.visible = true
                    wig.sprite.alpha = 1f
                    applyPositionOfFrame(entity.animation.animation.lastFrameIndex - 1)
                }
            }
            WigState.Falling -> {
                wig.sprite.visible = true
                wig.velocity.y = -440f
            }
        }
    }

    private fun applyPositionOfFrame(frameIndex: Int) {
        wig.position.set(x = entity.position.x, y = entity.position.y)

        val yDelta = arrayOf(0f, 0f, 0f, 150f, 200f, 190f, 150f, 0f, -30f, -30f)

        wig.position.y += yDelta[frameIndex] * 0.6f
    }
}

class TrumpRelativeShootingSystem : IteratingSystem(Family.all(
        TrumpRelativeComponent::class.java
).get()) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val trumpRelative = entity.getComponent(TrumpRelativeComponent::class.java)

        trumpRelative.timeLeftToShoot -= deltaTime

        if (trumpRelative.timeLeftToShoot <= 0f) {
            trumpRelative.timeLeftToShoot = (1 + Math.random() * 2).toFloat()

            engine.addSystem(WigSystem(entity))
        }
    }
}