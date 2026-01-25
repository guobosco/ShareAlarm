package com.example.sharealarm.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sharealarm.R
import com.example.sharealarm.data.remote.CloudBaseAuthService
import com.example.sharealarm.data.remote.CloudBaseDatabaseService
import com.example.sharealarm.data.repository.OrganizationRepository
import com.example.sharealarm.data.viewmodel.OrganizationViewModel
import com.example.sharealarm.data.viewmodel.OrganizationViewModel.OrganizationState
import com.example.sharealarm.ui.navigation.Screen
import com.example.sharealarm.ui.theme.ShareAlarmTheme

@OptIn(ExperimentalMaterial3Api::class)
/**
 * 加入组织屏幕
 * 功能：允许用户通过组织ID加入现有组织
 */
@Composable
fun JoinOrganizationScreen(navController: NavController) {
    // 组织ID输入状态
    var orgId by remember { mutableStateOf("") }
    // 加载状态
    var isLoading by remember { mutableStateOf(false) }
    // 错误信息状态
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // TODO: Replace with actual ViewModel instance from DI
    val authService = remember { CloudBaseAuthService() }
    val databaseService = remember { CloudBaseDatabaseService() }
    val organizationRepository = remember { OrganizationRepository(databaseService) }
    val organizationViewModel = remember { OrganizationViewModel(organizationRepository) }

    // 当前登录用户
    val currentUser = authService.currentUser
    // 组织状态
    val organizationState by organizationViewModel.organizationState.collectAsState()

    // 监听组织状态变化，处理加载、成功和错误情况
    LaunchedEffect(key1 = organizationState) {
        when (organizationState) {
            is OrganizationState.Success -> {
                isLoading = false
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.JoinOrganization.route) { inclusive = true }
                }
            }
            is OrganizationState.Error -> {
                isLoading = false
                errorMessage = (organizationState as OrganizationState.Error).message
            }
            OrganizationState.Loading -> {
                isLoading = true
            }
            else -> { /* Do nothing */ }
        }
    }

    ShareAlarmTheme {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = {
                        Text(text = stringResource(R.string.join_organization))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "加入现有组织",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                OutlinedTextField(
                    value = orgId,
                    onValueChange = { orgId = it },
                    label = { Text(stringResource(R.string.organization_code)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                Button(
                    onClick = {
                        if (orgId.isNotEmpty() && currentUser != null) {
                            organizationViewModel.joinOrganization(currentUser.id, orgId)
                        } else {
                            errorMessage = "请输入组织ID"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.join_organization))
                    }
                }
            }
        }
    }
}
