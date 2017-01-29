package io.syhids.mgj17.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import io.syhids.mgj17.TrumpRelativeComponent
import io.syhids.mgj17.Wig
import io.syhids.mgj17.position
import io.syhids.mgj17.wigMovement

class TrumpRelativeShootingSystem : IteratingSystem(Family.all(
        TrumpRelativeComponent::class.java
).get()) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val trumpRelative = entity.getComponent(TrumpRelativeComponent::class.java)

        trumpRelative.timeLeftToShoot -= deltaTime

        if (trumpRelative.timeLeftToShoot <= 0f) {
            trumpRelative.timeLeftToShoot = (1 + Math.random() * 2).toFloat()

            val wig = Wig()
            wig.wigMovement.parent = entity
            wig.position.set(entity.position)
            engine.addEntity(wig)
        }
    }
}