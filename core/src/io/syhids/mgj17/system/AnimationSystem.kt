package io.syhids.mgj17.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import io.syhids.mgj17.AnimationComponent
import io.syhids.mgj17.SpriteComponent
import io.syhids.mgj17.animation
import io.syhids.mgj17.sprite

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