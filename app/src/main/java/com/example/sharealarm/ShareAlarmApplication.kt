package com.example.sharealarm

import android.app.Application
import com.example.sharealarm.data.remote.CloudbaseInitializer

/**
 * 应用程序类
 * 功能：应用的入口点，初始化应用级别的资源和配置
 */
class ShareAlarmApplication : Application() {
    /**
     * 当应用创建时调用
     */
    override fun onCreate() {
        super.onCreate()
        // 初始化 Cloudbase SDK
        CloudbaseInitializer.initialize(this)
    }
}