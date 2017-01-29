package io.syhids.mgj17.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import io.syhids.mgj17.*

class GameStateSystem(val batch: SpriteBatch, val font: BitmapFont, val deportedSheet: DeportedSheet) : EntitySystem(1) {
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
                if (Gdx.input.justTouched()) {
                    val clickPos = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
                    log("Click at $clickPos")

                    if (deportedSheet.isPlayButtonClicked(clickPos)) {
                        state = State.Playing
                    } else if (deportedSheet.isExitButtonClicked(clickPos)) {
                        Gdx.app.exit()
                    }
                }

//                batch.begin()
//                font.draw(batch, "YOU DED", 0f, 0f, 0f, Align.center, false)
//                batch.end()
            }
        }
    }

    private fun onStateTransitioned(previousState: State, newState: State) {
        accDelta = 0f

        when (newState) {
            State.Playing -> {
                engine.getEntitiesFor(Family.all(WigMovementComponent::class.java).get()).forEach {
                    engine.removeEntity(it)
                }

                engine.getEntitiesFor(Family.all(MexicanComponent::class.java).get()).forEach {
                    it.position.x = 0f
                }

                deportedSheet.sprite.visible = false
                val gameMusic = Sounds.musicGame
                gameMusic.isLooping = true
                gameMusic.playMe()
            }
            State.Lost -> {
                deportedSheet.sprite.visible = true
                Sounds.musicGame.stop()
                val deathMusic = Sounds.musicDeath
                deathMusic.playMe()
            }
        }
    }

}