package com.rokoblak.blescanner.ui.screens.main

import androidx.lifecycle.ViewModel
import com.rokoblak.blescanner.service.PersistedStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    storage: PersistedStorage,
) : ViewModel() {

    val uiState = storage.prefsFlow().map {
        MainScreenUIState(isDarkTheme = it.darkMode)
    }
}
