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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

// 模拟数据模型 (UI 层)
data class MockEvent(
    val id: String,
    val title: String,
    val timeStr: String,
    val creator: String,
    val dateStr: String,
    val isExpired: Boolean = false
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

    // 处理数据：分组和排序
    val timelineItems = remember(allReminders, isExpiredExpanded) {
        val now = System.currentTimeMillis()
        val items = mutableListOf<TimelineItem>()
        
        // 分离未来事件和过期事件
        val (futureEvents, expiredEvents) = allReminders.partition { it.eventTime.time > now }

        // 1. 先处理过期事件 (在顶部，符合时间顺序：过去 -> 未来)
        if (expiredEvents.isNotEmpty()) {
            items.add(TimelineItem.ExpiredHeader(expiredEvents.size, isExpiredExpanded))
            
            if (isExpiredExpanded) {
                // 展开时按时间升序排列，形成连续的时间流
                val sortedExpired = expiredEvents.sortedBy { it.eventTime }
                val expiredGrouped = sortedExpired.groupBy { getFormattedDate(it.eventTime) }
                
                expiredGrouped.forEach { (date, eventList) ->
                    items.add(TimelineItem.Header(date, isExpired = true))
                    eventList.forEach { event ->
                        items.add(TimelineItem.Event(event.toMockEvent(isExpired = true)))
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
                items.add(TimelineItem.Event(event.toMockEvent()))
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
                                    is TimelineItem.Event -> EventCard(item.event, navController)
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
fun com.example.sharealarm.data.model.Reminder.toMockEvent(isExpired: Boolean = false): MockEvent {
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
        isExpired = isExpired
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
            
            // 自定义细线条菜单图标
            IconButton(
                onClick = { /* TODO: Open Menu */ },
                modifier = Modifier.size(24.dp)
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 1.5.dp.toPx()
                    val width = size.width
                    val height = size.height
                    val color = Color(0xFF8E8E93)
                    
                    drawLine(color = color, start = androidx.compose.ui.geometry.Offset(0f, height * 0.3f), end = androidx.compose.ui.geometry.Offset(width, height * 0.3f), strokeWidth = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    drawLine(color = color, start = androidx.compose.ui.geometry.Offset(0f, height * 0.5f), end = androidx.compose.ui.geometry.Offset(width, height * 0.5f), strokeWidth = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    drawLine(color = color, start = androidx.compose.ui.geometry.Offset(0f, height * 0.7f), end = androidx.compose.ui.geometry.Offset(width, height * 0.7f), strokeWidth = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
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

@Composable
fun EventCard(event: MockEvent, navController: NavController) {
    // 时间显示在左侧，卡片在右侧
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable { navController.navigate(Screen.ReminderDetail.createRoute(event.id)) }, // 点击跳转详情
        verticalAlignment = Alignment.Top
    ) {
        // 左侧时间刻度
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .width(60.dp) // 固定宽度
                .padding(top = 20.dp, end = 12.dp)
        ) {
            val timeParts = event.timeStr.split(" ")
            if (timeParts.size > 1) {
                Text(
                    text = timeParts[1], // 具体时间 09:00
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (event.isExpired) Color.LightGray else Color(0xFF333333),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                )
                Text(
                    text = timeParts[0], // 今天/明天
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

        // 卡片内容
        Card(
            modifier = Modifier.weight(1f),
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
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
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