package com.example.sharealarm.data.remote

import android.content.Context
import com.tencent.cloudbase.auth.Auth
import com.tencent.cloudbase.database.CloudbaseDatabase
import com.tencent.cloudbase.exception.CloudbaseException
import com.tencent.cloudbase.init.CloudbaseInitOptions
import com.tencent.cloudbase.init.CloudbaseManager

/**
 * Cloudbase 初始化器
 * 功能：初始化 Cloudbase SDK，提供获取 Auth 和 Database 实例的方法
 */
object CloudbaseInitializer {
    
    // Cloudbase 应用配置
    // TODO: 请替换为您的 Cloudbase 应用配置
    private const val CLOUDBASE_APP_ID = "your-cloudbase-app-id"
    private const val CLOUDBASE_ENV_ID = "your-cloudbase-env-id"
    
    /**
     * 初始化 Cloudbase SDK
     * @param context 上下文
     */
    fun initialize(context: Context) {
        val options = CloudbaseInitOptions.Builder()
            .setAppId(CLOUDBASE_APP_ID)
            .setEnvId(CLOUDBASE_ENV_ID)
            .build()
        
        CloudbaseManager.init(context, options)
    }
    
    /**
     * 获取 Auth 实例
     * @return Auth 实例
     */
    fun getAuth(): Auth {
        return Auth.getInstance()
    }
    
    /**
     * 获取 Database 实例
     * @return CloudbaseDatabase 实例
     */
    fun getDatabase(): CloudbaseDatabase {
        return CloudbaseDatabase.getInstance()
    }
}