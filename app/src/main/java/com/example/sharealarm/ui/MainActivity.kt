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
        
        // 检查并请求必要的权限
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
    
    /**
     * 检查并请求必要的权限
     */
    private fun checkAndRequestPermissions() {
        Log.d(TAG, "Checking permissions")
        
        // 检查精确闹钟权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!AlarmScheduler.hasAllAlarmPermissions(this)) {
                Log.w(TAG, "No exact alarm permission")
                PermissionService.openExactAlarmPermissionSettings(this)
            } else {
                Log.d(TAG, "Exact alarm permission granted")
            }
        }
        
        // 检查通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "No notification permission")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_PERMISSIONS_CODE)
            } else {
                Log.d(TAG, "Notification permission granted")
            }
        }
        
        // 检查振动权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "No vibrate permission")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.VIBRATE), REQUEST_PERMISSIONS_CODE)
        } else {
            Log.d(TAG, "Vibrate permission granted")
        }
        
        Log.d(TAG, "Permission check completed")
    }
    
    /**
     * 权限请求结果回调
     * @param requestCode 请求码
     * @param permissions 权限数组
     * @param grantResults 授权结果数组
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission granted: ${permissions[i]}")
                } else {
                    Log.w(TAG, "Permission denied: ${permissions[i]}")
                    // 可以在这里添加权限被拒绝的处理逻辑
                }
            }
        }
    }
}
