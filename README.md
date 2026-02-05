# ShareAlarm（共享提醒/闹钟）

一个现代化的共享提醒/闹钟 Android 应用示例，采用 Jetpack Compose 构建精美界面。目前项目处于演示阶段，使用本地模拟数据展示核心交互流程。

## ✨ 功能概览

### 核心功能

- **⏰ 智能时间轴**：以时间轴形式展示今日、明日及未来的提醒事项。
- **👥 伙伴/联系人**：模拟多用户环境，展示联系人列表。
- **📝 提醒管理**：
  - 创建个性化提醒（支持标题、时间、备注）。
  - 支持设置多个提醒时间点（如提前15分钟、30分钟）。
  - 标记提醒为已读/未读。
  - 左滑删除提醒。
  - 过期提醒自动归类折叠。
- **🎨 现代化 UI**：
  - 采用 Material3 设计语言。
  - 细腻的交互动画（侧滑删除、展开/收起）。
  - 适配不同屏幕尺寸。
- **🔔 系统集成**：
  - 自动请求并处理 Android 12+ 精确闹钟权限。
  - 自动请求 Android 13+ 通知权限。

## 🛠 技术栈

| 技术 | 说明 |
|------|------|
| **Kotlin** | 1.9.0+，主要开发语言 |
| **Jetpack Compose** | 声明式 UI 框架 (Material3) |
| **StateFlow** | 响应式状态管理 |
| **Navigation Compose** | 单 Activity 多页面导航 |
| **AlarmManager** | 系统级闹钟服务集成 |
| **Mock Data** | 本地模拟数据源 (Singleton Pattern) |

## 📂 项目结构

```
app/src/main/java/com/example/sharealarm/
├── data/
│   ├── local/
│   │   └── MockDataStore.kt      # 全局模拟数据源 (User, Reminder, Contacts)
│   └── model/                    # 数据实体模型 (User, Reminder, Organization)
├── service/                      # 后台服务
│   ├── AlarmReceiver.kt          # 接收系统闹钟广播
│   ├── AlarmScheduler.kt         # 闹钟调度逻辑
│   ├── AlarmService.kt           # 闹钟后台服务
│   └── NotificationService.kt    # 通知栏通知管理
├── ui/
│   ├── navigation/               # 路由导航配置
│   ├── screen/                   # 页面组件
│   │   ├── HomeScreen.kt         # 首页 (时间轴、FAB)
│   │   ├── CreateReminderScreen.kt # 创建提醒页
│   │   ├── ContactsScreen.kt     # 联系人/伙伴页
│   │   └── ReminderDetailScreen.kt # 提醒详情页
│   ├── theme/                    # 主题样式 (Color, Type, Theme)
│   └── MainActivity.kt           # 应用入口
└── ShareAlarmApplication.kt      # 全局 Application
```

## 🚀 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17 或更高版本
- Android SDK API 24 (Min) - 34 (Target)

### 运行步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd ShareAlarm
   ```

2. **打开项目**
   - 使用 Android Studio 打开项目根目录。
   - 等待 Gradle Sync 完成。

3. **运行**
   - 连接 Android 真机或启动模拟器。
   - 点击运行按钮 (Run 'app')。
   - 无需配置任何云服务密钥，项目默认使用 `MockDataStore` 加载演示数据。

## 📱 页面展示

- **首页 (Home)**: 展示欢迎语、过期事件折叠栏、按日期分组的未来提醒时间轴。
- **创建提醒 (Create)**: 设置标题、时间、备注，选择参与人（模拟）。
- **联系人 (Contacts)**: 展示好友列表。
- **详情页 (Detail)**: 查看提醒详细信息。

## 📅 开发计划

- [x] 完成基于 Compose 的现代化 UI 框架搭建
- [x] 实现本地模拟数据流 (MockDataStore)
- [x] 实现基础的提醒增删查改 (CRUD) 逻辑
- [x] 集成 System AlarmManager 和 Notification
- [ ] 接入 Room 本地数据库持久化
- [ ] 接入后端云服务 (如 Cloudbase/Firebase) 实现多端同步
- [ ] 完善用户登录注册流程

## 🤝 贡献

欢迎提交 Issue 或 Pull Request 来改进这个项目！

## 📄 许可证

MIT License
