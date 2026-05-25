package com.example.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.SiloViewModel
import com.example.viewmodel.TimerState

@Composable
fun MainScreen(viewModel: SiloViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    val stats by viewModel.userStats.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val activeMinutes by viewModel.activeSessionMinutes.collectAsState()
    val secondsLeft by viewModel.secondsRemaining.collectAsState()

    // Dialog state controllers
    var showShop by remember { mutableStateOf(false) }
    val showRevive by viewModel.showReviveModal.collectAsState()
    val showMultiplier by viewModel.showMultiplierModal.collectAsState()

    // Ads simulating trackers
    val isShowingSimAd by viewModel.adManager.isShowingSimAd.collectAsState()
    val simAdSecondsLeft by viewModel.adManager.simSecondsLeft.collectAsState()
    val simAdTitle by viewModel.adManager.simAdMessage.collectAsState()

    // Custom aesthetic wrapping
    SiloTheme(themeName = stats.currentTheme) {
        val colors = LocalSiloColors.current

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = colors.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("silo_focus_scaffold_box")
            ) {
                // Background subtle overlay for cyber grid (e.g. if Cyberpunk Night active)
                if (stats.currentTheme == "Cyberpunk Night") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.background)
                    )
                }

                // Core Main Alignment
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {

                    // -- TOP METRICS METERS BAR --
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title / Brand
                        Column {
                            Text(
                                text = "SILO FOCUS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.onBackground.copy(alpha = 0.5f),
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "STAY FOCUSED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurface
                            )
                        }

                        // Shards & Streak Balance
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Shards Tracker
                            Row(
                                modifier = Modifier
                                    .background(colors.surface, shape = RoundedCornerShape(16.dp))
                                    .border(1.dp, colors.inactive, shape = RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${stats.shards}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onBackground,
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "✦",
                                    fontSize = 12.sp,
                                    color = colors.accent
                                )
                            }

                            // Streak Tracker
                            Row(
                                modifier = Modifier
                                    .background(colors.surface, shape = RoundedCornerShape(12.dp))
                                    .border(1.dp, colors.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔥 ${stats.currentStreak}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }

                            // Max Streak Tracker
                            Row(
                                modifier = Modifier
                                    .background(colors.surface, shape = RoundedCornerShape(12.dp))
                                    .border(1.dp, colors.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏆 ${stats.maxStreak}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onBackground.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Ambient indicators details
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (stats.currentSound != "None") {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Active sound playing",
                                tint = colors.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SENSORY LOOP: ${stats.currentSound.uppercase()}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                                letterSpacing = 1.sp
                            )
                        } else {
                            Text(
                                text = "ABSOLUTE ZEN SILENCE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Normal,
                                color = colors.onSurface.copy(alpha = 0.4f),
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // -- CENTER POMODORO COUNTDOWN TRACKER CIRCLE --
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(280.dp)
                            .testTag("silo_timer_circle")
                    ) {
                        // Drawing local canvas circular clock
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 5.dp.toPx()
                            val centerOffset = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                            val radius = (size.minDimension - strokeWidth) / 2f

                            // Outer hollow background track
                            drawCircle(
                                color = colors.inactive.copy(alpha = 0.15f),
                                radius = radius,
                                center = centerOffset,
                                style = Stroke(width = strokeWidth)
                            )

                            // Foreground progress arc
                            val totalSeconds = activeMinutes * 60f
                            val sweepAngle = if (totalSeconds > 0) {
                                (secondsLeft / totalSeconds) * 360f
                            } else {
                                360f
                            }

                            drawArc(
                                color = colors.primary,
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        // Inner Clock labels
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val displayMinutes = secondsLeft / 60
                            val displaySeconds = secondsLeft % 60
                            val formattedTime = String.format("%02d:%02d", displayMinutes, displaySeconds)

                            Text(
                                text = formattedTime,
                                fontSize = 60.sp,
                                fontWeight = FontWeight.ExtraLight,
                                color = colors.onBackground,
                                letterSpacing = (-2).sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Micro Pulsing animation when Silo Focus is Active!
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
                            val alphaPulse by infiniteTransition.animateFloat(
                                initialValue = 0.4f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse_alpha"
                            )

                            val statusText = when (timerState) {
                                TimerState.IDLE -> "READY"
                                TimerState.RUNNING -> "FOCUSING"
                                TimerState.BREACHED -> "SESSION ABORTED"
                                TimerState.COMPLETED -> "SESSION COMPLETED"
                            }

                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (timerState == TimerState.RUNNING) colors.primary.copy(alpha = alphaPulse) else colors.onBackground.copy(alpha = 0.4f),
                                letterSpacing = 3.sp
                            )
                        }
                    }

                    // -- MODULE A LOCK DURATION SELECTION --
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "SELECT DURATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface.copy(alpha = 0.4f),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val options = listOf(15, 25, 45, 60)
                            options.forEach { minutes ->
                                val isSelected = activeMinutes == minutes
                                val isIdle = timerState == TimerState.IDLE

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isSelected) colors.primary else if (!isIdle) colors.surface.copy(alpha = 0.3f) else colors.surface
                                        )
                                        .clickable(enabled = isIdle) {
                                            viewModel.selectSessionDuration(minutes)
                                        }
                                        .border(
                                            1.dp,
                                            if (isSelected) colors.primary else colors.inactive.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                        .testTag("session_lock_btn_$minutes"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${minutes}m",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) colors.onPrimary else if (!isIdle) colors.onSurface.copy(alpha = 0.3f) else colors.onBackground
                                    )
                                }
                            }
                        }
                    }

                    // -- CONTROLS & SHOP INTERACTION PANEL --
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        if (timerState == TimerState.IDLE) {
                            // NEXT REWARD AD BANNER
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(colors.surface)
                                    .clickable { viewModel.doubleCompletedShards(activity) }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "NEXT REWARD",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.onSurface.copy(alpha = 0.6f),
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "Double Shards (+10 ✦)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.onBackground
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.inactive.copy(alpha = 0.1f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "AD",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.onBackground
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // ACTIVATE SILO FOCUS
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(colors.primary)
                                    .clickable { viewModel.startFocusSession() }
                                    .testTag("activate_silo_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Start Focus",
                                        tint = colors.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Start Session",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onPrimary,
                                        letterSpacing = (-0.5).sp
                                    )
                                }
                            }
                        } else {
                            // ABANDON CONTROL (Aborts Session)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .border(1.dp, colors.inactive, shape = RoundedCornerShape(24.dp))
                                    .background(colors.surface)
                                    .clickable {
                                        viewModel.handleAppLossOfFocus()
                                        viewModel.triggerBreachModalIfNeeded()
                                    }
                                    .testTag("breach_silo_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = "Stop Focus",
                                        tint = colors.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Stop / Reset",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface.copy(alpha = 0.8f),
                                        letterSpacing = (-0.5).sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom styling SHOP Open sub-menu triggered
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .border(
                                    1.dp,
                                    colors.primary.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .background(colors.background)
                                .clickable { showShop = true }
                                .testTag("shop_open_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "Custom Sub-shop",
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "THEMES & SOUNDS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                // -- DIALOG OVERLAYS TRIGGERS --

                // ShopDialog
                if (showShop) {
                    ShopDialog(
                        stats = stats,
                        onPurchaseTheme = { theme, cost -> viewModel.purchaseTheme(theme, cost) },
                        onUnlockThemeFree = { theme -> viewModel.unlockThemeFree(activity, theme) },
                        onPurchaseSound = { sound, cost -> viewModel.purchaseSound(sound, cost) },
                        onUnlockSoundFree = { sound -> viewModel.unlockSoundFree(activity, sound) },
                        onSelectTheme = { theme -> viewModel.selectTheme(theme) },
                        onSelectSound = { sound -> viewModel.selectSound(sound) },
                        onDismiss = { showShop = false }
                    )
                }

                // BreachDialog (Focus Revive Hook Modal)
                if (showRevive) {
                    BreachDialog(
                        streakCount = stats.currentStreak,
                        onRevive = { viewModel.reviveBreachedSilo(activity) },
                        onDismiss = { viewModel.abandonBreachedSilo() }
                    )
                }

                // MultiplierDialog (Shard multiplier screen)
                if (showMultiplier) {
                    MultiplierDialog(
                        onDouble = { viewModel.doubleCompletedShards(activity) },
                        onDismiss = { viewModel.dismissMultiplier() }
                    )
                }

                // Aesthetic Video Ads simulated fullscreen player overlay
                if (isShowingSimAd) {
                    AdSimulationOverlay(
                        title = simAdTitle,
                        secondsLeft = simAdSecondsLeft,
                        onCloseEarly = { viewModel.adManager.dismissAdEarly() }
                    )
                }
            }
        }
    }
}
