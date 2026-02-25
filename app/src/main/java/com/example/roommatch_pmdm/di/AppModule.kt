package com.example.roommatch_pmdm.di

import com.example.roommatch_pmdm.data.repositories.RoomRepository
import com.example.roommatch_pmdm.domain.usecase.AddRoomUseCase
import com.example.roommatch_pmdm.domain.usecase.DeleteRoomUseCase
import com.example.roommatch_pmdm.domain.usecase.ListRoomUseCase
import com.example.roommatch_pmdm.presentation.viewmodel.AddRoomsScreenViewModel
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module{
    single { FirebaseFirestore.getInstance() }
    single { RoomRepository(get()) }

    factory { AddRoomUseCase(get()) }
    factory { DeleteRoomUseCase(get()) }
    factory { ListRoomUseCase(get()) }
    viewModel { AddRoomsScreenViewModel(get()) }
}