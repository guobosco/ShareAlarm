package com.example.sharealarm.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Date
import java.util.Calendar

/**
 * 闹钟调度器
 * 功能：封装系统闹钟的设置和取消逻辑
 */
object AlarmScheduler {
    private const val TAG = "AlarmScheduler"
    
    /**
     * 设置系统闹钟
     * @param context 上下文
     * @param reminderId 提醒ID
     * @param alertTime 提醒时间
     * @return 是否成功设置闹钟
     */
    fun scheduleAlarm(context: Context, reminderId: String, alertTime: Date): Boolean {
        try {
            // 验证提醒时间
            if (!isValidAlertTime(alertTime)) {
                Log.e(TAG, "Invalid alert time: $alertTime")
                return false
            }
            
            Log.d(TAG, "Scheduling alarm for reminder $reminderId at $alertTime")
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra("reminderId", reminderId)
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
            
            // 检查是否有设置精确闹钟的权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "No permission to schedule exact alarms. Please enable SCHEDULE_EXACT_ALARM permission.")
                    // 打开权限设置页面
                    PermissionService.openExactAlarmPermissionSettings(context)
                    return false
                } else {
                    Log.d(TAG, "Exact alarm permission granted")
                }
            }
            
            // 设置精确闹钟
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alertTime.time,
                    pendingIntent
                )
                Log.d(TAG, "Using setExactAndAllowWhileIdle for API 23+")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alertTime.time,
                    pendingIntent
                )
                Log.d(TAG, "Using setExact for API < 23")
            }
            
            Log.d(TAG, "Alarm scheduled successfully for reminder $reminderId at ${alertTime}")
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when scheduling alarm: ${e.message}")
            // 权限问题
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PermissionService.openExactAlarmPermissionSettings(context)
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * 取消系统闹钟
     * @param context 上下文
     * @param reminderId 提醒ID
     * @return 是否成功取消闹钟
     */
    fun cancelAlarm(context: Context, reminderId: String): Boolean {
        try {
            Log.d(TAG, "Cancelling alarm for reminder $reminderId")
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
            
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            
            Log.d(TAG, "Alarm cancelled successfully for reminder $reminderId")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel alarm: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * 为多个提醒时间设置闹钟
     * @param context 上下文
     * @param reminderId 提醒ID
     * @param alertTimes 提醒时间列表
     * @return 成功设置的闹钟数量
     */
    fun scheduleMultipleAlarms(context: Context, reminderId: String, alertTimes: List<Date>): Int {
        Log.d(TAG, "Scheduling multiple alarms for reminder $reminderId: ${alertTimes.size} times")
        
        var successCount = 0
        alertTimes.forEachIndexed { index, alertTime ->
            // 为每个提醒时间创建唯一的ID
            val uniqueId = "$reminderId-$index"
            if (scheduleAlarm(context, uniqueId, alertTime)) {
                successCount++
            }
        }
        
        Log.d(TAG, "Scheduled $successCount out of ${alertTimes.size} alarms for reminder $reminderId")
        return successCount
    }
    
    /**
     * 取消多个提醒时间的闹钟
     * @param context 上下文
     * @param reminderId 提醒ID
     * @param alertCount 提醒时间数量
     * @return 成功取消的闹钟数量
     */
    fun cancelMultipleAlarms(context: Context, reminderId: String, alertCount: Int): Int {
        Log.d(TAG, "Cancelling multiple alarms for reminder $reminderId: $alertCount times")
        
        var successCount = 0
        for (index in 0 until alertCount) {
            val uniqueId = "$reminderId-$index"
            if (cancelAlarm(context, uniqueId)) {
                successCount++
            }
        }
        
        Log.d(TAG, "Cancelled $successCount out of $alertCount alarms for reminder $reminderId")
        return successCount
    }
    
    /**
     * 验证提醒时间是否有效
     * @param alertTime 提醒时间
     * @return 是否有效
     */
    private fun isValidAlertTime(alertTime: Date): Boolean {
        val now = Date()
        val calendar = Calendar.getInstance()
        calendar.time = now
        calendar.add(Calendar.YEAR, 1) // 最多允许设置一年后的提醒
        val maxTime = calendar.time
        
        return alertTime.after(now) && alertTime.before(maxTime)
    }
    
    /**
     * 检查是否有设置闹钟的权限
     * @param context 上下文
     * @return 是否有所有必要的权限
     */
    fun hasAllAlarmPermissions(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms()
        }
        
        return true
    }
}
