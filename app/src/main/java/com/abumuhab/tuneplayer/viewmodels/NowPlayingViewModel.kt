package com.abumuhab.tuneplayer.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abumuhab.tuneplayer.models.Audio
import com.abumuhab.tuneplayer.repositories.AudioRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class NowPlayingViewModel(application: Application) : ViewModel() {
    private val audioRepository = AudioRepository(application)

    val audios = MutableLiveData<List<Audio>>()

    init {
        getAudioFiles()
    }

    fun getAudioFiles() {
        viewModelScope.launch {
            audios.value = audioRepository.listAudioFiles().toList()
        }
    }
}

class NowPLayingViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NowPlayingViewModel::class.java)) {
            return NowPlayingViewModel(application) as T
        }
        throw  IllegalArgumentException("Unknown ViewModel class")
    }
}