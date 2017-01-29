package io.syhids.mgj17.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Rectangle
import io.syhids.mgj17.MexicanComponent
import io.syhids.mgj17.WigMovementComponent
import io.syhids.mgj17.collider
import io.syhids.mgj17.position

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

