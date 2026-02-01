package com.example.sharealarm.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sharealarm.data.local.MockDataStore
import com.example.sharealarm.data.model.User
import com.example.sharealarm.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(navController: NavController) {
    val contacts by MockDataStore.contacts.collectAsState()
    
    // 按拼音首字母分组（这里简化处理，直接用名字的首字符大写，实际项目需要拼音库）
    val groupedContacts = remember(contacts) {
        contacts.groupBy { 
            // 简单取首字母，如果是中文实际需要转拼音，这里假设模拟数据以英文或简单中文处理
            // 为演示效果，假设 MockDataStore 里的名字已经被处理过或按逻辑分组
            // 这里简单取第一个字符的 hashCode 模 26 映射到 A-Z (仅为演示分组效果)
            // 实际项目请使用 Pinyin4j
            val firstChar = it.name.firstOrNull()?.uppercaseChar() ?: '#'
            if (firstChar in 'A'..'Z') firstChar.toString() else "Z" // 简化演示：都归到 Z 或实际首字母
        }.toSortedMap()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("我的伙伴", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Add Contact/Group */ }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // 搜索框
            SearchBar()

            // 统计信息
            Text(
                text = "全部联系人 (${contacts.size})   群组 (2)",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = LinkBlue,
                fontWeight = FontWeight.Medium
            )

            // 联系人列表
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // 演示用的假分组数据，因为没有引入拼音库
                // 实际开发中替换为 groupedContacts
                
                // 模拟 A 组
                stickyHeader { ContactGroupHeader("L") }
                item { ContactItem(User("u1", "李四", ""), Color(0xFF5AC8FA)) }
                
                stickyHeader { ContactGroupHeader("Q") }
                item { ContactItem(User("u6", "钱九", ""), Color(0xFFFFCC00)) }

                stickyHeader { ContactGroupHeader("S") }
                item { ContactItem(User("u4", "孙七", ""), Color(0xFF007AFF)) }

                stickyHeader { ContactGroupHeader("W") }
                item { ContactItem(User("u2", "王五", ""), Color(0xFF5856D6)) }
                item { ContactItem(User("u7", "吴十", ""), Color(0xFFFF2D55)) }

                stickyHeader { ContactGroupHeader("Z") }
                item { ContactItem(User("u3", "赵六", ""), Color(0xFF34C759)) }
                item { ContactItem(User("u5", "周八", ""), Color(0xFFFF9500)) }
                item { ContactItem(User("u0", "张三", ""), Color(0xFFAF52DE)) }
            }
        }
    }
}

@Composable
fun SearchBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF2F2F7)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "搜索伙伴或群组",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ContactGroupHeader(letter: String) {
    Surface(
        color = Color(0xFFF2F2F7), // 浅灰色背景
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = letter,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ContactItem(user: User, avatarColor: Color) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.firstOrNull()?.toString() ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = user.name,
                fontSize = 17.sp,
                color = Color.Black
            )
        }
        Divider(
            color = Color(0xFFEEEEEE), 
            thickness = 1.dp,
            modifier = Modifier.padding(start = 72.dp) // iOS 风格缩进分割线
        )
    }
}
