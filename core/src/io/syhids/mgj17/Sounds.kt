package io.syhids.mgj17

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import io.syhids.mgj17.Sounds.MUTED
import io.syhids.mgj17.Sounds.VOLUME
import java.util.*

fun Sound.playMe() {
    play(VOLUME)
}

fun Music.playMe() {
    volume = VOLUME
    if (MUTED) volume = 0f
    if (!isPlaying) {
        play()
    }
}

object Sounds {
    val VOLUME = 0.05f
    private val random = Random()

    fun randomPhrase(): Sound {
        val index = random.nextInt(8)
        return phrase(index)
    }

    private val phrases by lazy { (0..7).map { index -> createSound("Frase$index.mp3") } }

    fun phrase(index: Int): Sound = phrases[index]

    private fun createSound(path: String) = Gdx.audio.newSound(asset(path))

    val musicMenu by lazy { Gdx.audio.newMusic(asset("inicio.mp3")) }
    val musicDeath by lazy { Gdx.audio.newMusic(asset("muerte.mp3")) }
    val musicGame by lazy { Gdx.audio.newMusic(asset("juego.mp3")) }
    val buttonSound by lazy { Gdx.audio.newSound(asset("Boton.mp3")) }

    private fun asset(assetFile: String) = Gdx.files.internal(assetFile)

    fun preload() {
        (0..7).forEach { index -> phrase(index) }

        musicMenu
        musicGame
        musicDeath
        buttonSound
    }

    private val allMusic by lazy { listOf(musicMenu, musicGame, musicDeath) }

    var MUTED: Boolean = false
        set(value) {
            field = value
            allMusic.forEach { if (it.isPlaying) musicGame.playMe() }
        }
}