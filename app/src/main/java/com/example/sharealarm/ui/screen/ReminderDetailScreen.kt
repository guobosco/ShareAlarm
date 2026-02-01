package com.example.sharealarm.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.sharealarm.data.local.MockDataStore
import com.example.sharealarm.data.model.User
import com.example.sharealarm.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(navController: NavController, reminderId: String?) {
    val reminder = reminderId?.let { MockDataStore.getReminderById(it) }
    val currentUser by MockDataStore.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    
    // 冒泡动画状态
    var showBubble by remember { mutableStateOf(false) }
    val bubbleScale = remember { Animatable(0f) }
    val bubbleAlpha = remember { Animatable(1f) }
    val bubbleOffset = remember { Animatable(0f) }

    // 触发冒泡动画
    fun triggerBubble() {
        if (showBubble) return
        showBubble = true
        scope.launch {
            // 弹出
            launch { bubbleScale.animateTo(1.2f, tween(300, easing = FastOutSlowInEasing)) }
            launch { bubbleOffset.animateTo(-200f, tween(1000, easing = FastOutSlowInEasing)) }
            launch { 
                delay(500)
                bubbleAlpha.animateTo(0f, tween(500)) 
            }
            delay(1000)
            // 重置
            showBubble = false
            bubbleScale.snapTo(0f)
            bubbleAlpha.snapTo(1f)
            bubbleOffset.snapTo(0f)
        }
    }

    if (reminder == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到提醒详情")
        }
        return
    }

    val isCreator = reminder.creator == currentUser.name // 简化判断
    val canEdit = isCreator
    
    // 格式化器
    val fullDateFormatter = SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("提醒详情", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            DetailBottomBar(
                onBubbleClick = { triggerBubble() },
                onMuteClick = { /* TODO */ },
                onCommentClick = { /* TODO */ }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // 顶部提示条
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = Blue5,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isCreator) "这是你创建的提醒，你可以编辑" else "这是 ${reminder.creator} 创建的提醒，你只能查看不能编辑",
                            color = LinkBlue,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 标题
                item {
                    Text(
                        text = "提醒内容",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = reminder.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 时间
                item {
                    Text(
                        text = "时间",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fullDateFormatter.format(reminder.eventTime),
                        fontSize = 20.sp,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 提前提醒列表
                item {
                    Text(
                        text = "提前提醒",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        reminder.alertTimes.forEach { alertTime ->
                            val diff = reminder.eventTime.time - alertTime.time
                            val minutes = diff / (1000 * 60)
                            
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF9F9F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "提前 $minutes 分钟",
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 创建人
                item {
                    Text(
                        text = "创建人",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF9F9F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reminder.creator,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // 备注
                if (reminder.description.isNotEmpty()) {
                    item {
                        Text(
                            text = "备注",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = reminder.description,
                            fontSize = 16.sp,
                            color = Color(0xFF333333),
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                
                // 提醒对象 (模拟已读状态)
                item {
                    Text(
                        text = "提醒对象",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // 这里可以展示头像列表，暂时留白或简单展示
                }
            }
            
            // 冒泡动画层
            if (showBubble) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 80.dp, start = 40.dp) // 对应按钮位置
                        .offset(y = bubbleOffset.value.dp)
                        .alpha(bubbleAlpha.value)
                        .scale(bubbleScale.value)
                ) {
                    Icon(
                        imageVector = Icons.Default.BubbleChart,
                        contentDescription = null,
                        tint = LinkBlue,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailBottomBar(
    onBubbleClick: () -> Unit,
    onMuteClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Divider(color = Color(0xFFEEEEEE))
        
        // 已查看状态
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { /* Show Viewers */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "1/2 人已查看",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
        
        // 操作按钮行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 收到冒泡
            Button(
                onClick = onBubbleClick,
                colors = ButtonDefaults.buttonColors(containerColor = Blue5),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.BubbleChart, // 用圆圈代替冒泡
                    contentDescription = null,
                    tint = LinkBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("收到冒泡", color = LinkBlue, fontSize = 13.sp)
            }
            
            // 取消提醒
            Button(
                onClick = onMuteClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("取消提醒", color = Color(0xFF333333), fontSize = 13.sp)
            }
            
            // 表情评论
            Button(
                onClick = onCommentClick,
                colors = ButtonDefaults.buttonColors(containerColor = Green5),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("表情评论", color = SuccessGreen, fontSize = 13.sp)
            }
        }
    }
}
