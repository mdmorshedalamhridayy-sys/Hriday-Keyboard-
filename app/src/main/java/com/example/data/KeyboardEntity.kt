package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_emojis")
data class CustomEmoji(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val emoji: String,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_shortcuts")
data class CustomShortcut(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val keyword: String,
    val phrase: String,
    val usageCount: Int = 0
)

@Entity(tableName = "keyboard_config")
data class KeyboardConfig(
    @PrimaryKey val id: Int = 1,
    val themeName: String = "Elegant Dark",
    val soundProfile: String = "Mechanical",
    val hapticEnabled: Boolean = true,
    val hapticIntensity: Float = 0.5f,
    val practiceHighScore: Int = 0
)
