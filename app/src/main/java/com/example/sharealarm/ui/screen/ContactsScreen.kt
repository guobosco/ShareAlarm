package com.example.sharealarm.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp
import com.example.sharealarm.data.local.MockDataStore
import com.example.sharealarm.data.model.User
import com.example.sharealarm.ui.theme.*
import com.example.sharealarm.ui.navigation.Screen
import net.sourceforge.pinyin4j.PinyinHelper
import androidx.compose.ui.text.style.TextOverflow
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(navController: NavController) {
    val contacts by MockDataStore.contacts.collectAsState()
    
    // 按拼音首字母分组
    val groupedContacts = remember(contacts) {
        contacts.groupBy { contact ->
            val nameChar = contact.name.firstOrNull() ?: '#'
            // 使用 Pinyin4j 将字符转换为拼音
            // PinyinHelper.toHanyuPinyinStringArray 返回 null 如果不是汉字
            val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(nameChar)
            
            val firstLetter = if (pinyinArray != null && pinyinArray.isNotEmpty()) {
                // 取拼音的第一个字符 (例如 "zhang1" -> 'z')
                pinyinArray[0].firstOrNull()?.uppercaseChar() ?: '#'
            } else {
                // 非汉字：如果是英文字母则直接使用，否则归为 '#'
                if (nameChar.isLetter()) nameChar.uppercaseChar() else '#'
            }
            
            if (firstLetter in 'A'..'Z') firstLetter.toString() else "#"
        }.toSortedMap { a, b ->
            // 将 '#' 分组放到最后
            if (a == "#") 1
            else if (b == "#") -1
            else a.compareTo(b)
        }
    }

    val themeSetting by ThemeSettings.isDarkTheme.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = themeSetting ?: isSystemDark

    // 1. 准备字母索引映射
    val alphabetToIndexMap = remember(groupedContacts) {
        val map = mutableMapOf<String, Int>()
        // 头部固定项数量 (New Friend, Group, Label) = 3
        var currentIndex = 3 
        
        groupedContacts.forEach { (initial, users) ->
            map[initial] = currentIndex
            // Header (1) + Items (users.size)
            currentIndex += 1 + users.size
        }
        map
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 定义完整的字母表
    val alphabet = remember { (('A'..'Z').map { it.toString() } + "#").toList() }
    
    // 当前手指触摸的字母
    var currentTouchLetter by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("我的伙伴", fontWeight = FontWeight.Normal, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { /* TODO: Add Contact/Group */ }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, RectangleShape)
            )
        },
        containerColor = MaterialTheme.colorScheme.background // 使用统一背景色
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // 联系人列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(top = 16.dp, bottom = 30.dp) // 添加内边距
            ) {
                // 固定功能入口 (封装在卡片中)
                item { 
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Column {
                            FunctionalItem(Icons.Default.PersonAdd, MaterialTheme.colorScheme.primary, "新的伙伴", showDivider = true) {}
                            FunctionalItem(Icons.Default.Group, MaterialTheme.colorScheme.primary, "群组", showDivider = true) {}
                            FunctionalItem(Icons.Default.Label, MaterialTheme.colorScheme.primary, "标签", showDivider = false) {
                                navController.navigate(Screen.TagManage.route)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // 分组数据 (每个分组一个卡片)
                groupedContacts.forEach { (initial, users) ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Column {
                                ContactGroupHeader(initial)
                                users.forEachIndexed { index, user ->
                                    ContactItem(user, MaterialTheme.colorScheme.primary, navController, showDivider = index < users.size - 1)
                                }
                            }
                        }
                    }
                }
                
                // 底部留白
                item { Spacer(modifier = Modifier.height(30.dp)) }
            }
            
            // 侧边索引栏 (保持不变)
            AlphabetSideBar(
                alphabet = alphabet,
                modifier = Modifier.align(Alignment.CenterEnd),
                onLetterSelected = { letter ->
                    currentTouchLetter = letter // 更新当前触摸字母
                    scope.launch {
                        // 查找跳转位置
                        // 如果选中的字母正好有分组，直接跳转
                        // 如果没有，找到该字母之后最近的一个存在的分组
                        val targetIndex = alphabetToIndexMap[letter] ?: run {
                            // 寻找 >= letter 的第一个 key
                            val sortedKeys = alphabetToIndexMap.keys.sorted()
                            val nextKey = sortedKeys.firstOrNull { it >= letter }
                            if (nextKey != null) alphabetToIndexMap[nextKey] else null
                        }
                        
                        if (targetIndex != null) {
                            listState.scrollToItem(targetIndex)
                        }
                    }
                },
                onTouchEnd = {
                    currentTouchLetter = null // 手指离开时清除
                }
            )
            
            // 屏幕中心的大字母气泡
            if (currentTouchLetter != null) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center)
                        .background(Color.LightGray.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentTouchLetter!!,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AlphabetSideBar(
    alphabet: List<String>,
    modifier: Modifier = Modifier,
    onLetterSelected: (String) -> Unit,
    onTouchEnd: () -> Unit
) {
    var touchedLetter by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .width(30.dp) // 稍微加宽一点触控区
            .padding(vertical = 16.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        val index = (offset.y / (size.height / alphabet.size)).toInt()
                        val letter = alphabet.getOrNull(index.coerceIn(0, alphabet.lastIndex))
                        if (letter != null) {
                            touchedLetter = letter
                            onLetterSelected(letter)
                        }
                    },
                    onDragEnd = { 
                        touchedLetter = null 
                        onTouchEnd()
                    },
                    onVerticalDrag = { change, _ ->
                        val index = (change.position.y / (size.height / alphabet.size)).toInt()
                        val letter = alphabet.getOrNull(index.coerceIn(0, alphabet.lastIndex))
                        if (letter != null && letter != touchedLetter) {
                            touchedLetter = letter
                            onLetterSelected(letter)
                        }
                    }
                )
                detectTapGestures(
                    onTap = { offset ->
                        val index = (offset.y / (size.height / alphabet.size)).toInt()
                        val letter = alphabet.getOrNull(index.coerceIn(0, alphabet.lastIndex))
                        if (letter != null) {
                            onLetterSelected(letter)
                            // 点击后短暂显示气泡然后消失
                            onTouchEnd() // 对于点击，我们可能不需要一直显示气泡，或者需要延迟消失。
                            // 这里为了简单，点击时就不显示气泡了，或者显示一下由上层逻辑控制。
                            // 但上面的 onTouchEnd 会立即清除 currentTouchLetter，导致气泡闪一下就没。
                            // 修正：detectTapGestures 不会触发 dragEnd，所以我们需要手动控制气泡消失，或者只在 drag 时显示气泡。
                            // 通常设计是：点击跳转（不显示气泡或短显），滑动跳转（显示气泡）。
                            // 为了体验一致，这里我们只在 drag 时显示气泡，tap 时只跳转。
                        }
                    },
                    onPress = { offset ->
                         // 处理按下的初始状态，类似于 DragStart
                        val index = (offset.y / (size.height / alphabet.size)).toInt()
                        val letter = alphabet.getOrNull(index.coerceIn(0, alphabet.lastIndex))
                        if (letter != null) {
                            touchedLetter = letter
                            onLetterSelected(letter)
                        }
                        tryAwaitRelease()
                        touchedLetter = null
                        onTouchEnd()
                    }
                )
            },
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        alphabet.forEach { letter ->
            val isSelected = letter == touchedLetter
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF07C160) else Color.Transparent), // 选中变绿
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) Color.White else Color.Gray,
                    modifier = Modifier.padding(vertical = 0.dp)
                )
            }
        }
    }
}

@Composable
fun FunctionalItem(icon: ImageVector, backgroundColor: Color, text: String, showDivider: Boolean = true, onClick: () -> Unit) {
    val themeSetting by ThemeSettings.isDarkTheme.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = themeSetting ?: isSystemDark
    val dividerColor = if (isDarkTheme) Color(0xFF38383A) else Color(0xFFE5E5EA)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp)) // 圆角矩形
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = text,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (showDivider) {
            Divider(
                color = dividerColor,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 72.dp)
            )
        }
    }
}



@Composable
fun ContactGroupHeader(letter: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface, // 背景色改为 Surface (白色)
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = letter,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary, // 强调色
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun ContactItem(user: User, avatarColor: Color, navController: NavController, showDivider: Boolean = true) {
    val themeSetting by ThemeSettings.isDarkTheme.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = themeSetting ?: isSystemDark
    val dividerColor = if (isDarkTheme) Color(0xFF38383A) else Color(0xFFE5E5EA)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    // user 对象来自 MockDataStore，直接使用其 ID 跳转
                    navController.navigate(Screen.PartnerProfile.createRoute(user.id))
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
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
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // 备注和标签小字
                val subInfo = remember(user) {
                    val parts = mutableListOf<String>()
                    if (user.remarkName.isNotBlank()) parts.add(user.remarkName)
                    parts.addAll(user.tags)
                    parts.joinToString("  ")
                }
                
                if (subInfo.isNotEmpty()) {
                    Text(
                        text = subInfo,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        if (showDivider) {
            Divider(
                color = dividerColor,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 72.dp) // iOS 风格缩进分割线
            )
        }
    }
}
