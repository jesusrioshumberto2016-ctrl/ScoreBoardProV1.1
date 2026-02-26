package com.studiosrios.scoreboardpro.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TournamentViewModel : ViewModel() {
    var imageUri by mutableStateOf<String?>(null)
        private set

    fun onImageUriChanged(uri: String?) {
        imageUri = uri
    }
}
