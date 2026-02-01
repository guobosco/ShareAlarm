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
        // 主页屏幕路由
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        // 创建提醒屏幕路由
        composable(Screen.CreateReminder.route) {
            CreateReminderScreen(navController = navController)
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
    }
}

/**
 * 屏幕路由密封类
 * 功能：定义应用中所有可用的屏幕路由，便于统一管理和使用
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreateReminder : Screen("create_reminder")
    object Contacts : Screen("contacts")
    object ReminderDetail : Screen("reminder_detail/{reminderId}") {
        fun createRoute(reminderId: String) = "reminder_detail/$reminderId"
    }
}