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
 * @property reminderRepository 提醒仓库
 */
class ReminderViewModel(private val reminderRepository: ReminderRepository) : ViewModel() {
    
    // 提醒状态密封类
    sealed class ReminderState {
        object Idle : ReminderState()
        object Loading : ReminderState()
        data class Success(val message: String) : ReminderState()
        data class Error(val message: String) : ReminderState()
    }
    
    // 提醒状态流
    private val _reminderState = MutableStateFlow<ReminderState>(ReminderState.Idle)
    val reminderState: StateFlow<ReminderState> = _reminderState
    
    // 提醒列表流
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders
    
    /**
     * 创建提醒
     * @param reminder 提醒对象
     */
    fun createReminder(reminder: Reminder) {
        _reminderState.value = ReminderState.Loading
        viewModelScope.launch {
            val result = reminderRepository.createReminder(reminder)
            _reminderState.value = when {
                result.isSuccess -> {
                    // 更新提醒列表
                    getRemindersByOrganization(reminder.orgId)
                    ReminderState.Success("Reminder created successfully")
                }
                result.isFailure -> ReminderState.Error(result.exceptionOrNull()?.message ?: "Failed to create reminder")
                else -> ReminderState.Idle
            }
        }
    }
    
    /**
     * 获取组织的提醒列表
     * @param orgId 组织ID
     */
    fun getRemindersByOrganization(orgId: String) {
        _reminderState.value = ReminderState.Loading
        viewModelScope.launch {
            val result = reminderRepository.getRemindersByOrganization(orgId)
            _reminderState.value = when {
                result.isSuccess -> {
                    _reminders.value = result.getOrThrow()
                    ReminderState.Idle
                }
                result.isFailure -> ReminderState.Error(result.exceptionOrNull()?.message ?: "Failed to get reminders")
                else -> ReminderState.Idle
            }
        }
    }
    
    /**
     * 获取用户的提醒列表
     * @param userId 用户ID
     */
    fun getRemindersByUser(userId: String) {
        _reminderState.value = ReminderState.Loading
        viewModelScope.launch {
            val result = reminderRepository.getRemindersByUser(userId)
            _reminderState.value = when {
                result.isSuccess -> {
                    _reminders.value = result.getOrThrow()
                    ReminderState.Idle
                }
                result.isFailure -> ReminderState.Error(result.exceptionOrNull()?.message ?: "Failed to get reminders")
                else -> ReminderState.Idle
            }
        }
    }
    
    /**
     * 更新提醒
     * @param reminder 提醒对象
     */
    fun updateReminder(reminder: Reminder) {
        _reminderState.value = ReminderState.Loading
        viewModelScope.launch {
            val result = reminderRepository.updateReminder(reminder)
            _reminderState.value = when {
                result.isSuccess -> {
                    // 更新提醒列表
                    getRemindersByOrganization(reminder.orgId)
                    ReminderState.Success("Reminder updated successfully")
                }
                result.isFailure -> ReminderState.Error(result.exceptionOrNull()?.message ?: "Failed to update reminder")
                else -> ReminderState.Idle
            }
        }
    }
    
    /**
     * 删除提醒
     * @param reminderId 提醒ID
     * @param orgId 组织ID
     */
    fun deleteReminder(reminderId: String, orgId: String) {
        _reminderState.value = ReminderState.Loading
        viewModelScope.launch {
            val result = reminderRepository.deleteReminder(reminderId)
            _reminderState.value = when {
                result.isSuccess -> {
                    // 更新提醒列表
                    getRemindersByOrganization(orgId)
                    ReminderState.Success("Reminder deleted successfully")
                }
                result.isFailure -> ReminderState.Error(result.exceptionOrNull()?.message ?: "Failed to delete reminder")
                else -> ReminderState.Idle
            }
        }
    }
}