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
    val mexican by lazy { engine.getEntitiesFor(Family.all(MexicanComponent::class.java).get()).first() }

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

    fun reset() {
        accDelta = 0f
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {

        when (gameState.state) {
            is GameStateSystem.State.Countdown -> {
                entity.velocity.x *= 0.86f
                return
            }
            is GameStateSystem.State.Lost -> {
                entity.velocity.x *= 0.86f
                return
            }
            is GameStateSystem.State.Menu -> {
                return
            }
        }

        accDelta += deltaTime

        val curState = state
        when (curState) {
            is State.MovingTo -> {
                val moveStep = deltaTime * (18f + Math.min(difficuty*difficuty/4, 1000f))

                if (entity.position.x < curState.x) {
                    entity.velocity.x += moveStep
                } else {
                    entity.velocity.x -= moveStep
                }

                if (Math.abs(Math.abs(entity.position.x) - Math.abs(curState.x)) < 24) {
                    val fixed = 2400 - Math.min(1500f, difficuty * 50f)
                    val variable = Random().nextInt(5000 - Math.min(4400f, difficuty * 50f).toInt())

                    state = State.IdlingFor(ms = (fixed + variable).toInt())
                }
            }
            is State.IdlingFor -> {
                if (curState.mustShootAtStart) {
                    curState.mustShootAtStart = false

                    val rnd = Math.random()
                    if (rnd < 0.1f) {
                        shootWig(entity)
                        shootMoney(entity)
                    } else if (rnd < 0.25f) {
                        shootMoney(entity)
                    } else {
                        shootWig(entity)
                    }
                }

                curState.idledTime += deltaTime

                if (curState.idledEnough) {
                    val finalTargetPosX = findRandomXInsideWorld(entity)
                    state = State.MovingTo(finalTargetPosX)
                }

                entity.velocity.x *= 0.92f - Math.min(0.15f, difficuty * 0.0015f)
            }
        }
    }

    private fun findRandomXInsideWorld(trump: Entity): Float {
        var finalTargetPosX: Float

        val leftBounds = -WORLD_WIDTH / 3
        val rightBounds = WORLD_WIDTH / 3
        var exitLoopIn = 10

        do {
            var delta = (100f + Math.random() * 400).toInt()

            if (mexican.position.x < trump.position.x) {
                delta *= -1
            }

            finalTargetPosX = trump.position.x + delta
            exitLoopIn--

            if (exitLoopIn <= 0)
                break
        } while (finalTargetPosX < leftBounds || finalTargetPosX > rightBounds)

        return finalTargetPosX
    }

    private fun shootWig(entity: Entity) {
        val trumpAnim = entity.animation

        if (trumpAnim.state !is AnimationComponent.State.PlayUntilFrame) {
            trumpAnim.reset()
            trumpAnim.state = AnimationComponent.State.PlayUntilFrame(0)
        }
        val wig = Wig()
        wig.throwable.parent = entity
        wig.position.set(entity.position)
        engine.addEntity(wig)
    }

    private fun shootMoney(entity: Entity) {
        val trumpAnim = entity.animation

        trumpAnim.reset()
        trumpAnim.state = AnimationComponent.State.PlayUntilFrame(0)
        val money = Money()
        money.throwable.parent = entity
        money.position.set(entity.position)
        engine.addEntity(money)
    }
}