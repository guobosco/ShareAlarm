package com.example.sharealarm.data.local

import com.example.sharealarm.data.model.User
import com.example.sharealarm.data.model.Group
import com.example.sharealarm.data.model.Reminder
import com.example.sharealarm.data.model.Comment
import java.util.Date
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * 模拟数据存储
 * 功能：提供全局单例的模拟数据，用于演示和测试
 */
object MockDataStore {

    // 当前登录用户
    private val _currentUser = MutableStateFlow(
        User(
            id = "user_current",
            name = "老聪",
            email = "simon@example.com",
            photoUrl = "",
            phoneNumber = "189******15"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // 联系人列表（伙伴）
    private val _contacts = MutableStateFlow<List<User>>(
        (listOf(
            User(
                id = "user_1", 
                name = "李四", 
                email = "lisi@example.com", 
                tags = listOf("工作", "开发小组"),
                shareAlarmId = "lisi888",
                remarkName = "李工",
                phoneNumber = "13800138001",
                memo = "后端开发负责人",
                createdAt = 1530403200000L // 2018-07-01
            ),
            User(
                id = "user_2", 
                name = "王五", 
                email = "wangwu@example.com", 
                tags = listOf("工作", "测试小组"),
                shareAlarmId = "wangwu666",
                remarkName = "王经理",
                phoneNumber = "13900139002",
                memo = "测试部经理",
                createdAt = 1561939200000L // 2019-07-01
            ),
            User(
                id = "user_3", 
                name = "赵六", 
                email = "zhaoliu@example.com", 
                tags = listOf("家人"),
                shareAlarmId = "zhaoliu520",
                remarkName = "表弟",
                phoneNumber = "13700137003",
                memo = "在老家工作",
                createdAt = 1593561600000L // 2020-07-01
            ),
            User(
                id = "user_4", 
                name = "孙七", 
                email = "sunqi@example.com", 
                tags = listOf("健身"),
                shareAlarmId = "sunqi_gym",
                remarkName = "孙教练",
                phoneNumber = "13600136004",
                memo = "周三周五去健身",
                createdAt = 1625097600000L // 2021-07-01
            ),
            User(
                id = "user_5", 
                name = "周八", 
                email = "zhouba@example.com", 
                tags = listOf("工作", "开发小组"),
                shareAlarmId = "zhouba_code",
                remarkName = "小周",
                phoneNumber = "13500135005",
                memo = "前端开发",
                createdAt = 1656633600000L // 2022-07-01
            ),
            User(
                id = "user_6", 
                name = "钱九", 
                email = "qianjiu@example.com", 
                tags = listOf("家人", "健身"),
                shareAlarmId = "qianjiu_fam",
                remarkName = "姑姑",
                phoneNumber = "13400134006",
                memo = "喜欢瑜伽",
                createdAt = 1688169600000L // 2023-07-01
            ),
            User(
                id = "user_7", 
                name = "吴十", 
                email = "wushi@example.com", 
                tags = listOf("测试小组"),
                shareAlarmId = "wushi_test",
                remarkName = "吴测试",
                phoneNumber = "13300133007",
                memo = "自动化测试",
                createdAt = 1719792000000L // 2024-07-01
            )
        ) + (8..200).map { i ->
            val familyNames = listOf("赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈", "褚", "卫", "蒋", "沈", "韩", "杨", "朱", "秦", "尤", "许", "何", "吕", "施", "张", "孔", "曹", "严", "华", "金", "魏", "陶", "姜")
            val givenNames = listOf("伟", "刚", "勇", "毅", "俊", "峰", "强", "军", "平", "保", "东", "文", "辉", "力", "明", "永", "健", "世", "广", "志", "义", "兴", "良", "海", "山", "仁", "波", "宁", "贵", "福", "生", "龙", "元", "全", "国", "胜", "学", "祥", "才", "发", "武", "新", "利", "清", "飞", "彬", "富", "顺", "信", "子", "杰", "涛", "昌", "成", "康", "星", "光", "天", "达", "安", "岩", "中", "茂", "进", "林", "有", "坚", "和", "彪", "博", "诚", "先", "敬", "震", "振", "壮", "会", "思", "群", "豪", "心", "邦", "承", "乐", "绍", "功", "松", "善", "厚", "庆", "磊", "民", "友", "裕", "河", "哲", "江", "超", "浩", "亮", "政", "谦", "亨", "奇", "固", "之", "轮", "翰", "朗", "伯", "宏", "言", "若", "鸣", "朋", "斌", "梁", "栋", "维", "启", "克", "伦", "翔", "旭", "鹏", "泽", "晨", "辰", "士", "以", "建", "家", "致", "树", "炎", "德", "行", "时", "泰", "盛")
            val randomName = familyNames.random() + givenNames.random() + (if (i % 2 == 0) givenNames.random() else "")
            
            User(
                id = "user_$i",
                name = randomName,
                email = "user$i@example.com",
                tags = listOf("模拟数据"),
                shareAlarmId = "id_$i",
                remarkName = "",
                phoneNumber = "139${String.format("%08d", i)}",
                memo = "这是第 $i 个模拟联系人",
                createdAt = System.currentTimeMillis()
            )
        }).sortedBy { it.name }
    )
    val contacts: StateFlow<List<User>> = _contacts.asStateFlow()

    // 群组列表
    private val _groups = MutableStateFlow<List<Group>>(
        listOf(
            Group("group_1", "开发小组", listOf("user_1", "user_5")),
            Group("group_2", "测试小组", listOf("user_2", "user_7"))
        )
    )
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    // 标签列表 (从联系人中提取去重)
    val tags: StateFlow<List<String>> = MutableStateFlow(listOf("工作", "家人", "健身", "开发小组", "测试小组"))

    // 提醒列表
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    init {
        // 初始化一些模拟提醒数据
        val calendar = Calendar.getInstance()
        
        // 今天未来的事件
        calendar.set(Calendar.HOUR_OF_DAY, 21)
        calendar.set(Calendar.MINUTE, 10)
        val todayEvent = createMockReminder(
            title = "团队会议讨论项目进度",
            time = calendar.time,
            creator = _currentUser.value,
            participants = listOf(_contacts.value[0]),
            isRead = true
        )

        // 明天的事件
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        val tomorrowEvent1 = createMockReminder(
            title = "健身房锻炼",
            time = calendar.time,
            creator = _contacts.value[0], // 李四创建
            participants = listOf(_currentUser.value),
            isRead = false // 未读
        )

        calendar.set(Calendar.HOUR_OF_DAY, 18)
        calendar.set(Calendar.MINUTE, 30)
        val tomorrowEvent2 = createMockReminder(
            title = "晚餐约会",
            time = calendar.time,
            creator = _currentUser.value,
            participants = listOf(_contacts.value[1]),
            isRead = true
        )

        // 过期事件
        calendar.add(Calendar.DAY_OF_YEAR, -2) // 昨天
        calendar.set(Calendar.HOUR_OF_DAY, 10)
        val expiredEvent = createMockReminder(
            title = "完成月度报告",
            time = calendar.time,
            creator = _contacts.value[1], // 王五创建
            participants = listOf(_currentUser.value),
            isRead = false // 未读
        )

        _reminders.value = listOf(todayEvent, tomorrowEvent1, tomorrowEvent2, expiredEvent)
    }

    // 辅助方法：创建模拟提醒
    private fun createMockReminder(
        title: String, 
        time: Date, 
        creator: User, 
        participants: List<User> = emptyList(),
        isRead: Boolean = false
    ): Reminder {
        return Reminder(
            id = UUID.randomUUID().toString(),
            title = title,
            description = "这是一个模拟的提醒事项详情描述。",
            eventTime = time,
            creator = creator.name, // 简化，直接存名字
            participants = participants.map { it.id },
            isRead = isRead,
            alertTimes = listOf(
                Date(time.time - 15 * 60 * 1000), // 提前15分钟
                Date(time.time - 30 * 60 * 1000)  // 提前30分钟
            ),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    // 添加新提醒
    fun addReminder(title: String, time: Date, note: String, participantIds: List<String>, alertOffsets: List<Int> = listOf(15)): Reminder {
        val newReminder = Reminder(
            id = UUID.randomUUID().toString(),
            title = title,
            description = note,
            eventTime = time,
            creator = _currentUser.value.name,
            participants = participantIds,
            isRead = true, // 自己创建的默认已读
            alertTimes = alertOffsets.map { Date(time.time - it * 60 * 1000L) }, // 根据偏移量列表计算提醒时间
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        // 关键修复：确保使用新的 List 实例触发 StateFlow 更新
        val currentList = _reminders.value.toMutableList()
        currentList.add(newReminder)
        _reminders.value = currentList
        return newReminder
    }

    // 更新提醒
    fun updateReminder(id: String, title: String, time: Date, note: String, participantIds: List<String>, alertOffsets: List<Int>) {
        val currentList = _reminders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            val oldReminder = currentList[index]
            val updatedReminder = oldReminder.copy(
                title = title,
                eventTime = time,
                description = note,
                participants = participantIds,
                alertTimes = alertOffsets.map { Date(time.time - it * 60 * 1000L) },
                updatedAt = System.currentTimeMillis(),
                isRead = false // 重置未读状态，以便显示更新提示
            )
            currentList[index] = updatedReminder
            _reminders.value = currentList
        }
    }
    
    // 标记为已读
    fun markAsRead(id: String) {
        val currentList = _reminders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            val updatedReminder = currentList[index].copy(isRead = true)
            currentList[index] = updatedReminder
            _reminders.value = currentList
        }
    }

    // 删除提醒
    fun deleteReminder(id: String) {
        _reminders.value = _reminders.value.filter { it.id != id }
    }
    
    // 获取单个提醒详情
    fun getReminderById(id: String): Reminder? {
        return _reminders.value.find { it.id == id }
    }
    
    // 获取用户详情
    fun getUserById(id: String): User? {
        if (id == _currentUser.value.id) return _currentUser.value
        return _contacts.value.find { it.id == id }
    }

    // 更新当前用户信息
    fun updateCurrentUser(user: User) {
        _currentUser.value = user
    }

    // 更新伙伴信息
    fun updatePartner(partner: User) {
        val currentList = _contacts.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == partner.id }
        if (index != -1) {
            currentList[index] = partner
            _contacts.value = currentList
        }
    }

    // 添加标签
    fun addTag(tag: String) {
        val currentTags = tags.value.toMutableList()
        if (!currentTags.contains(tag)) {
            currentTags.add(tag)
            (tags as MutableStateFlow).value = currentTags
        }
    }

    // 删除标签
    fun deleteTag(tag: String) {
        val currentTags = tags.value.toMutableList()
        if (currentTags.contains(tag)) {
            currentTags.remove(tag)
            (tags as MutableStateFlow).value = currentTags
        }
    }

    // 更新标签
    fun updateTag(oldTag: String, newTag: String) {
        val currentTags = tags.value.toMutableList()
        val index = currentTags.indexOf(oldTag)
        if (index != -1 && !currentTags.contains(newTag)) {
            currentTags[index] = newTag
            (tags as MutableStateFlow).value = currentTags
        }
    }

    // 添加评论
    fun addComment(reminderId: String, comment: Comment) {
        val currentList = _reminders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == reminderId }
        if (index != -1) {
            val oldReminder = currentList[index]
            val updatedReminder = oldReminder.copy(
                comments = oldReminder.comments + comment
            )
            currentList[index] = updatedReminder
            _reminders.value = currentList
        }
    }

    // 触发冒泡
    fun toggleBubble(reminderId: String, userId: String) {
        val currentList = _reminders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == reminderId }
        if (index != -1) {
            val oldReminder = currentList[index]
            if (!oldReminder.bubbles.contains(userId)) {
                val updatedReminder = oldReminder.copy(
                    bubbles = oldReminder.bubbles + userId
                )
                currentList[index] = updatedReminder
                _reminders.value = currentList
            }
        }
    }

    // 添加查看记录
    fun addViewed(reminderId: String, userId: String) {
        val currentList = _reminders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == reminderId }
        if (index != -1) {
            val oldReminder = currentList[index]
            if (!oldReminder.viewedBy.contains(userId)) {
                val updatedReminder = oldReminder.copy(
                    viewedBy = oldReminder.viewedBy + userId
                )
                currentList[index] = updatedReminder
                _reminders.value = currentList
            }
        }
    }

    // 取消提醒
    fun cancelReminder(reminderId: String) {
        val currentList = _reminders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == reminderId }
        if (index != -1) {
            val updatedReminder = currentList[index].copy(isCancelled = true)
            currentList[index] = updatedReminder
            _reminders.value = currentList
        }
    }

    // 恢复提醒
    fun restoreReminder(reminderId: String) {
        val currentList = _reminders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == reminderId }
        if (index != -1) {
            val updatedReminder = currentList[index].copy(isCancelled = false)
            currentList[index] = updatedReminder
            _reminders.value = currentList
        }
    }
}
