package io.syhids.mgj17.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import io.syhids.mgj17.*

class CannonShootSystem : IteratingSystem(Family.all(
        CannonComponent::class.java
).get()) {

    val gameState by lazy { engine.getSystem(GameStateSystem::class.java) }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val cannon = entity.getComponent(CannonComponent::class.java)
        val gameState = gameState.state
        if (gameState is GameStateSystem.State.Countdown || gameState is GameStateSystem.State.Menu)
            return

        val curState = cannon.state

        when(curState) {
            CannonComponent.State.Inactive -> {
                cannon.state = CannonComponent.State.WaitingToShoot((1 + Math.random() * 2).toFloat())
            }
            is CannonComponent.State.WaitingToShoot -> {
                curState.timeLeft -= deltaTime

                if (curState.timeLeft <= 0f) {
                    val animationDuration = entity.animation.animation.totalDurationMs/1000 - 1.5f
                    cannon.state = CannonComponent.State.AnimatingShoot(animationDuration)
                    entity.animation.reset()
                    entity.animation.state = AnimationComponent.State.PlayUntilFrame(0)
                }
            }
            is CannonComponent.State.AnimatingShoot -> {
                curState.timeLeft -= deltaTime
                if (curState.timeLeft <= 0f) {

                    val wig = Wig()
                    wig.throwable.parent = entity
                    wig.position.set(entity.position)
                    wig.position.y -= 90f
                    engine.addEntity(wig)

                    cannon.state = CannonComponent.State.Inactive
                }
            }
        }
    }
}