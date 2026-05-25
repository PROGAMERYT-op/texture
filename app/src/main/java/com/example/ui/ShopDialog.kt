package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.UserStats

data class ShopThemeItem(
    val name: String,
    val cost: Int,
    val mainColor: Color,
    val description: String
)

data class ShopSoundItem(
    val name: String,
    val cost: Int,
    val description: String
)

@Composable
fun ShopDialog(
    stats: UserStats,
    onPurchaseTheme: (String, Int) -> Unit,
    onUnlockThemeFree: (String) -> Unit,
    onPurchaseSound: (String, Int) -> Unit,
    onUnlockSoundFree: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    onSelectSound: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalSiloColors.current
    var activeTab by remember { mutableStateOf("Themes") } // "Themes" or "Ambient"

    val themesList = listOf(
        ShopThemeItem("Charcoal Zen", 0, CharcoalZenColors.background, "Default Nordic dark charcoal ambiance."),
        ShopThemeItem("Forest Green", 40, ForestGreenColors.primary, "Deep pine needles and soft mist."),
        ShopThemeItem("Matcha", 40, MatchaColors.primary, "Warm, soothing tea leaf hues."),
        ShopThemeItem("Cyberpunk Night", 60, CyberpunkNightColors.primary, "A dark terminal with electric violet & cyan glow."),
        ShopThemeItem("Mono Light", 60, MonoLightColors.background, "Clean white high-contrast minimal ink.")
    )

    val soundsList = listOf(
        ShopSoundItem("None", 0, "Absolute quiet focus."),
        ShopSoundItem("Rain", 20, "Programmatic cozy rainfall noise loop."),
        ShopSoundItem("Lo-Fi", 30, "A synth-chord harmonic loop with crackle.")
    )

    val unlockedThemes = stats.unlockedThemes.split(",").map { it.trim() }.toSet()
    val unlockedSounds = stats.unlockedSounds.split(",").map { it.trim() }.toSet()

    val context = LocalContext.current
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                // Ignore if permission taking fails
            }
            onSelectSound(uri.toString())
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .background(colors.background, shape = RoundedCornerShape(16.dp))
                .border(1.dp, colors.primary.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "THEMES & SOUNDS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onBackground,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Customize your sensory space",
                        fontSize = 12.sp,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Balance Tracker
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(colors.surface, shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "✦ ${stats.shards}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Selectors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, shape = RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activeTab == "Themes") colors.background else Color.Transparent)
                        .clickable { activeTab = "Themes" }
                        .padding(vertical = 10.dp)
                        .testTag("shop_tab_themes"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aesthetics",
                        fontSize = 13.sp,
                        fontWeight = if (activeTab == "Themes") FontWeight.Bold else FontWeight.Normal,
                        color = if (activeTab == "Themes") colors.primary else colors.onSurface.copy(alpha = 0.7f)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activeTab == "Ambient") colors.background else Color.Transparent)
                        .clickable { activeTab = "Ambient" }
                        .padding(vertical = 10.dp)
                        .testTag("shop_tab_ambient"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sensory Audio",
                        fontSize = 13.sp,
                        fontWeight = if (activeTab == "Ambient") FontWeight.Bold else FontWeight.Normal,
                        color = if (activeTab == "Ambient") colors.primary else colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of Items
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (activeTab == "Themes") {
                    items(themesList) { item ->
                        val isUnlocked = unlockedThemes.contains(item.name)
                        val isActive = stats.currentTheme == item.name

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isActive) colors.primary else colors.inactive.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(colors.surface, shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Visual color circular badge
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(item.mainColor)
                                            .border(1.dp, colors.onBackground.copy(alpha = 0.3f), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.name.uppercase(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onBackground,
                                        letterSpacing = 1.sp
                                    )
                                }

                                if (isActive) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Equipped",
                                            tint = colors.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                } else if (isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .background(colors.background, shape = RoundedCornerShape(4.dp))
                                            .clickable { onSelectTheme(item.name) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "EQUIP",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.onBackground,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                fontSize = 11.sp,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )

                            if (!isUnlocked) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = colors.background, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Buy with shards
                                    val canAfford = stats.shards >= item.cost
                                    Row(
                                        modifier = Modifier
                                            .background(
                                                if (canAfford) colors.primary else colors.background,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .clickable(enabled = canAfford) {
                                                onPurchaseTheme(item.name, item.cost)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = if (canAfford) colors.onPrimary else colors.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "BUY ✦ ${item.cost}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (canAfford) colors.onPrimary else colors.onSurface.copy(alpha = 0.4f),
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    // Instant video ad unlock
                                    Row(
                                        modifier = Modifier
                                            .background(colors.background, shape = RoundedCornerShape(4.dp))
                                            .border(1.dp, colors.primary.copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp))
                                            .clickable { onUnlockThemeFree(item.name) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tv,
                                            contentDescription = "Ad Unlock",
                                            tint = colors.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "FREE INSTANT (ADS)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(soundsList) { item ->
                        val isUnlocked = unlockedSounds.contains(item.name)
                        val isActive = stats.currentSound == item.name || (item.name == "Custom" && stats.currentSound.startsWith("content://"))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isActive) colors.primary else colors.inactive.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(colors.surface, shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                              ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Sound Wave",
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.name.uppercase(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onBackground,
                                        letterSpacing = 1.sp
                                    )
                                }

                                if (isActive) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active Sound",
                                            tint = colors.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                } else if (isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .background(colors.background, shape = RoundedCornerShape(4.dp))
                                            .clickable { onSelectSound(item.name) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "EQUIP",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.onBackground,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                fontSize = 11.sp,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )

                            if (!isUnlocked) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = colors.background, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val canAfford = stats.shards >= item.cost
                                    Row(
                                        modifier = Modifier
                                            .background(
                                                if (canAfford) colors.primary else colors.background,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .clickable(enabled = canAfford) {
                                                onPurchaseSound(item.name, item.cost)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = if (canAfford) colors.onPrimary else colors.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "BUY ✦ ${item.cost}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (canAfford) colors.onPrimary else colors.onSurface.copy(alpha = 0.4f),
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .background(colors.background, shape = RoundedCornerShape(4.dp))
                                            .border(1.dp, colors.primary.copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp))
                                            .clickable { onUnlockSoundFree(item.name) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tv,
                                            contentDescription = "Ad Unlock",
                                            tint = colors.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "FREE INSTANT (ADS)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Close button
            if (activeTab == "Ambient") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, colors.primary, shape = RoundedCornerShape(8.dp))
                        .clickable { audioPicker.launch(arrayOf("audio/*")) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "UPLOAD CUSTOM SOUND",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primary, shape = RoundedCornerShape(8.dp))
                    .clickable { onDismiss() }
                    .padding(vertical = 12.dp)
                    .testTag("shop_close_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DISMISS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimary,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
