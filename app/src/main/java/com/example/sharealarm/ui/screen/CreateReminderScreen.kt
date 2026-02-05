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
import com.example.sharealarm.data.model.Group
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
fun CreateReminderScreen(navController: NavController, reminderId: String? = null) {
    val context = LocalContext.current
    val currentUser by MockDataStore.currentUser.collectAsState()
    
    // 表单状态
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

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

    // 状态：提交中（防抖与防重复）
    var isSubmitting by remember { mutableStateOf(false) }
    // 状态：编辑确认弹窗
    var showEditConfirmDialog by remember { mutableStateOf(false) }
    
    // 焦点控制
    val focusRequester = remember { FocusRequester() }
    
    // 自动聚焦 (仅在新建时)
    LaunchedEffect(Unit) {
        if (reminderId == null) {
            try {
                kotlinx.coroutines.delay(100) // 缩短延迟
                focusRequester.requestFocus()
            } catch (e: Exception) {
                // 忽略焦点请求异常
            }
        }
    }
    
    // 加载已有数据
    LaunchedEffect(reminderId) {
        if (reminderId != null) {
            val reminder = MockDataStore.getReminderById(reminderId)
            if (reminder != null) {
                title = reminder.title
                description = reminder.description
                eventDate = reminder.eventTime
                
                // 恢复参与者
                val participants = reminder.participants.mapNotNull { MockDataStore.getUserById(it) }
                selectedParticipants = participants.filter { it.id != currentUser.id } // 排除自己
                
                // 恢复提醒时间
                alertOffsets.clear()
                val offsets = reminder.alertTimes.map { 
                    ((reminder.eventTime.time - it.time) / (1000 * 60)).toInt()
                }
                alertOffsets.addAll(offsets)
            }
        }
    }

    // 执行保存
    fun performSave() {
        isSubmitting = true

        if (reminderId != null) {
             MockDataStore.updateReminder(
                id = reminderId,
                title = title,
                time = eventDate,
                note = description,
                participantIds = selectedParticipants.map { it.id } + currentUser.id,
                alertOffsets = alertOffsets.toList()
            )
             // 重新设置闹钟
             val reminder = MockDataStore.getReminderById(reminderId)
             if (reminder != null) {
                 AlarmScheduler.scheduleMultipleAlarms(context, reminder.id, reminder.alertTimes)
             }
        } else {
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
        }
        
        navController.popBackStack()
    }

    // 提交处理
    fun handleSubmit() {
        if (isSubmitting) return
        
        if (title.isBlank()) {
            errorMessage = "请输入提醒内容"
            return
        }
        
        if (reminderId != null) {
            showEditConfirmDialog = true
        } else {
            performSave()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("添加提醒", fontWeight = FontWeight.Normal, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { handleSubmit() },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("完成")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                        placeholder = { Text("请输入提醒内容", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester), // 绑定焦点请求器
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                FormSection(title = "事件时间 *") {
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
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(eventDate),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(eventDate),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    title = "响铃提醒",
                    action = {
                        Box {
                            IconButton(
                                onClick = { showAlertMenu = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "添加提醒时间",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // 下拉菜单
                            DropdownMenu(
                                expanded = showAlertMenu,
                                onDismissRequest = { showAlertMenu = false },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface)
                                    .width(200.dp)
                            ) {
                                // 08:00 是一个特殊逻辑，这里我们暂时用 -1 表示，后续逻辑处理
                                // 但为了简单起见，我们可以计算距离最近的当日8:00的偏移量，但这会随eventDate变化而变化
                                // 所以更好的方式是把“添加”逻辑拆分开：
                                // 这里我们只展示固定的分钟数偏移量选项
                                
                                val options = listOf(5, 30, 60, 120)
                                val optionLabels = mapOf(
                                    5 to "提前 5 分钟",
                                    30 to "提前 30 分钟", 
                                    60 to "提前 1 小时", 
                                    120 to "提前 2 小时"
                                )
                                
                                options.forEach { minutes ->
                                    if (!alertOffsets.contains(minutes)) {
                                        DropdownMenuItem(
                                            text = { Text(optionLabels[minutes]!!, color = MaterialTheme.colorScheme.onSurface) },
                                            onClick = { 
                                                alertOffsets.add(minutes)
                                                alertOffsets.sort() // 保持顺序
                                                showAlertMenu = false
                                            }
                                        )
                                    }
                                }
                                
                                // 当日 08:00 选项
                                // 计算当日 08:00 距离 eventDate 的偏移量
                                val today8am = Calendar.getInstance().apply {
                                    time = eventDate
                                    set(Calendar.HOUR_OF_DAY, 8)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                
                                // 如果事件时间本身就是8点，或者今天已经过了8点且事件在今天，这个选项可能需要调整逻辑
                                // 这里简化为：如果计算出的偏移量 > 0 (即8点在事件时间之前)，则显示
                                val diffMillis8am = eventDate.time - today8am.timeInMillis
                                val offset8am = (diffMillis8am / (1000 * 60)).toInt()
                                
                                if (offset8am > 0 && !alertOffsets.contains(offset8am)) {
                                     DropdownMenuItem(
                                        text = { Text("当日 08:00", color = MaterialTheme.colorScheme.onSurface) },
                                        onClick = { 
                                            alertOffsets.add(offset8am)
                                            alertOffsets.sort()
                                            showAlertMenu = false
                                        }
                                    )
                                }
                                
                                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                DropdownMenuItem(
                                    text = { Text("自定义时间...", color = MaterialTheme.colorScheme.onSurface) },
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
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column {
                            // 引入一个临时状态来记录当前正在编辑的索引，-1表示没有编辑
                            var editingIndex by remember { mutableStateOf(-1) }
                            // 编辑菜单的显示位置需要更精细的控制，或者简单点，直接在点击项下方显示Dropdown
                            // 这里我们利用Box和DropdownMenu来实现点击项弹出菜单
                            
                            alertOffsets.forEachIndexed { index, minutes ->
                                if (index > 0) Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                                
                                Box {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { editingIndex = index } // 点击进入编辑模式（弹出菜单）
                                            .padding(16.dp)
                                    ) {
                                        // 统一显示为绝对时间格式
                                        val alertTime = Date(eventDate.time - minutes * 60 * 1000L)
                                        val dateFormat = SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA)
                                        val alertText = dateFormat.format(alertTime)
                                        Text(
                                            text = alertText,
                                            fontSize = 16.sp,
                                            modifier = Modifier.weight(1f),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        
                                        // 删除按钮
                                        IconButton(
                                            onClick = { alertOffsets.remove(minutes) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "删除",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // 编辑用的下拉菜单
                                    DropdownMenu(
                                        expanded = editingIndex == index,
                                        onDismissRequest = { editingIndex = -1 },
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surface)
                                            .width(200.dp)
                                    ) {
                                        val options = listOf(5, 15, 30, 60, 120)
                                        val optionLabels = mapOf(
                                            5 to "提前 5 分钟",
                                            15 to "提前 15 分钟",
                                            30 to "提前 30 分钟", 
                                            60 to "提前 1 小时", 
                                            120 to "提前 2 小时"
                                        )
                                        
                                        options.forEach { optMinutes ->
                                            DropdownMenuItem(
                                                text = { Text(optionLabels[optMinutes]!!, color = MaterialTheme.colorScheme.onSurface) },
                                                onClick = { 
                                                    // 更新偏移量
                                                    alertOffsets[index] = optMinutes
                                                    alertOffsets.sort() // 重新排序
                                                    editingIndex = -1
                                                },
                                                trailingIcon = if (optMinutes == minutes) {
                                                    { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                                } else null
                                            )
                                        }
                                        
                                        // 当日 08:00 选项
                                        val today8am = Calendar.getInstance().apply {
                                            time = eventDate
                                            set(Calendar.HOUR_OF_DAY, 8)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        val diffMillis8am = eventDate.time - today8am.timeInMillis
                                        val offset8am = (diffMillis8am / (1000 * 60)).toInt()
                                        
                                        if (offset8am > 0) {
                                             DropdownMenuItem(
                                                text = { Text("当日 08:00", color = MaterialTheme.colorScheme.onSurface) },
                                                onClick = { 
                                                    alertOffsets[index] = offset8am
                                                    alertOffsets.sort()
                                                    editingIndex = -1
                                                },
                                                trailingIcon = if (offset8am == minutes) {
                                                    { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                                } else null
                                            )
                                        }
                                        
                                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                        DropdownMenuItem(
                                            text = { Text("自定义时间...", color = MaterialTheme.colorScheme.onSurface) },
                                            onClick = { 
                                                editingIndex = -1
                                                // 这里的逻辑稍微复杂，因为我们要“修改”当前的条目
                                                // 但实际上自定义时间弹窗是添加逻辑。
                                                // 为了复用，我们可以先移除当前项，然后走添加流程？
                                                // 或者引入一个“正在编辑的offset”状态？
                                                // 简单起见：移除当前项，打开自定义时间选择器
                                                alertOffsets.removeAt(index)
                                                tempCustomDate = Date(eventDate.time - minutes * 60 * 1000L) // 默认选中当前这个时间
                                                showCustomDatePicker = true
                                            }
                                        )
                                    }
                                }
                            }
                            
                            if (alertOffsets.isEmpty()) {
                                Text(
                                    text = "暂无提醒",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // 提醒对象
            item {
                FormSection(title = "提醒对象") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showParticipantDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = if (selectedParticipants.isEmpty()) "选择提醒对象" else "已选 ${selectedParticipants.size} 人",
                            modifier = Modifier.padding(16.dp),
                            color = if (selectedParticipants.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 4. 备注
            item {
                FormSection(title = "备注 (选填)") {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("添加备注信息...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                        color = MaterialTheme.colorScheme.surfaceVariant, // 灰色背景表示只读
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = currentUser.name,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 底部留白
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    // 编辑确认弹窗
    if (showEditConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showEditConfirmDialog = false },
            title = { Text("确认保存") },
            text = { Text("修改后保存，所有人的提醒都会改变，是否继续？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEditConfirmDialog = false
                        performSave()
                    }
                ) {
                    Text("确定", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditConfirmDialog = false }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                containerColor = MaterialTheme.colorScheme.surface,
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
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        showModeToggle = false,
                        colors = DatePickerDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                            todayDateBorderColor = MaterialTheme.colorScheme.primary,
                            todayContentColor = MaterialTheme.colorScheme.primary,
                            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                            dayContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationContentColor = MaterialTheme.colorScheme.onSurface,
                            dividerColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCustomDatePicker = false }) {
                            Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
                containerColor = MaterialTheme.colorScheme.surface,
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
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                            clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                            selectorColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            periodSelectorBorderColor = MaterialTheme.colorScheme.primary,
                            periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            periodSelectorSelectedContentColor = MaterialTheme.colorScheme.primary,
                            timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            timeSelectorSelectedContentColor = MaterialTheme.colorScheme.primary,
                            timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCustomTimePicker = false }) {
                            Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
                containerColor = MaterialTheme.colorScheme.surface,
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
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        showModeToggle = false,
                        colors = DatePickerDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                            todayDateBorderColor = MaterialTheme.colorScheme.primary,
                            todayContentColor = MaterialTheme.colorScheme.primary,
                            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                            dayContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationContentColor = MaterialTheme.colorScheme.onSurface,
                            dividerColor = MaterialTheme.colorScheme.outlineVariant
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
                            Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
                containerColor = MaterialTheme.colorScheme.surface,
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
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                            clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                            selectorColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            periodSelectorBorderColor = MaterialTheme.colorScheme.primary,
                            periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            periodSelectorSelectedContentColor = MaterialTheme.colorScheme.primary,
                            timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            timeSelectorSelectedContentColor = MaterialTheme.colorScheme.primary,
                            timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
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
                            Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
    val groups by MockDataStore.groups.collectAsState()
    val tags by MockDataStore.tags.collectAsState()
    
    val selected = remember { mutableStateListOf<User>().apply { addAll(initialSelection) } }
    
    // 筛选模式
    var filterMode by remember { mutableStateOf(0) } // 0: 全部, 1: 标签, 2: 群组
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var selectedGroup by remember { mutableStateOf<Group?>(null) }
    
    // 计算当前显示的联系人列表
    val displayedContacts = remember(contacts, filterMode, selectedTag, selectedGroup) {
        when (filterMode) {
            1 -> if (selectedTag != null) contacts.filter { it.tags.contains(selectedTag) } else emptyList()
            2 -> if (selectedGroup != null) contacts.filter { selectedGroup!!.memberIds.contains(it.id) } else emptyList()
            else -> contacts
        }
    }
    
    // 全选/全不选逻辑
    val isAllSelected = displayedContacts.isNotEmpty() && displayedContacts.all { contact -> selected.any { it.id == contact.id } }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
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
                    Text(text = "选择提醒对象", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = "已选 ${selected.size} 人", 
                        fontSize = 14.sp, 
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                
                // 筛选栏
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 顶部 Tab
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        listOf("全部", "标签", "群组").forEachIndexed { index, title ->
                            Column(
                                modifier = Modifier.clickable { 
                                    filterMode = index 
                                    // 重置子选项
                                    if (index == 1 && tags.isNotEmpty()) selectedTag = tags.first()
                                    if (index == 2 && groups.isNotEmpty()) selectedGroup = groups.first()
                                },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = title,
                                    color = if (filterMode == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (filterMode == index) FontWeight.Bold else FontWeight.Normal
                                )
                                if (filterMode == index) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(width = 20.dp, height = 2.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                    
                    // 子筛选器 (标签/群组 Chip)
                    if (filterMode == 1) {
                        androidx.compose.foundation.lazy.LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tags) { tag: String ->
                                FilterChip(
                                    selected = selectedTag == tag,
                                    onClick = { selectedTag = tag },
                                    label = { Text(text = tag) }
                                )
                            }
                        }
                    } else if (filterMode == 2) {
                        androidx.compose.foundation.lazy.LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(groups) { group: Group ->
                                FilterChip(
                                    selected = selectedGroup?.id == group.id,
                                    onClick = { selectedGroup = group },
                                    label = { Text(text = group.name) }
                                )
                            }
                        }
                    }
                    
                    // 全选栏
                    if (filterMode != 0 || displayedContacts.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    if (isAllSelected) {
                                        // 全不选 (只移除当前显示的)
                                        displayedContacts.forEach { contact ->
                                            selected.removeAll { it.id == contact.id }
                                        }
                                    } else {
                                        // 全选 (添加当前显示且未选中的)
                                        displayedContacts.forEach { contact ->
                                            if (selected.none { it.id == contact.id }) {
                                                selected.add(contact)
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text(if (isAllSelected) "取消全选" else "全选")
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // 列表
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    if (displayedContacts.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("暂无联系人", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(displayedContacts) { user ->
                            val isSelected = selected.any { it.id == user.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            selected.removeAll { it.id == user.id }
                                        } else {
                                            selected.add(user)
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Checkbox
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                        .border(
                                            width = 1.dp, 
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
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
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = user.name.first().toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(text = user.name, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                    // 显示标签 (可选)
                                    if (user.tags.isNotEmpty()) {
                                        Text(
                                            text = user.tags.joinToString(", "),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                
                // 按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selected) },
                        colors = ButtonDefaults.buttonColors(containerColor = BellOrange)
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}
