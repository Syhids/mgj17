package io.syhids.mgj17.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import io.syhids.mgj17.Sounds
import io.syhids.mgj17.playMe

class GameStateSystem(val batch: SpriteBatch, val font: BitmapFont) : EntitySystem(1) {
    sealed class State {
        object None : State()
        object Playing : State()
        object Lost : State()
    }

    var previousState: State = State.None
    var state: State = State.Playing

    var accDelta: Float = 0f

    override fun addedToEngine(engine: Engine?) {
        accDelta = 0f
    }

    override fun update(deltaTime: Float) {
        val inputSystem = engine.getSystem(InputSystem::class.java)

        accDelta += deltaTime

        if (previousState != state) {
            onStateTransitioned(previousState, state)
            previousState = state
        }

        inputSystem.enabled = state is State.Playing

        when (state) {
            State.Playing -> {

            }
            State.Lost -> {
                batch.begin()
                font.draw(batch, "YOU DED", 0f, 0f, 0f, Align.center, false)
                batch.end()

                if (accDelta >= 5f) {
                    state = State.Playing
                }
            }
        }
    }

    private fun onStateTransitioned(previousState: State, newState: State) {
        accDelta = 0f

        when (newState) {
            State.Playing -> {
                val gameMusic = Sounds.musicGame
                gameMusic.isLooping = true
                gameMusic.playMe()
            }
            State.Lost -> {
                Sounds.musicGame.stop()
                val deathMusic = Sounds.musicDeath
                deathMusic.playMe()
            }
        }
    }

}