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
    
    // 认证状态密封类
    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val user: User) : AuthState()
        data class Error(val message: String) : AuthState()
    }
    
    // 认证状态流
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    
    /**
     * 用户注册
     * @param email 用户邮箱
     * @param password 用户密码
     * @param name 用户姓名
     */
    fun signUp(email: String, password: String, name: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.signUp(email, password, name)
            _authState.value = when {
                result.isSuccess -> {
                    val user = result.getOrNull()
                    if (user != null) {
                        // 创建User对象并保存到Firestore
                        val userObj = User(
                            id = user.uid,
                            name = user.displayName ?: "",
                            email = user.email ?: "",
                            photoUrl = user.photoUrl?.toString() ?: ""
                        )
                        authRepository.saveUserToFirestore(userObj)
                        AuthState.Success(userObj)
                    } else {
                        AuthState.Error("Failed to create user")
                    }
                }
                result.isFailure -> AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to sign up")
                else -> AuthState.Idle
            }
        }
    }
    
    /**
     * 用户登录
     * @param email 用户邮箱
     * @param password 用户密码
     */
    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.signIn(email, password)
            _authState.value = when {
                result.isSuccess -> {
                    val user = result.getOrNull()
                    if (user != null) {
                        // 从Firestore获取用户详细信息
                        val userResult = authRepository.getUserFromFirestore(user.uid)
                        if (userResult.isSuccess) {
                            AuthState.Success(userResult.getOrThrow())
                        } else {
                            // 如果Firestore中没有用户信息，创建一个
                            val userObj = User(
                                id = user.uid,
                                name = user.displayName ?: "",
                                email = user.email ?: "",
                                photoUrl = user.photoUrl?.toString() ?: ""
                            )
                            authRepository.saveUserToFirestore(userObj)
                            AuthState.Success(userObj)
                        }
                    } else {
                        AuthState.Error("Failed to login")
                    }
                }
                result.isFailure -> AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to sign in")
                else -> AuthState.Idle
            }
        }
    }
    
    /**
     * 用户登出
     */
    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Idle
    }
    
    /**
     * 重置密码
     * @param email 用户邮箱
     */
    fun resetPassword(email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.resetPassword(email)
            _authState.value = when {
                result.isSuccess -> AuthState.Error("Password reset email sent")
                result.isFailure -> AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to reset password")
                else -> AuthState.Idle
            }
        }
    }
}
