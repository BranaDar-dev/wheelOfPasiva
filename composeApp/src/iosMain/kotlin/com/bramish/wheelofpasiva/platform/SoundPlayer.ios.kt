package com.bramish.wheelofpasiva.platform

/**
 * iOS implementation of SoundPlayer using AVAudioPlayer.
 *
 * TODO: Add sound files to iOS bundle and implement actual playback.
 * See SOUND_IMPLEMENTATION_GUIDE.md for setup instructions.
 *
 * Current implementation is a no-op stub.
 */
actual class SoundPlayer actual constructor() {
    /**
     * Plays a sound effect once.
     * TODO: Implement with AVAudioPlayer
     */
    actual fun play(sound: GameSound) {
        // No-op: Sound files not yet added
        // Implementation: Use AVAudioPlayer to play sound from bundle
    }

    /**
     * Plays a sound effect in a loop.
     * TODO: Implement with AVAudioPlayer
     */
    actual fun playLoop(sound: GameSound) {
        // No-op: Sound files not yet added
        // Implementation: Use AVAudioPlayer with numberOfLoops = -1
    }

    /**
     * Stops a currently playing looped sound.
     * TODO: Implement with AVAudioPlayer
     */
    actual fun stop(sound: GameSound) {
        // No-op: Sound files not yet added
    }

    /**
     * Stops all currently playing sounds.
     * TODO: Implement with AVAudioPlayer
     */
    actual fun stopAll() {
        // No-op: Sound files not yet added
    }

    /**
     * Releases all sound resources.
     * TODO: Implement with AVAudioPlayer
     */
    actual fun release() {
        // No-op: Sound files not yet added
    }
}
