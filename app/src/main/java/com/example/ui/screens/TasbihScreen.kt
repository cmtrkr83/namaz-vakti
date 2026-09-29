package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.PrayerUiState
import com.example.ui.viewmodel.PrayerViewModel

data class DhikrPreset(
    val title: String,
    val arabic: String,
    val target: Int
)

@Composable
fun TasbihScreen(
    viewModel: PrayerViewModel,
    uiState: PrayerUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val dhikrPresets = listOf(
        DhikrPreset("Sübhânallah", "سُبْحَانَ اللَّهِ", 33),
        DhikrPreset("Elhamdülillâh", "الْحَمْدُ لِلَّهِ", 33),
        DhikrPreset("Allâhu Ekber", "اللَّهُ أَكْبَرُ", 33),
        DhikrPreset("Lâ İlâhe İllallâh", "لَا إِلَهَ إِلَّا اللَّهُ", 100),
        DhikrPreset("Estağfirullâh", "أَسْتَغْفِرُ اللَّهَ", 100),
        DhikrPreset("Salavât-ı Şerîfe", "اللَّهُمَّ صَلِّ عَلَى سَيِّدِنَا مُحَمَّدٍ", 100)
    )

    val currentDhikr = dhikrPresets.getOrElse(uiState.tasbihDhikrIndex) { dhikrPresets[0] }
    val progress = (uiState.tasbihCount % currentDhikr.target).toFloat() / currentDhikr.target.toFloat()

    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        label = "tasbihProgress"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Zikirmatik",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Sade ve huzurlu dijital tesbih",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = {
                    triggerTasbihHaptic(context, isComplete = false)
                    viewModel.resetTasbih()
                },
                modifier = Modifier.testTag("reset_tasbih_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sıfırla",
                    tint = GoldPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dhikr selection chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dhikrPresets.take(3).forEachIndexed { index, preset ->
                val isSelected = uiState.tasbihDhikrIndex == index
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.selectTasbihDhikr(index)
                        viewModel.setTasbihTarget(preset.target)
                    },
                    label = { Text(preset.title, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.weight(1f).testTag("dhikr_chip_$index")
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dhikrPresets.drop(3).forEachIndexed { relativeIndex, preset ->
                val actualIndex = relativeIndex + 3
                val isSelected = uiState.tasbihDhikrIndex == actualIndex
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.selectTasbihDhikr(actualIndex)
                        viewModel.setTasbihTarget(preset.target)
                    },
                    label = { Text(preset.title, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.weight(1f).testTag("dhikr_chip_$actualIndex")
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Selected Dhikr Display
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentDhikr.arabic,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentDhikr.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hedef: ${currentDhikr.target} • Tur: ${(uiState.tasbihCount / currentDhikr.target) + 1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Big Touch Counter Circle
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GoldPrimary.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .border(2.dp, GoldPrimary.copy(alpha = 0.5f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    val nextCount = uiState.tasbihCount + 1
                    val isTargetReached = (nextCount % currentDhikr.target == 0)
                    triggerTasbihHaptic(context, isComplete = isTargetReached)
                    viewModel.incrementTasbih()
                }
                .testTag("tasbih_counter_button"),
            contentAlignment = Alignment.Center
        ) {
            // Circular progress indicator around rim
            CircularProgressIndicator(
                progress = { animatedProgress.value },
                modifier = Modifier.size(236.dp),
                color = GoldPrimary,
                strokeWidth = 5.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${uiState.tasbihCount}",
                    fontSize = 62.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${uiState.tasbihCount % currentDhikr.target} / ${currentDhikr.target}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldPrimary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "DOKUN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun triggerTasbihHaptic(context: Context, isComplete: Boolean) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (isComplete) {
                    // Double buzz on target complete
                    val timings = longArrayOf(0, 100, 80, 150)
                    val amplitudes = intArrayOf(0, 255, 0, 255)
                    it.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    it.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(if (isComplete) 200 else 40)
            }
        }
    } catch (_: Exception) {}
}
