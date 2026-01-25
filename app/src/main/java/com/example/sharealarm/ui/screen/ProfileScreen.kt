@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sharealarm.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sharealarm.R
import com.example.sharealarm.data.model.Organization
import com.example.sharealarm.data.model.Reminder
import com.example.sharealarm.data.remote.CloudBaseAuthService
import com.example.sharealarm.data.remote.CloudBaseDatabaseService
import com.example.sharealarm.data.repository.OrganizationRepository
import com.example.sharealarm.data.repository.ReminderRepository
import com.example.sharealarm.data.viewmodel.OrganizationViewModel
import com.example.sharealarm.data.viewmodel.ReminderViewModel
import com.example.sharealarm.ui.navigation.Screen
import com.example.sharealarm.ui.theme.ShareAlarmTheme
import java.util.Date
import java.util.Calendar

/**
 * 我的页面屏幕
 * 功能：显示用户信息、组织、提醒等个人相关内容
 */
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current

    // TODO: Replace with actual ViewModel instance from DI
    val authService = remember { CloudBaseAuthService() }
    val databaseService = remember { CloudBaseDatabaseService() }
    val organizationRepository = remember { OrganizationRepository(databaseService) }
    val organizationViewModel = remember { OrganizationViewModel(organizationRepository) }
    val reminderRepository = remember { ReminderRepository(databaseService, context) }
    val reminderViewModel = remember { ReminderViewModel(reminderRepository) }

    // 当前登录用户
    val currentUser = authService.currentUser
    // 组织列表状态
    val organizations by organizationViewModel.organizations.collectAsState()
    // 提醒列表状态
    val reminders by reminderViewModel.reminders.collectAsState()
    
    // 过滤出用户创建的组织
    val createdOrganizations by remember(organizations, currentUser) {
        derivedStateOf {
            if (currentUser != null) {
                organizations.filter { it.creatorId == currentUser.id }
            } else {
                emptyList()
            }
        }
    }
    
    // 过滤出用户创建的提醒
    val createdReminders by remember(reminders, currentUser) {
        derivedStateOf {
            if (currentUser != null) {
                reminders.filter { it.creator == currentUser.id }
            } else {
                emptyList()
            }
        }
    }

    // 当用户ID变化时，加载用户所属的组织和提醒列表
    LaunchedEffect(key1 = currentUser?.id) {
        currentUser?.id?.let {
            organizationViewModel.getOrganizationsByUser(it)
            reminderViewModel.getRemindersByUser(it)
        }
    }

    ShareAlarmTheme {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = {
                        Text(text = "我的")
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
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    end = 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 用户信息区域
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 头像占位
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .align(Alignment.CenterHorizontally),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "用户头像",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = currentUser?.name ?: "未登录",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = currentUser?.email ?: "请登录账号",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "个性签名：这个人很懒，什么都没写",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // 账号设置区域
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Default.Person,
                                title = "账号安全",
                                onClick = { /* 账号安全逻辑 */ }
                            )
                            Divider()
                            ProfileMenuItem(
                                icon = Icons.Default.Key,
                                title = "修改密码",
                                onClick = { /* 修改密码逻辑 */ }
                            )
                        }
                    }
                }

                // 组织管理区域
                item {
                    Text(
                        text = "组织管理",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Default.Group,
                                title = "我加入的组织",
                                subtitle = "${organizations.size}个",
                                onClick = { /* 我加入的组织逻辑 */ }
                            )
                            Divider()
                            ProfileMenuItem(
                                icon = Icons.Default.AddBox,
                                title = "我创建的组织",
                                subtitle = "${createdOrganizations.size}个",
                                onClick = { /* 我创建的组织逻辑 */ }
                            )
                        }
                    }
                }

                // 提醒管理区域
                item {
                    Text(
                        text = "提醒管理",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Default.Notifications,
                                title = "我创建的提醒",
                                subtitle = "${createdReminders.size}个",
                                onClick = { /* 我创建的提醒逻辑 */ }
                            )
                        }
                    }
                }

                // 支付信息区域
                item {
                    Text(
                        text = "其他",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Default.Payment,
                                title = "支付信息",
                                onClick = { /* 支付信息逻辑 */ }
                            )
                        }
                    }
                }

                // 退出登录按钮
                item {
                    Button(
                        onClick = {
                            authService.signOut()
                            navController.navigate(Screen.SignIn.route) {
                                popUpTo(Screen.Profile.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(text = "退出登录")
                    }
                }


            }
        }
    }
}

/**
 * 个人页面菜单项组件
 * 功能：显示个人页面中的单个菜单项
 */
@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = "前往",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}
