package com.bramish.wheelofpasiva.domain.usecase

import com.bramish.wheelofpasiva.domain.repository.RoomRepository

/**
 * Use case for setting the secret word/phrase in a game room.
 * Only the host should call this after starting the game.
 */
class SetSecretWordUseCase(
    private val roomRepository: RoomRepository
) {
    /**
     * Sets the secret word for the game.
     *
     * @param roomId The room to set the word for
     * @param secretWord The secret word or phrase to guess
     * @param language The language of the secret word
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(roomId: String, secretWord: String, language: com.bramish.wheelofpasiva.domain.model.Language): Result<Unit> {
        return roomRepository.setSecretWord(roomId, secretWord, language)
    }
}
