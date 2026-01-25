package com.example.sharealarm.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import android.os.Build
import android.app.NotificationManager
import android.util.Log
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.window.Dialog
import com.example.sharealarm.service.PermissionService

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
    val selectedTabIndex = remember { mutableStateOf(0) }
    
    // 初始化时设置当前时间
    LaunchedEffect(Unit) {
        val now = Date()
        if (selectedDate.value.before(now)) {
            selectedDate.value = now
        }
    }
    
    if (isEventTime && showEventTimePicker || !isEventTime && showAlertTimePicker) {
        // 定义时间选择相关的状态变量
        val selectedHour = remember { mutableStateOf(0) }
        val selectedMinute = remember { mutableStateOf(0) }
        
        // 初始化时间
        LaunchedEffect(selectedDate.value) {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate.value
            selectedHour.value = calendar.get(Calendar.HOUR_OF_DAY)
            selectedMinute.value = calendar.get(Calendar.MINUTE)
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
                    
                    // Tab导航
                    TabRow(selectedTabIndex = selectedTabIndex.value) {
                        Tab(
                            selected = selectedTabIndex.value == 0,
                            onClick = { selectedTabIndex.value = 0 },
                            text = { Text("日期") }
                        )
                        Tab(
                            selected = selectedTabIndex.value == 1,
                            onClick = { selectedTabIndex.value = 1 },
                            text = { Text("时间") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Tab内容
                    when (selectedTabIndex.value) {
                        0 -> {
                            // 全月日历选择器
                            CalendarTab(
                                selectedDate = selectedDate.value,
                                minDate = minDate,
                                maxDate = maxDate,
                                selectedTabIndex = selectedTabIndex,
                                onDateSelected = { date ->
                                    selectedDate.value = date
                                    selectedTabIndex.value = 1 // 自动切换到时间选择tab
                                }
                            )
                        }
                        1 -> {
                            // 时钟选择器
                            ClockTab(
                                selectedHour = selectedHour.value,
                                selectedMinute = selectedMinute.value,
                                minDate = minDate,
                                maxDate = maxDate,
                                onTimeSelected = { hour, minute ->
                                    selectedHour.value = hour
                                    selectedMinute.value = minute
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 操作按钮
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                calendar.time = selectedDate.value
                                calendar.set(Calendar.HOUR_OF_DAY, selectedHour.value)
                                calendar.set(Calendar.MINUTE, selectedMinute.value)
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

/**
 * 日历选择Tab页面
 * 功能：显示全月日历，允许用户选择日期
 */
@Composable
fun CalendarTab(
    selectedDate: Date,
    minDate: Date,
    maxDate: Date?,
    selectedTabIndex: MutableState<Int>,
    onDateSelected: (Date) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.time = selectedDate
    
    val currentYear = remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    val currentMonth = remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    
    // 获取当前月份的天数
    val daysInMonth = remember(currentYear.value, currentMonth.value) {
        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(currentYear.value, currentMonth.value, 1)
        tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    
    // 获取当月第一天是星期几（0-6，0表示星期日）
    val firstDayOfMonth = remember(currentYear.value, currentMonth.value) {
        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(currentYear.value, currentMonth.value, 1)
        tempCalendar.get(Calendar.DAY_OF_WEEK) - 1 // 转换为0-6
    }
    
    // 生成日历数据
    val calendarDays = remember(daysInMonth, firstDayOfMonth) {
        val days = mutableListOf<Int>()
        // 添加空白日期（当月第一天之前的日期）
        for (i in 0 until firstDayOfMonth) {
            days.add(0)
        }
        // 添加当月日期
        for (i in 1..daysInMonth) {
            days.add(i)
        }
        days
    }
    
    // 月份名称
    val monthNames = arrayOf(
        "一月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "十一月", "十二月"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 月份导航
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    // 上一个月
                    currentMonth.value = (currentMonth.value - 1 + 12) % 12
                    if (currentMonth.value == 11) { // 月份从0开始，11表示12月
                        currentYear.value--
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "上一个月"
                )
            }
            
            Text(
                text = "${currentYear.value}年${monthNames[currentMonth.value]}",
                style = MaterialTheme.typography.titleMedium
            )
            
            IconButton(
                onClick = {
                    // 下一个月
                    currentMonth.value = (currentMonth.value + 1) % 12
                    if (currentMonth.value == 0) { // 0表示1月
                        currentYear.value++
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "下一个月"
                )
            }
        }
        
        // 星期标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val weekdays = arrayOf("日", "一", "二", "三", "四", "五", "六")
            weekdays.forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 日历网格
        Column(modifier = Modifier.fillMaxWidth()) {
            for (row in 0 until 6) { // 最多显示6行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0 until 7) { // 7列（一周7天）
                        val index = row * 7 + col
                        if (index < calendarDays.size) {
                            val day = calendarDays[index]
                            Box(modifier = Modifier.weight(1f)) {
                                CalendarDayItem(
                                    day = day,
                                    currentYear = currentYear.value,
                                    currentMonth = currentMonth.value,
                                    selectedDate = selectedDate,
                                    minDate = minDate,
                                    maxDate = maxDate,
                                    onDateSelected = onDateSelected
                                )
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f)) {}
                        }
                    }
                }
            }
        }
    }
}

/**
 * 日历日期项组件
 */
@Composable
fun CalendarDayItem(
    day: Int,
    currentYear: Int,
    currentMonth: Int,
    selectedDate: Date,
    minDate: Date,
    maxDate: Date?,
    onDateSelected: (Date) -> Unit
) {
    val modifier = Modifier
        .size(48.dp)
    
    if (day == 0) {
        // 空白日期
        Box(modifier = modifier)
    } else {
        val dateCalendar = Calendar.getInstance()
        dateCalendar.set(currentYear, currentMonth, day, 0, 0, 0)
        dateCalendar.set(Calendar.MILLISECOND, 0)
        val date = dateCalendar.time
        
        // 检查日期是否可选
        val isSelectable = date.after(minDate) || date.equals(minDate)
        val isAfterMaxDate = maxDate != null && date.after(maxDate)
        val isToday = isSameDay(date, Date())
        val isSelected = isSameDay(date, selectedDate)
        
        Box(
            modifier = modifier
                .clickable {
                    if (isSelectable && !isAfterMaxDate) {
                        onDateSelected(date)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    !isSelectable || isAfterMaxDate -> MaterialTheme.colorScheme.tertiary
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onSecondary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

/**
 * 检查两个日期是否是同一天
 */
private fun isSameDay(date1: Date, date2: Date): Boolean {
    val calendar1 = Calendar.getInstance()
    calendar1.time = date1
    val calendar2 = Calendar.getInstance()
    calendar2.time = date2
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
            calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)
}

/**
 * 时钟选择Tab页面
 * 功能：显示转盘式时钟，允许用户选择时间
 */
@Composable
fun ClockTab(
    selectedHour: Int,
    selectedMinute: Int,
    minDate: Date,
    maxDate: Date?,
    onTimeSelected: (Int, Int) -> Unit
) {
    val hour = remember { mutableStateOf(selectedHour) }
    val minute = remember { mutableStateOf(selectedMinute) }
    val isHourSelection = remember { mutableStateOf(true) } // true: 选择小时，false: 选择分钟
    
    // 常用时间选项
    val commonTimes = listOf(
        Pair(6, 0), // 06:00
        Pair(8, 0), // 08:00
        Pair(9, 0), // 09:00
        Pair(12, 0), // 12:00
        Pair(14, 0), // 14:00
        Pair(15, 0), // 15:00
        Pair(18, 0), // 18:00
        Pair(20, 0), // 20:00
        Pair(22, 0)  // 22:00
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 时间显示
        Text(
            text = String.format("%02d:%02d", hour.value, minute.value),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // 选择模式切换
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { isHourSelection.value = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHourSelection.value) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            ) {
                Text(text = "小时")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { isHourSelection.value = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isHourSelection.value) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            ) {
                Text(text = "分钟")
            }
        }
        
        // 时钟转盘
        Box(
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 24.dp)
        ) {
            // 绘制时钟背景
            val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = size
                val centerX = canvasSize.width / 2
                val centerY = canvasSize.height / 2
                val radius = canvasSize.width / 2 - 20
                
                // 绘制外圈
                drawCircle(
                    color = surfaceVariantColor,
                    radius = radius,
                    center = Offset(centerX, centerY)
                )
                
                // 绘制刻度
                val steps = if (isHourSelection.value) 12 else 60
                val stepAngle = 360f / steps
                
                for (i in 0 until steps) {
                    val angle = Math.toRadians((i * stepAngle - 90).toDouble())
                    val startX = centerX + Math.cos(angle) * (radius - 10)
                    val startY = centerY + Math.sin(angle) * (radius - 10)
                    val endX = centerX + Math.cos(angle) * radius
                    val endY = centerY + Math.sin(angle) * radius
                    
                    val isMajorTick = if (isHourSelection.value) {
                        true
                    } else {
                        i % 5 == 0
                    }
                    
                    drawLine(
                        color = if (isMajorTick) {
                            primaryColor
                        } else {
                            secondaryColor
                        },
                        start = Offset(startX.toFloat(), startY.toFloat()),
                        end = Offset(endX.toFloat(), endY.toFloat()),
                        strokeWidth = if (isMajorTick) 3f else 1f
                    )
                    
                    // 绘制数字
                    if (isMajorTick) {
                        val value = if (isHourSelection.value) {
                            if (i == 0) 12 else i
                        } else {
                            i
                        }
                        
                        val textX = centerX + Math.cos(angle) * (radius - 25)
                        val textY = centerY + Math.sin(angle) * (radius - 25)
                        
                        // 绘制数字
                        // 简化处理，暂时不绘制数字
                    }
                }
                
                // 绘制选中的指针
                val selectedValue = if (isHourSelection.value) hour.value % 12 else minute.value
                val selectedAngle = Math.toRadians((selectedValue * stepAngle - 90).toDouble())
                val pointerLength = radius - 30
                val pointerX = centerX + Math.cos(selectedAngle) * pointerLength
                val pointerY = centerY + Math.sin(selectedAngle) * pointerLength
                
                drawLine(
                    color = primaryColor,
                    start = Offset(centerX, centerY),
                    end = Offset(pointerX.toFloat(), pointerY.toFloat()),
                    strokeWidth = 4f
                )
                
                // 绘制中心圆点
                drawCircle(
                    color = primaryColor,
                    radius = 8f,
                    center = Offset(centerX, centerY)
                )
            }
            
            // 触摸检测
            val density = LocalDensity.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val centerX = with(density) { 250.dp.toPx() / 2 }
                            val centerY = with(density) { 250.dp.toPx() / 2 }
                            val dx = offset.x - centerX
                            val dy = offset.y - centerY
                            val angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())) + 90
                            val normalizedAngle = if (angle < 0) angle + 360 else angle
                            
                            if (isHourSelection.value) {
                                // Calculate hour in 12-hour format first
                                val newHour12 = ((normalizedAngle / 30).toInt() + 1) % 12
                                val displayHour = if (newHour12 == 0) 12 else newHour12
                                
                                // Convert to 24-hour format based on current selection
                                // If current hour is PM (13-23) and new hour is AM (1-11), keep as PM
                                val newHour24 = if (hour.value >= 12 && displayHour < 12) {
                                    displayHour + 12
                                } else if (hour.value < 12 && displayHour == 12) {
                                    12 // Noon
                                } else if (hour.value >= 12 && displayHour == 12) {
                                    0 // Midnight
                                } else {
                                    displayHour
                                }
                                
                                hour.value = newHour24
                                onTimeSelected(hour.value, minute.value)
                            } else {
                                val newMinute = (normalizedAngle / 6).toInt()
                                minute.value = newMinute
                                onTimeSelected(hour.value, minute.value)
                            }
                        }
                    }
            )
        }
        
        // 常用时间快速选择
        Text(
            text = "常用时间",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            commonTimes.forEach { (h, m) ->
                Button(
                    onClick = {
                        hour.value = h
                        minute.value = m
                        onTimeSelected(h, m)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hour.value == h && minute.value == m) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    ),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(text = String.format("%02d:%02d", h, m))
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
    val context = LocalContext.current

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
    val reminderRepository = remember { ReminderRepository(databaseService, context) }
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
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
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
                                // 检查是否有精确闹钟权限
                                if (!PermissionService.hasExactAlarmPermission(context)) {
                                    PermissionService.openExactAlarmPermissionSettings(context)
                                    errorMessage = "请授予精确闹钟权限，否则提醒可能不会响铃"
                                    return@Button
                                }
                                
                                // 检查是否有通知权限
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val notificationManager = context.getSystemService(NotificationManager::class.java)
                                    if (!notificationManager.areNotificationsEnabled()) {
                                        PermissionService.openNotificationPermissionSettings(context)
                                        errorMessage = "请授予通知权限，否则提醒可能不会显示"
                                        return@Button
                                    }
                                }
                                
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
                                
                                Log.d("CreateReminderScreen", "Creating reminder with title: $title")
                                Log.d("CreateReminderScreen", "Event time: $eventTime")
                                Log.d("CreateReminderScreen", "Alert times count: ${finalAlertTimes.size}")
                                
                                finalAlertTimes.forEachIndexed { index, time ->
                                    Log.d("CreateReminderScreen", "Alert time ${index + 1}: ${timeFormat.format(time)}")
                                }
                                
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

                                Log.d("CreateReminderScreen", "Calling createReminder")
                                reminderViewModel.createReminder(reminder)
                                Log.d("CreateReminderScreen", "createReminder called successfully")
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
