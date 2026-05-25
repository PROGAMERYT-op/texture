package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdManager(private val context: Context) {
    // We strictly use the simulated states for offline/fallback as it's the Amazon Appstore version
    private val _isShowingSimAd = MutableStateFlow(false)
    val isShowingSimAd: StateFlow<Boolean> = _isShowingSimAd.asStateFlow()

    private val _simSecondsLeft = MutableStateFlow(0)
    val simSecondsLeft: StateFlow<Int> = _simSecondsLeft.asStateFlow()

    private val _simAdMessage = MutableStateFlow("")
    val simAdMessage: StateFlow<String> = _simAdMessage.asStateFlow()

    fun initializeSdk() {
        Log.d("AdManager", "Amazon Appstore version doesn't use Admob SDK")
    }

    fun showRewardedAd(
        activity: Activity?,
        adTitle: String,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit
    ) {
        // Since Amazon Appstore version has no Google Mobile Ads, immediately fallback to simulation
        fallbackToSimulation(adTitle, onRewardEarned, onAdClosed)
    }

    private fun fallbackToSimulation(
        adTitle: String,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit
    ) {
        _simAdMessage.value = adTitle
        onRewardEarned()
        onAdClosed()
    }
    
    fun dismissAdEarly() {
        _isShowingSimAd.value = false
    }
}
