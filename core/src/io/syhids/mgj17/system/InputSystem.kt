package io.syhids.mgj17.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Input
import io.syhids.mgj17.MexicanComponent
import io.syhids.mgj17.keyPressed
import io.syhids.mgj17.movement

class InputSystem : IteratingSystem(Family.all(
        MexicanComponent::class.java
).get()) {
    var enabled: Boolean = true

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (!enabled) {
            entity.movement.shouldMoveLeft = false
            entity.movement.shouldMoveRight = false
            return
        }

        entity.movement.shouldMoveLeft = keyPressed(Input.Keys.LEFT)
        entity.movement.shouldMoveRight = keyPressed(Input.Keys.RIGHT)
    }
}