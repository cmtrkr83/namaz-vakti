package com.example.ui.screens

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.SageCardSurface
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SereneSageGradient
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.PrayerViewModel

sealed class AppNavTab(
    val index: Int,
    @StringRes val titleRes: Int,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val tag: String
) {
    object Prayers : AppNavTab(
        0, R.string.nav_prayers, Icons.Filled.AccessTimeFilled, Icons.Outlined.AccessTime, "nav_prayers"
    )
    object Monthly : AppNavTab(
        1, R.string.nav_monthly, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_monthly"
    )
    object Qibla : AppNavTab(
        2, R.string.nav_qibla, Icons.Filled.Explore, Icons.Outlined.Explore, "nav_qibla"
    )
    object Notifications : AppNavTab(
        3, R.string.nav_notifications, Icons.Filled.Notifications, Icons.Outlined.Notifications, "nav_notifications"
    )
    object Tasbih : AppNavTab(
        4, R.string.nav_tasbih, Icons.Filled.TouchApp, Icons.Outlined.TouchApp, "nav_tasbih"
    )
}

@Composable
fun MainScreen(
    viewModel: PrayerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = isMenuExpanded || selectedTabIndex != 0) {
        if (isMenuExpanded) {
            isMenuExpanded = false
        } else {
            selectedTabIndex = 0
        }
    }

    val tabs = remember {
        listOf(
            AppNavTab.Prayers,
            AppNavTab.Monthly,
            AppNavTab.Qibla,
            AppNavTab.Notifications,
            AppNavTab.Tasbih
        )
    }

    // Root container with soft pastel sage gradient extending edge-to-edge
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SereneSageGradient)
    ) {
        // Main Screen Content with top status bar safe inset
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            when (selectedTabIndex) {
                0 -> PrayerTimesScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> MonthlyScheduleScreen(
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize()
                )
                2 -> QiblaCompassScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize()
                )
                3 -> NotificationSettingsScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize()
                )
                4 -> TasbihScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Dimmed Scrim when FAB Menu is Expanded
        AnimatedVisibility(
            visible = isMenuExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x52172A22))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isMenuExpanded = false
                    }
            )
        }

        // Expandable Floating Action Button Menu
        FloatingNavigationMenu(
            isExpanded = isMenuExpanded,
            selectedTabIndex = selectedTabIndex,
            tabs = tabs,
            onToggle = { isMenuExpanded = !isMenuExpanded },
            onTabSelected = { index ->
                selectedTabIndex = index
                isMenuExpanded = false
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 18.dp)
        )
    }
}

@Composable
private fun FloatingNavigationMenu(
    isExpanded: Boolean,
    selectedTabIndex: Int,
    tabs: List<AppNavTab>,
    onToggle: () -> Unit,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val iconRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "fabIconRotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Expandable Menu Pills
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }) + scaleIn(initialScale = 0.92f),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 }) + scaleOut(targetScale = 0.92f)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTabIndex == tab.index
                    val pillShape = RoundedCornerShape(18.dp)

                    Surface(
                        onClick = { onTabSelected(tab.index) },
                        shape = pillShape,
                        color = if (isSelected) SagePrimary else SageCardSurface,
                        border = BorderStroke(
                            width = 0.9.dp,
                            color = if (isSelected) SagePrimary else Color(0xCCFFFFFF)
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .shadow(
                                elevation = if (isSelected) 10.dp else 6.dp,
                                shape = pillShape,
                                clip = false,
                                ambientColor = Color(0x1E172A22),
                                spotColor = Color(0x2E172A22)
                            )
                            .testTag(tab.tag)
                    ) {
                        val tabTitle = stringResource(tab.titleRes)
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tabTitle,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = tabTitle,
                                tint = if (isSelected) Color.White else SagePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Primary Floating Action Button
        FloatingActionButton(
            onClick = onToggle,
            shape = RoundedCornerShape(20.dp),
            containerColor = SagePrimary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            ),
            modifier = Modifier
                .size(58.dp)
                .testTag("fab_menu_toggle")
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.Close else Icons.Filled.Menu,
                contentDescription = stringResource(
                    if (isExpanded) R.string.cd_menu_close else R.string.cd_menu_open
                ),
                modifier = Modifier
                    .size(24.dp)
                    .rotate(iconRotation)
            )
        }
    }
}
