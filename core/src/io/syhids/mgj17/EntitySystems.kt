package io.syhids.mgj17

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch

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

        sprite.img = animation.getCurrentAnimation()
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

        batch.begin()
        batch.projectionMatrix = camera.combined
        super.update(deltaTime)
        batch.end()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val position = position.get(entity)
        val sprite = sprite.get(entity)

        val posX = position.x - sprite.midWidth
        val posY = position.y - sprite.midHeight
        batch.draw(sprite.img, posX, posY, sprite.width.toFloat(), sprite.height.toFloat())
    }
}