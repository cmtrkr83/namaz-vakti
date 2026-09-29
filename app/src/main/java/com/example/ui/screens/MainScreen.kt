package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IslamicGreenGradient
import com.example.ui.theme.LiquidGlassBackgroundBrush
import com.example.ui.theme.LiquidGlassBorderBrush
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.PrayerViewModel

sealed class AppNavTab(val index: Int, val title: String, val icon: ImageVector, val tag: String) {
    object Prayers : AppNavTab(0, "Vakitler", Icons.Default.AccessTime, "nav_prayers")
    object Monthly : AppNavTab(1, "İmsakiye", Icons.Default.CalendarMonth, "nav_monthly")
    object Qibla : AppNavTab(2, "Kıble", Icons.Default.Explore, "nav_qibla")
    object Notifications : AppNavTab(3, "Bildirim", Icons.Default.Notifications, "nav_notifications")
    object Tasbih : AppNavTab(4, "Zikirmatik", Icons.Default.TouchApp, "nav_tasbih")
}

@Composable
fun MainScreen(
    viewModel: PrayerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabs = remember {
        listOf(
            AppNavTab.Prayers,
            AppNavTab.Monthly,
            AppNavTab.Qibla,
            AppNavTab.Notifications,
            AppNavTab.Tasbih
        )
    }

    // Root Container with continuous Islamic Emerald Gradient
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IslamicGreenGradient)
    ) {
        // Main Screen Content that extends all the way to the bottom edge
        Box(
            modifier = Modifier.fillMaxSize()
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

        // Floating Liquid Frosted Glass Navigation Bar (No black background, true floating glass)
        FloatingLiquidGlassNavBar(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it },
            tabs = tabs,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        )
    }
}

@Composable
fun FloatingLiquidGlassNavBar(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<AppNavTab>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color(0x99000000),
                ambientColor = Color(0x4010B981)
            )
            .border(
                width = 1.3.dp,
                brush = LiquidGlassBorderBrush,
                shape = RoundedCornerShape(32.dp)
            ),
        shape = RoundedCornerShape(32.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LiquidGlassBackgroundBrush)
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTabIndex == tab.index

                    val animatedIconColor by animateColorAsState(
                        targetValue = if (isSelected) GoldPrimary else TextSecondaryDark,
                        label = "tabIconColor"
                    )

                    val animatedTextColor by animateColorAsState(
                        targetValue = if (isSelected) GoldLight else TextSecondaryDark,
                        label = "tabTextColor"
                    )

                    val animatedPillColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0x38F1C40F) else Color.Transparent,
                        label = "tabPillColor"
                    )

                    val animatedPillBorder by animateColorAsState(
                        targetValue = if (isSelected) Color(0x66F1C40F) else Color.Transparent,
                        label = "tabPillBorder"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(animatedPillColor)
                            .border(
                                width = 1.dp,
                                color = animatedPillBorder,
                                shape = RoundedCornerShape(22.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = GoldPrimary)
                            ) {
                                onTabSelected(tab.index)
                            }
                            .padding(horizontal = 11.dp, vertical = 7.dp)
                            .testTag(tab.tag),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = animatedIconColor,
                                modifier = Modifier.size(21.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = animatedTextColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
