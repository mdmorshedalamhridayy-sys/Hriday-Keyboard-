package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class KeyboardRepository(private val dao: KeyboardDao) {
    
    // Emojis
    val allEmojis: Flow<List<CustomEmoji>> = dao.getAllEmojis().distinctUntilChanged()

    suspend fun insertEmoji(emoji: String) {
        if (emoji.isNotBlank()) {
            dao.insertEmoji(CustomEmoji(emoji = emoji.trim()))
        }
    }

    suspend fun deleteEmoji(emoji: CustomEmoji) {
        dao.deleteEmoji(emoji)
    }

    suspend fun deleteEmojiById(id: Int) {
        dao.deleteEmojiById(id)
    }

    // Shortcuts
    val allShortcuts: Flow<List<CustomShortcut>> = dao.getAllShortcuts().distinctUntilChanged()

    suspend fun insertShortcut(keyword: String, phrase: String) {
        if (keyword.isNotBlank() && phrase.isNotBlank()) {
            dao.insertShortcut(CustomShortcut(keyword = keyword.trim().lowercase(), phrase = phrase.trim()))
        }
    }

    suspend fun deleteShortcut(shortcut: CustomShortcut) {
        dao.deleteShortcut(shortcut)
    }

    suspend fun deleteShortcutById(id: Int) {
        dao.deleteShortcutById(id)
    }

    // Config
    val config: Flow<KeyboardConfig?> = dao.getConfigFlow().distinctUntilChanged()

    suspend fun getConfigDirect(): KeyboardConfig {
        return dao.getConfigDirect() ?: KeyboardConfig()
    }

    suspend fun saveConfig(config: KeyboardConfig) {
        dao.saveConfig(config)
    }

    suspend fun updateTheme(themeName: String) {
        val current = getConfigDirect()
        dao.saveConfig(current.copy(themeName = themeName))
    }

    suspend fun updateSoundProfile(profile: String) {
        val current = getConfigDirect()
        dao.saveConfig(current.copy(soundProfile = profile))
    }

    suspend fun updateHaptics(enabled: Boolean, intensity: Float) {
        val current = getConfigDirect()
        dao.saveConfig(current.copy(hapticEnabled = enabled, hapticIntensity = intensity))
    }

    suspend fun updateHighScore(score: Int) {
        val current = getConfigDirect()
        if (score > current.practiceHighScore) {
            dao.saveConfig(current.copy(practiceHighScore = score))
        }
    }
}
