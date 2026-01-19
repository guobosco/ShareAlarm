package com.example.sharealarm.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.sharealarm.R

/**
 * 闹钟服务
 * 功能：处理闹钟响铃逻辑，显示通知，播放声音等
 */
class AlarmService : Service() {
    // 通知渠道ID
    private val CHANNEL_ID = "alarm_channel"
    // 通知ID
    private val NOTIFICATION_ID = 1
    
    /**
     * 服务创建时调用
     */
    override fun onCreate() {
        super.onCreate()
        // 创建通知渠道（Android 8.0+ 必需）
        createNotificationChannel()
    }
    
    /**
     * 当服务通过startService()启动时调用
     * @param intent 启动服务的意图
     * @param flags 启动标志
     * @param startId 启动ID
     * @return 服务的启动模式
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 显示闹钟通知
        showAlarmNotification()
        
        // TODO: 这里可以添加闹钟响铃逻辑，比如播放声音、振动等
        
        // 服务在被杀死后不要重新启动
        return START_NOT_STICKY
    }
    
    /**
     * 绑定服务时调用
     * @param intent 绑定意图
     * @return 服务的IBinder接口，这里返回null表示不支持绑定
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.alarm_channel_name)
            val descriptionText = getString(R.string.alarm_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // 注册通知渠道到系统
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 显示闹钟通知
     */
    private fun showAlarmNotification() {
        // 点击通知后要启动的活动
        val intent = Intent(this, ui.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        
        // 构建通知
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.alarm_title))
            .setContentText(getString(R.string.alarm_ringing))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // 显示通知
        startForeground(NOTIFICATION_ID, notification)
    }
}