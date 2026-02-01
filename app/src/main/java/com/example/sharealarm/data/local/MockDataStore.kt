package com.example.sharealarm.data.local

import com.example.sharealarm.data.model.User
import com.example.sharealarm.data.model.Reminder
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
            name = "张三",
            email = "zhangsan@example.com",
            photoUrl = ""
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // 联系人列表（伙伴）
    private val _contacts = MutableStateFlow<List<User>>(
        listOf(
            User("user_1", "李四", "lisi@example.com"),
            User("user_2", "王五", "wangwu@example.com"),
            User("user_3", "赵六", "zhaoliu@example.com"),
            User("user_4", "孙七", "sunqi@example.com"),
            User("user_5", "周八", "zhouba@example.com"),
            User("user_6", "钱九", "qianjiu@example.com"), // 增加一些联系人以测试滚动
            User("user_7", "吴十", "wushi@example.com")
        ).sortedBy { it.name } // 简单按名字排序
    )
    val contacts: StateFlow<List<User>> = _contacts.asStateFlow()

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
        _reminders.value = _reminders.value + newReminder
        return newReminder
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
}
