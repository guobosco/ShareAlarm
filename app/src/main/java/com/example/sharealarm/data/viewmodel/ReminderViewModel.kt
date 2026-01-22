package com.example.sharealarm.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharealarm.data.model.Reminder
import com.example.sharealarm.data.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 提醒视图模型
 * 功能：管理提醒相关的UI状态，处理提醒的创建、获取、更新、删除等操作
 * @property reminderRepository 提醒仓库，用于调用提醒相关的数据操作
 */
class ReminderViewModel(private val reminderRepository: ReminderRepository) : ViewModel() {
    
    /**
     * 提醒状态密封类
     * 用于表示不同的提醒操作状态
     */
    sealed class ReminderState {
        /**
         * 空闲状态，初始状态
         */
        object Idle : ReminderState()
        /**
         * 加载状态，正在执行提醒操作
         */
        object Loading : ReminderState()
        /**
         * 成功状态，提醒操作成功
         * @param message 成功消息
         */
        data class Success(val message: String) : ReminderState()
        /**
         * 错误状态，提醒操作失败
         * @param message 错误消息
         */
        data class Error(val message: String) : ReminderState()
    }
    
    /**
     * 私有可变提醒状态流，用于内部更新状态
     */
    private val _reminderState = MutableStateFlow<ReminderState>(ReminderState.Idle)
    /**
     * 公开的不可变提醒状态流，供UI层观察
     */
    val reminderState: StateFlow<ReminderState> = _reminderState
    
    /**
     * 私有可变提醒列表流，用于内部更新提醒列表
     */
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    /**
     * 公开的不可变提醒列表流，供UI层观察
     */
    val reminders: StateFlow<List<Reminder>> = _reminders
    
    /**
     * 创建提醒
     * @param reminder 提醒对象
     */
    fun createReminder(reminder: Reminder) {
        // 更新状态为加载中
        _reminderState.value = ReminderState.Loading
        // 在ViewModel作用域中执行协程
        viewModelScope.launch {
            // 调用仓库层的创建提醒方法
            val result = reminderRepository.createReminder(reminder)
            // 根据结果更新状态
            _reminderState.value = when {
                result.isSuccess -> {
                    // 创建成功，更新提醒列表
                    getRemindersByOrganization(reminder.orgId)
                    // 更新状态为成功
                    ReminderState.Success("提醒创建成功")
                }
                result.isFailure -> {
                    // 创建失败，更新状态为错误
                    ReminderState.Error(result.exceptionOrNull()?.message ?: "创建提醒失败")
                }
                else -> {
                    // 其他情况，更新状态为空闲
                    ReminderState.Idle
                }
            }
        }
    }
    
    /**
     * 获取组织的提醒列表
     * @param orgId 组织ID
     */
    fun getRemindersByOrganization(orgId: String) {
        // 更新状态为加载中
        _reminderState.value = ReminderState.Loading
        // 在ViewModel作用域中执行协程
        viewModelScope.launch {
            // 调用仓库层的获取组织提醒列表方法
            val result = reminderRepository.getRemindersByOrganization(orgId)
            // 根据结果更新状态
            _reminderState.value = when {
                result.isSuccess -> {
                    // 获取成功，更新提醒列表
                    _reminders.value = result.getOrThrow()
                    // 更新状态为空闲
                    ReminderState.Idle
                }
                result.isFailure -> {
                    // 获取失败，更新状态为错误
                    ReminderState.Error(result.exceptionOrNull()?.message ?: "获取提醒列表失败")
                }
                else -> {
                    // 其他情况，更新状态为空闲
                    ReminderState.Idle
                }
            }
        }
    }
    
    /**
     * 获取用户的提醒列表
     * @param userId 用户ID
     */
    fun getRemindersByUser(userId: String) {
        // 更新状态为加载中
        _reminderState.value = ReminderState.Loading
        // 在ViewModel作用域中执行协程
        viewModelScope.launch {
            // 调用仓库层的获取用户提醒列表方法
            val result = reminderRepository.getRemindersByUser(userId)
            // 根据结果更新状态
            _reminderState.value = when {
                result.isSuccess -> {
                    // 获取成功，更新提醒列表
                    _reminders.value = result.getOrThrow()
                    // 更新状态为空闲
                    ReminderState.Idle
                }
                result.isFailure -> {
                    // 获取失败，更新状态为错误
                    ReminderState.Error(result.exceptionOrNull()?.message ?: "获取提醒列表失败")
                }
                else -> {
                    // 其他情况，更新状态为空闲
                    ReminderState.Idle
                }
            }
        }
    }
    
    /**
     * 更新提醒
     * @param reminder 提醒对象
     */
    fun updateReminder(reminder: Reminder) {
        // 更新状态为加载中
        _reminderState.value = ReminderState.Loading
        // 在ViewModel作用域中执行协程
        viewModelScope.launch {
            // 调用仓库层的更新提醒方法
            val result = reminderRepository.updateReminder(reminder)
            // 根据结果更新状态
            _reminderState.value = when {
                result.isSuccess -> {
                    // 更新成功，更新提醒列表
                    getRemindersByOrganization(reminder.orgId)
                    // 更新状态为成功
                    ReminderState.Success("提醒更新成功")
                }
                result.isFailure -> {
                    // 更新失败，更新状态为错误
                    ReminderState.Error(result.exceptionOrNull()?.message ?: "更新提醒失败")
                }
                else -> {
                    // 其他情况，更新状态为空闲
                    ReminderState.Idle
                }
            }
        }
    }
    
    /**
     * 删除提醒
     * @param reminderId 提醒ID
     * @param orgId 组织ID
     */
    fun deleteReminder(reminderId: String, orgId: String) {
        // 更新状态为加载中
        _reminderState.value = ReminderState.Loading
        // 在ViewModel作用域中执行协程
        viewModelScope.launch {
            // 调用仓库层的删除提醒方法
            val result = reminderRepository.deleteReminder(reminderId)
            // 根据结果更新状态
            _reminderState.value = when {
                result.isSuccess -> {
                    // 删除成功，更新提醒列表
                    getRemindersByOrganization(orgId)
                    // 更新状态为成功
                    ReminderState.Success("提醒删除成功")
                }
                result.isFailure -> {
                    // 删除失败，更新状态为错误
                    ReminderState.Error(result.exceptionOrNull()?.message ?: "删除提醒失败")
                }
                else -> {
                    // 其他情况，更新状态为空闲
                    ReminderState.Idle
                }
            }
        }
    }
}