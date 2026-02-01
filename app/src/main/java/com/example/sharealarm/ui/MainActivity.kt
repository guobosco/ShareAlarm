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
            // 使用应用主题
            @OptIn(ExperimentalMaterial3Api::class)
            ShareAlarmTheme {
                // 渲染应用导航主机
                AppNavHost()
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
