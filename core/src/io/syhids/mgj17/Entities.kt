package io.syhids.mgj17

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture

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
        add(VelocityComponent())
        add(PositionComponent(y = -300f))
        add(MexicanComponent())
        add(SpriteComponent(scale = 0.3f))
        add(MovableComponent())
        add(AnimationComponent(animation = MEXICAN_ANIMATION, speed = 8f))
    }
}

class Trump : Entity() {
    init {
        add(TrumpComponent())
        add(VelocityComponent())
        add(PositionComponent(y = 200f))
        add(SpriteComponent(scale = 0.26f))
        val animationComponent = AnimationComponent(animation = TRUMP_ANIMATION, speed = 1.5f)
        animationComponent.state = AnimationComponent.State.Paused
        add(animationComponent)
    }
}

class TrumpRelative : Entity() {
    init {
        add(TrumpRelativeComponent())
        add(VelocityComponent())
        add(PositionComponent(y = 200f))
        add(SpriteComponent(scale = 0.14f, img = TRUMP_ANIMATION.frames[0].texture))
        val animationComponent = AnimationComponent(animation = TRUMP_ANIMATION, speed = 1.2f)
        animationComponent.state = AnimationComponent.State.Paused
        add(animationComponent)
    }
}

class Wig: Entity() {
    init {
        add(WigMovementComponent())
        add(VelocityComponent())
        add(PositionComponent(y = 100f))
        add(SpriteComponent(scale = 0.2f, visible = false, img = Texture("badlogic.jpg")))
    }
}