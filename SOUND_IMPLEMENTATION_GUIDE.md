# Sound Implementation Guide

This project includes the infrastructure for game sound effects. The sound files need to be added to complete the implementation.

## Current Status

✅ Sound player interface created (`SoundPlayer.kt`)
✅ Android implementation ready (`SoundPlayer.android.kt`)
✅ iOS implementation ready (`SoundPlayer.ios.kt`)
⏳ Sound files need to be added
⏳ Android res/raw directory needs to be created
⏳ iOS bundle resources need to be configured

## Sound Files Needed

Add the following sound files to enable audio:

### For Android:
Create directory: `composeApp/src/androidMain/res/raw/`

Add these files:
- `wheel_spin.mp3` - Wheel spinning sound (2-3s, looping)
- `correct_answer.mp3` - Correct letter sound (0.5-1s)
- `incorrect_answer.mp3` - Wrong letter sound (0.5-1s)
- `bankrupt.mp3` - Bankrupt sound (1-2s)
- `extra_turn.mp3` - Extra turn bonus sound (1-2s)

### For iOS:
Add sound files to the iOS app bundle via Xcode:
1. Open `iosApp.xcodeproj` in Xcode
2. Add files to the project (File > Add Files...)
3. Ensure "Copy items if needed" is checked
4. Add to target: `iosApp`

Files needed (same as Android):
- `wheel_spin.mp3`
- `correct_answer.mp3`
- `incorrect_answer.mp3`
- `bankrupt.mp3`
- `extra_turn.mp3`

## Sound Resources

Free sound effect sources:
1. **Freesound.org** - Search terms:
   - "wheel spin", "ratchet", "game show"
   - "success", "correct", "bell"
   - "buzzer", "wrong", "error"
   - "sad trombone", "fail"
   - "power up", "bonus", "fanfare"

2. **Pixabay.com** - Free game sound effects section

3. **Zapsplat.com** - Free with attribution

4. **OpenGameArt.org** - CC-licensed game audio

## Recommended Sounds

### Wheel Spin
- Ratchet clicking sound
- Game show wheel spinning
- Continuous loop-friendly sound

### Correct Answer
- Bell ding (single note)
- Success chime
- Positive beep

### Incorrect Answer
- Buzzer sound
- Wrong answer tone
- Short error sound

### Bankrupt
- Sad trombone
- Losing horn
- Dramatic negative chord

### Extra Turn
- Power-up sound
- Ascending chime
- Short fanfare

## Integration with GameViewModel

Once sound files are added, integrate into `GameViewModel.kt`:

```kotlin
class GameViewModel(
    // ... existing parameters
    private val soundPlayer: SoundPlayer?
) : ViewModel() {

    fun spinWheel() {
        // Start wheel spin sound
        soundPlayer?.playLoop(GameSound.WHEEL_SPIN)

        // ... existing spin logic

        // Stop wheel spin sound after animation
        viewModelScope.launch {
            delay(2000) // Match wheel animation duration
            soundPlayer?.stop(GameSound.WHEEL_SPIN)

            // Play result sound based on slice
            when (lastSliceResult) {
                is WheelSlice.Bankrupt -> soundPlayer?.play(GameSound.BANKRUPT)
                is WheelSlice.ExtraTurn -> soundPlayer?.play(GameSound.EXTRA_TURN)
                else -> {} // Points slices don't need a sound
            }
        }
    }

    fun guessLetter(letter: Char) {
        // ... existing guess logic

        // Play sound based on result
        if (letterInWord) {
            soundPlayer?.play(GameSound.CORRECT_ANSWER)
        } else {
            soundPlayer?.play(GameSound.INCORRECT_ANSWER)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer?.release()
    }
}
```

## Testing Sounds

1. **Android**: Run on emulator or device, sounds should play automatically
2. **iOS**: Run on simulator or device, sounds should play automatically

If sounds don't play:
- Check file names match exactly
- Verify files are in correct directories
- Check file formats (MP3 is most compatible)
- Ensure volume is turned up on device

## File Format Recommendations

- **Format**: MP3 (best compatibility) or OGG
- **Sample Rate**: 44.1 kHz
- **Bit Rate**: 128-192 kbps
- **File Size**: Keep under 100KB each
- **Length**:
  - Wheel spin: 2-3 seconds (will loop)
  - Other effects: 0.5-2 seconds (one-shot)

## Android-Specific Notes

The Android implementation uses `MediaPlayer` API which:
- Automatically handles MP3, OGG, WAV formats
- Requires files in `res/raw/` directory
- File names must be lowercase, no special characters
- Releases resources automatically after playback

## iOS-Specific Notes

The iOS implementation uses `AVAudioPlayer` which:
- Supports MP3, M4A, WAV, AIFF formats
- Requires files added to app bundle via Xcode
- File names are case-sensitive
- Can detect format automatically from extension

## License Compliance

Ensure all sound files are:
- Public domain, OR
- Creative Commons (with attribution if required), OR
- Royalty-free with commercial use license

Document the source and license for each sound file.
