package com.example.sharealarm.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import com.example.sharealarm.data.local.MockDataStore

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
        
        if (reminderId == null) return

        // 关键修复：检查该提醒是否在当前数据源中有效
        // 防止应用重启后，旧的系统残留闹钟（僵尸闹钟）意外触发
        // 因为 Mock 模式下每次启动 ID 都会变，旧 ID 肯定找不到，从而完美过滤掉残留闹钟
        // 注意：在实际测试中，如果应用被杀死，MockDataStore 会重置，导致刚设置的闹钟无法响铃
        // 因此暂时注释掉此检查，确保测试时能正常响铃
        /*
        if (MockDataStore.getReminderById(reminderId) == null) {
            Log.w(TAG, "Reminder $reminderId not found in current session. Ignoring zombie alarm.")
            return
        }
        */
        
        // 启动闹钟服务，显示通知并传递提醒ID
        val serviceIntent = Intent(context, AlarmService::class.java)
        serviceIntent.putExtra("reminderId", reminderId)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}