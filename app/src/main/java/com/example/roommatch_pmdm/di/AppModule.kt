package com.example.roommatch_pmdm.di

import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.RoomRepository
import com.example.roommatch_pmdm.domain.usecase.AddRoomUseCase
import com.example.roommatch_pmdm.domain.usecase.DeleteRoomUseCase
import com.example.roommatch_pmdm.domain.usecase.ListRoomUseCase
import com.example.roommatch_pmdm.presentation.viewmodel.AddRoomsScreenViewModel
import com.example.roommatch_pmdm.presentation.viewmodel.LoginViewModel
import com.example.roommatch_pmdm.presentation.viewmodel.MainScreenViewModel
import com.example.roommatch_pmdm.presentation.viewmodel.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.module.dsl.viewModel
import org.koin.core.scope.get
import org.koin.dsl.module

val appModule = module {
    single { FirebaseFirestore.getInstance() }
    single { FirebaseAuth.getInstance() }           // ← nuevo
    single { AuthRepository(get()) }               // ← nuevo
    single { RoomRepository(get()) }

    factory { AddRoomUseCase(get()) }
    factory { DeleteRoomUseCase(get()) }
    factory { ListRoomUseCase(get()) }

    viewModel { AddRoomsScreenViewModel(get()) }
    viewModel { MainScreenViewModel(get(), get()) }
    viewModel { LoginViewModel(get()) }            // ← actualizado
    viewModel { RegisterViewModel(get()) }         // ← actualizado
}