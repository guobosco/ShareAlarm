package com.tencent.tcb.cloudbase

import android.content.Context
import com.tencent.tcb.cloudbase.database.CloudBaseDatabase

class CloudBaseCore private constructor() {
    companion object {
        private var instance: CloudBaseCore? = null

        fun getInstance(): CloudBaseCore {
            if (instance == null) {
                instance = CloudBaseCore()
            }
            return instance!!
        }

        fun initialize(context: Context, config: CloudBaseInitConfig) {
            // Mock initialization
            println("Mock CloudBase SDK initialized with env: ${config.env}")
        }
    }

    private val databaseInstance by lazy { CloudBaseDatabase() }

    fun auth(): CloudBaseAuth {
        return CloudBaseAuth()
    }

    fun database(): CloudBaseDatabase {
        return databaseInstance
    }
}

class CloudBaseInitConfig private constructor(
    val env: String,
    val appSecret: String
) {
    class Builder {
        private var env: String = ""
        private var appSecret: String = ""

        fun setEnv(env: String): Builder {
            this.env = env
            return this
        }

        fun setAppSecret(appSecret: String): Builder {
            this.appSecret = appSecret
            return this
        }

        fun build(): CloudBaseInitConfig {
            return CloudBaseInitConfig(env, appSecret)
        }
    }
}
