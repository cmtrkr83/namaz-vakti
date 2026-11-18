package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
import com.example.ui.viewmodel.PrayerUiState
import com.example.ui.viewmodel.PrayerViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaCompassScreen(
    viewModel: PrayerViewModel,
    uiState: PrayerUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val compassState by viewModel.compassState.collectAsState()

    DisposableEffect(Unit) {
        viewModel.compassHelper.startListening()
        onDispose {
            viewModel.compassHelper.stopListening()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.compassHelper.requestLocation(uiState.cityId)
        }
    }

    var wasFacingQibla by remember { mutableStateOf(false) }
    LaunchedEffect(compassState.isFacingQibla) {
        if (compassState.isFacingQibla && !wasFacingQibla) {
            triggerCompassHaptic(context)
        }
        wasFacingQibla = compassState.isFacingQibla
    }

    val animatedAzimuth by animateFloatAsState(
        targetValue = compassState.azimuth,
        animationSpec = spring(stiffness = 200f),
        label = "azimuthAnim"
    )

    val dialBorderColor by animateColorAsState(
        targetValue = if (compassState.isFacingQibla) SagePrimary else SageHairlineBorder,
        label = "dialBorder"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 124.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title & Location Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.qibla_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (compassState.isGpsActive) {
                        stringResource(R.string.qibla_gps_active)
                    } else {
                        stringResource(R.string.qibla_city_coords_fmt, uiState.cityName)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.8.dp, SagePrimary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("request_gps_location_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.MyLocation,
                    contentDescription = null,
                    tint = SagePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.qibla_gps_button),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SagePrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alignment Status Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (compassState.isFacingQibla) SagePrimaryMuted else SageCardSurface,
            border = BorderStroke(
                width = 0.8.dp,
                color = if (compassState.isFacingQibla) SagePrimary.copy(alpha = 0.5f) else SageHairlineBorder
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (compassState.isFacingQibla) Icons.Filled.CheckCircle else Icons.Outlined.NearMe,
                    contentDescription = null,
                    tint = if (compassState.isFacingQibla) SagePrimary else WarmGoldAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(
                        if (compassState.isFacingQibla) R.string.qibla_aligned else R.string.qibla_align_instruction
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (compassState.isFacingQibla) SagePrimary else TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Light Airy Compass Dial with Clean Center Readout Hub
        val needleColor = if (compassState.isFacingQibla) SagePrimary else WarmGoldAccent
        val minorTickColor = TextMuted.copy(alpha = 0.55f)
        val southNeedleColor = TextMuted.copy(alpha = 0.35f)
        val hubBorderColor = if (compassState.isFacingQibla) SagePrimary.copy(alpha = 0.45f) else SageHairlineBorder

        Box(
            modifier = Modifier
                .size(276.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF0F5F1),
                            SageCardVariant
                        )
                    )
                )
                .border(1.5.dp, dialBorderColor, CircleShape)
                .testTag("compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2 - 16.dp.toPx()

                for (i in 0 until 360 step 15) {
                    val angleRad = Math.toRadians((i - animatedAzimuth).toDouble())
                    val isCardinal = i % 90 == 0
                    val tickLen = if (isCardinal) 14.dp.toPx() else 7.dp.toPx()
                    val strokeW = if (isCardinal) 2.2f else 1f

                    val startX = center.x + (radius - tickLen) * sin(angleRad).toFloat()
                    val startY = center.y - (radius - tickLen) * cos(angleRad).toFloat()
                    val endX = center.x + radius * sin(angleRad).toFloat()
                    val endY = center.y - radius * cos(angleRad).toFloat()

                    drawLine(
                        color = if (isCardinal) SagePrimary.copy(alpha = 0.85f) else minorTickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeW
                    )
                }

                val relativeQiblaAngle = compassState.qiblaAngle - animatedAzimuth
                rotate(relativeQiblaAngle, pivot = center) {
                    val needlePath = Path().apply {
                        moveTo(center.x, center.y - radius + 12.dp.toPx())
                        lineTo(center.x - 13.dp.toPx(), center.y - 44.dp.toPx())
                        lineTo(center.x, center.y - 36.dp.toPx())
                        lineTo(center.x + 13.dp.toPx(), center.y - 44.dp.toPx())
                        close()
                    }
                    drawPath(
                        path = needlePath,
                        color = needleColor
                    )

                    val southNeedle = Path().apply {
                        moveTo(center.x, center.y + radius - 22.dp.toPx())
                        lineTo(center.x - 7.dp.toPx(), center.y + 42.dp.toPx())
                        lineTo(center.x, center.y + 36.dp.toPx())
                        lineTo(center.x + 7.dp.toPx(), center.y + 42.dp.toPx())
                        close()
                    }
                    drawPath(
                        path = southNeedle,
                        color = southNeedleColor
                    )

                    drawCircle(
                        color = needleColor,
                        radius = 7.dp.toPx(),
                        center = Offset(center.x, center.y - radius + 12.dp.toPx())
                    )
                }

                // Clean frosted center hub ring for crisp heading text
                drawCircle(
                    color = Color(0xFFEBF2ED),
                    radius = 40.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = hubBorderColor,
                    radius = 40.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            val headingInt = compassState.azimuth.toInt()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${headingInt}°",
                    style = TextStyle(
                        fontFamily = AppMonoFamily,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum",
                        color = TextPrimary
                    )
                )
                Text(
                    text = stringResource(R.string.compass_north),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Distance & Angle Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = SageCardSurface,
                border = BorderStroke(0.8.dp, SageHairlineBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.qibla_angle_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${compassState.qiblaAngle.toInt()}°",
                        fontFamily = AppMonoFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SagePrimary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = SageCardSurface,
                border = BorderStroke(0.8.dp, SageHairlineBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.qibla_distance_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.qibla_distance_km_fmt, compassState.distanceKm),
                        fontFamily = AppMonoFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Calibration Tip Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SageCardSurface,
            border = BorderStroke(0.6.dp, SageHairlineBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = SagePrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.qibla_calibration_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private fun triggerCompassHaptic(context: Context) {
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
                it.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(100)
            }
        }
    } catch (_: Exception) {}
}
