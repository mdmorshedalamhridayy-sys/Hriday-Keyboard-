package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KeyboardViewModel(application: Application) : AndroidViewModel(application) {
    private val database = KeyboardDatabase.getDatabase(application)
    private val repository = KeyboardRepository(database.keyboardDao())

    val allEmojis: StateFlow<List<CustomEmoji>> = repository.allEmojis
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allShortcuts: StateFlow<List<CustomShortcut>> = repository.allShortcuts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val config: StateFlow<KeyboardConfig?> = repository.config
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = KeyboardConfig()
        )

    fun addEmoji(emoji: String) {
        viewModelScope.launch {
            repository.insertEmoji(emoji)
        }
    }

    fun removeEmoji(emoji: CustomEmoji) {
        viewModelScope.launch {
            repository.deleteEmoji(emoji)
        }
    }

    fun removeEmojiById(id: Int) {
        viewModelScope.launch {
            repository.deleteEmojiById(id)
        }
    }

    fun addShortcut(keyword: String, phrase: String) {
        viewModelScope.launch {
            repository.insertShortcut(keyword, phrase)
        }
    }

    fun removeShortcut(shortcut: CustomShortcut) {
        viewModelScope.launch {
            repository.deleteShortcut(shortcut)
        }
    }

    fun removeShortcutById(id: Int) {
        viewModelScope.launch {
            repository.deleteShortcutById(id)
        }
    }

    fun selectTheme(themeName: String) {
        viewModelScope.launch {
            repository.updateTheme(themeName)
        }
    }

    fun selectSoundProfile(profile: String) {
        viewModelScope.launch {
            repository.updateSoundProfile(profile)
        }
    }

    fun updateHapticFeedback(enabled: Boolean, intensity: Float) {
        viewModelScope.launch {
            repository.updateHaptics(enabled, intensity)
        }
    }

    fun saveHighScore(score: Int) {
        viewModelScope.launch {
            repository.updateHighScore(score)
        }
    }

    // A factory wrapper for easy ViewModel creation
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(KeyboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return KeyboardViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
