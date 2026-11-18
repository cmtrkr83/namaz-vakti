package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NightlightRound
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.PrayerType
import com.example.ui.theme.AppMonoFamily
import com.example.ui.theme.SageCardSurface
import com.example.ui.theme.SageCardVariant
import com.example.ui.theme.SageHairlineBorder
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmGoldAccent
import com.example.ui.theme.WarmGoldMuted
import com.example.ui.viewmodel.PrayerUiState
import com.example.ui.viewmodel.PrayerViewModel

private data class SoundOptionUi(
    val key: String,
    val title: String,
    val subtitle: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

private data class PrayerToggleMeta(
    val key: String,
    val type: PrayerType,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: PrayerViewModel,
    uiState: PrayerUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    val offsetOptions = remember {
        listOf(
            0 to "Tam vaktinde",
            5 to "5 dk önce",
            10 to "10 dk önce",
            15 to "15 dk önce",
            30 to "30 dk önce",
            45 to "45 dk önce"
        )
    }

    val soundOptions = remember {
        listOf(
            SoundOptionUi(
                key = "ezan",
                title = "Ezan / Melodik Uyarı",
                subtitle = "Vakit girdiğinde belirgin sesli bildirim",
                activeIcon = Icons.Filled.MusicNote,
                inactiveIcon = Icons.Outlined.MusicNote
            ),
            SoundOptionUi(
                key = "bip",
                title = "Kısa Bildirim Sesi",
                subtitle = "Sakin ve kısa sistem uyarı tonu",
                activeIcon = Icons.Filled.Notifications,
                inactiveIcon = Icons.Outlined.Notifications
            ),
            SoundOptionUi(
                key = "vibrate",
                title = "Yalnızca Titreşim",
                subtitle = "Sessiz ortamlarda titreşimle hatırlat",
                activeIcon = Icons.Filled.Vibration,
                inactiveIcon = Icons.Outlined.Vibration
            )
        )
    }

    val prayersList = remember {
        listOf(
            PrayerToggleMeta("imsak", PrayerType.IMSAK, "İmsak (Sahur)", Icons.Outlined.WbTwilight),
            PrayerToggleMeta("gunes", PrayerType.GUNES, "Güneş Doğuşu", Icons.Outlined.WbSunny),
            PrayerToggleMeta("ogle", PrayerType.OGLE, "Öğle Vakti", Icons.Outlined.LightMode),
            PrayerToggleMeta("ikindi", PrayerType.IKINDI, "İkindi Vakti", Icons.Outlined.WbCloudy),
            PrayerToggleMeta("aksam", PrayerType.AKSAM, "Akşam (İftar)", Icons.Outlined.Bedtime),
            PrayerToggleMeta("yatsi", PrayerType.YATSI, "Yatsı Vakti", Icons.Outlined.NightlightRound)
        )
    }

    val prayerTimesMap = remember(uiState.schedule) {
        uiState.schedule?.prayers?.associate { it.type to it.time } ?: emptyMap()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 124.dp)
            .testTag("notification_settings_screen")
    ) {
        // Editorial Header with Compact "Test Et" Pill Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bildirim Ayarları",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ezan ve vakit hatırlatıcılarını düzenleyin",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = SagePrimaryMuted,
                border = BorderStroke(0.8.dp, SagePrimary.copy(alpha = 0.32f)),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.triggerTestNotification()
                        }
                    }
                    .testTag("test_notification_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = "Bildirimi test et",
                        tint = SagePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Test Et",
                        style = MaterialTheme.typography.labelLarge,
                        color = SagePrimary
                    )
                }
            }
        }

        // Compact Test Feedback Banner
        AnimatedVisibility(
            visible = uiState.testNotificationMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SagePrimaryMuted,
                    border = BorderStroke(0.8.dp, SagePrimary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SagePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.testNotificationMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Android 13+ Permission Card (only if not yet granted)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = WarmGoldMuted,
                border = BorderStroke(0.8.dp, WarmGoldAccent.copy(alpha = 0.38f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bildirim İzni Gerekli",
                            style = MaterialTheme.typography.titleSmall,
                            color = WarmGoldAccent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Vakit hatırlatıcıları için sistem iznini onaylayın.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SagePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("grant_notification_permission_button")
                    ) {
                        Text(
                            text = "İzin Ver",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Section 1: Hatırlatma Zamanı
        SectionLabel(text = "HATIRLATMA ZAMANI")
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SageCardSurface,
            border = BorderStroke(0.8.dp, SageHairlineBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Vakitten ne kadar önce haber verilsin?",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    offsetOptions.forEach { (offset, label) ->
                        val isSelected = uiState.notificationOffsetMinutes == offset
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) SagePrimary else SageCardVariant,
                            border = BorderStroke(
                                width = 0.8.dp,
                                color = if (isSelected) SagePrimary else SageHairlineBorder
                            ),
                            modifier = Modifier
                                .heightIn(min = 42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { viewModel.setNotificationOffsetMinutes(offset) }
                                .testTag("offset_chip_$offset")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Ses ve Uyarı Modu
        SectionLabel(text = "SES VE UYARI TÜRÜ")
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SageCardSurface,
            border = BorderStroke(0.8.dp, SageHairlineBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                soundOptions.forEachIndexed { index, option ->
                    val isSelected = uiState.notificationSoundMode == option.key
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .clickable { viewModel.setNotificationSoundMode(option.key) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("sound_mode_${option.key}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) SagePrimaryMuted else SageCardVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) option.activeIcon else option.inactiveIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) SagePrimary else TextSecondary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) SagePrimary else TextPrimary
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = option.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SagePrimary else Color.Transparent)
                                .border(
                                    width = 1.2.dp,
                                    color = if (isSelected) SagePrimary else TextMuted,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seçili",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    if (index < soundOptions.size - 1) {
                        HorizontalDivider(
                            color = SageHairlineBorder.copy(alpha = 0.45f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Vakitlere Göre Hatırlatıcılar + Daily Repeat
        SectionLabel(text = "VAKİTLERE GÖRE HATIRLATICILAR")
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SageCardSurface,
            border = BorderStroke(0.8.dp, SageHairlineBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                prayersList.forEach { prayerMeta ->
                    val isChecked = uiState.prayerNotificationToggles[prayerMeta.key] ?: true
                    val rawTime = prayerTimesMap[prayerMeta.type]
                    val triggerHint = buildTriggerHint(
                        rawTime = rawTime,
                        offsetMinutes = uiState.notificationOffsetMinutes,
                        isEnabled = isChecked
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 58.dp)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isChecked) SagePrimaryMuted else SageCardVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = prayerMeta.icon,
                                    contentDescription = null,
                                    tint = if (isChecked) SagePrimary else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = prayerMeta.label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isChecked) TextPrimary else TextSecondary
                                    )
                                    if (rawTime != null) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = rawTime,
                                            fontFamily = AppMonoFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isChecked) SagePrimary else TextMuted
                                        )
                                    }
                                }
                                Text(
                                    text = triggerHint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isChecked,
                            onCheckedChange = { viewModel.togglePrayerNotification(prayerMeta.key, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SagePrimary,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = SageCardVariant,
                                uncheckedBorderColor = SageHairlineBorder
                            ),
                            modifier = Modifier.testTag("toggle_prayer_${prayerMeta.key}")
                        )
                    }

                    HorizontalDivider(
                        color = SageHairlineBorder.copy(alpha = 0.45f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Integrated Daily Repeat Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(WarmGoldMuted),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Repeat,
                                contentDescription = null,
                                tint = WarmGoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Her Gün Otomatik Yenile",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Alarmlar her gün yeni vakitlere göre kurulur",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Switch(
                        checked = uiState.notificationRepeatDaily,
                        onCheckedChange = { viewModel.setNotificationRepeatDaily(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SagePrimary,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SageCardVariant,
                            uncheckedBorderColor = SageHairlineBorder
                        ),
                        modifier = Modifier.testTag("repeat_daily_switch")
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        color = TextSecondary,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

private fun buildTriggerHint(
    rawTime: String?,
    offsetMinutes: Int,
    isEnabled: Boolean
): String {
    if (!isEnabled) return "Bildirim kapalı"
    if (rawTime == null) {
        return if (offsetMinutes == 0) "Tam vaktinde bildirilir" else "$offsetMinutes dk önce bildirilir"
    }
    if (offsetMinutes == 0) return "Tam vaktinde ($rawTime) bildirilir"

    val parts = rawTime.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()
    val minute = parts.getOrNull(1)?.toIntOrNull()
    if (hour == null || minute == null) return "$offsetMinutes dk önce bildirilir"

    val totalMinutes = ((hour * 60 + minute - offsetMinutes) % 1440 + 1440) % 1440
    val targetHour = totalMinutes / 60
    val targetMinute = totalMinutes % 60
    val formatted = String.format("%02d:%02d", targetHour, targetMinute)
    return "$formatted'de hatırlatılır ($offsetMinutes dk önce)"
}
