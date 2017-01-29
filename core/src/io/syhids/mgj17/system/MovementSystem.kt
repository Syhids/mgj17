package io.syhids.mgj17.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import io.syhids.mgj17.*

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

        if (entity is Mexican) {
            //Do not cross the border... of the screen
            if (entity.position.x < -WORLD_WIDTH/2 + 100f && entity.velocity.x < 0f) {
                entity.velocity.x = 1f
            }else if (entity.position.x > WORLD_WIDTH/2 - 100f && entity.velocity.x > 0f) {
                entity.velocity.x = -1f
            }
        }
    }
}