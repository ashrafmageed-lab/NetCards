package com.example.cardsmanager.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

enum class AppDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME(
        route = "home",
        title = Strings.NAV_HOME,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_tab_home"
    ),
    CATEGORIES(
        route = "categories",
        title = Strings.NAV_CATEGORIES,
        selectedIcon = Icons.Filled.Category,
        unselectedIcon = Icons.Outlined.Category,
        testTag = "nav_tab_categories"
    ),
    HISTORY(
        route = "history",
        title = Strings.NAV_HISTORY,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
        testTag = "nav_tab_history"
    ),
    SETTINGS(
        route = "settings",
        title = Strings.NAV_SETTINGS,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_tab_settings"
    )
}

@Composable
fun CardsApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.HOME) }

    val homeViewModel: HomeViewModel = hiltViewModel()
    val categoriesViewModel: CategoriesViewModel = hiltViewModel()
    val historyViewModel: HistoryViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val template by settingsViewModel.messageTemplate.collectAsState()
    val pinLockEnabled by settingsViewModel.pinLockEnabled.collectAsState()
    var isAppUnlocked by rememberSaveable(pinLockEnabled) { mutableStateOf(!pinLockEnabled) }

    if (pinLockEnabled && !isAppUnlocked) {
        PinLockScreen(
            mode = PinMode.UNLOCK,
            verifyPinAgainst = { settingsViewModel.verifyPin(it) },
            onPinSuccess = { isAppUnlocked = true }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AppDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentDestination) {
            AppDestination.HOME -> {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToCategories = {
                        currentDestination = AppDestination.CATEGORIES
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.CATEGORIES -> {
                CategoriesScreen(
                    viewModel = categoriesViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.HISTORY -> {
                HistoryScreen(
                    viewModel = historyViewModel,
                    template = template,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.SETTINGS -> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
