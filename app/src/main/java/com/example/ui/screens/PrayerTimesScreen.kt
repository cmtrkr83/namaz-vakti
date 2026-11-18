package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.ui.components.CitySelectionSheet
import com.example.ui.components.DailyWisdomCard
import com.example.ui.components.LanguageSelectionSheet
import com.example.ui.components.PrayerCountdownCard
import com.example.ui.components.PrayerTimeRow
import com.example.ui.theme.SageCardSurface
import com.example.ui.theme.SageHairlineBorder
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmGoldAccent
import com.example.ui.viewmodel.PrayerUiState
import com.example.ui.viewmodel.PrayerViewModel
import com.example.util.LocaleHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    viewModel: PrayerViewModel,
    uiState: PrayerUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = remember(context) { context.findActivity() }

    var showCitySheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val languageSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val locationPermissions = remember {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val permissionDeniedToast = stringResource(R.string.location_permission_denied_toast)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.refreshLocationPermissionState(autoFetchIfNewlyGranted = false)
        if (granted) {
            viewModel.detectLocationAndFetchPrayerTimes { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (success) showCitySheet = false
            }
        } else {
            Toast.makeText(
                context,
                permissionDeniedToast,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Re-check location permission when returning from Android System Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshLocationPermissionState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Prompt runtime location permission automatically once on initial launch if not yet requested
    LaunchedEffect(Unit) {
        if (!viewModel.locationProvider.hasLocationPermission() &&
            !viewModel.preferences.hasRequestedLocationPermission
        ) {
            viewModel.preferences.hasRequestedLocationPermission = true
            locationPermissionLauncher.launch(locationPermissions)
        }
    }

    val isPermanentlyDenied = !uiState.hasLocationPermission &&
        viewModel.preferences.hasRequestedLocationPermission &&
        activity != null &&
        !ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) &&
        !ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    fun triggerLocationDetection() {
        if (viewModel.locationProvider.hasLocationPermission()) {
            viewModel.detectLocationAndFetchPrayerTimes { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (success) showCitySheet = false
            }
        } else if (isPermanentlyDenied) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        } else {
            viewModel.preferences.hasRequestedLocationPermission = true
            locationPermissionLauncher.launch(locationPermissions)
        }
    }

    val rotationAngle by animateFloatAsState(
        targetValue = if (uiState.isLoading) 360f else 0f,
        label = "refreshRotation"
    )

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.loadPrayerTimes(forceRefresh = true) },
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // Top Navigation & Location Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SageCardSurface,
                        border = BorderStroke(0.8.dp, SageHairlineBorder),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showCitySheet = true }
                            .testTag("location_picker_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = SagePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${uiState.cityName}, ${uiState.districtName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.cd_change_location),
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showLanguageSheet = true },
                            modifier = Modifier.testTag("language_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = stringResource(R.string.cd_change_language),
                                tint = SagePrimary
                            )
                        }

                        IconButton(
                            onClick = { triggerLocationDetection() },
                            enabled = !uiState.isDetectingLocation,
                            modifier = Modifier.testTag("quick_gps_location_button")
                        ) {
                            if (uiState.isDetectingLocation) {
                                CircularProgressIndicator(
                                    color = SagePrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.MyLocation,
                                    contentDescription = stringResource(R.string.cd_use_current_location),
                                    tint = SagePrimary
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.loadPrayerTimes(forceRefresh = true) },
                            modifier = Modifier.testTag("refresh_prayer_times_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.cd_refresh),
                                tint = SagePrimary,
                                modifier = Modifier.rotate(rotationAngle)
                            )
                        }
                    }
                }
            }

            // Location Permission Rationale / Prompt Card (shown if permission is not granted)
            item {
                AnimatedVisibility(
                    visible = !uiState.hasLocationPermission && !uiState.locationBannerDismissed,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val bannerShape = RoundedCornerShape(20.dp)
                    Surface(
                        shape = bannerShape,
                        color = SageCardSurface,
                        border = BorderStroke(0.9.dp, Color(0xCCFFFFFF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 5.dp,
                                shape = bannerShape,
                                clip = false,
                                ambientColor = Color(0x1C172A22),
                                spotColor = Color(0x26172A22)
                            )
                            .testTag("location_permission_banner")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(SagePrimaryMuted),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.MyLocation,
                                        contentDescription = null,
                                        tint = SagePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.location_banner_title),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(
                                            if (isPermanentlyDenied) {
                                                R.string.location_banner_desc_settings
                                            } else {
                                                R.string.location_banner_desc_prompt
                                            }
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { viewModel.dismissLocationBanner() },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("dismiss_location_banner_button")
                                ) {
                                    Text(
                                        text = stringResource(R.string.location_banner_dismiss),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { triggerLocationDetection() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SagePrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("request_location_permission_banner_button")
                                ) {
                                    Text(
                                        text = stringResource(
                                            if (isPermanentlyDenied) {
                                                R.string.location_banner_open_settings
                                            } else {
                                                R.string.location_banner_use_location
                                            }
                                        ),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Date Bar (Miladi & Hicri)
            item {
                uiState.schedule?.let { schedule ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SageCardSurface,
                        border = BorderStroke(0.7.dp, SageHairlineBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = null,
                                    tint = SagePrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = schedule.dateUzun,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = schedule.hicriDate,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = WarmGoldAccent
                            )
                        }
                    }
                }
            }

            // Hero Countdown Card
            item {
                uiState.schedule?.let { schedule ->
                    PrayerCountdownCard(schedule = schedule)
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SagePrimary)
                    }
                }
            }

            // Section Title: Vakitler
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.section_todays_prayers),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.3.sp,
                        color = TextSecondary
                    )

                    Text(
                        text = stringResource(R.string.source_diyanet_short),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            // 6 Prayer Rows
            uiState.schedule?.let { schedule ->
                items(schedule.prayers.size) { index ->
                    val prayer = schedule.prayers[index]
                    val isEnabled = uiState.prayerNotificationToggles[prayer.type.name.lowercase()] ?: true
                    PrayerTimeRow(
                        item = prayer,
                        isNotificationActive = isEnabled,
                        weatherInfo = uiState.prayerWeather[prayer.type]
                    )
                }
            }

            // Daily Ayet & Hadis
            item {
                DailyWisdomCard(wisdom = uiState.dailyWisdom)
            }
        }
    }

    if (showCitySheet) {
        CitySelectionSheet(
            sheetState = sheetState,
            currentCityId = uiState.cityId,
            currentDistrictId = uiState.districtId,
            districts = uiState.districts,
            isFetchingDistricts = uiState.isFetchingDistricts,
            isDetectingLocation = uiState.isDetectingLocation,
            onDetectLocation = { triggerLocationDetection() },
            onDismiss = { showCitySheet = false },
            onCitySelected = { id, name ->
                viewModel.selectCity(id, name)
            },
            onDistrictSelected = { id, name ->
                viewModel.selectDistrict(id, name)
            }
        )
    }

    if (showLanguageSheet) {
        LanguageSelectionSheet(
            sheetState = languageSheetState,
            currentLanguageCode = uiState.languageCode,
            onDismiss = { showLanguageSheet = false },
            onLanguageSelected = { lang ->
                viewModel.setAppLanguage(lang.code)
                val updatedContext = LocaleHelper.wrapContext(context, lang.code)
                val langName = LocaleHelper.displayTitle(updatedContext, lang)
                Toast.makeText(
                    updatedContext,
                    updatedContext.getString(R.string.language_changed_toast, langName),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
