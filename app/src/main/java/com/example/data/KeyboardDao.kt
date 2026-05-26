package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KeyboardDao {
    // Emojis
    @Query("SELECT * FROM custom_emojis ORDER BY dateAdded DESC")
    fun getAllEmojis(): Flow<List<CustomEmoji>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmoji(emoji: CustomEmoji)

    @Delete
    suspend fun deleteEmoji(emoji: CustomEmoji)

    @Query("DELETE FROM custom_emojis WHERE id = :id")
    suspend fun deleteEmojiById(id: Int)

    // Shortcuts
    @Query("SELECT * FROM custom_shortcuts ORDER BY keyword ASC")
    fun getAllShortcuts(): Flow<List<CustomShortcut>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: CustomShortcut)

    @Delete
    suspend fun deleteShortcut(shortcut: CustomShortcut)

    @Query("DELETE FROM custom_shortcuts WHERE id = :id")
    suspend fun deleteShortcutById(id: Int)

    // Config
    @Query("SELECT * FROM keyboard_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<KeyboardConfig?>

    @Query("SELECT * FROM keyboard_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigDirect(): KeyboardConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: KeyboardConfig)
}
