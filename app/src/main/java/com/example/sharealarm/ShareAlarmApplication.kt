package com.example.sharealarm

import android.app.Application
import com.tencent.tcb.cloudbase.CloudBaseCore
import com.tencent.tcb.cloudbase.CloudBaseInitConfig

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
        // 初始化 CloudBase SDK
        try {
            val config = CloudBaseInitConfig.Builder()
                .setEnv("sharealarm-6gt6msx9794135c5") // CloudBase 环境 ID
                .setAppSecret("YOUR_APP_SECRET") // 替换为你的 CloudBase 应用密钥
                .build()
            CloudBaseCore.initialize(this, config)
            println("CloudBase SDK 初始化成功")
        } catch (e: Exception) {
            println("CloudBase SDK 初始化失败: ${e.message}")
            // 即使 CloudBase 初始化失败，应用也能继续运行
        }
    }
}