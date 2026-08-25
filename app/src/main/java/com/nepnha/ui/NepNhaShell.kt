package com.nepnha.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nepnha.ui.calendar.CalendarScreen
import com.nepnha.ui.family.AddMemberScreen
import com.nepnha.ui.family.FamilyScreen
import com.nepnha.ui.home.HomeScreen
import com.nepnha.ui.navigation.Routes
import com.nepnha.ui.navigation.TopLevelDestination
import com.nepnha.ui.settings.SettingsScreen

/**
 * Vỏ ứng dụng: thanh điều hướng dưới + NavHost.
 *
 * Đây là nơi **duy nhất** biết về cấu trúc điều hướng; màn hình con chỉ nhận callback
 * (`onAddMember`, `onBack`) chứ không cầm `NavController`. Nhờ vậy mỗi màn hình
 * preview và test được độc lập.
 */
@Composable
fun NepNhaShell(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Màn hình con (Thêm thành viên) tự có TopAppBar và nút Back nên ẩn thanh dưới:
    // giữ người dùng ở trong luồng thay vì mời họ nhảy tab giữa chừng.
    val showBottomBar = currentDestination?.route != Routes.ADD_MEMBER

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(modifier = Modifier.testTag("bottom_bar")) {
                    TopLevelDestination.entries.forEach { destination ->
                        val label = stringResource(destination.labelRes)
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateToTab(destination) },
                            icon = {
                                Icon(
                                    painter = painterResource(destination.iconRes),
                                    contentDescription = null,
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            modifier = Modifier.testTag("tab_${destination.route}"),
                        )
                    }
                }
            }
        },
    ) { insets ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.HOME.route,
            modifier = Modifier.padding(insets),
        ) {
            composable(TopLevelDestination.HOME.route) {
                HomeScreen(
                    onSetupFamily = { navController.navigateToTab(TopLevelDestination.FAMILY) },
                )
            }
            composable(TopLevelDestination.CALENDAR.route) {
                CalendarScreen()
            }
            composable(TopLevelDestination.FAMILY.route) {
                FamilyScreen(onAddMember = { navController.navigate(Routes.ADD_MEMBER) })
            }
            composable(TopLevelDestination.SETTINGS.route) {
                SettingsScreen()
            }
            composable(Routes.ADD_MEMBER) {
                AddMemberScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

/**
 * Chuyển tab theo hành vi chuẩn của bottom navigation: không chồng chất back stack,
 * và giữ lại vị trí cuộn của tab cũ khi quay lại.
 */
private fun NavHostController.navigateToTab(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
