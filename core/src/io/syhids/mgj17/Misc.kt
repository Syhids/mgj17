package io.syhids.mgj17

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.gdx.Gdx
import kotlin.reflect.KClass

@Suppress("NOTHING_TO_INLINE")
inline fun Any.log(message : String) {
    Gdx.app.log(javaClass.simpleName, message)
}

fun <T : Component> component(componentClass: KClass<T>) = ComponentMapper.getFor(componentClass.java)
fun keyPressed(key: Int) = Gdx.input.isKeyPressed(key)