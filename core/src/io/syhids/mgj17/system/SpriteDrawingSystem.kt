package io.syhids.mgj17.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.syhids.mgj17.PositionComponent
import io.syhids.mgj17.SpriteComponent
import io.syhids.mgj17.component
import io.syhids.mgj17.sprite

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