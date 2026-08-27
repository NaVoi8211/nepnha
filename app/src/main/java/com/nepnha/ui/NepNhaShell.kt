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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
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
import com.nepnha.ui.calendar.CalendarViewModel
import com.nepnha.ui.family.ChooseWorshipperScreen
import com.nepnha.ui.family.FamilyScreen
import com.nepnha.ui.family.FamilyViewModel
import com.nepnha.ui.family.MemberEditorScreen
import com.nepnha.ui.family.MemberEditorViewModel
import com.nepnha.ui.home.HomeScreen
import com.nepnha.ui.home.HomeViewModel
import com.nepnha.ui.memorial.MemorialEditorScreen
import com.nepnha.ui.memorial.MemorialEditorViewModel
import com.nepnha.ui.memorial.MemorialListScreen
import com.nepnha.ui.memorial.MemorialListViewModel
import com.nepnha.ui.navigation.Routes
import com.nepnha.ui.navigation.TopLevelDestination
import com.nepnha.ui.settings.SettingsScreen
import com.nepnha.ui.settings.SettingsViewModel

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
                // Đọc lại ngày mỗi khi màn hình quay lại tiền cảnh. KHÔNG bộ đếm,
                // KHÔNG AlarmManager, KHÔNG việc chạy nền — chỉ một lần đọc khi người
                // dùng thực sự nhìn vào màn hình.
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refreshToday() }
                HomeScreen(
                    state = state,
                    onSetupFamily = { navController.navigateToTab(TopLevelDestination.FAMILY) },
                    onOpenMemorials = { navController.navigate(Routes.MEMORIALS) },
                )
            }

            composable(TopLevelDestination.CALENDAR.route) {
                val vm: CalendarViewModel = viewModel(factory = CalendarViewModel.factory(container))
                val state by vm.state.collectAsStateWithLifecycle()
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    vm.refreshToday(container.dateProvider.today())
                }
                CalendarScreen(
                    state = state,
                    onPreviousMonth = vm::showPreviousMonth,
                    onNextMonth = vm::showNextMonth,
                    onSelectDay = vm::select,
                )
            }

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

            composable(TopLevelDestination.SETTINGS.route) {
                val ctx = androidx.compose.ui.platform.LocalContext.current
                // buildConfig bị tắt để build nhanh ⇒ lấy version từ PackageManager.
                val version = remember(ctx) {
                    runCatching {
                        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
                    }.getOrNull() ?: "—"
                }
                val vm: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.factory(container, ctx.contentResolver, version),
                )
                val state by vm.state.collectAsStateWithLifecycle()
                SettingsScreen(
                    state = state,
                    versionName = version,
                    suggestedFileName = vm.suggestedFileName(),
                    onExportTo = vm::export,
                    onImportFrom = vm::prepareImport,
                    onConfirmImport = vm::confirmImport,
                    onCancelImport = vm::cancelImport,
                    onDismissMessage = vm::dismissMessage,
                )
            }

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

            composable(Routes.MEMORIALS) {
                val vm: MemorialListViewModel =
                    viewModel(factory = MemorialListViewModel.factory(container))
                val state by vm.state.collectAsStateWithLifecycle()
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refreshToday() }
                MemorialListScreen(
                    state = state,
                    onAdd = { navController.navigate(Routes.memorialEditor(null)) },
                    onEdit = { id -> navController.navigate(Routes.memorialEditor(id)) },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.MEMORIAL_EDITOR,
                arguments = listOf(
                    navArgument(Routes.ARG_MEMORIAL_ID) {
                        type = NavType.LongType
                        defaultValue = Routes.NO_MEMORIAL_ID
                    },
                ),
            ) { entry ->
                val rawId = entry.arguments?.getLong(Routes.ARG_MEMORIAL_ID) ?: Routes.NO_MEMORIAL_ID
                val memorialId = rawId.takeIf { it != Routes.NO_MEMORIAL_ID }
                val vm: MemorialEditorViewModel = viewModel(
                    key = "memorial_editor_$rawId",
                    factory = MemorialEditorViewModel.factory(container, memorialId),
                )
                val state by vm.state.collectAsStateWithLifecycle()
                MemorialEditorScreen(
                    state = state,
                    onInputChange = vm::updateInput,
                    onSelectMember = vm::selectMember,
                    onSave = { vm.save { navController.popBackStack() } },
                    onDelete = { vm.delete { navController.popBackStack() } },
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
