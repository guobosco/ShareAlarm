package com.example.sharealarm.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sharealarm.R
import com.example.sharealarm.data.local.MockDataStore
import com.example.sharealarm.data.model.User

import com.example.sharealarm.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(navController: NavController) {
    val user by MockDataStore.currentUser.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "个人资料", 
                        fontSize = 17.sp, 
                        fontWeight = FontWeight.Normal
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Group 1
            ProfileGroup {
                // Avatar
                ProfileItem(
                    label = "头像",
                    showArrow = true,
                    onClick = { navController.navigate(Screen.MyProfileEdit.route) }
                ) {
                    // Placeholder for Avatar Image
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray) // Placeholder color
                    ) {
                        // In a real app, use AsyncImage here. 
                        // For now, maybe an Icon or just the gray box as placeholder matching the image style roughly
                        // The image shows a photo of a building/tower.
                        Text(
                            text = user.name.take(1),
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White,
                            fontSize = 24.sp
                        )
                    }
                }
                
                Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(start = 16.dp))

                // Name
                ProfileItem(
                    label = "名字",
                    value = user.name,
                    showArrow = true,
                    onClick = { navController.navigate(Screen.MyProfileEdit.route) }
                )
                
                Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(start = 16.dp))

                // Gender
                ProfileItem(
                    label = "性别",
                    value = "男", // Mocked
                    showArrow = true,
                    onClick = { navController.navigate(Screen.MyProfileEdit.route) }
                )
                
                Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(start = 16.dp))

                // Region
                ProfileItem(
                    label = "地区",
                    value = "百慕大", // Mocked
                    showArrow = true,
                    onClick = { navController.navigate(Screen.MyProfileEdit.route) }
                )
                
                Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(start = 16.dp))

                // Phone
                ProfileItem(
                    label = "手机号",
                    value = user.phoneNumber.ifEmpty { "189******15" },
                    showArrow = true,
                    onClick = { navController.navigate(Screen.MyProfileEdit.route) }
                )
                
                Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(start = 16.dp))

                // QR Code
                ProfileItem(
                    label = "我的二维码",
                    showArrow = true,
                    onClick = { navController.navigate(Screen.MyQrCode.route) }
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Code",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(start = 16.dp))

                // Pai Yi Pai
                ProfileItem(
                    label = "拍一拍",
                    value = "郭炳聪", // Mocked
                    showArrow = true,
                    onClick = { navController.navigate(Screen.MyProfileEdit.route) }
                )
                
                Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(start = 16.dp))

                // Signature
                ProfileItem(
                    label = "签名",
                    value = user.memo.ifEmpty { "未填写" },
                    showArrow = true,
                    onClick = { navController.navigate(Screen.MyProfileEdit.route) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Group 2
            ProfileGroup {
                // Ringtone
                ProfileItem(
                    label = "来电铃声",
                    value = "You Belong To Me", // Mocked
                    showArrow = true,
                    onClick = { /* TODO: Edit Ringtone */ }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Group 3
            ProfileGroup {
                // Address
                ProfileItem(
                    label = "我的地址",
                    showArrow = true,
                    onClick = { /* TODO: Edit Address */ }
                )
                
                // Invoice Title removed
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // 增加垂直间距
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun ProfileItem(
    label: String,
    value: String? = null,
    showArrow: Boolean = false,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (value != null) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = if (showArrow) 4.dp else 0.dp)
                )
            }
            
            if (trailingContent != null) {
                trailingContent()
                if (showArrow) Spacer(modifier = Modifier.width(4.dp))
            }

            if (showArrow) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
