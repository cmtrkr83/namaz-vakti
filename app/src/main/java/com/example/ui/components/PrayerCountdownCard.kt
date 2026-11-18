package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DailyPrayerSchedule
import com.example.ui.theme.AppMonoFamily
import com.example.ui.theme.KerahatAmber
import com.example.ui.theme.SageCardSurface
import com.example.ui.theme.SageCardVariant
import com.example.ui.theme.SageHairlineBorder
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.LocaleHelper
import com.example.util.PrayerTimeHelper

@Composable
fun PrayerCountdownCard(
    schedule: DailyPrayerSchedule,
    modifier: Modifier = Modifier
) {
    val countdownText = PrayerTimeHelper.formatSecondsToCountdown(schedule.remainingSecondsToNext)
    val cardShape = RoundedCornerShape(26.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = cardShape,
                clip = false,
                ambientColor = Color(0x1E172A22),
                spotColor = Color(0x2E172A22)
            )
            .testTag("prayer_countdown_card"),
        shape = cardShape,
        color = SageCardSurface,
        border = BorderStroke(
            width = 0.9.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xD9FFFFFF),
                    SageHairlineBorder
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SagePrimary.copy(alpha = 0.055f),
                            Color.Transparent
                        ),
                        radius = 750f
                    )
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Next prayer label & exact time pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(SagePrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val nextPrayerName = stringResource(LocaleHelper.prayerNameRes(schedule.nextPrayer.type))
                        Text(
                            text = stringResource(R.string.countdown_remaining_fmt, nextPrayerName.uppercase()),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.1.sp,
                            color = TextSecondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SagePrimaryMuted,
                        border = BorderStroke(0.6.dp, SagePrimary.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = schedule.nextPrayer.time,
                            fontFamily = AppMonoFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SagePrimary,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Hero Countdown Timer with Tabular Numerals
                Text(
                    text = countdownText,
                    style = TextStyle(
                        fontFamily = AppMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 44.sp,
                        letterSpacing = 1.5.sp,
                        fontFeatureSettings = "tnum",
                        color = TextPrimary
                    ),
                    modifier = Modifier.testTag("countdown_timer_text")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Prayer Period Progress Bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { schedule.progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SagePrimary,
                        trackColor = SageCardVariant,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val currentName = stringResource(LocaleHelper.prayerNameRes(schedule.currentPrayer.type))
                        val nextName = stringResource(LocaleHelper.prayerNameRes(schedule.nextPrayer.type))
                        Text(
                            text = "$currentName • ${schedule.currentPrayer.time}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "$nextName • ${schedule.nextPrayer.time}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SagePrimary
                        )
                    }
                }

                // Kerahat Alert Banner
                AnimatedVisibility(
                    visible = schedule.isKerahat,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = KerahatAmber.copy(alpha = 0.1f),
                            border = BorderStroke(0.8.dp, KerahatAmber.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.WarningAmber,
                                    contentDescription = stringResource(R.string.cd_kerahat_warning),
                                    tint = KerahatAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = schedule.kerahatReason ?: stringResource(R.string.kerahat_default),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KerahatAmber,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subtle Info Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Explore,
                            contentDescription = null,
                            tint = SagePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.qibla_time_fmt, schedule.kibleSaati),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.calendar_diyanet),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
