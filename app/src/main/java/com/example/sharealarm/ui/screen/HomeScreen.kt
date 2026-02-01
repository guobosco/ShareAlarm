package com.example.sharealarm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sharealarm.R
import com.example.sharealarm.data.local.MockDataStore
import com.example.sharealarm.ui.navigation.Screen
import com.example.sharealarm.ui.theme.ShareAlarmTheme
import com.example.sharealarm.ui.theme.*
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch

// 模拟数据模型 (UI 层)
data class MockEvent(
    val id: String,
    val title: String,
    val timeStr: String,
    val creator: String,
    val dateStr: String,
    val isExpired: Boolean = false,
    val isRead: Boolean = false,
    val isMine: Boolean = false // 新增字段
)

// 视图模型项
sealed class TimelineItem {
    data class Header(val date: String, val isExpired: Boolean = false) : TimelineItem()
    data class Event(val event: MockEvent) : TimelineItem()
    data class ExpiredHeader(val count: Int, val isExpanded: Boolean) : TimelineItem()
}

@Composable
fun HomeScreen(navController: NavController) {
    // 观察模拟数据
    val currentUser by MockDataStore.currentUser.collectAsState()
    val allReminders by MockDataStore.reminders.collectAsState()

    // 状态：是否展开过期事件
    var isExpiredExpanded by remember { mutableStateOf(false) }
    
    // 权限检测相关
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    // 检查权限函数
    fun checkPermissions(): Boolean {
        // Android 12+ (S) 需要精确闹钟权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(android.app.AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == false) return false
        }
        // Android 13+ (T) 需要通知权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
             if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                 return false
             }
        }
        return true
    }

    // 启动时检查权限
    LaunchedEffect(Unit) {
        if (!checkPermissions()) {
            showPermissionDialog = true
        }
    }
    
    // 权限提示对话框
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("需要权限", fontWeight = FontWeight.Bold) },
            text = { Text("为了准时提醒您，飞铃需要“通知”和“闹钟”权限。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        // 跳转逻辑
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                             val alarmManager = context.getSystemService(android.app.AlarmManager::class.java)
                             // 优先跳转精确闹钟设置
                             if (alarmManager?.canScheduleExactAlarms() == false) {
                                 val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                 context.startActivity(intent)
                             } else {
                                 // 跳转应用详情页 (处理通知权限)
                                 val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                     data = Uri.fromParts("package", context.packageName, null)
                                 }
                                 context.startActivity(intent)
                             }
                        } else {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    }
                ) {
                    Text("去设置", color = LinkBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("取消", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color(0xFF333333),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 删除确认对话框
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reminderToDelete by remember { mutableStateOf<MockEvent?>(null) }
    
    // 提示对话框
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoDialogMessage by remember { mutableStateOf("") }
    
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("提示") },
            text = { Text(infoDialogMessage) },
            confirmButton = {
                TextButton(
                    onClick = { showInfoDialog = false }
                ) {
                    Text("知道了", color = BellYellow)
                }
            },
            containerColor = Color.White
        )
    }
    
    if (showDeleteDialog && reminderToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除提醒") },
            text = { Text("确定要删除“${reminderToDelete?.title}”吗？此操作无法撤销。\n您删除后，所有人都将删除该提醒。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        reminderToDelete?.let { event ->
                            MockDataStore.deleteReminder(event.id)
                        }
                        showDeleteDialog = false
                        reminderToDelete = null
                    }
                ) {
                    Text("删除", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    // 处理数据：分组和排序
    val timelineItems = remember(allReminders, isExpiredExpanded, currentUser) {
        val now = System.currentTimeMillis()
        val items = mutableListOf<TimelineItem>()
        
        // 分离未来事件和过期事件
        val (futureEvents, expiredEvents) = allReminders.partition { it.eventTime.time > now }

        // 1. 先处理过期事件 (在顶部，符合时间顺序：过去 -> 未来)
        if (expiredEvents.isNotEmpty()) {
            items.add(TimelineItem.ExpiredHeader(expiredEvents.size, isExpiredExpanded))
            
            if (isExpiredExpanded) {
                // 展开时按时间降序排列 (刚刚过期的在最上面)
                val sortedExpired = expiredEvents.sortedByDescending { it.eventTime }
                val expiredGrouped = sortedExpired.groupBy { getFormattedDate(it.eventTime) }
                
                expiredGrouped.forEach { (date, eventList) ->
                    items.add(TimelineItem.Header(date, isExpired = true))
                    eventList.forEach { event ->
                        items.add(TimelineItem.Event(event.toMockEvent(isExpired = true, currentUserName = currentUser.name)))
                    }
                }
            }
        }
        
        // 2. 再处理未来事件 (按时间升序)
        val sortedFuture = futureEvents.sortedBy { it.eventTime }
        val futureGrouped = sortedFuture.groupBy { getFormattedDate(it.eventTime) }
        
        futureGrouped.forEach { (date, eventList) ->
            items.add(TimelineItem.Header(date))
            eventList.forEach { event ->
                items.add(TimelineItem.Event(event.toMockEvent(currentUserName = currentUser.name)))
            }
        }
        
        items
    }

    ShareAlarmTheme {
        // 整体背景设为浅灰色，模拟卡片悬浮效果
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F7)) // iOS 风格背景灰
        ) {
            // 主体白色卡片，带大圆角
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 0.dp),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = Color.White
            ) {
                Scaffold(
                    topBar = { HomeTopBar() },
                    floatingActionButton = { HomeFloatingButtons(navController) },
                    containerColor = Color.Transparent
                ) { paddingValues ->
                    if (allReminders.isEmpty()) {
                        // 空状态：咖啡插画
                        EmptyStateView(paddingValues)
                    } else {
                        // 时间轴列表
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 24.dp),
                            contentPadding = PaddingValues(bottom = 100.dp) // 底部留白给 FAB
                        ) {
                            items(timelineItems) { item ->
                                when (item) {
                                    is TimelineItem.Header -> DateHeader(item.date, item.isExpired)
                                    is TimelineItem.Event -> EventCard(
                                        event = item.event, 
                                        navController = navController,
                                        onDelete = {
                                            reminderToDelete = item.event
                                            showDeleteDialog = true
                                        },
                                        onLongClick = {
                                            reminderToDelete = item.event
                                            showDeleteDialog = true
                                        },
                                        onShowInfo = { message ->
                                            infoDialogMessage = message
                                            showInfoDialog = true
                                        }
                                    )
                                    is TimelineItem.ExpiredHeader -> ExpiredEventsBanner(
                                        count = item.count,
                                        isExpanded = item.isExpanded,
                                        onToggle = { isExpiredExpanded = !isExpiredExpanded }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 辅助扩展函数：转换 Reminder 为 MockEvent (适配现有 UI)
fun com.example.sharealarm.data.model.Reminder.toMockEvent(
    isExpired: Boolean = false,
    currentUserName: String = "" // 增加当前用户参数
): MockEvent {
    val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    val dayFormatter = java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault())
    
    // 判断是今天、明天还是其他日期
    val now = java.util.Calendar.getInstance()
    val eventCal = java.util.Calendar.getInstance().apply { time = this@toMockEvent.eventTime }
    
    val timePrefix = when {
        now.get(java.util.Calendar.YEAR) == eventCal.get(java.util.Calendar.YEAR) &&
        now.get(java.util.Calendar.DAY_OF_YEAR) == eventCal.get(java.util.Calendar.DAY_OF_YEAR) -> "今天"
        now.get(java.util.Calendar.YEAR) == eventCal.get(java.util.Calendar.YEAR) &&
        now.get(java.util.Calendar.DAY_OF_YEAR) + 1 == eventCal.get(java.util.Calendar.DAY_OF_YEAR) -> "明天"
        else -> dayFormatter.format(this.eventTime)
    }

    return MockEvent(
        id = this.id,
        title = this.title,
        timeStr = "$timePrefix ${formatter.format(this.eventTime)}",
        creator = this.creator, // 暂时使用 creator name
        dateStr = getFormattedDate(this.eventTime),
        isExpired = isExpired,
        isRead = this.isRead,
        isMine = this.creator == currentUserName // 判断是否是自己的
    )
}

fun getFormattedDate(date: java.util.Date): String {
    val formatter = java.text.SimpleDateFormat("yyyy/M/d", java.util.Locale.getDefault())
    return formatter.format(date)
}

@Composable
fun EmptyStateView(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Coffee,
                contentDescription = "Empty State",
                modifier = Modifier.size(80.dp),
                tint = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无提醒，来杯咖啡休息一下吧",
                color = Color.Gray,
                fontSize = 16.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun HomeTopBar() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // 标题文本组合
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "飞铃",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraLight,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    letterSpacing = 2.sp,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "BuddyBell",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Thin,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.alignByBaseline()
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 右上角菜单
            var menuExpanded by remember { mutableStateOf(false) }

            Box(contentAlignment = Alignment.TopStart) {
                // 自定义 2x2 四点图标按钮
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val dotSize = 4.dp.toPx() // 点的大小
                        val gap = 4.dp.toPx()     // 间距
                        val color = Color(0xFF8E8E93)
                        
                        // 计算起始位置以居中
                        val totalSize = dotSize * 2 + gap
                        val startX = (size.width - totalSize) / 2
                        val startY = (size.height - totalSize) / 2

                        // 左上
                        drawCircle(
                            color = color, 
                            radius = dotSize / 2, 
                            center = androidx.compose.ui.geometry.Offset(startX + dotSize / 2, startY + dotSize / 2)
                        )
                        // 右上
                        drawCircle(
                            color = color, 
                            radius = dotSize / 2, 
                            center = androidx.compose.ui.geometry.Offset(startX + dotSize * 1.5f + gap, startY + dotSize / 2)
                        )
                        // 左下
                        drawCircle(
                            color = color, 
                            radius = dotSize / 2, 
                            center = androidx.compose.ui.geometry.Offset(startX + dotSize / 2, startY + dotSize * 1.5f + gap)
                        )
                        // 右下
                        drawCircle(
                            color = color, 
                            radius = dotSize / 2, 
                            center = androidx.compose.ui.geometry.Offset(startX + dotSize * 1.5f + gap, startY + dotSize * 1.5f + gap)
                        )
                    }
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("我的") },
                        onClick = { menuExpanded = false },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) }
                    )
                    DropdownMenuItem(
                        text = { Text("搜索") },
                        onClick = { menuExpanded = false },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) }
                    )
                    DropdownMenuItem(
                        text = { Text("前往指定日期") },
                        onClick = { menuExpanded = false },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray) }
                    )
                    DropdownMenuItem(
                        text = { Text("切换风格") },
                        onClick = { menuExpanded = false },
                        leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, tint = Color.Gray) }
                    )
                }
            }
        }
        
        // 细细的黑线 (分割线)
        Divider(
            color = Color(0xFFE0E0E0),
            thickness = 0.5.dp
        )
    }
}

@Composable
fun ExpiredEventsBanner(count: Int, isExpanded: Boolean, onToggle: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clickable { onToggle() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isExpanded) "收起过期事件" else "查看 $count 个过期事件",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
    }
}

@Composable
fun DateHeader(date: String, isExpired: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 竖线颜色区分
        val indicatorColor = if (isExpired) Color.LightGray else BellYellow
        
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(indicatorColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = date,
            fontSize = 24.sp,
            color = if (isExpired) Color.Gray else Color(0xFF333333),
            fontWeight = FontWeight.Light,
            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCard(
    event: MockEvent, 
    navController: NavController, 
    onDelete: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onShowInfo: (String) -> Unit = {}
) {
    // 时间显示在左侧，卡片在右侧
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 左侧时间刻度 (固定不变)
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .width(60.dp)
                .padding(top = 20.dp, end = 12.dp)
        ) {
            val timeParts = event.timeStr.split(" ")
            if (timeParts.size > 1) {
                Text(
                    text = timeParts[1],
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (event.isExpired) Color.LightGray else Color(0xFF333333),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                )
                Text(
                    text = timeParts[0],
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.Gray,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                )
            } else {
                Text(
                    text = event.timeStr,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        // 右侧卡片区域 (可侧滑)
        Box(modifier = Modifier.weight(1f)) {
            // 侧滑状态管理 (公用)
            val offsetX = remember { Animatable(0f) }
            val scope = rememberCoroutineScope()
            val maxSwipeDistance = 80.dp.value * LocalContext.current.resources.displayMetrics.density // 转换为像素

            if (event.isMine) {
                
                // 背景层 (删除按钮)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Red, RoundedCornerShape(16.dp))
                        .clickable { 
                            // 点击删除按钮时触发，并重置滑动状态
                            onDelete()
                            scope.launch { offsetX.animateTo(0f) }
                        }
                        .padding(end = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color.White
                    )
                }

                // 前景层 (卡片内容)
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                scope.launch {
                                    // 限制滑动范围：只能向左滑，最大滑到 -maxSwipeDistance
                                    val targetVal = (offsetX.value + delta).coerceIn(-maxSwipeDistance, 0f)
                                    offsetX.snapTo(targetVal)
                                }
                            },
                            onDragStopped = {
                                // 松手时的回弹/吸附逻辑
                                if (offsetX.value < -maxSwipeDistance / 2) {
                                    // 滑动超过一半，吸附到展开状态
                                    scope.launch { offsetX.animateTo(-maxSwipeDistance, tween(300)) }
                                } else {
                                    // 否则回弹关闭
                                    scope.launch { offsetX.animateTo(0f, tween(300)) }
                                }
                            }
                        )
                ) {
                    EventCardContent(event, navController, onLongClick, onShowInfo)
                }
            } else {
                // 别人的事件：也可以滑动，但会回弹
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                scope.launch {
                                    // 限制滑动范围：只能向左滑，最大滑到 -maxSwipeDistance (可以稍微多一点阻尼效果)
                                    val targetVal = (offsetX.value + delta * 0.5f).coerceIn(-maxSwipeDistance, 0f)
                                    offsetX.snapTo(targetVal)
                                }
                            },
                            onDragStopped = {
                                // 松手时始终回弹关闭
                                scope.launch { offsetX.animateTo(0f, tween(300)) }
                            }
                        )
                ) {
                    EventCardContent(event, navController, onLongClick, onShowInfo)
                }
            }
        }
    }
}

@Composable
fun EventCardContent(
    event: MockEvent, 
    navController: NavController,
    onLongClick: () -> Unit,
    onShowInfo: (String) -> Unit
) {
    // 纯卡片内容
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { 
                        if (event.isMine) {
                            onLongClick() 
                        } else {
                            onShowInfo("这是 ${event.creator} 创建的事件，您无法删除，可以去详情页取消响铃。")
                        }
                    },
                    onTap = { 
                        MockDataStore.markAsRead(event.id) // 标记为已读
                        navController.navigate(Screen.ReminderDetail.createRoute(event.id)) 
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题和过期标签
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (event.isExpired) {
                    Surface(
                        color = Color(0xFFF2F2F7),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "已过期",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Text(
                    text = event.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    color = if (event.isExpired) Color.Gray else Color(0xFF222222),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f) // 占据剩余空间
                )
                
                // 未读标记 (10dp 红点)
                if (!event.isRead) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp) // 调整为 10dp
                            .clip(CircleShape)
                            .background(ErrorRed)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.creator,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    color = Color.Gray
                )
                
                // “我创建的”微型标签
                if (event.isMine) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFF0F0F0),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(
                            text = "我创建的",
                            fontSize = 9.sp,
                            color = Color(0xFF8E8E93),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeFloatingButtons(navController: NavController) {
    // 底部双 FAB 布局
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, end = 8.dp) // 调整位置
    ) {
        // 主按钮：添加提醒 (右下角)
        FloatingActionButton(
            onClick = { navController.navigate(Screen.CreateReminder.route) },
            containerColor = BellYellow,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(64.dp) // 稍大一点
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
        }

        // 次按钮：我的伙伴 (左侧一点，或者左下角？设计描述是右下角组合？通常 FAB 在右下。
        // 根据描述：“底部操作按钮（固定于右下角）... 次按钮（稍小、低对比度）”
        // 我们把次按钮放在主按钮左边
        SmallFloatingActionButton(
            onClick = { navController.navigate(Screen.Contacts.route) },
            containerColor = Color(0xFFF2F2F7), // 低对比度
            contentColor = Color(0xFF8E8E93),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 80.dp, bottom = 8.dp) // 放在主按钮左侧
        ) {
            Icon(Icons.Default.Group, contentDescription = "Contacts")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(rememberNavController())
}