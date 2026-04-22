package com.example.roommatch_pmdm.di

import com.example.roommatch_pmdm.data.repositories.*
import com.example.roommatch_pmdm.domain.usecase.*
import com.example.roommatch_pmdm.presentation.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Firebase
    single { FirebaseFirestore.getInstance() }
    single { FirebaseAuth.getInstance() }

    // Repositories
    single { AuthRepository(get()) }
    single { RoomPostRepository(get()) }
    single { UserRepository(get()) }
    single { MatchRepository(get()) }   // ← nuevo
    single { ChatRepository(get()) }    // ← nuevo

    // Use cases
    factory { AddRoomPostUseCase(get()) }
    factory { DeleteRoomPostUseCase(get()) }
    factory { ListRoomPostsUseCase(get()) }

    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get(), get()) }
    viewModel { MatchingViewModel(get(), get()) }
    viewModel { ChatListViewModel(get(), get(), get()) }
    viewModel { ChatDetailViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { AddRoomPostViewModel(get(), get()) }
    viewModel { RoomPostListViewModel(get(), get(), get()) }
}