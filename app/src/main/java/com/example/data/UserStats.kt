package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val shards: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val currentTheme: String = "Charcoal Zen",
    val currentSound: String = "None",
    val unlockedThemes: String = "Charcoal Zen",
    val unlockedSounds: String = "None"
)
