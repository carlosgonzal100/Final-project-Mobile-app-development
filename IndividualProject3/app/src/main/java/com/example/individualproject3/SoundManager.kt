package com.example.individualproject3

import android.content.Context
import android.media.MediaPlayer

/**
 * Plays all sound effects used throughout the game.
 *
 * Each sound is stored in /res/raw and played using a short MediaPlayer instance
 * that automatically releases itself after completion.
 */
class SoundManager(private val context: Context) {

    /** Plays victory sound when the player reaches the goal. */
    fun playSuccess() {
        playRawSound(R.raw.zelda_victory_ost)
    }

    /** Plays failure sound for general losing conditions (no-goal, out-of-bounds, etc.). */
    fun playFailure() {
        playRawSound(R.raw.game_over_sound)
    }

    /**
     * Plays the “bonk” sound when the hero hits a wall.
     * Currently uses bump_into_wall. Swap the raw file later if desired.
     */
    fun playBonk() {
        playRawSound(R.raw.bump_into_wall)
    }

    /** Plays splash sound when hero falls into water. */
    fun playSplash() {
        playRawSound(R.raw.falling_in_water)
    }

    /**
     * Core sound-playing function.
     * Creates a MediaPlayer for each effect, plays it, then releases it safely.
     */
    private fun playRawSound(resId: Int) {
        val player = MediaPlayer.create(context, resId)

        player?.apply {
            setOnCompletionListener { it.release() }
            start()
        }
    }
}
