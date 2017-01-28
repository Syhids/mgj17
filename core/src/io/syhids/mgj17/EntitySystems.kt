package io.syhids.mgj17

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import java.util.*

class InputSystem : IteratingSystem(Family.all(
        MexicanComponent::class.java
).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.movement.shouldMoveLeft = keyPressed(Keys.LEFT)
        entity.movement.shouldMoveRight = keyPressed(Keys.RIGHT)
    }
}

class MovementSystem : IteratingSystem(Family.all(
        VelocityComponent::class.java,
        MovableComponent::class.java
).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val power = entity.velocity.POWER
        val maxVelocity = entity.velocity.MAX_VELOCITY
        val movement = entity.movement

        if (movement.shouldMoveLeft) {
            entity.velocity.x -= deltaTime * power
        } else if (movement.shouldMoveRight) {
            entity.velocity.x += deltaTime * power
        } else {
            entity.velocity.x *= 0.9f
        }

        entity.velocity.x = Math.min(maxVelocity, entity.velocity.x)
        entity.velocity.x = Math.max(-maxVelocity, entity.velocity.x)
    }
}

class AccelerationSystem : IteratingSystem(Family.all(
        PositionComponent::class.java,
        VelocityComponent::class.java
).get()) {
    private val position = component(PositionComponent::class)
    private val velocity = component(VelocityComponent::class)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val position = position.get(entity)
        val velocity = velocity.get(entity)

        position.x += velocity.x * deltaTime
        position.y += velocity.y * deltaTime
    }
}

class AnimationSystem : IteratingSystem(Family.all(
        SpriteComponent::class.java,
        AnimationComponent::class.java
).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val animation = entity.animation
        val sprite = entity.sprite

        animation.step(deltaTime)
        animation.updateCurrentAnimation()

        sprite.img = animation.getCurrentAnimation()
    }
}

class TrumpShootSystem : IteratingSystem(Family.all(
        TrumpComponent::class.java
).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val trumpAnim = entity.animation

        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            trumpAnim.reset()
            trumpAnim.state = AnimationComponent.State.PlayUntilFrame(0)
            engine.addSystem(WigSystem())
        }
    }
}

class TrumpMovementSystem : IteratingSystem(Family.all(
        TrumpComponent::class.java
).get()) {
    var accDelta : Float = 1f

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
                val moveStep = deltaTime * 1f + Math.min(accDelta*0.2f, 25f)

                if (entity.position.x < curState.x) {
                    entity.velocity.x += moveStep
                } else {
                    entity.velocity.x -= moveStep
                }

                if (Math.abs(Math.abs(entity.position.x) - Math.abs(curState.x)) < 10) {
                    val fixed = 2500 - Math.min(1200f, accDelta * 200f)
                    val variable = Random().nextInt(4400 - Math.min(4399f, accDelta * 400f).toInt())

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

        val leftBounds = -WORLD_WIDTH/3 + 0
        val rightBounds = WORLD_WIDTH/3 - 0

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
        engine.addSystem(WigSystem())
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

    private val TRANSLATION_X = -380f
    private val TRANSLATION_Y = -240f

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

        val posX = position.x - sprite.midWidth
        val posY = position.y - sprite.midHeight

        val spriteToDraw = sprite.sprite

        spriteToDraw.setAlpha(sprite.alpha)
        spriteToDraw.setScale(sprite.scale)
        spriteToDraw.setPosition(posX, posY)
        spriteToDraw.translate(TRANSLATION_X, TRANSLATION_Y)

        spriteToDraw.draw(batch)
    }
}

class WigSystem : EntitySystem() {
    val wig = Wig()

    var state: WigState = WigState.Invisible

    lateinit var trump: Trump

    sealed class WigState {
        object Invisible : WigState()
        object InTrumpsHands : WigState()
        object Falling : WigState()
    }

    override fun addedToEngine(engine: Engine) {
        engine.addEntity(wig)
    }

    override fun update(deltaTime: Float) {
        trump = engine.getEntitiesFor(Family.one(TrumpComponent::class.java).get()).first() as Trump

        val curState = state
        when (curState) {
            WigState.Invisible -> {
                state = WigState.InTrumpsHands
                wig.sprite.visible = false
            }
            WigState.InTrumpsHands -> {
                val frameIndex = trump.animation.currentAnimationIndex

                wig.sprite.visible = frameIndex != 0

                wig.sprite.alpha = when (frameIndex) {
                    1 -> 0.5f
                    2 -> 0.75f
                    else -> 1f
                }

                applyPositionOfFrame(frameIndex)

                if (trump.animation.currentAnimationIndex >= trump.animation.animation.lastFrameIndex - 1) {
                    state = WigState.Falling
                    wig.sprite.visible = true
                    wig.sprite.alpha = 1f
                    applyPositionOfFrame(trump.animation.animation.lastFrameIndex - 1)
                }
            }
            WigState.Falling -> {
                wig.velocity.y = -440f
            }
        }
    }

    private fun applyPositionOfFrame(frameIndex: Int) {
        wig.position.set(x = trump.position.x + trump.sprite.midWidth + 200f, y = trump.position.y + 240f)

        val yDelta = arrayOf(0f, 0f, 0f, 150f, 200f, 190f, 150f, 0f, -30f, -30f)

        wig.position.y += yDelta[frameIndex] * 0.7f
    }
}