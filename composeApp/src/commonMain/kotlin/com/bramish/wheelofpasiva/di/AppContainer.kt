package com.bramish.wheelofpasiva.di

import com.bramish.wheelofpasiva.data.datasource.FirestoreDataSource
import com.bramish.wheelofpasiva.data.repository.RoomRepositoryImpl
import com.bramish.wheelofpasiva.domain.repository.RoomRepository
import com.bramish.wheelofpasiva.domain.usecase.CreateRoomUseCase
import com.bramish.wheelofpasiva.domain.usecase.GetRoomUseCase
import com.bramish.wheelofpasiva.domain.usecase.JoinRoomUseCase
import com.bramish.wheelofpasiva.domain.usecase.ObserveRoomUseCase
import com.bramish.wheelofpasiva.presentation.game.GameViewModel
import com.bramish.wheelofpasiva.presentation.home.HomeViewModel
import com.bramish.wheelofpasiva.presentation.home.JoinRoomViewModel
import com.bramish.wheelofpasiva.presentation.room.RoomViewModel

/**
 * Dependency injection container for the application.
 * Provides instances of ViewModels, use cases, and repositories.
 * Uses manual dependency injection to avoid heavy DI frameworks.
 */
class AppContainer {

    // Data Layer
    private val firestoreDataSource: FirestoreDataSource by lazy {
        FirestoreDataSource()
    }

    private val roomRepository: RoomRepository by lazy {
        RoomRepositoryImpl(firestoreDataSource)
    }

    // Domain Layer - Use Cases
    private val createRoomUseCase: CreateRoomUseCase by lazy {
        CreateRoomUseCase(roomRepository)
    }

    private val joinRoomUseCase: JoinRoomUseCase by lazy {
        JoinRoomUseCase(roomRepository)
    }

    private val getRoomUseCase: GetRoomUseCase by lazy {
        GetRoomUseCase(roomRepository)
    }

    private val observeRoomUseCase: ObserveRoomUseCase by lazy {
        ObserveRoomUseCase(roomRepository)
    }

    // Presentation Layer - ViewModels

    /**
     * Provides a new instance of HomeViewModel.
     */
    fun provideHomeViewModel(): HomeViewModel {
        return HomeViewModel(createRoomUseCase)
    }

    /**
     * Provides a new instance of JoinRoomViewModel.
     */
    fun provideJoinRoomViewModel(): JoinRoomViewModel {
        return JoinRoomViewModel(joinRoomUseCase)
    }

    /**
     * Provides a new instance of RoomViewModel for a specific room.
     *
     * @param roomId The unique room identifier
     */
    fun provideRoomViewModel(roomId: String): RoomViewModel {
        return RoomViewModel(observeRoomUseCase, roomId)
    }

    /**
     * Provides a new instance of GameViewModel for a specific room.
     *
     * @param roomId The unique room identifier
     */
    fun provideGameViewModel(roomId: String): GameViewModel {
        return GameViewModel(observeRoomUseCase, roomId)
    }
}
