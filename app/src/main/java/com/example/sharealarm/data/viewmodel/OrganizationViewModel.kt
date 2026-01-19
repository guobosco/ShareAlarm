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
 * @property organizationRepository 组织仓库
 */
class OrganizationViewModel(private val organizationRepository: OrganizationRepository) : ViewModel() {
    
    // 组织状态密封类
    sealed class OrganizationState {
        object Idle : OrganizationState()
        object Loading : OrganizationState()
        data class Success(val message: String) : OrganizationState()
        data class Error(val message: String) : OrganizationState()
    }
    
    // 组织状态流
    private val _organizationState = MutableStateFlow<OrganizationState>(OrganizationState.Idle)
    val organizationState: StateFlow<OrganizationState> = _organizationState
    
    // 组织列表流
    private val _organizations = MutableStateFlow<List<Organization>>(emptyList())
    val organizations: StateFlow<List<Organization>> = _organizations
    
    /**
     * 创建组织
     * @param name 组织名称
     * @param creatorId 创建者ID
     */
    fun createOrganization(name: String, creatorId: String) {
        _organizationState.value = OrganizationState.Loading
        viewModelScope.launch {
            val result = organizationRepository.createOrganization(name, creatorId)
            _organizationState.value = when {
                result.isSuccess -> {
                    // 更新组织列表
                    getOrganizationsByUser(creatorId)
                    OrganizationState.Success("Organization created successfully")
                }
                result.isFailure -> OrganizationState.Error(result.exceptionOrNull()?.message ?: "Failed to create organization")
                else -> OrganizationState.Idle
            }
        }
    }
    
    /**
     * 加入组织
     * @param userId 用户ID
     * @param orgId 组织ID
     */
    fun joinOrganization(userId: String, orgId: String) {
        _organizationState.value = OrganizationState.Loading
        viewModelScope.launch {
            val result = organizationRepository.joinOrganization(userId, orgId)
            _organizationState.value = when {
                result.isSuccess -> {
                    // 更新组织列表
                    getOrganizationsByUser(userId)
                    OrganizationState.Success("Joined organization successfully")
                }
                result.isFailure -> OrganizationState.Error(result.exceptionOrNull()?.message ?: "Failed to join organization")
                else -> OrganizationState.Idle
            }
        }
    }
    
    /**
     * 获取用户所属的组织列表
     * @param userId 用户ID
     */
    fun getOrganizationsByUser(userId: String) {
        _organizationState.value = OrganizationState.Loading
        viewModelScope.launch {
            val result = organizationRepository.getOrganizationsByUser(userId)
            _organizationState.value = when {
                result.isSuccess -> {
                    _organizations.value = result.getOrThrow()
                    OrganizationState.Idle
                }
                result.isFailure -> OrganizationState.Error(result.exceptionOrNull()?.message ?: "Failed to get organizations")
                else -> OrganizationState.Idle
            }
        }
    }
    
    /**
     * 退出组织
     * @param userId 用户ID
     * @param orgId 组织ID
     */
    fun leaveOrganization(userId: String, orgId: String) {
        _organizationState.value = OrganizationState.Loading
        viewModelScope.launch {
            val result = organizationRepository.leaveOrganization(userId, orgId)
            _organizationState.value = when {
                result.isSuccess -> {
                    // 更新组织列表
                    getOrganizationsByUser(userId)
                    OrganizationState.Success("Left organization successfully")
                }
                result.isFailure -> OrganizationState.Error(result.exceptionOrNull()?.message ?: "Failed to leave organization")
                else -> OrganizationState.Idle
            }
        }
    }
}
