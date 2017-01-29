package io.syhids.mgj17.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import io.syhids.mgj17.*
import java.util.*

class TrumpMovementSystem : IteratingSystem(Family.all(
        TrumpComponent::class.java
).get()) {
    private var accDelta: Float = 0f
    private val difficuty: Float
        get() = accDelta + 1

    var state: State = State.IdlingFor(ms = 2000)

    val gameState by lazy { engine.getSystem(GameStateSystem::class.java) }

    sealed class State {
        class MovingTo(val x: Float) : State()
        class IdlingFor(val ms: Int, var mustShootAtStart: Boolean = true) : State() {
            var idledTime: Float = 0f

            val seconds: Float
                get() = ms / 1000f

            val idledEnough: Boolean
                get() = idledTime >= seconds
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (gameState.state is GameStateSystem.State.Countdown) {
            entity.velocity.x *= 0.86f
            return
        }

        accDelta += deltaTime

        val curState = state
        when (curState) {
            is State.MovingTo -> {
                val moveStep = deltaTime * 1f + Math.min(difficuty * 0.2f, 25f)

                if (entity.position.x < curState.x) {
                    entity.velocity.x += moveStep
                } else {
                    entity.velocity.x -= moveStep
                }

                if (Math.abs(Math.abs(entity.position.x) - Math.abs(curState.x)) < 10) {
                    val fixed = 2500 - Math.min(1200f, difficuty * 200f)
                    val variable = Random().nextInt(4400 - Math.min(4399f, difficuty * 400f).toInt())

                    state = State.IdlingFor(ms = (fixed + variable).toInt())
                }
            }
            is State.IdlingFor -> {
                if (curState.mustShootAtStart) {
                    curState.mustShootAtStart = false

                    shootWig(entity)
                }

                curState.idledTime += deltaTime

                if (curState.idledEnough) {
                    val finalTargetPosX = findRandomXInsideWorld(entity)
                    state = State.MovingTo(finalTargetPosX)
                }

                entity.velocity.x *= 0.88f
            }
        }
    }

    private fun findRandomXInsideWorld(entity: Entity): Float {
        var finalTargetPosX: Float

        val leftBounds = -WORLD_WIDTH / 3
        val rightBounds = WORLD_WIDTH / 3

        do {
            val delta = (Math.random() * 800 - 400).toInt()
            finalTargetPosX = entity.position.x + delta
        } while (finalTargetPosX < leftBounds || finalTargetPosX > rightBounds)

        return finalTargetPosX
    }

    private fun shootWig(entity: Entity) {
        val trumpAnim = entity.animation

        trumpAnim.reset()
        trumpAnim.state = AnimationComponent.State.PlayUntilFrame(0)
        val wig = Wig()
        wig.wigMovement.parent = entity
        wig.position.set(entity.position)
        engine.addEntity(wig)
    }
}