package com.example.sharealarm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.sharealarm.data.local.MockDataStore
import com.example.sharealarm.data.model.User
import com.example.sharealarm.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration

import java.util.TimeZone
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

import com.example.sharealarm.service.AlarmScheduler

@Composable
fun ProvideChineseLocale(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val newConfiguration = android.content.res.Configuration(configuration).apply {
        setLocale(Locale.CHINA)
    }
    val context = LocalContext.current
    val newContext = remember(context) {
        context.createConfigurationContext(newConfiguration)
    }
    
    // 临时修改默认 Locale，确保 DatePicker 内部组件 (如 WeekDays) 正确加载资源
    val currentLocale = remember { Locale.getDefault() }
    SideEffect {
        Locale.setDefault(Locale.CHINA)
    }
    DisposableEffect(Unit) {
        onDispose {
            Locale.setDefault(currentLocale)
        }
    }
    
    CompositionLocalProvider(
        LocalConfiguration provides newConfiguration,
        LocalContext provides newContext
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderScreen(navController: NavController) {
    val context = LocalContext.current
    val currentUser by MockDataStore.currentUser.collectAsState()
    
    // 表单状态
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // 焦点控制
    val focusRequester = remember { FocusRequester() }
    
    // 自动聚焦
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    // 时间处理
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MINUTE, 15) // 默认 +15分钟
    var eventDate by remember { mutableStateOf(calendar.time) }
    
    // 提醒时间偏移量列表 (分钟)，默认15分钟
    var alertOffsets = remember { mutableStateListOf(15) }
    
    // 参与者
    var showParticipantDialog by remember { mutableStateOf(false) }
    var selectedParticipants by remember { mutableStateOf(listOf<User>()) }
    
    // 错误信息
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 状态：是否显示日期/时间选择器
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // 状态：飞铃时间菜单
    var showAlertMenu by remember { mutableStateOf(false) }
    // 状态：自定义提醒的日期时间选择
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var showCustomTimePicker by remember { mutableStateOf(false) }
    var tempCustomDate by remember { mutableStateOf(Date()) }

    // 提交处理
    fun handleSubmit() {
        if (title.isBlank()) {
            errorMessage = "请输入提醒内容"
            return
        }
        
        // 创建提醒
        val newReminder = MockDataStore.addReminder(
            title = title,
            time = eventDate,
            note = description,
            participantIds = selectedParticipants.map { it.id } + currentUser.id,
            alertOffsets = alertOffsets.toList()
        )
        
        // 设置系统闹钟
        AlarmScheduler.scheduleMultipleAlarms(context, newReminder.id, newReminder.alertTimes)
        
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("添加提醒", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { handleSubmit() },
                        colors = ButtonDefaults.buttonColors(containerColor = LinkBlue),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("完成")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF2F2F7) // iOS 背景灰
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. 提醒内容卡片
            item {
                FormSection(title = "提醒内容 *") {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { 
                            title = it
                            errorMessage = null 
                        },
                        placeholder = { Text("请输入提醒内容", color = Color.LightGray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester), // 绑定焦点请求器
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFE5E5EA),
                            focusedBorderColor = LinkBlue
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = errorMessage != null
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = ErrorRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            }

            // 2. 日期时间卡片
            item {
                FormSection(title = "日期与时间 *") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 日期选择 (模拟点击)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5EA))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(eventDate),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 16.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // 时间选择 (模拟点击)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable { showTimePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5EA))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(eventDate),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 16.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. 提前提醒
            item {
                FormSection(
                    title = "飞铃时间",
                    action = {
                        Box {
                            IconButton(
                                onClick = { showAlertMenu = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "添加提醒时间",
                                    tint = LinkBlue
                                )
                            }
                            
                            // 下拉菜单
                            DropdownMenu(
                                expanded = showAlertMenu,
                                onDismissRequest = { showAlertMenu = false },
                                modifier = Modifier
                                    .background(Color.White)
                                    .width(200.dp)
                            ) {
                                val options = listOf(30, 60, 120)
                                val optionLabels = mapOf(30 to "提前 30 分钟", 60 to "提前 1 小时", 120 to "提前 2 小时")
                                
                                options.forEach { minutes ->
                                    if (!alertOffsets.contains(minutes)) {
                                        DropdownMenuItem(
                                            text = { Text(optionLabels[minutes]!!) },
                                            onClick = { 
                                                alertOffsets.add(minutes)
                                                alertOffsets.sort() // 保持顺序
                                                showAlertMenu = false
                                            }
                                        )
                                    }
                                }
                                
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("自定义时间...") },
                                    onClick = { 
                                        showAlertMenu = false
                                        // 初始化为当前事件时间，方便调整
                                        tempCustomDate = eventDate 
                                        showCustomDatePicker = true
                                    }
                                )
                            }
                        }
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5EA))
                    ) {
                        Column {
                            alertOffsets.forEachIndexed { index, minutes ->
                                if (index > 0) Divider(color = Color(0xFFF2F2F7), thickness = 1.dp)
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // 统一显示为绝对时间格式
                                    val alertTime = Date(eventDate.time - minutes * 60 * 1000L)
                                    val dateFormat = SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA)
                                    val alertText = dateFormat.format(alertTime)
                                    Text(
                                        text = alertText,
                                        fontSize = 16.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    // 删除按钮
                                    IconButton(
                                        onClick = { alertOffsets.remove(minutes) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "删除",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            
                            if (alertOffsets.isEmpty()) {
                                Text(
                                    text = "暂无提醒",
                                    color = Color.Gray,
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // 4. 备注
            item {
                FormSection(title = "备注 (选填)") {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("添加备注信息...", color = Color.LightGray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFE5E5EA),
                            focusedBorderColor = LinkBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 5. 创建人 (只读)
            item {
                FormSection(title = "创建人") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF5F5F5), // 灰色背景表示只读
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5EA))
                    ) {
                        Text(
                            text = currentUser.name,
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray
                        )
                    }
                }
            }

            // 6. 提醒对象
            item {
                FormSection(title = "提醒对象") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showParticipantDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5EA))
                    ) {
                        Text(
                            text = if (selectedParticipants.isEmpty()) "选择提醒对象" else "已选 ${selectedParticipants.size} 人",
                            modifier = Modifier.padding(16.dp),
                            color = if (selectedParticipants.isEmpty()) Color.LightGray else Color.Black
                        )
                    }
                }
            }
            
            // 底部留白
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    // 参与者选择弹窗
    if (showParticipantDialog) {
        ParticipantSelectionDialog(
            initialSelection = selectedParticipants,
            onDismiss = { showParticipantDialog = false },
            onConfirm = { 
                selectedParticipants = it
                showParticipantDialog = false
            }
        )
    }

    // 自定义提醒 - 日期选择器
    if (showCustomDatePicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        ProvideChineseLocale {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = tempCustomDate.time
            )
            
            ModalBottomSheet(
                onDismissRequest = { showCustomDatePicker = false },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DatePicker(
                        state = datePickerState,
                        title = {
                            Text(
                                text = "选择提醒日期",
                                modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        headline = {
                            Row(modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp)) {
                                val dateText = datePickerState.selectedDateMillis?.let { millis ->
                                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                    cal.timeInMillis = millis
                                    "${cal.get(Calendar.YEAR)}年${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日"
                                } ?: "请选择日期"
                                
                                Text(
                                    text = dateText,
                                    fontSize = 32.sp,
                                    lineHeight = 40.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        },
                        showModeToggle = false,
                        colors = DatePickerDefaults.colors(
                            containerColor = Color.White,
                            selectedDayContainerColor = LinkBlue,
                            todayDateBorderColor = LinkBlue,
                            todayContentColor = LinkBlue,
                            weekdayContentColor = Color.Gray,
                            selectedDayContentColor = Color.White
                        )
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCustomDatePicker = false }) {
                            Text("取消", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    val cal = Calendar.getInstance()
                                    cal.time = tempCustomDate
                                    val newCal = Calendar.getInstance().apply { timeInMillis = it }
                                    cal.set(Calendar.YEAR, newCal.get(Calendar.YEAR))
                                    cal.set(Calendar.MONTH, newCal.get(Calendar.MONTH))
                                    cal.set(Calendar.DAY_OF_MONTH, newCal.get(Calendar.DAY_OF_MONTH))
                                    tempCustomDate = cal.time
                                }
                                showCustomDatePicker = false
                                showCustomTimePicker = true // 下一步：选择时间
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LinkBlue)
                        ) {
                            Text("下一步")
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // 自定义提醒 - 时间选择器
    if (showCustomTimePicker) {
        val cal = Calendar.getInstance().apply { time = tempCustomDate }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ProvideChineseLocale {
            ModalBottomSheet(
                onDismissRequest = { showCustomTimePicker = false },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "选择提醒时间",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color(0xFFF2F2F7),
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = Color.Black,
                            selectorColor = LinkBlue,
                            containerColor = Color.White,
                            periodSelectorBorderColor = LinkBlue,
                            periodSelectorSelectedContainerColor = LinkBlue.copy(alpha = 0.2f),
                            periodSelectorSelectedContentColor = LinkBlue,
                            timeSelectorSelectedContainerColor = LinkBlue.copy(alpha = 0.2f),
                            timeSelectorSelectedContentColor = LinkBlue,
                            timeSelectorUnselectedContainerColor = Color(0xFFF2F2F7),
                            timeSelectorUnselectedContentColor = Color.Black
                        )
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCustomTimePicker = false }) {
                            Text("取消", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val newCal = Calendar.getInstance().apply { time = tempCustomDate }
                                newCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                newCal.set(Calendar.MINUTE, timePickerState.minute)
                                tempCustomDate = newCal.time
                                
                                // 计算偏移量 (忽略秒和毫秒差异)
                                val eventCal = Calendar.getInstance().apply { 
                                    time = eventDate 
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                val customCal = Calendar.getInstance().apply { 
                                    time = tempCustomDate 
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                
                                val diffMillis = eventCal.timeInMillis - customCal.timeInMillis
                                
                                // 计算偏移量 (允许负数，即延后提醒)
                                val minutes = (diffMillis / (1000 * 60)).toInt()
                                
                                alertOffsets.add(minutes)
                                alertOffsets.sort() // 保持顺序
                                
                                showCustomTimePicker = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LinkBlue)
                        ) {
                            Text("确定")
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // 日期选择器 (底部弹窗)
    if (showDatePicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        ProvideChineseLocale {
            // 在新的 Locale 环境中初始化 datePickerState
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = eventDate.time
            )
            
            ModalBottomSheet(
                onDismissRequest = { showDatePicker = false },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DatePicker(
                        state = datePickerState,
                        title = {
                            Text(
                                text = "选择日期",
                                modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        headline = {
                            Row(modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp)) {
                                val dateText = datePickerState.selectedDateMillis?.let { millis ->
                                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                    cal.timeInMillis = millis
                                    "${cal.get(Calendar.YEAR)}年${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日"
                                } ?: "请选择日期"
                                
                                Text(
                                    text = dateText,
                                    fontSize = 32.sp,
                                    lineHeight = 40.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        },
                        showModeToggle = false,
                        colors = DatePickerDefaults.colors(
                            containerColor = Color.White,
                            selectedDayContainerColor = LinkBlue,
                            todayDateBorderColor = LinkBlue,
                            todayContentColor = LinkBlue,
                            weekdayContentColor = Color.Gray,
                            selectedDayContentColor = Color.White
                        )
                    )
                    
                    // 底部确认按钮栏
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("取消", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    val cal = Calendar.getInstance()
                                    cal.time = eventDate
                                    val newCal = Calendar.getInstance().apply { timeInMillis = it }
                                    cal.set(Calendar.YEAR, newCal.get(Calendar.YEAR))
                                    cal.set(Calendar.MONTH, newCal.get(Calendar.MONTH))
                                    cal.set(Calendar.DAY_OF_MONTH, newCal.get(Calendar.DAY_OF_MONTH))
                                    eventDate = cal.time
                                }
                                showDatePicker = false
                                showTimePicker = true // 自动弹出时间选择器
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LinkBlue)
                        ) {
                            Text("确定")
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp)) // 底部安全区
                }
            }
        }
    }

    // 时间选择器 (底部弹窗)
    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { time = eventDate }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ProvideChineseLocale {
            ModalBottomSheet(
                onDismissRequest = { showTimePicker = false },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "选择时间",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color(0xFFF2F2F7),
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = Color.Black,
                            selectorColor = LinkBlue,
                            containerColor = Color.White,
                            periodSelectorBorderColor = LinkBlue,
                            periodSelectorSelectedContainerColor = LinkBlue.copy(alpha = 0.2f),
                            periodSelectorSelectedContentColor = LinkBlue,
                            timeSelectorSelectedContainerColor = LinkBlue.copy(alpha = 0.2f),
                            timeSelectorSelectedContentColor = LinkBlue,
                            timeSelectorUnselectedContainerColor = Color(0xFFF2F2F7),
                            timeSelectorUnselectedContentColor = Color.Black
                        )
                    )
                    
                    // 底部确认按钮栏
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("取消", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val newCal = Calendar.getInstance().apply { time = eventDate }
                                newCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                newCal.set(Calendar.MINUTE, timePickerState.minute)
                                eventDate = newCal.time
                                showTimePicker = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LinkBlue)
                        ) {
                            Text("确定")
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun FormSection(title: String, action: (@Composable () -> Unit)? = null, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray
            )
            action?.invoke()
        }
        content()
    }
}

@Composable
fun ParticipantSelectionDialog(
    initialSelection: List<User>,
    onDismiss: () -> Unit,
    onConfirm: (List<User>) -> Unit
) {
    val contacts by MockDataStore.contacts.collectAsState()
    val selected = remember { mutableStateListOf<User>().apply { addAll(initialSelection) } }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 标题
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "选择提醒对象", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "已选 ${selected.size} 人", 
                        fontSize = 14.sp, 
                        color = LinkBlue
                    )
                }
                
                Divider(color = Color(0xFFEEEEEE))
                
                // 列表
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(contacts) { user ->
                        val isSelected = selected.contains(user)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selected.remove(user) else selected.add(user)
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) LinkBlue else Color.White)
                                    .border(
                                        width = 1.dp, 
                                        color = if (isSelected) LinkBlue else Color.LightGray,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // 头像
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = user.name.first().toString())
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(text = user.name, fontSize = 16.sp)
                        }
                    }
                }
                
                Divider(color = Color(0xFFEEEEEE))
                
                // 按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selected) },
                        colors = ButtonDefaults.buttonColors(containerColor = LinkBlue)
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}
