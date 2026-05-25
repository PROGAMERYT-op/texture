package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdManager
import com.example.audio.AmbientSoundManager
import com.example.data.AppDatabase
import com.example.data.UserStats
import com.example.data.UserStatsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TimerState {
    IDLE,
    RUNNING,
    BREACHED,
    COMPLETED
}

class SiloViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = UserStatsRepository(db.userStatsDao())

    val soundManager = AmbientSoundManager(application)
    val adManager = AdManager(application)

    // User details reactive data flow
    val userStats: StateFlow<UserStats> = repository.userStatsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserStats()
    )

    // UI and Timer Tracking States
    val timerState = MutableStateFlow(TimerState.IDLE)
    val activeSessionMinutes = MutableStateFlow(25) // Selected duration: 15, 25, 45, 60
    val secondsRemaining = MutableStateFlow(1500) // Initial 25 * 60

    // Breached capture attributes for recovery
    private var pausedRemainingSeconds = 0
    private var pausedStreak = 0

    // Modals visibility states
    val showReviveModal = MutableStateFlow(false)
    val showMultiplierModal = MutableStateFlow(false)

    private var tickingJob: Job? = null

    init {
        adManager.initializeSdk()

        // Sync local synthesizer state with active database selection
        viewModelScope.launch {
            userStats.collect { stats ->
                if (stats.currentSound != "None" && timerState.value == TimerState.RUNNING) {
                    soundManager.start(stats.currentSound)
                } else if (stats.currentSound == "None" || timerState.value != TimerState.RUNNING) {
                    soundManager.stop()
                }
            }
        }
    }

    fun selectSessionDuration(minutes: Int) {
        if (timerState.value == TimerState.IDLE) {
            activeSessionMinutes.value = minutes
            secondsRemaining.value = minutes * 60
        }
    }

    fun startFocusSession() {
        if (timerState.value == TimerState.IDLE) {
            timerState.value = TimerState.RUNNING
            
            // Start local synthesizer sound looped if selected
            val activeSound = userStats.value.currentSound
            if (activeSound != "None") {
                soundManager.start(activeSound)
            }

            startTicking()
        }
    }

    private fun startTicking() {
        tickingJob?.cancel()
        tickingJob = viewModelScope.launch {
            while (secondsRemaining.value > 0) {
                delay(1000)
                secondsRemaining.value -= 1
            }
            completeFocusSessionSuccessfully()
        }
    }

    private suspend fun completeFocusSessionSuccessfully() {
        timerState.value = TimerState.COMPLETED
        soundManager.stop()
        
        // Award 10 shards local persist
        repository.completeFocusSession(10)
        
        // Trigger clean Shard Multiplier modal overlay
        showMultiplierModal.value = true
    }

    fun doubleCompletedShards(activity: android.app.Activity?) {
        adManager.showRewardedAd(
            activity = activity,
            adTitle = "Double your Focus Shards (+10 ✦)",
            onRewardEarned = {
                viewModelScope.launch {
                    repository.addShards(10)
                    showMultiplierModal.value = false
                    timerState.value = TimerState.IDLE
                    resetTimer()
                }
            },
            onAdClosed = {
                showMultiplierModal.value = false
                timerState.value = TimerState.IDLE
                resetTimer()
            }
        )
    }

    fun dismissMultiplier() {
        showMultiplierModal.value = false
        timerState.value = TimerState.IDLE
        resetTimer()
    }

    fun handleAppLossOfFocus() {
        // If ticking is running, leaving app breaks the streak and aborts the session!
        if (timerState.value == TimerState.RUNNING) {
            tickingJob?.cancel()
            pausedRemainingSeconds = secondsRemaining.value
            pausedStreak = userStats.value.currentStreak
            timerState.value = TimerState.BREACHED
            soundManager.stop()

            viewModelScope.launch {
                repository.breakStreak()
            }
        }
    }

    fun triggerBreachModalIfNeeded() {
        if (timerState.value == TimerState.BREACHED) {
            showReviveModal.value = true
        }
    }

    fun reviveBreachedSilo(activity: android.app.Activity?) {
        adManager.showRewardedAd(
            activity = activity,
            adTitle = "Restore Focus Streak",
            onRewardEarned = {
                viewModelScope.launch {
                    // Restore streak and active session countdown successfully!
                    timerState.value = TimerState.RUNNING
                    secondsRemaining.value = pausedRemainingSeconds
                    repository.restoreStreak(pausedStreak)
                    showReviveModal.value = false

                    // Restart background sounds loop
                    val activeSound = userStats.value.currentSound
                    if (activeSound != "None") {
                        soundManager.start(activeSound)
                    }

                    startTicking()
                }
            },
            onAdClosed = {
                // If they cancel or reject, breach officialized
                viewModelScope.launch {
                    showReviveModal.value = false
                    if (timerState.value == TimerState.BREACHED) {
                        timerState.value = TimerState.IDLE
                        resetTimer()
                    }
                }
            }
        )
    }

    fun abandonBreachedSilo() {
        showReviveModal.value = false
        timerState.value = TimerState.IDLE
        resetTimer()
    }

    fun resetTimer() {
        tickingJob?.cancel()
        timerState.value = TimerState.IDLE
        secondsRemaining.value = activeSessionMinutes.value * 60
        soundManager.stop()
    }

    // --- Customization Shop Functions ---
    fun purchaseTheme(themeName: String, shardCost: Int) {
        viewModelScope.launch {
            val success = repository.unlockTheme(themeName, shardCost)
            if (success) {
                repository.selectTheme(themeName)
            }
        }
    }

    fun unlockThemeFree(activity: android.app.Activity?, themeName: String) {
        adManager.showRewardedAd(
            activity = activity,
            adTitle = "Instant Theme Unlock: $themeName",
            onRewardEarned = {
                viewModelScope.launch {
                    repository.unlockThemeFree(themeName)
                    repository.selectTheme(themeName)
                }
            },
            onAdClosed = {}
        )
    }

    fun purchaseSound(soundName: String, shardCost: Int) {
        viewModelScope.launch {
            val success = repository.unlockSound(soundName, shardCost)
            if (success) {
                repository.selectSound(soundName)
            }
        }
    }

    fun unlockSoundFree(activity: android.app.Activity?, soundName: String) {
        adManager.showRewardedAd(
            activity = activity,
            adTitle = "Instant Sound Unlock: $soundName",
            onRewardEarned = {
                viewModelScope.launch {
                    repository.unlockSoundFree(soundName)
                    repository.selectSound(soundName)
                }
            },
            onAdClosed = {}
        )
    }

    fun selectTheme(themeName: String) {
        viewModelScope.launch {
            repository.selectTheme(themeName)
        }
    }

    fun selectSound(soundName: String) {
        viewModelScope.launch {
            repository.selectSound(soundName)
            // If timer is already running, switch live background wave audio!
            if (timerState.value == TimerState.RUNNING) {
                if (soundName == "None") {
                    soundManager.stop()
                } else {
                    soundManager.start(soundName)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.stop()
    }
}
