package io.syhids.mgj17

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class Trumpocalypse : ApplicationAdapter() {
    lateinit var batch: SpriteBatch
    val engine = Engine()
    lateinit var mexican: Mexican
    lateinit var trump: Trump

    @JvmField
    val WORLD_WIDTH = 1300
    @JvmField
    val WORLD_HEIGHT = WORLD_WIDTH * 6 / 10

    var GAME_SPEED = 1f

    override fun create() {
        batch = SpriteBatch()

        mexican = Mexican()
        trump = Trump()
        val wig = Wig()

        val camera = OrthographicCamera(WORLD_WIDTH.toFloat(), WORLD_HEIGHT.toFloat())

        engine.addEntity(mexican)
        engine.addEntity(trump)
        engine.addEntity(wig)

        engine.addSystem(InputSystem())
        engine.addSystem(TrumpShootSystem())
        engine.addSystem(MovementSystem())
        engine.addSystem(AccelerationSystem())
        engine.addSystem(AnimationSystem())
        engine.addSystem(SpriteDrawingSystem(batch, camera))
    }

    override fun render() {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        engine.update(Gdx.graphics.deltaTime * GAME_SPEED)
    }

    override fun dispose() {
        batch.dispose()
    }
}
