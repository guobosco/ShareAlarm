package com.example.sharealarm.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sharealarm.ui.screen.CreateReminderScreen
import com.example.sharealarm.ui.screen.ContactsScreen
import com.example.sharealarm.ui.screen.HomeScreen
import com.example.sharealarm.ui.screen.ReminderDetailScreen
import com.example.sharealarm.ui.screen.PartnerProfileScreen
import com.example.sharealarm.ui.screen.UserInfoScreen
import com.example.sharealarm.ui.screen.SearchScreen

import com.example.sharealarm.ui.screen.MyProfileEditScreen
import com.example.sharealarm.ui.screen.MyQrCodeScreen
import com.example.sharealarm.ui.screen.PartnerEditScreen
import com.example.sharealarm.ui.screen.TagManageScreen

/**
 * 应用导航主机
 * 功能：配置应用的所有导航路由，定义不同屏幕之间的导航关系
 */
@ExperimentalMaterial3Api
@Composable
fun AppNavHost() {
    // 创建导航控制器
    val navController = rememberNavController()
    
    // 导航主机，定义所有路由和对应的屏幕
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route // Mock模式直接进入主页
    ) {
        // ... existing routes ...
        // 主页屏幕路由
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        // 创建提醒屏幕路由
        composable(
            route = Screen.CreateReminder.route,
            arguments = listOf(androidx.navigation.navArgument("reminderId") {
                type = androidx.navigation.NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getString("reminderId")
            CreateReminderScreen(navController = navController, reminderId = reminderId)
        }
        // 联系人列表（我的伙伴）屏幕路由
        composable(Screen.Contacts.route) {
            ContactsScreen(navController = navController)
        }
        // 提醒详情屏幕路由
        composable(
            route = Screen.ReminderDetail.route,
            arguments = listOf(androidx.navigation.navArgument("reminderId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getString("reminderId")
            ReminderDetailScreen(navController = navController, reminderId = reminderId)
        }
        // 伙伴资料屏幕路由
        composable(
            route = Screen.PartnerProfile.route,
            arguments = listOf(androidx.navigation.navArgument("userId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            PartnerProfileScreen(navController = navController, userId = userId)
        }
        composable(Screen.UserInfo.route) {
            UserInfoScreen(navController = navController)
        }
        // 搜索屏幕路由
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        
        // 新增页面路由
        composable(Screen.MyProfileEdit.route) {
            MyProfileEditScreen(navController = navController)
        }
        composable(Screen.MyQrCode.route) {
            MyQrCodeScreen(navController = navController)
        }
        composable(Screen.TagManage.route) {
            TagManageScreen(navController = navController)
        }
        composable(
            route = Screen.PartnerEdit.route,
            arguments = listOf(androidx.navigation.navArgument("userId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            PartnerEditScreen(navController = navController, userId = userId)
        }
    }
}

/**
 * 屏幕路由密封类
 * 功能：定义应用中所有可用的屏幕路由，便于统一管理和使用
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreateReminder : Screen("create_reminder?reminderId={reminderId}") {
        fun createRoute(reminderId: String? = null): String {
            return if (reminderId != null) "create_reminder?reminderId=$reminderId" else "create_reminder"
        }
    }
    object Contacts : Screen("contacts")
    object ReminderDetail : Screen("reminder_detail/{reminderId}") {
        fun createRoute(reminderId: String) = "reminder_detail/$reminderId"
    }
    object PartnerProfile : Screen("partner_profile/{userId}") {
        fun createRoute(userId: String) = "partner_profile/$userId"
    }
    object UserInfo : Screen("user_info")
    object Search : Screen("search")
    
    // 新增页面
    object MyProfileEdit : Screen("my_profile_edit")
    object MyQrCode : Screen("my_qr_code")
    object TagManage : Screen("tag_manage")
    object PartnerEdit : Screen("partner_edit/{userId}") {
        fun createRoute(userId: String) = "partner_edit/$userId"
    }
}