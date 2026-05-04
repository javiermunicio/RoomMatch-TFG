package com.example.roommatch_pmdm.di

import com.example.roommatch_pmdm.data.repositories.*
import com.example.roommatch_pmdm.domain.usecase.*
import com.example.roommatch_pmdm.presentation.ui.screen.InterestedUserProfileViewModel
import com.example.roommatch_pmdm.presentation.ui.screen.InterestedUsersListViewModel
import com.example.roommatch_pmdm.presentation.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Firebase
    single { FirebaseFirestore.getInstance() }
    single { FirebaseAuth.getInstance() }
    single { FirebaseStorage.getInstance() }

    // Repositories
    single { StorageRepository(androidContext()) }
    single { AuthRepository(get()) }
    single { RoomPostRepository(get()) }
    single { UserRepository(get()) }
    single { MatchRepository(get()) }
    single { ChatRepository(get()) }
    single { InterestRepository(get()) }

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
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { AddRoomPostViewModel(get(), get(), get()) }
    viewModel { RoomPostListViewModel(get(), get(), get()) }
    viewModel { OnboardingViewModel(get(), get(), get()) }
    viewModel { RoomPostDetailViewModel(get(), get(), get(), get()) }
    viewModel { EditRoomPostViewModel(get(), get(), get()) }
    viewModel { InterestedUsersListViewModel(get(), get()) }
    viewModel { InterestedUserProfileViewModel(get()) }
}