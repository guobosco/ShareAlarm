package com.example.sharealarm.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sharealarm.R
import com.example.sharealarm.data.model.Organization
import com.example.sharealarm.data.remote.CloudbaseAuthService
import com.example.sharealarm.data.remote.CloudbaseDatabaseService
import com.example.sharealarm.data.repository.OrganizationRepository
import com.example.sharealarm.data.viewmodel.OrganizationViewModel
import com.example.sharealarm.ui.navigation.Screen
import com.example.sharealarm.ui.theme.ShareAlarmTheme

@OptIn(ExperimentalMaterial3Api::class)
/**
 * 主页屏幕
 * 功能：显示用户所在的组织列表、提醒列表，以及提供创建提醒、创建组织、加入组织的入口
 */
@Composable
fun HomeScreen(navController: NavController) {
    // TODO: Replace with actual ViewModel instance from DI
    val authService = remember { CloudbaseAuthService(
        CloudbaseInitializer.getAuth(),
        CloudbaseInitializer.getDatabase()
    ) }
    val databaseService = remember { CloudbaseDatabaseService(
        CloudbaseInitializer.getDatabase()
    ) }
    val organizationRepository = remember { OrganizationRepository(databaseService) }
    val organizationViewModel = remember { OrganizationViewModel(organizationRepository) }

    // 当前登录用户
    val currentUser = authService.currentUser
    // 组织列表状态
    val organizations by organizationViewModel.organizations.collectAsState()

    // 当用户ID变化时，加载用户所属的组织
    LaunchedEffect(key1 = currentUser?.uid) {
        currentUser?.uid?.let {
            organizationViewModel.getOrganizationsByUser(it)
        }
    }

    ShareAlarmTheme {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = {
                        Text(text = stringResource(R.string.app_name))
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.CreateReminder.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.create_reminder)
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "欢迎使用共享闹钟",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 组织操作区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.organizations),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row {
                        // 加入组织按钮
                        IconButton(
                            onClick = {
                                navController.navigate(Screen.JoinOrganization.route)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = stringResource(R.string.join_organization)
                            )
                        }
                        // 创建组织按钮
                        IconButton(
                            onClick = {
                                navController.navigate(Screen.CreateOrganization.route)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.create_organization)
                            )
                        }
                    }
                }

                // 组织列表
                if (organizations.isEmpty()) {
                    Text(
                        text = "还没有加入任何组织，请创建或加入一个组织。",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn {
                        items(organizations) {
                            OrganizationCard(organization = it)
                        }
                    }
                }

                // 提醒区域
                Text(
                    text = stringResource(R.string.reminders),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                // 示例提醒卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = "示例会议", style = MaterialTheme.typography.titleSmall)
                        Text(text = "明天上午10:00", style = MaterialTheme.typography.bodySmall)
                        Text(text = "地点: 会议室A", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

/**
 * 组织卡片组件
 * 功能：显示单个组织的名称和成员数量
 */
@Composable
fun OrganizationCard(organization: Organization) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = organization.name, style = MaterialTheme.typography.titleSmall)
            Text(text = "成员: ${organization.members.size}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
