package com.example.cloudbase

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class CloudBaseClient(
    private val envId: String,
    private var accessToken: String
) {
    private val baseUrl = "https://$envId.api.tcloudbasegateway.com"
    private val gson = Gson()
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 更新访问令牌
     * 
     * @param newToken 新的访问令牌
     */
    fun updateAccessToken(newToken: String) {
        this.accessToken = newToken
        println("访问令牌已更新")
    }

    /**
     * 统一的HTTP请求方法
     * 
     * @param method 请求方法 (GET, POST, PUT, PATCH, DELETE)
     * @param path API路径 (如 /v1/rdb/rest/table_name)
     * @param body 请求体数据
     * @param customHeaders 自定义headers
     * 
     * @return 响应数据或null
     */
    suspend fun <T> request(
        method: String,
        path: String,
        body: Any? = null,
        customHeaders: Map<String, String> = emptyMap(),
        typeToken: TypeToken<T>? = null
    ): T? = withContext(Dispatchers.IO) {
        val url = "$baseUrl$path"
        
        val requestBuilder = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $accessToken")
        
        // 添加自定义headers
        customHeaders.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }
        
        // 设置请求方法和body
        when (method.uppercase()) {
            "GET" -> requestBuilder.get()
            "POST", "PUT", "PATCH", "DELETE" -> {
                val jsonBody = if (body != null) {
                    gson.toJson(body).toRequestBody("application/json".toMediaType())
                } else {
                    "{}".toRequestBody("application/json".toMediaType())
                }
                when (method.uppercase()) {
                    "POST" -> requestBuilder.post(jsonBody)
                    "PUT" -> requestBuilder.put(jsonBody)
                    "PATCH" -> requestBuilder.patch(jsonBody)
                    "DELETE" -> requestBuilder.delete(jsonBody)
                }
            }
        }
        
        try {
            val response = client.newCall(requestBuilder.build()).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                
                // 如果响应为空，返回true表示成功
                if (responseBody.isNullOrEmpty()) {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext true as? T
                }
                
                return@withContext if (typeToken != null) {
                    gson.fromJson(responseBody, typeToken.type)
                } else {
                    @Suppress("UNCHECKED_CAST")
                    gson.fromJson(responseBody, Any::class.java) as? T
                }
            } else {
                println("请求失败: ${response.code} ${response.body?.string()}")
                return@withContext null
            }
        } catch (e: Exception) {
            println("请求失败: ${e.message}")
            e.printStackTrace()
            return@withContext null
        }
    }
}

// 配置文件或初始化时创建实例
// val cloudbase = CloudBaseClient(
//     envId = "your-env-id",
//     accessToken = "your-access-token"
// )
