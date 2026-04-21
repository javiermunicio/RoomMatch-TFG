package com.example.roommatch_pmdm.di

import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.RoomRepository
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.usecase.AddRoomUseCase
import com.example.roommatch_pmdm.domain.usecase.DeleteRoomUseCase
import com.example.roommatch_pmdm.domain.usecase.ListRoomUseCase
import com.example.roommatch_pmdm.presentation.viewmodel.AddRoomsScreenViewModel
import com.example.roommatch_pmdm.presentation.viewmodel.ChatDetailViewModel
import com.example.roommatch_pmdm.presentation.viewmodel.ChatListViewModel
import com.example.roommatch_pmdm.presentation.viewmodel.LoginViewModel
import com.example.roommatch_pmdm.presentation.viewmodel.MainScreenViewModel
import com.example.roommatch_pmdm.presentation.viewmodel.MatchingViewModel
import com.example.roommatch_pmdm.presentation.viewmodel.ProfileViewModel
import com.example.roommatch_pmdm.presentation.viewmodel.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.module.dsl.viewModel
import org.koin.core.scope.get
import org.koin.dsl.module

val appModule = module {
    // Firebase
    single { FirebaseFirestore.getInstance() }
    single { FirebaseAuth.getInstance() }

    // Repositories
    single { AuthRepository(get()) }
    single { RoomRepository(get()) }
    single { UserRepository(get()) }

    // Use cases
    factory { AddRoomUseCase(get()) }
    factory { DeleteRoomUseCase(get()) }
    factory { ListRoomUseCase(get()) }

    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { MatchingViewModel() }
    viewModel { ChatListViewModel() }
    viewModel { ChatDetailViewModel() }
    viewModel { ProfileViewModel(get(), get()) }   // ← AuthRepository + UserRepository
    viewModel { AddRoomsScreenViewModel(get()) }
    viewModel { MainScreenViewModel(get(), get()) }
}