package com.example.roommatch_pmdm.di

import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.RoomPostRepository
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.usecase.AddRoomPostUseCase
import com.example.roommatch_pmdm.domain.usecase.DeleteRoomPostUseCase
import com.example.roommatch_pmdm.domain.usecase.ListRoomPostsUseCase
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

    // Use cases
    factory { AddRoomPostUseCase(get()) }
    factory { DeleteRoomPostUseCase(get()) }
    factory { ListRoomPostsUseCase(get()) }

    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { MatchingViewModel() }
    viewModel { ChatListViewModel() }
    viewModel { ChatDetailViewModel() }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { AddRoomPostViewModel(get(), get()) }
    viewModel { RoomPostListViewModel(get(), get()) }
}