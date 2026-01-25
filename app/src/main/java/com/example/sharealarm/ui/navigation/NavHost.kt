package com.example.sharealarm.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sharealarm.ui.screen.CreateOrganizationScreen
import com.example.sharealarm.ui.screen.CreateReminderScreen
import com.example.sharealarm.ui.screen.HomeScreen
import com.example.sharealarm.ui.screen.JoinOrganizationScreen
import com.example.sharealarm.ui.screen.ProfileScreen
import com.example.sharealarm.ui.screen.SignInScreen
import com.example.sharealarm.ui.screen.SignUpScreen

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
        startDestination = Screen.SignIn.route
    ) {
        // 登录屏幕路由
        composable(Screen.SignIn.route) {
            SignInScreen(navController = navController)
        }
        // 注册屏幕路由
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        // 主页屏幕路由
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        // 我的页面屏幕路由
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        // 创建组织屏幕路由
        composable(Screen.CreateOrganization.route) {
            CreateOrganizationScreen(navController = navController)
        }
        // 加入组织屏幕路由
        composable(Screen.JoinOrganization.route) {
            JoinOrganizationScreen(navController = navController)
        }
        // 创建提醒屏幕路由
        composable(Screen.CreateReminder.route) {
            CreateReminderScreen(navController = navController)
        }
    }
}

/**
 * 屏幕路由密封类
 * 功能：定义应用中所有可用的屏幕路由，便于统一管理和使用
 */
sealed class Screen(val route: String) {
    object SignIn : Screen("sign_in")
    object SignUp : Screen("sign_up")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object CreateOrganization : Screen("create_organization")
    object JoinOrganization : Screen("join_organization")
    object CreateReminder : Screen("create_reminder")
}