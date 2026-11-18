package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AppMonoFamily
import com.example.ui.theme.AppSerifFamily
import com.example.ui.theme.MintSoft
import com.example.ui.theme.SageCardSurface
import com.example.ui.theme.SageCardVariant
import com.example.ui.theme.SageHairlineBorder
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PrayerUiState
import com.example.ui.viewmodel.PrayerViewModel

data class DhikrPreset(
    @StringRes val titleRes: Int,
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

    val dhikrPresets = remember {
        listOf(
            DhikrPreset(R.string.dhikr_subhanallah, "سُبْحَانَ اللَّهِ", 33),
            DhikrPreset(R.string.dhikr_alhamdulillah, "الْحَمْدُ لِلَّهِ", 33),
            DhikrPreset(R.string.dhikr_allahuakbar, "اللَّهُ أَكْبَرُ", 33),
            DhikrPreset(R.string.dhikr_lailahaillallah, "لَا إِلَهَ إِلَّا اللَّهُ", 100),
            DhikrPreset(R.string.dhikr_astaghfirullah, "أَسْتَغْفِرُ اللَّهَ", 100),
            DhikrPreset(R.string.dhikr_salawat, "اللَّهُمَّ صَلِّ عَلَى سَيِّدِنَا مُحَمَّدٍ", 100)
        )
    }

    val currentDhikr = dhikrPresets.getOrElse(uiState.tasbihDhikrIndex) { dhikrPresets[0] }
    val progress = (uiState.tasbihCount % currentDhikr.target).toFloat() / currentDhikr.target.toFloat()

    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        label = "tasbihProgress"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 12.dp, bottom = 124.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.tasbih_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.tasbih_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
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
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.cd_reset),
                    tint = SagePrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clean single-line horizontal scrollable Dhikr pills
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(dhikrPresets) { index, preset ->
                val isSelected = uiState.tasbihDhikrIndex == index
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) SagePrimary else SageCardSurface,
                    border = BorderStroke(
                        width = 0.8.dp,
                        color = if (isSelected) SagePrimary else SageHairlineBorder
                    ),
                    modifier = Modifier
                        .heightIn(min = 42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            viewModel.selectTasbihDhikr(index)
                            viewModel.setTasbihTarget(preset.target)
                        }
                        .testTag("dhikr_chip_$index")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = stringResource(preset.titleRes),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Selected Dhikr Display
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = SageCardSurface,
            border = BorderStroke(0.8.dp, SageHairlineBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentDhikr.arabic,
                    fontSize = 27.sp,
                    fontFamily = AppSerifFamily,
                    fontWeight = FontWeight.Medium,
                    color = SagePrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(currentDhikr.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.tasbih_target_round_fmt,
                        currentDhikr.target,
                        (uiState.tasbihCount / currentDhikr.target) + 1
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Big Touch Counter Circle
        Box(
            modifier = Modifier
                .size(236.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF0F5F1),
                            SageCardVariant
                        )
                    )
                )
                .border(1.2.dp, SagePrimary.copy(alpha = 0.25f), CircleShape)
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
            CircularProgressIndicator(
                progress = { animatedProgress.value },
                modifier = Modifier.size(232.dp),
                color = SagePrimary,
                strokeWidth = 5.dp,
                trackColor = MintSoft,
                strokeCap = StrokeCap.Round
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${uiState.tasbihCount}",
                    style = TextStyle(
                        fontFamily = AppMonoFamily,
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum",
                        color = TextPrimary
                    )
                )
                Text(
                    text = "${uiState.tasbihCount % currentDhikr.target} / ${currentDhikr.target}",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SagePrimaryMuted,
                    border = BorderStroke(0.6.dp, SagePrimary.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = stringResource(R.string.tasbih_tap),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = SagePrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
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
