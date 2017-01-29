package io.syhids.mgj17.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Rectangle
import io.syhids.mgj17.*

class MexicanCollisionSystem : IteratingSystem(Family.all(
        MexicanComponent::class.java
).get()) {

    fun rectangleFrom(entity: Entity): Rectangle {
        val collider = entity.collider
        val position = entity.position

        return Rectangle(position.x, position.y, collider.width, collider.height)
    }

    val gameStateSystem by lazy { engine.getSystem(GameStateSystem::class.java) }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val mexicanRect = rectangleFrom(entity)

        engine.getEntitiesFor(Family.all(
                ThrowableComponent::class.java
        ).get()).forEach { affectedEntity ->
            val rect = rectangleFrom(affectedEntity)

            if (rect.overlaps(mexicanRect)) {
                if (gameStateSystem.state is GameStateSystem.State.Playing) {
                    if (affectedEntity is Wig) {
                        engine.removeEntity(affectedEntity)
                        gameStateSystem.state = GameStateSystem.State.Lost
                    } else {
                        engine.removeEntity(affectedEntity)
                        gameStateSystem.money++
                    }
                }
            }
        }
    }
}

