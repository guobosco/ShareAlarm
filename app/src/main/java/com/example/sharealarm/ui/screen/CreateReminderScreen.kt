package com.example.sharealarm.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sharealarm.R
import com.example.sharealarm.data.model.Reminder
import com.example.sharealarm.data.remote.CloudbaseAuthService
import com.example.sharealarm.data.remote.CloudbaseDatabaseService
import com.example.sharealarm.data.repository.ReminderRepository
import com.example.sharealarm.data.viewmodel.ReminderViewModel
import com.example.sharealarm.ui.navigation.Screen
import com.example.sharealarm.ui.theme.ShareAlarmTheme
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
/**
 * 创建提醒屏幕
 * 功能：允许用户创建新的提醒，输入事件名称、描述、地点、时间等信息，并可添加多个提醒时间
 */
@Composable
fun CreateReminderScreen(navController: NavController) {
    // 事件名称状态
    var title by remember { mutableStateOf("") }
    // 事件描述状态
    var description by remember { mutableStateOf("") }
    // 事件地点状态
    var location by remember { mutableStateOf("") }
    // 事件时间状态
    var eventTime by remember { mutableStateOf("") }
    // 提醒时间列表状态
    var alertTimes by remember { mutableStateOf(mutableListOf("")) }
    // 提醒人员状态
    var participants by remember { mutableStateOf("") }
    // 加载状态
    var isLoading by remember { mutableStateOf(false) }
    // 错误信息状态
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // TODO: Replace with actual ViewModel instance from DI
    val authService = remember { CloudbaseAuthService(
        CloudbaseInitializer.getAuth(),
        CloudbaseInitializer.getDatabase()
    ) }
    val databaseService = remember { CloudbaseDatabaseService(
        CloudbaseInitializer.getDatabase()
    ) }
    val reminderRepository = remember { ReminderRepository(databaseService) }
    val reminderViewModel = remember { ReminderViewModel(reminderRepository) }

    val currentUser = authService.currentUser
    val reminderState by reminderViewModel.reminderState.collectAsState()

    // 监听状态变化，处理加载、成功和错误情况
    LaunchedEffect(key1 = reminderState) {
        when (reminderState) {
            is com.example.sharealarm.data.viewmodel.ReminderState.Success -> {
                isLoading = false
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.CreateReminder.route) { inclusive = true }
                }
            }
            is com.example.sharealarm.data.viewmodel.ReminderState.Error -> {
                isLoading = false
                errorMessage = (reminderState as com.example.sharealarm.data.viewmodel.ReminderState.Error).message
            }
            com.example.sharealarm.data.viewmodel.ReminderState.Loading -> {
                isLoading = true
            }
            else -> { isLoading = false }
        }
    }

    ShareAlarmTheme {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = {
                        Text(text = stringResource(R.string.create_reminder))
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        text = "创建新提醒",
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
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.event_name)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.description)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text(stringResource(R.string.location)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    Text(
                        text = stringResource(R.string.event_time),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = eventTime,
                        onValueChange = { eventTime = it },
                        label = { Text("选择事件时间") },
                        placeholder = { Text("例如: 2024-01-15 14:30") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    Text(
                        text = stringResource(R.string.alert_times),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    alertTimes.forEachIndexed { index, time ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = time,
                                onValueChange = { newTime ->
                                    // 更新特定位置的提醒时间
                                    val newAlertTimes = alertTimes.toMutableList()
                                    newAlertTimes[index] = newTime
                                    alertTimes = newAlertTimes
                                },
                                label = { Text("提醒时间 ${index + 1}") },
                                placeholder = { Text("例如: 2024-01-15 14:25") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (alertTimes.size > 1) {
                                        alertTimes = alertTimes.toMutableList().apply { removeAt(index) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "删除提醒时间"
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            // 添加新的提醒时间输入框
                            alertTimes = alertTimes.toMutableList().apply { add("") }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.add_alert_time),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.add_alert_time))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.participants),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = participants,
                        onValueChange = { participants = it },
                        label = { Text(stringResource(R.string.select_participants)) },
                        placeholder = { Text("例如: 张三, 李四") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    )

                    Button(
                        onClick = {
                            if (title.isNotEmpty()) {
                                // 在开发阶段，我们可以使用模拟数据，不依赖真实的云服务用户
                                val mockUserId = "mock-user-id"
                                val orgId = "test-org-id"
                                val participants = listOf(mockUserId)
                                // TODO: 实际应用中需要将字符串时间转换为Date对象
                                val reminder = Reminder(
                                    orgId = orgId,
                                    title = title,
                                    description = description,
                                    eventTime = Date(),
                                    location = location,
                                    alertTimes = listOf(Date()),
                                    participants = participants,
                                    creator = mockUserId
                                )

                                reminderViewModel.createReminder(reminder)
                            } else {
                                errorMessage = "请填写事件名称"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text(text = stringResource(R.string.create_reminder))
                        }
                    }
                }
            }
        }
    }
}
