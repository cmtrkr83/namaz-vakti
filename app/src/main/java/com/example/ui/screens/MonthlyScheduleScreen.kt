package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.PrayerTimeEntity
import com.example.ui.theme.AppMonoFamily
import com.example.ui.theme.SageCardSurface
import com.example.ui.theme.SageCardVariant
import com.example.ui.theme.SageHairlineBorder
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PrayerUiState
import com.example.util.PrayerTimeHelper

@Composable
fun MonthlyScheduleScreen(
    uiState: PrayerUiState,
    modifier: Modifier = Modifier
) {
    val todayDateKisa = PrayerTimeHelper.getTodayDateKisa()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("monthly_schedule_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title & Location Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.monthly_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = SagePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${uiState.cityName} - ${uiState.districtName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SagePrimaryMuted,
                border = BorderStroke(0.6.dp, SagePrimary.copy(alpha = 0.25f))
            ) {
                Text(
                    text = stringResource(R.string.monthly_badge),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SagePrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Table Header
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SageCardVariant,
            border = BorderStroke(0.6.dp, SageHairlineBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.monthly_col_date),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1.45f),
                    color = TextSecondary
                )
                listOf(
                    R.string.prayer_imsak,
                    R.string.prayer_gunes,
                    R.string.prayer_ogle,
                    R.string.prayer_ikindi,
                    R.string.prayer_aksam,
                    R.string.prayer_yatsi
                ).forEach { headerRes ->
                    Text(
                        text = stringResource(headerRes),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.monthlyTimes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SagePrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.monthlyTimes, key = { it.id }) { item ->
                    val isToday = item.miladiTarihKisa == todayDateKisa
                    MonthlyDayRow(item = item, isToday = isToday)
                }
            }
        }
    }
}

@Composable
private fun MonthlyDayRow(
    item: PrayerTimeEntity,
    isToday: Boolean
) {
    val monoTimeStyle = TextStyle(
        fontFamily = AppMonoFamily,
        fontSize = 11.5.sp,
        fontFeatureSettings = "tnum",
        fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
    )
    val rowShape = RoundedCornerShape(16.dp)

    Surface(
        shape = rowShape,
        color = if (isToday) Color(0xFFE4EFE8) else SageCardSurface,
        border = BorderStroke(
            width = if (isToday) 1.dp else 0.7.dp,
            color = if (isToday) SagePrimary.copy(alpha = 0.5f) else Color(0xA6FFFFFF)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isToday) 7.dp else 3.dp,
                shape = rowShape,
                clip = false,
                ambientColor = Color(0x18172A22),
                spotColor = Color(0x22172A22)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.45f)) {
                Text(
                    text = item.miladiTarihKisa.substringBeforeLast("."),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    color = if (isToday) SagePrimary else TextPrimary
                )
                if (isToday) {
                    Text(
                        text = stringResource(R.string.monthly_today),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SagePrimary
                    )
                }
            }

            Text(
                text = item.imsak,
                style = monoTimeStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                color = if (isToday) SagePrimary else TextPrimary
            )
            Text(
                text = item.gunes,
                style = monoTimeStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                color = TextSecondary
            )
            Text(
                text = item.ogle,
                style = monoTimeStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                color = if (isToday) SagePrimary else TextPrimary
            )
            Text(
                text = item.ikindi,
                style = monoTimeStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                color = if (isToday) SagePrimary else TextPrimary
            )
            Text(
                text = item.aksam,
                style = monoTimeStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                color = if (isToday) SagePrimary else TextPrimary
            )
            Text(
                text = item.yatsi,
                style = monoTimeStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                color = if (isToday) SagePrimary else TextPrimary
            )
        }
    }
}
