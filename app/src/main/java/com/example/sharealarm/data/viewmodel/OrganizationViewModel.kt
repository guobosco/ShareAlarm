package com.example.sharealarm.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharealarm.data.model.Organization
import com.example.sharealarm.data.repository.OrganizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 组织视图模型
 * 功能：管理组织相关的UI状态，处理组织的创建、加入、获取等操作
 * @property organizationRepository 组织仓库，用于调用组织相关的数据操作
 */
class OrganizationViewModel(private val organizationRepository: OrganizationRepository) : ViewModel() {
    
    /**
     * 组织状态密封类
     * 用于表示不同的组织操作状态
     */
    sealed class OrganizationState {
        /**
         * 空闲状态，初始状态
         */
        object Idle : OrganizationState()
        /**
         * 加载状态，正在执行组织操作
         */
        object Loading : OrganizationState()
        /**
         * 成功状态，组织操作成功
         * @param message 成功消息
         */
        data class Success(val message: String) : OrganizationState()
        /**
         * 错误状态，组织操作失败
         * @param message 错误消息
         */
        data class Error(val message: String) : OrganizationState()
    }
    
    /**
     * 私有可变组织状态流，用于内部更新状态
     */
    private val _organizationState = MutableStateFlow<OrganizationState>(OrganizationState.Idle)
    /**
     * 公开的不可变组织状态流，供UI层观察
     */
    val organizationState: StateFlow<OrganizationState> = _organizationState
    
    /**
     * 私有可变组织列表流，用于内部更新组织列表
     */
    private val _organizations = MutableStateFlow<List<Organization>>(emptyList())
    /**
     * 公开的不可变组织列表流，供UI层观察
     */
    val organizations: StateFlow<List<Organization>> = _organizations
    
    /**
     * 创建组织
     * @param name 组织名称
     * @param creatorId 创建者ID
     */
    fun createOrganization(name: String, creatorId: String) {
        // 更新状态为加载中
        _organizationState.value = OrganizationState.Loading
        // 在ViewModel作用域中执行协程
        viewModelScope.launch {
            // 调用仓库层的创建组织方法
            val result = organizationRepository.createOrganization(name, creatorId)
            // 根据结果更新状态
            _organizationState.value = when {
                result.isSuccess -> {
                    // 创建成功，更新组织列表
                    getOrganizationsByUser(creatorId)
                    // 更新状态为成功
                    OrganizationState.Success("组织创建成功")
                }
                result.isFailure -> {
                    // 创建失败，更新状态为错误
                    OrganizationState.Error(result.exceptionOrNull()?.message ?: "创建组织失败")
                }
                else -> {
                    // 其他情况，更新状态为空闲
                    OrganizationState.Idle
                }
            }
        }
    }
    
    /**
     * 加入组织
     * @param userId 用户ID
     * @param orgId 组织ID
     */
    fun joinOrganization(userId: String, orgId: String) {
        // 更新状态为加载中
        _organizationState.value = OrganizationState.Loading
        // 在ViewModel作用域中执行协程
        viewModelScope.launch {
            // 调用仓库层的加入组织方法
            val result = organizationRepository.joinOrganization(userId, orgId)
            // 根据结果更新状态
            _organizationState.value = when {
                result.isSuccess -> {
                    // 加入成功，更新组织列表
                    getOrganizationsByUser(userId)
                    // 更新状态为成功
                    OrganizationState.Success("加入组织成功")
                }
                result.isFailure -> {
                    // 加入失败，更新状态为错误
                    OrganizationState.Error(result.exceptionOrNull()?.message ?: "加入组织失败")
                }
                else -> {
                    // 其他情况，更新状态为空闲
                    OrganizationState.Idle
                }
            }
        }
    }
    
    /**
     * 获取用户所属的组织列表
     * @param userId 用户ID
     */
    fun getOrganizationsByUser(userId: String) {
        // 更新状态为加载中
        _organizationState.value = OrganizationState.Loading
        // 在ViewModel作用域中执行协程
        viewModelScope.launch {
            // 调用仓库层的获取组织列表方法
            val result = organizationRepository.getOrganizationsByUser(userId)
            // 根据结果更新状态
            _organizationState.value = when {
                result.isSuccess -> {
                    // 获取成功，更新组织列表
                    _organizations.value = result.getOrThrow()
                    // 更新状态为空闲
                    OrganizationState.Idle
                }
                result.isFailure -> {
                    // 获取失败，更新状态为错误
                    OrganizationState.Error(result.exceptionOrNull()?.message ?: "获取组织列表失败")
                }
                else -> {
                    // 其他情况，更新状态为空闲
                    OrganizationState.Idle
                }
            }
        }
    }
    
    /**
     * 退出组织
     * @param userId 用户ID
     * @param orgId 组织ID
     */
    fun leaveOrganization(userId: String, orgId: String) {
        // 更新状态为加载中
        _organizationState.value = OrganizationState.Loading
        // 在ViewModel作用域中执行协程
        viewModelScope.launch {
            // 调用仓库层的退出组织方法
            val result = organizationRepository.leaveOrganization(userId, orgId)
            // 根据结果更新状态
            _organizationState.value = when {
                result.isSuccess -> {
                    // 退出成功，更新组织列表
                    getOrganizationsByUser(userId)
                    // 更新状态为成功
                    OrganizationState.Success("退出组织成功")
                }
                result.isFailure -> {
                    // 退出失败，更新状态为错误
                    OrganizationState.Error(result.exceptionOrNull()?.message ?: "退出组织失败")
                }
                else -> {
                    // 其他情况，更新状态为空闲
                    OrganizationState.Idle
                }
            }
        }
    }
}
