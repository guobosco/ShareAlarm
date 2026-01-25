package com.example.sharealarm.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 闹钟接收器
 * 功能：接收系统闹钟广播，触发提醒通知和闹钟服务
 */
class AlarmReceiver : BroadcastReceiver() {
    // 日志标签
    private val TAG = "AlarmReceiver"
    
    /**
     * 当接收到广播时调用
     * @param context 上下文
     * @param intent 接收到的意图
     */
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received")
        
        // 获取提醒ID
        val reminderId = intent.getStringExtra("reminderId")
        Log.d(TAG, "Received reminderId: $reminderId")
        
        // 启动闹钟服务，显示通知并传递提醒ID
        val serviceIntent = Intent(context, AlarmService::class.java)
        serviceIntent.putExtra("reminderId", reminderId)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        
        // 这里可以添加其他处理逻辑，比如播放声音、振动等
    }
}