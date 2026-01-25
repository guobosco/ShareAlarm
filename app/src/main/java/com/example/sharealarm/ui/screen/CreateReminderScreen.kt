package com.example.sharealarm.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
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
import com.example.sharealarm.data.remote.CloudBaseAuthService
import com.example.sharealarm.data.remote.CloudBaseDatabaseService
import com.example.sharealarm.data.repository.ReminderRepository
import com.example.sharealarm.data.viewmodel.ReminderViewModel
import com.example.sharealarm.data.viewmodel.ReminderViewModel.ReminderState
import com.example.sharealarm.ui.navigation.Screen
import com.example.sharealarm.ui.theme.ShareAlarmTheme
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
/**
 * 时间选择器组件
 * 功能：允许用户选择日期和时间
 */
@Composable
fun TimePickerDialog(
    isEventTime: Boolean,
    showEventTimePicker: Boolean,
    showAlertTimePicker: Boolean,
    minDate: Date = Date(),
    maxDate: Date? = null,
    onDismiss: () -> Unit,
    onTimeSelected: (Date) -> Unit,
    onError: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    val selectedDate = remember { mutableStateOf(maxOf(minDate, calendar.time)) }
    val showDatePicker = remember { mutableStateOf(true) }
    
    // 初始化时设置当前时间
    LaunchedEffect(Unit) {
        val now = Date()
        if (selectedDate.value.before(now)) {
            selectedDate.value = now
        }
    }
    
    if (isEventTime && showEventTimePicker || !isEventTime && showAlertTimePicker) {
        // 定义时间选择相关的状态变量，放在更高的作用域
        val hour = remember { mutableStateOf(0) }
        val minute = remember { mutableStateOf(0) }
        
        // 当切换到时间选择界面时，初始化小时和分钟
        LaunchedEffect(showDatePicker.value, selectedDate.value) {
            if (!showDatePicker.value) {
                val calendar = Calendar.getInstance()
                calendar.time = selectedDate.value
                hour.value = calendar.get(Calendar.HOUR_OF_DAY)
                minute.value = calendar.get(Calendar.MINUTE)
            }
        }
        
        Dialog(
            onDismissRequest = onDismiss
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (isEventTime) "选择事件时间" else "选择提醒时间",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    if (showDatePicker.value) {
                        // 简化版日期选择器
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.value),
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                IconButton(
                                    onClick = {
                                        val newDate = Calendar.getInstance()
                                        newDate.time = selectedDate.value
                                        newDate.add(Calendar.DAY_OF_MONTH, -1)
                                        if (newDate.time.after(minDate) || newDate.time.equals(minDate)) {
                                            selectedDate.value = newDate.time
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "减少日期"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        selectedDate.value = maxOf(minDate, Date())
                                    }
                                ) {
                                    Text(text = "今天")
                                }
                                IconButton(
                                    onClick = {
                                        val newDate = Calendar.getInstance()
                                        newDate.time = selectedDate.value
                                        newDate.add(Calendar.DAY_OF_MONTH, 1)
                                        if (maxDate == null || newDate.time.before(maxDate) || newDate.time.equals(maxDate)) {
                                            selectedDate.value = newDate.time
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "增加日期"
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("取消")
                            }
                            TextButton(
                                onClick = {
                                    showDatePicker.value = false
                                }
                            ) {
                                Text("下一步")
                            }
                        }
                    } else {
                        // 简化版时间选择器（只精确到分钟）
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val newHour = (hour.value - 1 + 24) % 24
                                        val testCalendar = Calendar.getInstance()
                                        testCalendar.time = selectedDate.value
                                        testCalendar.set(Calendar.HOUR_OF_DAY, newHour)
                                        testCalendar.set(Calendar.MINUTE, minute.value)
                                        testCalendar.set(Calendar.SECOND, 0)
                                        testCalendar.set(Calendar.MILLISECOND, 0)
                                        
                                        if (testCalendar.time.after(minDate) || testCalendar.time.equals(minDate)) {
                                            if (maxDate == null || testCalendar.time.before(maxDate) || testCalendar.time.equals(maxDate)) {
                                                hour.value = newHour
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "减少小时"
                                    )
                                }
                                Text(
                                    text = hour.value.toString().padStart(2, '0'),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = ":",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = minute.value.toString().padStart(2, '0'),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                IconButton(
                                    onClick = {
                                        val newHour = (hour.value + 1) % 24
                                        val testCalendar = Calendar.getInstance()
                                        testCalendar.time = selectedDate.value
                                        testCalendar.set(Calendar.HOUR_OF_DAY, newHour)
                                        testCalendar.set(Calendar.MINUTE, minute.value)
                                        testCalendar.set(Calendar.SECOND, 0)
                                        testCalendar.set(Calendar.MILLISECOND, 0)
                                        
                                        if (testCalendar.time.after(minDate) || testCalendar.time.equals(minDate)) {
                                            if (maxDate == null || testCalendar.time.before(maxDate) || testCalendar.time.equals(maxDate)) {
                                                hour.value = newHour
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "增加小时"
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val newMinute = (minute.value - 1 + 60) % 60
                                        val testCalendar = Calendar.getInstance()
                                        testCalendar.time = selectedDate.value
                                        testCalendar.set(Calendar.HOUR_OF_DAY, hour.value)
                                        testCalendar.set(Calendar.MINUTE, newMinute)
                                        testCalendar.set(Calendar.SECOND, 0)
                                        testCalendar.set(Calendar.MILLISECOND, 0)
                                        
                                        if (testCalendar.time.after(minDate) || testCalendar.time.equals(minDate)) {
                                            if (maxDate == null || testCalendar.time.before(maxDate) || testCalendar.time.equals(maxDate)) {
                                                minute.value = newMinute
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "减少分钟"
                                    )
                                }
                                Text(
                                    text = "分钟",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(
                                    onClick = {
                                        val newMinute = (minute.value + 1) % 60
                                        val testCalendar = Calendar.getInstance()
                                        testCalendar.time = selectedDate.value
                                        testCalendar.set(Calendar.HOUR_OF_DAY, hour.value)
                                        testCalendar.set(Calendar.MINUTE, newMinute)
                                        testCalendar.set(Calendar.SECOND, 0)
                                        testCalendar.set(Calendar.MILLISECOND, 0)
                                        
                                        if (testCalendar.time.after(minDate) || testCalendar.time.equals(minDate)) {
                                            if (maxDate == null || testCalendar.time.before(maxDate) || testCalendar.time.equals(maxDate)) {
                                                minute.value = newMinute
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "增加分钟"
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    showDatePicker.value = true
                                }
                            ) {
                                Text("上一步")
                            }
                            TextButton(
                                onClick = {
                                    val calendar = Calendar.getInstance()
                                    calendar.time = selectedDate.value
                                    calendar.set(Calendar.HOUR_OF_DAY, hour.value)
                                    calendar.set(Calendar.MINUTE, minute.value)
                                    // 确保秒数为0
                                    calendar.set(Calendar.SECOND, 0)
                                    calendar.set(Calendar.MILLISECOND, 0)
                                    
                                    val selectedTime = calendar.time
                                    
                                    // 验证时间范围
                                    if (selectedTime.before(minDate)) {
                                        onError("选择的时间必须晚于当前时间")
                                        return@TextButton
                                    }
                                    
                                    if (maxDate != null && selectedTime.after(maxDate)) {
                                        onError("提醒时间必须早于事件时间")
                                        return@TextButton
                                    }
                                    
                                    onTimeSelected(selectedTime)
                                    showDatePicker.value = true
                                }
                            ) {
                                Text("确认")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * 创建提醒屏幕
 * 功能：允许用户创建新的提醒，输入事件名称、描述、地点、时间等信息，并可添加多个提醒时间
 */
@Composable
fun CreateReminderScreen(navController: NavController) {
    // 时间格式
    val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    // 获取星期几
    fun getWeekday(date: Date): String {
        val weekdays = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        val calendar = Calendar.getInstance()
        calendar.time = date
        val weekdayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
        return weekdays[weekdayIndex]
    }
    
    // 事件名称状态
    var title by remember { mutableStateOf("") }
    // 事件描述状态
    var description by remember { mutableStateOf("") }
    // 事件地点状态
    var location by remember { mutableStateOf("") }
    // 事件时间选择器状态
    var showEventTimePicker by remember { mutableStateOf(false) }
    // 提醒时间列表状态 (时间字符串, 星期几)
    var alertTimes by remember { mutableStateOf(mutableListOf(Pair("", ""))) }
    // 提醒时间选择器状态
    var showAlertTimePicker by remember { mutableStateOf(false) }
    // 当前编辑的提醒时间索引
    var currentAlertTimeIndex by remember { mutableStateOf(0) }
    // 提醒人员状态
    var participants by remember { mutableStateOf("") }
    // 加载状态
    var isLoading by remember { mutableStateOf(false) }
    // 错误信息状态
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // 提醒时间错误信息
    var alertTimeError by remember { mutableStateOf<String?>(null) }
    
    // 事件时间状态，默认为当前时间
    val currentDate = Date()
    val currentTime = timeFormat.format(currentDate)
    var eventTime by remember { mutableStateOf(currentTime) }
    // 事件时间对应的星期几
    var eventWeekday by remember { mutableStateOf(getWeekday(currentDate)) }
    // 事件时间对应的Date对象
    var eventTimeDate by remember { mutableStateOf(currentDate) }

    // TODO: Replace with actual ViewModel instance from DI
    val authService = remember { CloudBaseAuthService() }
    val databaseService = remember { CloudBaseDatabaseService() }
    val reminderRepository = remember { ReminderRepository(databaseService) }
    val reminderViewModel = remember { ReminderViewModel(reminderRepository) }

    val currentUser = authService.currentUser
    val reminderState by reminderViewModel.reminderState.collectAsState()

    // 监听状态变化，处理加载、成功和错误情况
    LaunchedEffect(key1 = reminderState) {
        when (reminderState) {
            is ReminderState.Success -> {
                isLoading = false
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.CreateReminder.route) { inclusive = true }
                }
            }
            is ReminderState.Error -> {
                isLoading = false
                errorMessage = (reminderState as ReminderState.Error).message
            }
            ReminderState.Loading -> {
                isLoading = true
            }
            else -> { isLoading = false }
        }
    }

    // 显示时间选择器
    fun showTimePicker(isEventTime: Boolean, index: Int = 0) {
        if (isEventTime) {
            showEventTimePicker = true
        } else {
            currentAlertTimeIndex = index
            showAlertTimePicker = true
        }
    }
    
    // 处理时间选择
    fun onTimeSelected(date: Date, isEventTime: Boolean) {
        val formattedTime = timeFormat.format(date)
        val weekday = getWeekday(date)
        if (isEventTime) {
            eventTime = formattedTime
            eventWeekday = weekday
            eventTimeDate = date
            showEventTimePicker = false
            errorMessage = null
            
            // 设置第一个提醒时间为事件时间的前15分钟
            val reminderCalendar = Calendar.getInstance()
            reminderCalendar.time = date
            reminderCalendar.add(Calendar.MINUTE, -15)
            val reminderDate = reminderCalendar.time
            val reminderTime = timeFormat.format(reminderDate)
            val reminderWeekday = getWeekday(reminderDate)
            
            val newAlertTimes = alertTimes.toMutableList()
            newAlertTimes[0] = Pair(reminderTime, reminderWeekday)
            alertTimes = newAlertTimes
            alertTimeError = null
        } else {
            val newAlertTimes = alertTimes.toMutableList()
            newAlertTimes[currentAlertTimeIndex] = Pair(formattedTime, weekday)
            alertTimes = newAlertTimes
            showAlertTimePicker = false
            alertTimeError = null
        }
    }
    
    // 处理时间选择错误
    fun onTimeError(message: String) {
        if (showEventTimePicker) {
            errorMessage = message
        } else if (showAlertTimePicker) {
            alertTimeError = message
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
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showTimePicker(isEventTime = true) }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "选择事件时间"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    // 显示事件时间对应的星期几
                        Text(
                            text = "(${eventWeekday})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        // 显示事件时间错误信息
                        errorMessage?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                    Text(
                        text = stringResource(R.string.alert_times),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // 显示提醒时间错误信息
                    alertTimeError?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    alertTimes.forEachIndexed { index, (time, weekday) ->
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = time,
                                    onValueChange = { newTime ->
                                        // 更新特定位置的提醒时间
                                        val newAlertTimes = alertTimes.toMutableList()
                                        newAlertTimes[index] = Pair(newTime, "")
                                        alertTimes = newAlertTimes
                                    },
                                    label = { Text("提醒时间 ${index + 1}") },
                                    placeholder = { Text("例如: 2024-01-15 14:25") },
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { showTimePicker(isEventTime = false, index = index) }) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = "选择提醒时间"
                                            )
                                        }
                                    },
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
                            // 显示提醒时间对应的星期几
                            if (weekday.isNotEmpty()) {
                                Text(
                                    text = "(${weekday})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            // 添加新的提醒时间输入框
                            alertTimes = alertTimes.toMutableList().apply { add(Pair("", "")) }
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
                                // 验证事件时间是否晚于当前时间
                                if (eventTimeDate.before(Date())) {
                                    errorMessage = "事件时间必须晚于当前时间"
                                    return@Button
                                }
                                
                                // 转换提醒时间为Date对象
                                val alertTimeDates = alertTimes
                                    .filter { it.first.isNotEmpty() } // 过滤空的提醒时间
                                    .mapNotNull { (timeStr, _) ->
                                        try {
                                            timeFormat.parse(timeStr)
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                
                                // 验证所有提醒时间是否早于事件时间且晚于当前时间
                                for (alertTime in alertTimeDates) {
                                    if (alertTime.after(eventTimeDate)) {
                                        alertTimeError = "提醒时间必须早于事件时间"
                                        return@Button
                                    }
                                    if (alertTime.before(Date())) {
                                        alertTimeError = "提醒时间必须晚于当前时间"
                                        return@Button
                                    }
                                }
                                
                                // 如果没有提醒时间，使用默认值
                                val finalAlertTimes = if (alertTimeDates.isNotEmpty()) {
                                    alertTimeDates
                                } else {
                                    // 默认提醒时间为事件时间前15分钟
                                    val defaultReminderTime = Calendar.getInstance()
                                    defaultReminderTime.time = eventTimeDate
                                    defaultReminderTime.add(Calendar.MINUTE, -15)
                                    listOf(defaultReminderTime.time)
                                }
                                
                                // 在开发阶段，我们可以使用模拟数据，不依赖真实的云服务用户
                                val mockUserId = "mock-user-id"
                                val orgId = "test-org-id"
                                val participants = listOf(mockUserId)
                                
                                val reminder = Reminder(
                                    orgId = orgId,
                                    title = title,
                                    description = description,
                                    eventTime = eventTimeDate,
                                    location = location,
                                    alertTimes = finalAlertTimes,
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
        
        // 事件时间选择器
        TimePickerDialog(
            isEventTime = true,
            showEventTimePicker = showEventTimePicker,
            showAlertTimePicker = showAlertTimePicker,
            minDate = Date(),
            onDismiss = { showEventTimePicker = false },
            onTimeSelected = { date -> onTimeSelected(date, true) },
            onError = ::onTimeError
        )
        // 提醒时间选择器
        TimePickerDialog(
            isEventTime = false,
            showEventTimePicker = showEventTimePicker,
            showAlertTimePicker = showAlertTimePicker,
            minDate = Date(),
            maxDate = eventTimeDate,
            onDismiss = { showAlertTimePicker = false },
            onTimeSelected = { date -> onTimeSelected(date, false) },
            onError = ::onTimeError
        )
    }
}
