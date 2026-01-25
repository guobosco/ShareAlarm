package com.example.sharealarm.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sharealarm.R
import java.io.IOException

/**
 * 闹钟服务
 * 功能：处理闹钟响铃逻辑，显示通知，播放声音等
 */
class AlarmService : Service() {
    // 日志标签
    private val TAG = "AlarmService"
    // 通知渠道ID
    private val CHANNEL_ID = "alarm_channel"
    // 通知ID
    private val NOTIFICATION_ID = 1
    // 媒体播放器
    private var mediaPlayer: MediaPlayer? = null
    // 振动器
    private var vibrator: Vibrator? = null
    // 提醒ID
    private var reminderId: String? = null
    
    /**
     * 服务创建时调用
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        // 创建通知渠道（Android 8.0+ 必需）
        createNotificationChannel()
        // 初始化振动器
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator?
    }
    
    /**
     * 当服务通过startService()启动时调用
     * @param intent 启动服务的意图
     * @param flags 启动标志
     * @param startId 启动ID
     * @return 服务的启动模式
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        
        // 检查是否是停止闹钟的请求
        if (intent?.action == "STOP_ALARM") {
            Log.d(TAG, "Stopping alarm")
            // 停止服务
            stopSelf()
            return START_NOT_STICKY
        }
        
        // 获取提醒ID
        reminderId = intent?.getStringExtra("reminderId")
        Log.d(TAG, "Received reminderId: $reminderId")
        
        // 立即显示通知，确保在5秒内调用startForeground
        showAlarmNotification()
        
        // 启动响铃逻辑
        startAlarmRinging()
        
        // 服务在被杀死后不要重新启动
        return START_NOT_STICKY
    }
    
    /**
     * 服务销毁时调用
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        // 停止响铃和振动
        stopAlarmRinging()
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
            Log.d(TAG, "Notification channel created")
        }
    }
    
    /**
     * 显示闹钟通知
     */
    private fun showAlarmNotification() {
        // 点击通知后要启动的活动
        val intent = Intent(this, com.example.sharealarm.ui.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminderId", reminderId)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        
        // 停止闹钟的意图
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = "STOP_ALARM"
        }
        val stopPendingIntent: PendingIntent = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        
        // 构建通知
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(getString(R.string.alarm_title))
            .setContentText(getString(R.string.alarm_ringing))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "关闭", stopPendingIntent)
            .setAutoCancel(true)
            .setFullScreenIntent(pendingIntent, true) // 全屏通知
            .build()
        
        // 显示通知
        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "Notification shown")
    }
    
    /**
     * 开始闹钟响铃
     */
    private fun startAlarmRinging() {
        Log.d(TAG, "Starting alarm ringing")
        
        // 播放声音
        try {
            mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_RINGTONE_URI)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
            Log.d(TAG, "Alarm sound started")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to play alarm sound: ${e.message}")
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid ringtone: ${e.message}")
        }
        
        // 振动
        try {
            val pattern = longArrayOf(0, 1000, 1000, 1000, 1000) // 振动模式：停止1秒，振动1秒，重复3次
            vibrator?.vibrate(pattern, 0) // 0表示无限重复
            Log.d(TAG, "Vibration started")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to start vibration: ${e.message}")
        }
    }
    
    /**
     * 停止闹钟响铃
     */
    private fun stopAlarmRinging() {
        Log.d(TAG, "Stopping alarm ringing")
        
        // 停止声音
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
            Log.d(TAG, "Alarm sound stopped")
        }
        
        // 停止振动
        vibrator?.cancel()
        Log.d(TAG, "Vibration stopped")
    }
}