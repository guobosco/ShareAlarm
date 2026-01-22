package com.example.sharealarm.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharealarm.data.model.User
import com.example.sharealarm.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 认证视图模型
 * 功能：管理认证相关的UI状态，处理用户的注册、登录、登出等操作
 * @property authRepository 认证仓库
 */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    /**
     * 认证状态密封类
     * 用于表示不同的认证状态
     */
    sealed class AuthState {
        /**
         * 空闲状态，初始状态
         */
        object Idle : AuthState()
        /**
         * 加载状态，正在执行认证操作
         */
        object Loading : AuthState()
        /**
         * 成功状态，认证操作成功
         * @param user 登录成功的用户对象
         */
        data class Success(val user: User) : AuthState()
        /**
         * 错误状态，认证操作失败
         * @param message 错误信息
         */
        data class Error(val message: String) : AuthState()
    }
    
    /**
     * 私有可变认证状态流，用于内部更新状态
     */
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    /**
     * 公开的不可变认证状态流，供UI层观察
     */
    val authState: StateFlow<AuthState> = _authState
    
    /**
     * 用户注册
     * @param email 用户邮箱
     * @param password 用户密码
     * @param name 用户姓名
     */
    fun signUp(email: String, password: String, name: String) {
        // 更新状态为加载中
        _authState.value = AuthState.Loading
        // 在ViewModel作用域中执行协程
        viewModelScope.launch {
            try {
                // 调用仓库层的注册方法
                val user = authRepository.signUp(email, password, name)
                // 保存用户信息到数据库
                authRepository.saveUserToDatabase(user)
                // 更新状态为成功
                _authState.value = AuthState.Success(user)
            } catch (e: Exception) {
                // 更新状态为错误
                _authState.value = AuthState.Error(e.message ?: "注册失败")
            }
        }
    }
    
    /**
     * 用户登录
     * @param email 用户邮箱
     * @param password 用户密码
     */
    fun signIn(email: String, password: String) {
        // 更新状态为加载中
        _authState.value = AuthState.Loading
        // 在ViewModel作用域中执行协程
        viewModelScope.launch {
            try {
                // 调用仓库层的登录方法
                val user = authRepository.signIn(email, password)
                // 更新状态为成功
                _authState.value = AuthState.Success(user)
            } catch (e: Exception) {
                // 更新状态为错误
                _authState.value = AuthState.Error(e.message ?: "登录失败")
            }
        }
    }
    
    /**
     * 用户登出
     */
    fun signOut() {
        // 调用仓库层的登出方法
        authRepository.signOut()
        // 更新状态为空闲
        _authState.value = AuthState.Idle
    }
    
    /**
     * 重置密码
     * @param email 用户邮箱
     */
    fun resetPassword(email: String) {
        // 更新状态为加载中
        _authState.value = AuthState.Loading
        // 在ViewModel作用域中执行协程
        viewModelScope.launch {
            try {
                // 调用仓库层的重置密码方法
                authRepository.resetPassword(email)
                // 更新状态为成功，显示密码重置邮件已发送
                _authState.value = AuthState.Error("密码重置邮件已发送")
            } catch (e: Exception) {
                // 更新状态为错误
                _authState.value = AuthState.Error(e.message ?: "重置密码失败")
            }
        }
    }
}
