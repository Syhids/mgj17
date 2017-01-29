package io.syhids.mgj17.system

import com.badlogic.ashley.core.EntitySystem
import io.syhids.mgj17.system.GameStateSystem
import io.syhids.mgj17.Sounds
import io.syhids.mgj17.playMe

class TrumpPhrasesSystem : EntitySystem(400) {
    val gameState by lazy { engine.getSystem(GameStateSystem::class.java) }
    var accDelta: Float = 0f

    var nextTotalDelay : Float = randomNextTotalDelay()

    override fun update(deltaTime: Float) {
        if (gameState.state is GameStateSystem.State.Playing) {
            accDelta += deltaTime

            if (accDelta > nextTotalDelay) {
                nextTotalDelay = randomNextTotalDelay()
                accDelta = 0f
                val phrase = Sounds.randomPhrase()
                phrase.playMe()
            }
        }
    }

    private fun randomNextTotalDelay() : Float = (8f + Math.random() * 8).toFloat()
}