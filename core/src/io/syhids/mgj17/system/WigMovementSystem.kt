package io.syhids.mgj17.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import io.syhids.mgj17.*

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
            WigMovementComponent.WigState.Invisible -> {
                if (parent is Trump) {
                    wig.wigMovement.state = WigMovementComponent.WigState.InTrumpsHands
                } else {
                    wig.wigMovement.state = WigMovementComponent.WigState.Falling
                    wig.velocity.y = -INITIAL_VELOCITY
                }

                wig.sprite.visible = false
            }
            WigMovementComponent.WigState.InTrumpsHands -> {
                val frameIndex = parent.animation.currentAnimationIndex

                wig.sprite.visible = frameIndex != 0

                wig.sprite.alpha = when (frameIndex) {
                    1 -> 0.5f
                    2 -> 0.75f
                    else -> 1f
                }

                applyPositionOfFrame(wig, frameIndex)

                if (parent.animation.currentAnimationIndex >= parent.animation.animation.lastFrameIndex - 1) {
                    wig.wigMovement.state = WigMovementComponent.WigState.Falling
                    wig.velocity.y = -INITIAL_VELOCITY

                    wig.sprite.visible = true
                    wig.sprite.alpha = 1f
                    applyPositionOfFrame(wig, parent.animation.animation.lastFrameIndex - 1)
                }
            }
            WigMovementComponent.WigState.Falling -> {
                wig.sprite.rotation += deltaTime*180

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