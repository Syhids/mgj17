package io.syhids.mgj17

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

val Entity.position: PositionComponent
    get() = getComponent(PositionComponent::class.java)

val Entity.velocity: VelocityComponent
    get() = getComponent(VelocityComponent::class.java)

val Entity.sprite: SpriteComponent
    get() = getComponent(SpriteComponent::class.java)

val Entity.movement: MovableComponent
    get() = getComponent(MovableComponent::class.java)

val Entity.animation: AnimationComponent
    get() = getComponent(AnimationComponent::class.java)

val Entity.wigMovement: WigMovementComponent
    get() = getComponent(WigMovementComponent::class.java)

val Entity.collider: ColliderComponent
    get() = getComponent(ColliderComponent::class.java)

val TRUMP_ANIMATION = Animation(
        Frame("t1.png", 1000),
        Frame("t2.png", 100),
        Frame("t3.png", 100),
        Frame("t4.png", 50),
        Frame("t5.png", 500),
        Frame("t6.png", 100),
        Frame("t7.png", 50),
        Frame("t8.png", 100),
        Frame("t9.png", 200),
        Frame("t10.png", 1000)
)

val MEXICAN_ANIMATION = Animation(
        Frame("m_1.png", 4000),
        Frame("m_2.png", 1000),
        Frame("m_3.png", 3000),
        Frame("m_4.png", 1000)
)

class Mexican : Entity() {
    init {
        val scale = 0.3f
        add(VelocityComponent())
//        add(PositionComponent(y = yAlignBottom(MEXICAN_ANIMATION.frames[0].texture, scale = 0.3f)))
        add(PositionComponent(y = -294f))
        val tex = MEXICAN_ANIMATION.frames[0].texture
        add(ColliderComponent(width = scale * tex.width*0.4f, height = scale * tex.height*0.4f))
        add(MexicanComponent())
        add(SpriteComponent(scale = scale))
        add(MovableComponent())
        add(AnimationComponent(animation = MEXICAN_ANIMATION, speed = 8f))
    }
}

class Trump : Entity() {
    init {
        add(TrumpComponent())
        add(VelocityComponent())
        add(PositionComponent(y = 192f))
        add(SpriteComponent(scale = 0.28f))
        val animationComponent = AnimationComponent(animation = TRUMP_ANIMATION, speed = 1.5f)
        animationComponent.state = AnimationComponent.State.Paused
        add(animationComponent)
    }
}

class TrumpRelative : Entity() {
    init {
        add(TrumpRelativeComponent())
        add(VelocityComponent())
        add(PositionComponent(y = 160f))
        add(SpriteComponent(scale = 0.14f, img = TRUMP_ANIMATION.frames[0].texture))
        val animationComponent = AnimationComponent(animation = TRUMP_ANIMATION, speed = 1.2f)
        animationComponent.state = AnimationComponent.State.Paused
        add(animationComponent)
    }
}

class Wig : Entity() {
    init {
        val tex = Texture("peluca.png")
        val scale = 0.15f

        add(WigMovementComponent())
        add(VelocityComponent())
        add(ColliderComponent(width = tex.width*scale, height = tex.height*scale))
        add(PositionComponent(y = 0f))
        add(SpriteComponent(scale = scale, visible = false, img = tex))
    }
}

fun yAlignBottom(tex: Texture, scale: Float = 1f): Float {
    return -(WORLD_HEIGHT / 2 - tex.height*scale / 2)
}

class Wall : Entity() {
    init {
        val texture = Texture("Muro.png")
        add(PositionComponent(y = yAlignBottom(texture)))
        add(SpriteComponent(img = texture, depth = -1))
    }
}

class Background : Entity() {
    init {
        val texture = Texture("background.png")
        add(PositionComponent(y = yAlignBottom(texture) + 115f))
        add(SpriteComponent(img = texture, depth = -2))
    }
}

class DeportedSheet : Entity() {
    init {
        add(PositionComponent(x = -50f))
        add(SpriteComponent(img = Texture("Muerte.png"), scale = 0.5f, visible = false, depth = 10))
    }

    private val BUTTON_WIDTH = 140f
    private val BUTTON_HEIGHT = 68f

    val playButtonArea = Rectangle(-154f, -189f, BUTTON_WIDTH, BUTTON_HEIGHT)
    val exitButtonArea = Rectangle(23f, -189f, BUTTON_WIDTH, BUTTON_HEIGHT)

    fun isPlayButtonClicked(clickPos: Vector2): Boolean {
        return playButtonArea.contains(clickPos)
    }

    fun isExitButtonClicked(clickPos: Vector2): Boolean {
        return exitButtonArea.contains(clickPos)
    }
}

class Menu : Entity(){
    init {
        add(MenuComponent())
        add(PositionComponent(y = 26f))
        add(SpriteComponent(img = Texture("titulo.png"), scale = 1f, visible = false, depth = 1000))
    }

    val playButtonArea = Rectangle(-575f, -138f, 370f, 140f)
    val exitButtonArea = Rectangle(-575f, -323f, 370f, 140f)

    fun isPlayButtonClicked(clickPos: Vector2): Boolean {
        return playButtonArea.contains(clickPos)
    }

    fun isExitButtonClicked(clickPos: Vector2): Boolean {
        return exitButtonArea.contains(clickPos)
    }
}