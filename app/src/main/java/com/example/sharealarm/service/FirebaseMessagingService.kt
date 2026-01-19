package com.example.sharealarm.service

import android.app.Service
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase 消息服务
 * 功能：处理 Firebase Cloud Messaging (FCM) 推送通知，包括设备令牌的获取和推送消息的接收
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // 日志标签
    private val TAG = "MyFirebaseMessagingService"

    /**
     * 当设备令牌更新时调用
     * @param token 更新后的设备令牌
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "新的设备令牌: $token")
        // 将令牌发送到服务器
        sendRegistrationToServer(token)
    }

    /**
     * 接收推送消息时调用
     * @param remoteMessage 推送消息对象
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "推送消息来源: ${remoteMessage.from}")

        // 处理通知消息
        remoteMessage.notification?.let {
            Log.d(TAG, "通知标题: ${it.title}")
            Log.d(TAG, "通知内容: ${it.body}")
            
            // 使用NotificationService显示通知
            it.title?.let {title ->
                it.body?.let {body ->
                    NotificationService.showNotification(this, title, body)
                }
            }
        }

        // 处理数据消息
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "数据消息内容: ${remoteMessage.data}")
        }
    }

    /**
     * 将设备令牌发送到服务器
     * @param token 设备令牌
     */
    private fun sendRegistrationToServer(token: String) {
        // TODO: 实现将令牌发送到服务器的逻辑
        // 例如：调用API将令牌保存到Firestore数据库
        Log.d(TAG, "将设备令牌发送到服务器: $token")
    }
}
