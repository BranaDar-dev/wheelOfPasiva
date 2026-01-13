package com.bramish.wheelofpasiva.platform

/**
 * Android implementation of SoundPlayer using MediaPlayer.
 *
 * TODO: Add sound files to res/raw/ directory and implement actual playback.
 * See SOUND_IMPLEMENTATION_GUIDE.md for setup instructions.
 *
 * Current implementation is a no-op stub.
 */
actual class SoundPlayer actual constructor() {
    /**
     * Plays a sound effect once.
     * TODO: Implement with MediaPlayer
     */
    actual fun play(sound: GameSound) {
        // No-op: Sound files not yet added
        // Implementation: Use MediaPlayer to play sound from res/raw/
    }

    /**
     * Plays a sound effect in a loop.
     * TODO: Implement with MediaPlayer
     */
    actual fun playLoop(sound: GameSound) {
        // No-op: Sound files not yet added
        // Implementation: Use MediaPlayer with isLooping = true
    }

    /**
     * Stops a currently playing looped sound.
     * TODO: Implement with MediaPlayer
     */
    actual fun stop(sound: GameSound) {
        // No-op: Sound files not yet added
    }

    /**
     * Stops all currently playing sounds.
     * TODO: Implement with MediaPlayer
     */
    actual fun stopAll() {
        // No-op: Sound files not yet added
    }

    /**
     * Releases all sound resources.
     * TODO: Implement with MediaPlayer
     */
    actual fun release() {
        // No-op: Sound files not yet added
    }
}
