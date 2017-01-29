package io.syhids.mgj17.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Align
import io.syhids.mgj17.*

class GameStateSystem(val batch: SpriteBatch, val font: BitmapFont, val deportedSheet: DeportedSheet, val camera: OrthographicCamera) : EntitySystem(1) {
    sealed class State {
        object None : State()
        object Menu : State()
        object Countdown : State()
        object Playing : State()
        object Lost : State()
    }

    var previousState: State = State.None
    var state: State = State.Menu

    var accDelta: Float = 0f
    var realAccDelta: Float = 0f

    override fun addedToEngine(engine: Engine?) {
        accDelta = 0f
    }

    override fun update(deltaTime: Float) {
        val inputSystem = engine.getSystem(InputSystem::class.java)

        accDelta += deltaTime
        realAccDelta += Gdx.graphics.deltaTime

        if (previousState != state) {
            onStateTransitioned(previousState, state)
            previousState = state
        }

        inputSystem.enabled = state is State.Playing

        when (state) {
            State.Playing -> {
            }
            State.Countdown -> {
                val countdown = Math.max((4 - realAccDelta).toInt(), 0)
                batch.begin()
                font.draw(batch, "$countdown", 0f, 100f, 0f, Align.center, false)
                batch.end()

                if (realAccDelta > 3f) {
                    state = State.Playing
                }
            }
            State.Lost -> {
                if (Gdx.input.justTouched()) {
                    val clickPos = clickPositionInGameCoords()
                    log("Click at $clickPos")

                    if (deportedSheet.isPlayButtonClicked(clickPos)) {
                        Sounds.buttonSound.playMe()
                        state = State.Countdown
                    } else if (deportedSheet.isExitButtonClicked(clickPos)) {
                        Sounds.buttonSound.playMe()
                        Gdx.app.exit()
                    }
                }
            }
            State.Menu -> {
                if (Gdx.input.justTouched()) {
                    val clickPos = clickPositionInGameCoords()
                    log("Click at $clickPos")

                    if (menu.isPlayButtonClicked(clickPos)) {
                        Sounds.buttonSound.playMe()
                        state = State.Countdown
                    } else if (menu.isExitButtonClicked(clickPos)) {
                        Sounds.buttonSound.playMe()
                        Gdx.app.exit()
                    }
                }
            }
        }
    }

    private fun clickPositionInGameCoords(): Vector2 {
        val windowX = Gdx.input.x.toFloat()
        val windowY = Gdx.input.y.toFloat()
        val gamePos = camera.unproject(Vector3(windowX, windowY, 0f))

        log("Window($windowX, $windowY) -> Game(${gamePos.x}, ${gamePos.y})")
        return Vector2(gamePos.x, gamePos.y)
    }

    val menu by lazy { engine.getEntitiesFor(Family.all(MenuComponent::class.java).get()).first() as Menu }

    private fun onStateTransitioned(previousState: State, newState: State) {
        accDelta = 0f
        realAccDelta = 0f

        when (newState) {
            State.Menu -> {
                Sounds.musicMenu.playMe()
                menu.sprite.visible = true
            }
            State.Countdown -> {
                Sounds.musicMenu.stop()
                menu.sprite.visible = false
                resetPlayingEntitiesState()
            }
            State.Playing -> {
                resetPlayingEntitiesState()
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

    private fun resetPlayingEntitiesState() {
        engine.getEntitiesFor(Family.all(WigMovementComponent::class.java).get()).forEach {
            engine.removeEntity(it)
        }

        engine.getEntitiesFor(Family.all(MexicanComponent::class.java).get()).forEach {
            it.position.x = 0f
        }

        deportedSheet.sprite.visible = false
    }
}