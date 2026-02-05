package com.example.sharealarm.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Send
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
import com.example.sharealarm.data.model.Comment
import com.example.sharealarm.data.model.Reminder
import com.example.sharealarm.data.model.User
import com.example.sharealarm.ui.navigation.Screen
import com.example.sharealarm.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(navController: NavController, reminderId: String?) {
    // 响应式获取数据
    val reminders by MockDataStore.reminders.collectAsState()
    val reminder = reminders.find { it.id == reminderId }
    val currentUser by MockDataStore.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    
    // UI 状态
    var showCancelDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showCommentSheet by remember { mutableStateOf(false) }
    var showViewersSheet by remember { mutableStateOf(false) }

    // 冒泡动画状态
    var showBubble by remember { mutableStateOf(false) }
    val bubbleScale = remember { Animatable(0f) }
    val bubbleAlpha = remember { Animatable(1f) }
    val bubbleOffset = remember { Animatable(0f) }

    // 自动标记已读/已查看
    LaunchedEffect(reminder) {
        if (reminder != null) {
            MockDataStore.addViewed(reminder.id, currentUser.id)
            if (!reminder.isRead) {
                MockDataStore.markAsRead(reminder.id)
            }
        }
    }

    // 触发冒泡动画
    fun triggerBubble() {
        if (reminder == null) return
        // 更新数据
        MockDataStore.toggleBubble(reminder.id, currentUser.id)
        
        // 播放动画
        if (showBubble) return
        showBubble = true
        scope.launch {
            launch { bubbleScale.animateTo(1.2f, tween(300, easing = FastOutSlowInEasing)) }
            launch { bubbleOffset.animateTo(-200f, tween(1000, easing = FastOutSlowInEasing)) }
            launch { 
                delay(500)
                bubbleAlpha.animateTo(0f, tween(500)) 
            }
            delay(1000)
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

    val isCreator = reminder.creator == currentUser.name
    val canEdit = isCreator
    val fullDateFormatter = SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("提醒详情", fontWeight = FontWeight.Normal, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (canEdit) {
                        TextButton(onClick = { 
                            navController.navigate(Screen.CreateReminder.createRoute(reminder.id)) 
                        }) {
                            Text("编辑", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            DetailBottomBar(
                reminder = reminder,
                onBubbleClick = { triggerBubble() },
                onMuteClick = { 
                    if (reminder.isCancelled) {
                        showRestoreDialog = true
                    } else {
                        showCancelDialog = true
                    }
                },
                onCommentClick = { showCommentSheet = true },
                onViewersClick = { showViewersSheet = true }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isCreator) "这是你创建的提醒，你可以编辑" else "这是 ${reminder.creator} 创建的提醒，你只能查看不能编辑",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 标题
                item {
                    Text(text = "提醒内容", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = reminder.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (reminder.isCancelled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                        style = if (reminder.isCancelled) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else LocalTextStyle.current
                    )
                    if (reminder.isCancelled) {
                        Text(
                            text = "(已取消)",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 时间
                item {
                    Text(text = "事件时间", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fullDateFormatter.format(reminder.eventTime),
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 响铃时间
                item {
                    Text(text = "响铃时间", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        reminder.alertTimes.forEach { alertTime ->
                            val timeStr = fullDateFormatter.format(alertTime)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = timeStr,
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 备注
                if (reminder.description.isNotEmpty()) {
                    item {
                        Text(text = "备注", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = reminder.description,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // 创建人
                item {
                    Text(text = "创建人", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reminder.creator,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            
            // 冒泡动画层
            if (showBubble) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 80.dp, start = 40.dp)
                        .offset(y = bubbleOffset.value.dp)
                        .alpha(bubbleAlpha.value)
                        .scale(bubbleScale.value)
                ) {
                    Icon(
                        imageVector = Icons.Default.BubbleChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }

    // 取消提醒对话框
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("取消提醒") },
            text = { Text("确定要取消这个提醒吗？取消后将不会响铃。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        MockDataStore.cancelReminder(reminder.id)
                        showCancelDialog = false
                    }
                ) {
                    Text("确定取消", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("再想想")
                }
            }
        )
    }

    // 恢复提醒对话框
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("恢复提醒") },
            text = { Text("确定要恢复这个提醒吗？恢复后将正常响铃。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        MockDataStore.restoreReminder(reminder.id)
                        showRestoreDialog = false
                    }
                ) {
                    Text("确定恢复", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 评论底部弹窗
    if (showCommentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            // 确保底部输入框不被键盘遮挡
            windowInsets = WindowInsets.ime
        ) {
            CommentSheetContent(
                comments = reminder.comments,
                currentUser = currentUser,
                onSendComment = { content, isEmoji ->
                    MockDataStore.addComment(
                        reminder.id,
                        Comment(
                            id = UUID.randomUUID().toString(),
                            userId = currentUser.id,
                            userName = currentUser.name,
                            content = content,
                            timestamp = Date(),
                            isEmoji = isEmoji
                        )
                    )
                }
            )
        }
    }

    // 查看者列表底部弹窗
    if (showViewersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showViewersSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ViewersSheetContent(viewerIds = reminder.viewedBy)
        }
    }
}

@Composable
fun DetailBottomBar(
    reminder: Reminder,
    onBubbleClick: () -> Unit,
    onMuteClick: () -> Unit,
    onCommentClick: () -> Unit,
    onViewersClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Divider(color = MaterialTheme.colorScheme.outlineVariant)
        
        // 已查看状态
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onViewersClick)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                // 显示查看人数
                val viewedCount = reminder.viewedBy.size
                // 假设总人数是参与者人数 + 创建者 (如果不重叠)
                val totalParticipants = (reminder.participants + reminder.creator).distinct().size
                // 为了演示，如果totalParticipants为0或1，就只显示 viewedCount 人已查看
                val text = if (totalParticipants > 1) "$viewedCount/$totalParticipants 人已查看" else "$viewedCount 人已查看"
                
                Text(
                    text = text,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.BubbleChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("收到冒泡", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            }
            
            // 取消提醒
            val isCancelled = reminder.isCancelled
            Button(
                onClick = onMuteClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCancelled) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = if (isCancelled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isCancelled) "已取消提醒" else "取消提醒", 
                    color = if (isCancelled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant, 
                    fontSize = 13.sp
                )
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
                val commentCount = reminder.comments.size
                Text(
                    text = if (commentCount > 0) "评论 $commentCount" else "表情评论", 
                    color = SuccessGreen, 
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun CommentSheetContent(
    comments: List<Comment>,
    currentUser: User,
    onSendComment: (String, Boolean) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val maxChars = 20
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .heightIn(min = 300.dp, max = 500.dp)
            .imePadding() // 确保内容避让键盘
    ) {
        Text(
            text = "评论 (${comments.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        Divider()
        
        // 评论列表
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            reverseLayout = true
        ) {
            items(comments.sortedByDescending { it.timestamp }) { comment ->
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    // ... (保持原样)
                    // 简单头像
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = comment.userName.take(1),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = comment.userName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(comment.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        if (comment.isEmoji) {
                            Text(text = comment.content, fontSize = 24.sp)
                        } else {
                            Text(
                                text = comment.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
        
        Divider()
        
        // 输入区
        Column(modifier = Modifier.padding(16.dp)) {
            // 快捷表情
            val emojis = listOf("👍", "🎉", "❤️", "😂", "👌", "🔥")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(emojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .clickable { 
                                onSendComment(emoji, true) 
                                // 点击表情后不关闭，或者清空输入框(如果需要)
                            }
                            .padding(4.dp) // 增加点击区域
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 24.sp
                        )
                    }
                }
            }
            
            // 输入框
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { if (it.length <= maxChars) inputText = it },
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                    decorationBox = { innerTextField ->
                        Box {
                            if (inputText.isEmpty()) {
                                Text("写评论...", color = MaterialTheme.colorScheme.outline)
                            }
                            innerTextField()
                        }
                    }
                )
                
                Text(
                    text = "${inputText.length}/$maxChars",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendComment(inputText, false)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "发送",
                        tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun ViewersSheetContent(viewerIds: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .heightIn(min = 200.dp, max = 400.dp)
    ) {
        Text(
            text = "已查看人员 (${viewerIds.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        Divider()
        
        if (viewerIds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无查看记录", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn {
                items(viewerIds) { userId ->
                    // 获取用户信息 (Mock)
                    val user = MockDataStore.getUserById(userId)
                    if (user != null) {
                        ListItem(
                            headlineContent = { Text(user.name) },
                            supportingContent = { Text(user.email) },
                            leadingContent = {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(user.name.take(1), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
