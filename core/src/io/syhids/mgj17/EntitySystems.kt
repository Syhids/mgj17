package io.syhids.mgj17

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import io.syhids.mgj17.WigMovementComponent.WigState
import io.syhids.mgj17.system.InputSystem
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
        val wig = Wig()
        wig.wigMovement.parent = entity
        wig.position.set(entity.position)
        engine.addEntity(wig)
    }
}

class SpriteDrawingSystem(
        private val batch: SpriteBatch,
        private val camera: OrthographicCamera
) : SortedIteratingSystem(Family.all(
        PositionComponent::class.java,
        SpriteComponent::class.java
).get(), { e, e2 ->
    e.sprite.depth - e2.sprite.depth
}) {

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

class WigMovementSystem : IteratingSystem(Family.all(
        WigMovementComponent::class.java
).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val wig = entity as Wig
        val parent = wig.wigMovement.parent
        val curState = wig.wigMovement.state

        val INITIAL_VELOCITY = 100f
        val VELOCITY_INCREMENT = 500f

        when (curState) {
            WigState.Invisible -> {
                if (parent is Trump) {
                    wig.wigMovement.state = WigState.InTrumpsHands
                } else {
                    wig.wigMovement.state = WigState.Falling
                    wig.velocity.y = -INITIAL_VELOCITY
                }

                wig.sprite.visible = false
            }
            WigState.InTrumpsHands -> {
                val frameIndex = parent.animation.currentAnimationIndex

                wig.sprite.visible = frameIndex != 0

                wig.sprite.alpha = when (frameIndex) {
                    1 -> 0.5f
                    2 -> 0.75f
                    else -> 1f
                }

                applyPositionOfFrame(wig, frameIndex)

                if (parent.animation.currentAnimationIndex >= parent.animation.animation.lastFrameIndex - 1) {
                    wig.wigMovement.state = WigState.Falling
                    wig.velocity.y = -INITIAL_VELOCITY

                    wig.sprite.visible = true
                    wig.sprite.alpha = 1f
                    applyPositionOfFrame(wig, parent.animation.animation.lastFrameIndex - 1)
                }
            }
            WigState.Falling -> {
                wig.sprite.visible = true
                wig.sprite.alpha = 1f
                wig.velocity.y -= deltaTime * VELOCITY_INCREMENT

                if (wig.position.y < -WORLD_HEIGHT / 2) {
                    engine.removeEntity(wig)
                }
            }
        }
    }

    private fun applyPositionOfFrame(wig: Wig, frameIndex: Int) {
        val parent = wig.wigMovement.parent
        wig.position.set(x = parent.position.x, y = parent.position.y)

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

            val wig = Wig()
            wig.wigMovement.parent = entity
            wig.position.set(entity.position)
            engine.addEntity(wig)
        }
    }
}

class WigMexicanCollisionSystem : IteratingSystem(Family.all(
        MexicanComponent::class.java
).get()) {

    fun rectangleFrom(entity: Entity): Rectangle {
        val collider = entity.collider
        val position = entity.position

        return Rectangle(position.x, position.y, collider.width, collider.height)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val mexicanRect = rectangleFrom(entity)

        engine.getEntitiesFor(Family.all(
                WigMovementComponent::class.java
        ).get()).forEach { wig ->
            val wigRect = rectangleFrom(wig)

            if (wigRect.overlaps(mexicanRect)) {
                val gameStateSystem = engine.getSystem(GameStateSystem::class.java)
                gameStateSystem.state = GameStateSystem.State.Lost
            }
        }
    }
}

class GameStateSystem(val batch: SpriteBatch, val font: BitmapFont) : EntitySystem(1) {
    sealed class State {
        object Playing : State()
        object Lost : State()
    }

    var previousState: State = State.Playing
    var state: State = State.Playing

    var accDelta: Float = 0f

    override fun addedToEngine(engine: Engine?) {
        accDelta = 0f
    }

    override fun update(deltaTime: Float) {
        val inputSystem = engine.getSystem(InputSystem::class.java)

        accDelta += deltaTime

        if (previousState != state) {
            onStateTransitioned(previousState, state)
            previousState = state
        }

        inputSystem.enabled = state is State.Playing

        when (state) {
            State.Playing -> {

            }
            State.Lost -> {
                batch.begin()
                font.draw(batch, "YOU DED", 0f, 0f, 0f, Align.center, false)
                batch.end()

                if (accDelta >= 5f) {
                    state = State.Playing
                }
            }
        }
    }

    private fun onStateTransitioned(previousState: State, newState: State) {
        accDelta = 0f

        when (newState) {
            State.Playing -> {

            }
            State.Lost -> {
            }
        }
    }

}