package io.syhids.mgj17

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import io.syhids.mgj17.system.*

@JvmField
val WORLD_WIDTH = 1280
@JvmField
val WORLD_HEIGHT: Int = (WORLD_WIDTH * 6 / 10).toInt()

class Trumpocalypse : ApplicationAdapter() {
    lateinit var batch: SpriteBatch
    val engine = Engine()
    lateinit var mexican: Mexican
    lateinit var trump: Trump
    lateinit var camera: OrthographicCamera
    lateinit var font: BitmapFont
    lateinit var bigFont: BitmapFont

    var GAME_SPEED = 2f

    override fun create() {
        Sounds.preload()
        font = generateFont(56)
        bigFont = generateFont(128)

        batch = SpriteBatch()

        mexican = Mexican()
        trump = Trump()

        camera = OrthographicCamera(WORLD_WIDTH.toFloat(), WORLD_HEIGHT.toFloat())

        engine.addEntity(mexican)
        engine.addEntity(trump)

        val relative1 = TrumpRelative()
        relative1.position.x = (-WORLD_WIDTH / 2 + relative1.sprite.width).toFloat()
        val relative2 = TrumpRelative()
        relative2.position.x = (WORLD_WIDTH / 2 - relative2.sprite.width).toFloat()

        engine.addEntity(relative1)
        engine.addEntity(relative2)

        engine.addEntity(Wall())
        engine.addEntity(Background())
        val deportedSheet = DeportedSheet()
        engine.addEntity(deportedSheet)
        engine.addEntity(Menu())

        engine.addSystem(InputSystem())
        engine.addSystem(TrumpMovementSystem())
        engine.addSystem(WigMovementSystem())
        engine.addSystem(TrumpRelativeShootingSystem())
        engine.addSystem(MovementSystem())
        engine.addSystem(AccelerationSystem())
        engine.addSystem(AnimationSystem())
        engine.addSystem(WigMexicanCollisionSystem())
        engine.addSystem(SpriteDrawingSystem(batch, camera))
        engine.addSystem(GameStateSystem(batch, bigFont, deportedSheet))
        engine.addSystem(TrumpPhrasesSystem())
        engine.addSystem(TimeDrawingSystem(font, batch))
    }

    private fun generateFont(size: Int): BitmapFont {
        val generator = FreeTypeFontGenerator(Gdx.files.internal("ALIN_KID.ttf"))
        val parameter = FreeTypeFontParameter()
        parameter.size = size
        parameter.color = Color.WHITE
        parameter.borderColor = Color.BLACK
        parameter.borderWidth = 3f
        val font = generator.generateFont(parameter)
        generator.dispose()

        return font
    }

    private var time: Float = 0f

    override fun render() {
        val dt = Gdx.graphics.deltaTime * GAME_SPEED

        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        engine.update(dt)

        time += Gdx.graphics.deltaTime
    }

    override fun dispose() {
        batch.dispose()
    }
}