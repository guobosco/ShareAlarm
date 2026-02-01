package com.example.sharealarm

import android.app.Application

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
        // Mock 模式下无需初始化后端 SDK
    }
}