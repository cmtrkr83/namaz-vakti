package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.FilterDrama
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NightlightRound
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerType
import com.example.data.model.PrayerWeatherInfo
import com.example.data.model.WeatherCondition
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

@Composable
fun PrayerTimeRow(
    item: PrayerItem,
    isNotificationActive: Boolean = true,
    weatherInfo: PrayerWeatherInfo? = null,
    modifier: Modifier = Modifier
) {
    val displayIcon: ImageVector = if (weatherInfo != null) {
        when (weatherInfo.condition) {
            WeatherCondition.CLEAR_DAY -> Icons.Outlined.WbSunny
            WeatherCondition.CLEAR_NIGHT -> Icons.Outlined.NightsStay
            WeatherCondition.PARTLY_CLOUDY -> Icons.Outlined.WbCloudy
            WeatherCondition.CLOUDY -> Icons.Outlined.Cloud
            WeatherCondition.FOGGY -> Icons.Outlined.FilterDrama
            WeatherCondition.RAINY -> Icons.Outlined.WaterDrop
            WeatherCondition.SNOWY -> Icons.Outlined.AcUnit
            WeatherCondition.THUNDERSTORM -> Icons.Outlined.Bolt
        }
    } else {
        when (item.type) {
            PrayerType.IMSAK -> Icons.Outlined.WbTwilight
            PrayerType.GUNES -> Icons.Outlined.WbSunny
            PrayerType.OGLE -> Icons.Outlined.LightMode
            PrayerType.IKINDI -> Icons.Outlined.WbCloudy
            PrayerType.AKSAM -> Icons.Outlined.Bedtime
            PrayerType.YATSI -> Icons.Outlined.NightlightRound
        }
    }

    val iconDescription = if (weatherInfo != null) {
        "${item.type.displayName} - ${weatherInfo.condition.labelTr} ${weatherInfo.temperatureCelsius}°C"
    } else {
        item.type.displayName
    }

    val cardBgColor by animateColorAsState(
        targetValue = when {
            item.isCurrent -> Color(0xFFE4EFE8)
            item.isNext -> Color(0xFFF0EDE2)
            else -> SageCardSurface
        },
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            item.isCurrent -> SagePrimary.copy(alpha = 0.55f)
            item.isNext -> WarmGoldAccent.copy(alpha = 0.35f)
            else -> Color(0xB3FFFFFF)
        },
        label = "borderColor"
    )

    val elevation by animateDpAsState(
        targetValue = when {
            item.isCurrent -> 10.dp
            item.isNext -> 7.dp
            else -> 5.dp
        },
        label = "cardElevation"
    )

    val cardShape = RoundedCornerShape(22.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = elevation,
                shape = cardShape,
                clip = false,
                ambientColor = Color(0x1E172A22),
                spotColor = if (item.isCurrent) Color(0x36245C49) else Color(0x26172A22)
            )
            .testTag("prayer_row_${item.type.name.lowercase()}"),
        shape = cardShape,
        color = cardBgColor,
        border = BorderStroke(
            width = if (item.isCurrent) 1.1.dp else 0.8.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Icon + Name + Description
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                item.isCurrent -> SagePrimaryMuted
                                item.isNext -> WarmGoldMuted
                                else -> SageCardVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = displayIcon,
                        contentDescription = iconDescription,
                        tint = when {
                            item.isCurrent -> SagePrimary
                            item.isNext -> WarmGoldAccent
                            else -> TextSecondary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.type.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (item.isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (item.isCurrent) SagePrimary else TextPrimary
                        )

                        if (item.isCurrent) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SagePrimary
                            ) {
                                Text(
                                    text = "ŞU AN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        } else if (item.isNext) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = WarmGoldMuted
                            ) {
                                Text(
                                    text = "SIRADAKİ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarmGoldAccent,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (weatherInfo != null) {
                            "${weatherInfo.temperatureCelsius}°C • ${weatherInfo.condition.labelTr} • ${item.type.subtitle}"
                        } else {
                            item.type.subtitle
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Subtle notification status + Tabular Prayer Time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isNotificationActive) {
                        Icons.Outlined.NotificationsActive
                    } else {
                        Icons.Outlined.NotificationsOff
                    },
                    contentDescription = if (isNotificationActive) "Bildirim açık" else "Bildirim kapalı",
                    tint = if (isNotificationActive) SagePrimary.copy(alpha = 0.72f) else TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = item.time,
                    style = TextStyle(
                        fontFamily = AppMonoFamily,
                        fontSize = 20.sp,
                        fontWeight = if (item.isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                        fontFeatureSettings = "tnum",
                        color = if (item.isCurrent) SagePrimary else TextPrimary
                    )
                )
            }
        }
    }
}
