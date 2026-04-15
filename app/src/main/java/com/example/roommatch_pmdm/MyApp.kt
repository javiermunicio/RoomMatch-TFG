package com.example.roommatch_pmdm

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApp)
            modules(appModule)
        }
    }
}

val appModule = module {
    // ViewModels
    single { com.example.roommatch_pmdm.presentation.viewmodel.LoginViewModel() }
    single { com.example.roommatch_pmdm.presentation.viewmodel.RegisterViewModel() }
    single { com.example.roommatch_pmdm.presentation.viewmodel.MatchingViewModel() }
    single { com.example.roommatch_pmdm.presentation.viewmodel.ChatListViewModel() }
    single { com.example.roommatch_pmdm.presentation.viewmodel.ChatDetailViewModel() }
    single { com.example.roommatch_pmdm.presentation.viewmodel.ProfileViewModel() }
}
