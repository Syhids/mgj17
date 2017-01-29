package io.syhids.mgj17.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import io.syhids.mgj17.*

class ThrowableMovementSystem : IteratingSystem(Family.all(
        ThrowableComponent::class.java
).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val parent = entity.throwable.parent
        val curState = entity.throwable.state

        val INITIAL_VELOCITY = 100f
        val VELOCITY_INCREMENT = 500f

        when (curState) {
            ThrowableComponent.WigState.Invisible -> {
                if (parent is Trump) {
                    entity.throwable.state = ThrowableComponent.WigState.InTrumpsHands
                } else {
                    entity.throwable.state = ThrowableComponent.WigState.Falling
                    entity.velocity.y = -INITIAL_VELOCITY
                }

                entity.sprite.visible = false
            }
            ThrowableComponent.WigState.InTrumpsHands -> {
                val frameIndex = parent.animation.currentAnimationIndex

                entity.sprite.visible = frameIndex != 0

                entity.sprite.alpha = when (frameIndex) {
                    1 -> 0.5f
                    2 -> 0.75f
                    else -> 1f
                }

                applyPositionOfFrame(entity, frameIndex)

                if (parent.animation.currentAnimationIndex >= parent.animation.animation.lastFrameIndex - 1) {
                    entity.throwable.state = ThrowableComponent.WigState.Falling
                    entity.velocity.y = -INITIAL_VELOCITY

                    entity.sprite.visible = true
                    entity.sprite.alpha = 1f
                    applyPositionOfFrame(entity, parent.animation.animation.lastFrameIndex - 1)
                }
            }
            ThrowableComponent.WigState.Falling -> {
                entity.sprite.rotation += deltaTime*180

                entity.sprite.visible = true
                entity.sprite.alpha = 1f
                entity.velocity.y -= deltaTime * VELOCITY_INCREMENT

                if (entity.position.y < -WORLD_HEIGHT / 2) {
                    engine.removeEntity(entity)
                }
            }
        }
    }

    private fun applyPositionOfFrame(entity: Entity, frameIndex: Int) {
        val parent = entity.throwable.parent
        entity.position.set(x = parent.position.x, y = parent.position.y)

        val yDelta = arrayOf(0f, 0f, 0f, 150f, 200f, 190f, 150f, 0f, -30f, -30f)

        entity.position.y += yDelta[frameIndex] * 0.6f
    }
}