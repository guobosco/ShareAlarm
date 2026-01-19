package com.example.sharealarm.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.sharealarm.ui.navigation.AppNavHost
import com.example.sharealarm.ui.theme.ShareAlarmTheme

/**
 * 应用主活动
 * 功能：应用的入口点，设置Compose内容和主题
 */
class MainActivity : ComponentActivity() {
    /**
     * 活动创建时调用
     * @param savedInstanceState 保存的实例状态
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}
