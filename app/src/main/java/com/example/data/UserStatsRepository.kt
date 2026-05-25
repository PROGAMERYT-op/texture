package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserStatsRepository(private val userStatsDao: UserStatsDao) {

    val userStatsFlow: Flow<UserStats> = userStatsDao.getUserStatsFlow().map { stats ->
        stats ?: UserStats().also { userStatsDao.insertUserStats(it) }
    }

    suspend fun getStats(): UserStats {
        return userStatsDao.getUserStats() ?: UserStats().also { userStatsDao.insertUserStats(it) }
    }

    suspend fun updateStats(stats: UserStats) {
        userStatsDao.updateUserStats(stats)
    }

    suspend fun addShards(amount: Int) {
        val current = getStats()
        updateStats(current.copy(shards = current.shards + amount))
    }

    suspend fun unlockTheme(themeName: String, cost: Int): Boolean {
        val current = getStats()
        if (current.shards >= cost) {
            val unlockedList = current.unlockedThemes.split(",").map { it.trim() }.toMutableSet()
            if (unlockedList.add(themeName)) {
                updateStats(current.copy(
                    shards = current.shards - cost,
                    unlockedThemes = unlockedList.joinToString(",")
                ))
                return true
            }
        }
        return false
    }

    suspend fun unlockThemeFree(themeName: String) {
        val current = getStats()
        val unlockedList = current.unlockedThemes.split(",").map { it.trim() }.toMutableSet()
        if (unlockedList.add(themeName)) {
            updateStats(current.copy(
                unlockedThemes = unlockedList.joinToString(",")
            ))
        }
    }

    suspend fun selectTheme(themeName: String) {
        val current = getStats()
        updateStats(current.copy(currentTheme = themeName))
    }

    suspend fun unlockSound(soundName: String, cost: Int): Boolean {
        val current = getStats()
        if (current.shards >= cost) {
            val unlockedList = current.unlockedSounds.split(",").map { it.trim() }.toMutableSet()
            if (unlockedList.add(soundName)) {
                updateStats(current.copy(
                    shards = current.shards - cost,
                    unlockedSounds = unlockedList.joinToString(",")
                ))
                return true
            }
        }
        return false
    }

    suspend fun unlockSoundFree(soundName: String) {
        val current = getStats()
        val unlockedList = current.unlockedSounds.split(",").map { it.trim() }.toMutableSet()
        if (unlockedList.add(soundName)) {
            updateStats(current.copy(
                unlockedSounds = unlockedList.joinToString(",")
            ))
        }
    }

    suspend fun selectSound(soundName: String) {
        val current = getStats()
        updateStats(current.copy(currentSound = soundName))
    }

    suspend fun completeFocusSession(earnedShards: Int) {
        val current = getStats()
        val nextStreak = current.currentStreak + 1
        val nextMax = if (nextStreak > current.maxStreak) nextStreak else current.maxStreak
        updateStats(current.copy(
            shards = current.shards + earnedShards,
            currentStreak = nextStreak,
            maxStreak = nextMax
        ))
    }

    suspend fun breakStreak() {
        val current = getStats()
        updateStats(current.copy(currentStreak = 0))
    }

    suspend fun restoreStreak(streak: Int) {
        val current = getStats()
        updateStats(current.copy(currentStreak = streak))
    }
}
