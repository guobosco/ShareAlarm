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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderScreen(navController: NavController) {
    val context = LocalContext.current
    val currentUser by MockDataStore.currentUser.collectAsState()
    
    // 表单状态
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // 时间处理
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MINUTE, 15) // 默认 +15分钟
    var eventDate by remember { mutableStateOf(calendar.time) }
    
    // 提醒时间偏移量 (分钟)，默认15分钟
    var alertOffsetMinutes by remember { mutableStateOf(15) }
    
    // 参与者
    var showParticipantDialog by remember { mutableStateOf(false) }
    var selectedParticipants by remember { mutableStateOf(listOf<User>()) }
    
    // 错误信息
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 日期格式化
    val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    // 提交处理
    fun handleSubmit() {
        if (title.isBlank()) {
            errorMessage = "请输入提醒内容"
            return
        }
        
        // 创建提醒
        MockDataStore.addReminder(
            title = title,
            time = eventDate,
            note = description,
            participantIds = selectedParticipants.map { it.id } + currentUser.id
        )
        
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
                        modifier = Modifier.fillMaxWidth(),
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
                                .clickable { /* TODO: Open Date Picker */ },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5EA))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = dateFormatter.format(eventDate),
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
                                .clickable { /* TODO: Open Time Picker */ },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5EA))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = timeFormatter.format(eventDate),
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
                FormSection(title = "提前提醒") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* TODO: Show Alert Options */ },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5EA))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "提前 ${alertOffsetMinutes} 分钟",
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "+ 添加",
                                color = LinkBlue,
                                fontSize = 14.sp
                            )
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
}

@Composable
fun FormSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
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
