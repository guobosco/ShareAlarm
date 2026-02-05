package com.example.sharealarm.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sharealarm.service.AlarmScheduler
import com.example.sharealarm.service.PermissionService
import com.example.sharealarm.ui.navigation.AppNavHost
import com.example.sharealarm.ui.theme.ShareAlarmTheme
import com.example.sharealarm.ui.theme.ThemeSettings
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

import androidx.compose.foundation.isSystemInDarkTheme

/**
 * 应用主活动
 * 功能：应用的入口点，设置Compose内容和主题
 */
class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private val REQUEST_PERMISSIONS_CODE = 100
    
    /**
     * 活动创建时调用
     * @param savedInstanceState 保存的实例状态
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 检查并请求权限
        checkAndRequestPermissions()
        
        // 设置Compose内容
        setContent {
            val themeSetting by ThemeSettings.isDarkTheme.collectAsState()
            val systemDark = isSystemInDarkTheme()
            
            // 如果设置为 null 则跟随系统，否则使用设置值
            val isDark = themeSetting ?: systemDark
            
            // 使用应用主题
            @OptIn(ExperimentalMaterial3Api::class)
            ShareAlarmTheme(darkTheme = isDark) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // 渲染应用导航主机
                    AppNavHost()
                    
                    // 全局权限监测
                    PermissionMonitor()
                }
            }
        }
    }
    
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, 
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 
                    REQUEST_PERMISSIONS_CODE
                )
            }
        }
        
        // 检查精确闹钟权限 (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(android.app.AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                // 引导用户去设置页面开启权限
                // 这里简单起见，可以直接跳转，或者弹出对话框提示
                // PermissionService.openExactAlarmPermissionSettings(this)
                Log.w(TAG, "Exact alarm permission not granted")
            }
        }
    }
}

@Composable
fun PermissionMonitor() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var missingPermissionType by remember { mutableStateOf("") } // "EXACT_ALARM", "NOTIFICATION", "OVERLAY"

    // 每分钟检查一次权限
    LaunchedEffect(Unit) {
        while (true) {
            val hasExactAlarm = PermissionService.hasExactAlarmPermission(context)
            val hasNotification = PermissionService.hasNotificationPermission(context)
            val hasOverlay = PermissionService.hasOverlayPermission(context)

            if (!hasExactAlarm) {
                missingPermissionType = "EXACT_ALARM"
                showDialog = true
            } else if (!hasNotification) {
                missingPermissionType = "NOTIFICATION"
                showDialog = true
            } else if (!hasOverlay) {
                missingPermissionType = "OVERLAY"
                showDialog = true
            } else {
                showDialog = false
            }
            
            delay(60 * 1000L) // 1分钟
        }
    }

    if (showDialog) {
        val title = "需要权限"
        val message = when (missingPermissionType) {
            "EXACT_ALARM" -> "为了保证闹钟准时响铃，请授予“闹钟和提醒”权限。"
            "NOTIFICATION" -> "为了接收闹钟通知，请授予“通知”权限。"
            "OVERLAY" -> "为了在锁屏或后台显示闹钟界面，请授予“显示在其他应用上层”权限。"
            else -> "需要必要的权限以保证功能正常。"
        }
        
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        when (missingPermissionType) {
                            "EXACT_ALARM" -> PermissionService.openExactAlarmPermissionSettings(context)
                            "NOTIFICATION" -> PermissionService.openNotificationPermissionSettings(context)
                            "OVERLAY" -> PermissionService.openOverlayPermissionSettings(context)
                        }
                    }
                ) {
                    Text("去设置")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
