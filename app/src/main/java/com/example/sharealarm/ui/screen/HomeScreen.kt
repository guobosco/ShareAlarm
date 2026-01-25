package com.example.sharealarm.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
/**
 * 主页屏幕
 * 功能：显示用户所在的组织列表、提醒列表，以及提供创建提醒、创建组织、加入组织的入口
 */
@Composable
fun HomeScreen(navController: NavController) {
    // TODO: Replace with actual ViewModel instance from DI
    val authService = remember { CloudBaseAuthService() }
    val databaseService = remember { CloudBaseDatabaseService() }
    val organizationRepository = remember { OrganizationRepository(databaseService) }
    val organizationViewModel = remember { OrganizationViewModel(organizationRepository) }
    val reminderRepository = remember { ReminderRepository(databaseService) }
    val reminderViewModel = remember { ReminderViewModel(reminderRepository) }

    // 当前登录用户
    val currentUser = authService.currentUser
    // 组织列表状态
    val organizations by organizationViewModel.organizations.collectAsState()
    // 提醒列表状态
    val reminders by reminderViewModel.reminders.collectAsState()
    
    // 排序后的提醒列表
    val sortedReminders by remember(reminders) {
        derivedStateOf {
            reminders.sortedBy { it.eventTime }
        }
    }
    
    // 判断提醒是否已过期
    fun isReminderExpired(reminder: Reminder): Boolean {
        return reminder.eventTime.before(Date())
    }

    // 格式化事件时间
    fun formatEventTime(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val weekday = "日一二三四五六"[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        
        return String.format("%d年%02d月%02d日（星期%s） %02d:%02d", 
            year, month, day, weekday, hour, minute)
    }

    // 当用户ID变化时，加载用户所属的组织和提醒列表
    LaunchedEffect(key1 = currentUser?.id) {
        currentUser?.id?.let {
            organizationViewModel.getOrganizationsByUser(it)
            reminderViewModel.getRemindersByUser(it)
        }
    }
    
    // 当屏幕显示时，重新加载提醒列表
    LaunchedEffect(key1 = navController.currentBackStackEntry?.destination?.route) {
        // 在开发阶段，使用模拟用户ID
        val mockUserId = "mock-user-id"
        Log.d("HomeScreen", "重新加载提醒列表 for user: $mockUserId")
        reminderViewModel.getRemindersByUser(mockUserId)
        
        // 也加载组织提醒列表，确保数据完整
        val testOrgId = "test-org-id"
        Log.d("HomeScreen", "重新加载组织提醒列表 for org: $testOrgId")
        reminderViewModel.getRemindersByOrganization(testOrgId)
    }

    ShareAlarmTheme {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = {
                        Text(text = stringResource(R.string.app_name))
                    },
                    actions = {
                        // 我的页面入口
                        IconButton(
                            onClick = {
                                navController.navigate(Screen.Profile.route)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "我的页面"
                            )
                        }
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "欢迎使用共享闹钟",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                item {
                    // 组织操作区域
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.organizations),
                                style = MaterialTheme.typography.titleMedium
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
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                organizations.forEach {
                                    OrganizationCard(organization = it)
                                }
                            }
                        }
                    }
                }

                item {
                    // 提醒区域
                    Column {
                        Text(
                            text = stringResource(R.string.reminders),
                            style = MaterialTheme.typography.titleMedium
                        )

                        // 提醒列表
                        if (sortedReminders.isEmpty()) {
                            Text(
                                text = "还没有提醒，请创建一个新的提醒。",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                sortedReminders.forEach {
                                    ReminderCard(reminder = it, isExpired = isReminderExpired(it))
                                }
                            }
                        }
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

/**
 * 提醒卡片组件
 * 功能：显示单个提醒的详细信息，包括事件名称、时间、地点等
 */
@Composable
fun ReminderCard(reminder: Reminder, isExpired: Boolean) {
    // 展开/折叠状态
    // 确保过期的提醒默认折叠
    var isExpanded by remember(isExpired) { mutableStateOf(!isExpired) }
    
    // 格式化事件时间
    fun formatEventTime(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val weekday = "日一二三四五六"[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        
        return String.format("%d年%02d月%02d日（星期%s） %02d:%02d", 
            year, month, day, weekday, hour, minute)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable {
                if (isExpired) {
                    isExpanded = !isExpanded
                }
            },
        colors = if (isExpired) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = reminder.title, style = MaterialTheme.typography.titleSmall)
            Text(text = "时间: ${formatEventTime(reminder.eventTime)}", style = MaterialTheme.typography.bodySmall)
            
            if (!isExpired || isExpanded) {
                Text(text = "地点: ${reminder.location}", style = MaterialTheme.typography.bodySmall)
                if (reminder.description.isNotEmpty()) {
                    Text(text = "描述: ${reminder.description}", style = MaterialTheme.typography.bodySmall)
                }
                // 显示提醒时间
                if (reminder.alertTimes.isNotEmpty()) {
                    Text(text = "提醒时间:", style = MaterialTheme.typography.bodySmall)
                    reminder.alertTimes.forEachIndexed { index, alertTime ->
                        Text(text = "  ${index + 1}. ${formatEventTime(alertTime)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                // 显示参加人
                if (reminder.participants.isNotEmpty()) {
                    Text(text = "参加人: ${reminder.participants.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            if (isExpired) {
                Text(
                    text = "（已过期，点击展开详情）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
