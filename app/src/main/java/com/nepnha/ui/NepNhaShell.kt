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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nepnha.AppContainer
import com.nepnha.ui.calendar.CalendarScreen
import com.nepnha.ui.family.ChooseWorshipperScreen
import com.nepnha.ui.family.FamilyScreen
import com.nepnha.ui.family.FamilyViewModel
import com.nepnha.ui.family.MemberEditorScreen
import com.nepnha.ui.family.MemberEditorViewModel
import com.nepnha.ui.home.HomeScreen
import com.nepnha.ui.home.HomeViewModel
import com.nepnha.ui.navigation.Routes
import com.nepnha.ui.navigation.TopLevelDestination
import com.nepnha.ui.settings.SettingsScreen

/**
 * Vỏ ứng dụng: thanh điều hướng dưới + NavHost.
 *
 * Nơi **duy nhất** biết cấu trúc điều hướng; màn hình con chỉ nhận state và callback
 * nên preview và test được độc lập.
 *
 * [container] được truyền vào thay vì lấy từ `Application`: nhờ vậy UI test dựng
 * được nguyên app trên Room in-memory mà không cần hook đặc biệt nào trong code
 * production.
 */
@Composable
fun NepNhaShell(
    container: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentDestination = backStackEntry?.destination

    // Màn hình con tự có TopAppBar và nút Back ⇒ ẩn thanh dưới để giữ người dùng
    // trong luồng thay vì mời họ nhảy tab giữa chừng.
    val showBottomBar = currentRoute == null || currentRoute in TopLevelDestination.entries.map { it.route }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(modifier = Modifier.testTag("bottom_bar")) {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy
                                ?.any { it.route == destination.route } == true,
                            onClick = { navController.navigateToTab(destination) },
                            icon = {
                                Icon(
                                    painter = painterResource(destination.iconRes),
                                    contentDescription = null,
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(destination.labelRes),
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
                val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(container))
                val state by vm.state.collectAsStateWithLifecycle()
                HomeScreen(
                    state = state,
                    onSetupFamily = { navController.navigateToTab(TopLevelDestination.FAMILY) },
                )
            }

            composable(TopLevelDestination.CALENDAR.route) { CalendarScreen() }

            composable(TopLevelDestination.FAMILY.route) { entry ->
                val vm: FamilyViewModel = entry.familyViewModel(container, navController)
                val state by vm.state.collectAsStateWithLifecycle()
                FamilyScreen(
                    state = state,
                    onAddMember = { navController.navigate(Routes.memberEditor(null)) },
                    onEditMember = { id -> navController.navigate(Routes.memberEditor(id)) },
                    onChooseWorshipper = { navController.navigate(Routes.CHOOSE_WORSHIPPER) },
                    onRenameFamily = vm::renameFamily,
                    onDeleteMember = vm::deleteMember,
                )
            }

            composable(TopLevelDestination.SETTINGS.route) { SettingsScreen() }

            composable(
                route = Routes.MEMBER_EDITOR,
                arguments = listOf(
                    navArgument(Routes.ARG_MEMBER_ID) {
                        type = NavType.LongType
                        defaultValue = Routes.NO_MEMBER_ID
                    },
                ),
            ) { entry ->
                val rawId = entry.arguments?.getLong(Routes.ARG_MEMBER_ID) ?: Routes.NO_MEMBER_ID
                val memberId = rawId.takeIf { it != Routes.NO_MEMBER_ID }
                val vm: MemberEditorViewModel = viewModel(
                    key = "member_editor_$rawId",
                    factory = MemberEditorViewModel.factory(container, memberId),
                )
                val state by vm.state.collectAsStateWithLifecycle()
                MemberEditorScreen(
                    state = state,
                    onInputChange = vm::updateInput,
                    onSave = { vm.save { navController.popBackStack() } },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.CHOOSE_WORSHIPPER) { entry ->
                val vm: FamilyViewModel = entry.familyViewModel(container, navController)
                val state by vm.state.collectAsStateWithLifecycle()
                ChooseWorshipperScreen(
                    members = state.members,
                    selectedId = state.primaryMember?.id,
                    // Ghi xong mới quay lại — xem chú thích ở FamilyViewModel.setPrimaryMember.
                    onSelect = { id -> vm.setPrimaryMember(id) { navController.popBackStack() } },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

/**
 * Màn Gia đình và màn Chọn tín chủ dùng chung một [FamilyViewModel], scope theo
 * điểm đến `family` — chọn xong tín chủ quay lại là danh sách đã đúng ngay, không
 * phải nạp lại.
 */
@Composable
private fun androidx.navigation.NavBackStackEntry.familyViewModel(
    container: AppContainer,
    navController: NavHostController,
): FamilyViewModel {
    val owner = remember(this) {
        runCatching { navController.getBackStackEntry(TopLevelDestination.FAMILY.route) }
            .getOrNull()
    } ?: this
    return viewModel(viewModelStoreOwner = owner, factory = FamilyViewModel.factory(container))
}

private fun NavHostController.navigateToTab(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
