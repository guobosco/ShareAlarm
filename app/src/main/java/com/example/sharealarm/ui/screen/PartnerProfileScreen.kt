package com.example.sharealarm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sharealarm.data.local.MockDataStore
import java.text.SimpleDateFormat
import java.util.Locale

import com.example.sharealarm.ui.navigation.Screen
import androidx.compose.material.icons.filled.Edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerProfileScreen(navController: NavController, userId: String?) {
    val user = MockDataStore.getUserById(userId ?: "")
    
    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到用户")
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("伙伴资料", fontWeight = FontWeight.Normal, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        navController.navigate(Screen.PartnerEdit.createRoute(user.id))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background // 使用统一背景色
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // 账号信息
            item {
                ProfileSectionHeader("账号信息")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column {
                        ProfileItem(label = "飞铃号", value = user.shareAlarmId.ifEmpty { "未设置" }, showChevron = false)
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                        ProfileItem(label = "昵称", value = user.name, showChevron = false)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // 备注信息
            item {
                ProfileSectionHeader("备注")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column {
                        ProfileItem(label = "备注名", value = user.remarkName.ifEmpty { "未设置" })
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                        ProfileItem(label = "电话", value = user.phoneNumber.ifEmpty { "未设置" })
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                        ProfileItem(label = "标签", value = if (user.tags.isNotEmpty()) user.tags.joinToString(", ") else "未设置")
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                        ProfileItem(label = "备忘", value = user.memo.ifEmpty { "无" })
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // 更多信息
            item {
                ProfileSectionHeader("更多信息")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column {
                        ProfileItem(label = "来源", value = "通过搜索手机号码添加")
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
                        val dateFormat = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
                        ProfileItem(label = "添加时间", value = dateFormat.format(user.createdAt), showChevron = false)
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, bottom = 8.dp), // 调整间距适配卡片
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp
    )
}

@Composable
fun ProfileItem(
    label: String,
    value: String,
    showChevron: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) // 确保卡片内也是白色
            .clickable(enabled = showChevron, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(120.dp) // 固定标签宽度，保持对齐
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = value,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 4.dp)
        )
        
        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        } else {
             Spacer(modifier = Modifier.width(24.dp)) // 占位保持对齐
        }
    }
}
