package io.syhids.mgj17.system

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import io.syhids.mgj17.WORLD_HEIGHT
import io.syhids.mgj17.WORLD_WIDTH

class TimeDrawingSystem(val font: BitmapFont, val batch: SpriteBatch) : EntitySystem(1000) {
    val gameState by lazy { engine.getSystem(GameStateSystem::class.java) }

    private var timeSeconds: Int = 0

    override fun update(deltaTime: Float) {
        when (gameState.state) {
            GameStateSystem.State.Playing -> timeSeconds = gameState.realAccDelta.toInt()
            GameStateSystem.State.Countdown -> timeSeconds = 0
        }

        batch.begin()
        font.draw(batch,
                "$timeSeconds}",
                (WORLD_WIDTH / 2).toFloat() - 8f,
                (WORLD_HEIGHT / 2).toFloat() - 8f,
                0f, //Target width
                Align.topRight,
                false
        )
        batch.end()
    }
}