package com.bramish.wheelofpasiva.platform

/**
 * Sound effects used in the game.
 */
enum class GameSound {
    /** Wheel spinning sound - continuous spinning sound */
    WHEEL_SPIN,

    /** Correct letter guess - positive feedback */
    CORRECT_ANSWER,

    /** Incorrect letter guess - negative feedback */
    INCORRECT_ANSWER,

    /** Landing on Bankrupt - dramatic negative sound */
    BANKRUPT,

    /** Landing on Extra Turn - positive jingle */
    EXTRA_TURN
}

/**
 * Platform-specific sound player interface.
 * Uses expect/actual pattern for Android and iOS implementations.
 */
expect class SoundPlayer() {
    /**
     * Plays a sound effect once.
     * @param sound The sound to play
     */
    fun play(sound: GameSound)

    /**
     * Plays a sound effect in a loop.
     * @param sound The sound to play
     */
    fun playLoop(sound: GameSound)

    /**
     * Stops a currently playing looped sound.
     * @param sound The sound to stop
     */
    fun stop(sound: GameSound)

    /**
     * Stops all currently playing sounds.
     */
    fun stopAll()

    /**
     * Releases all sound resources.
     * Should be called when the sound player is no longer needed.
     */
    fun release()
}
