# ShareAlarm（小秘书）

一个以组织为单位的共享提醒/闹钟 Android 应用示例，采用 Jetpack Compose 构建现代化界面，并以 ViewModel + Repository 的分层方式组织业务逻辑。项目使用 Cloudbase 作为云服务实现，支持多用户协作管理提醒。

## 功能概览

### 核心功能

- 🔐 **用户认证**：支持邮箱登录与注册
- 🏢 **组织管理**：创建、加入、退出组织，实现团队协作
- ⏰ **共享提醒**：创建个性化提醒，支持多人员、多时间点提醒
- 📱 **响应式设计**：适配不同屏幕尺寸的 Material3 界面
- 📣 **智能通知**：系统闹钟 + 推送通知双重提醒机制
- 💾 **本地同步**：Room 数据库 + Cloudbase 云端数据同步

### 提醒特性

- 支持事件名称、描述、地点、事件时间
- 可设置多个提醒时间点
- 可选择组织内成员作为提醒接收者
- 系统闹钟精确触发
- 云端同步，多设备共享

## 技术栈与依赖

### 核心技术

| 技术 | 版本/说明 | 用途 |
|------|-----------|------|
| Kotlin | 1.9.0 | 主要开发语言 |
| Android SDK | API 24-34 | 应用运行环境 |
| Jetpack Compose | Material3 | 现代化UI框架 |
| Navigation Compose | 2.8.0 | 页面导航管理 |
| Room | 2.6.1 | 本地数据库 |
| ViewModel | 2.7.0 | 状态管理 |
| WorkManager | 2.9.0 | 后台任务管理 |

### 云服务

| 服务 | 版本 | 用途 |
|------|------|------|
| Cloudbase SDK | 1.9.0 | 身份认证、数据库、推送 |

## 架构设计

### MVVM 架构

项目采用 MVVM (Model-View-ViewModel) 架构模式，实现了清晰的分层设计：

1. **View 层**：使用 Jetpack Compose 构建 UI 组件，响应 ViewModel 中的状态变化
2. **ViewModel 层**：处理业务逻辑，管理 UI 状态，与 Repository 层交互
3. **Repository 层**：封装数据访问逻辑，协调本地数据库与云端服务
4. **Data 层**：包含本地 Room 数据库和 Cloudbase 云端服务实现

### 模块划分

- **UI 层**：`ui/`（Compose 页面与导航）
- **ViewModel 层**：`data/viewmodel/`（状态管理与业务编排）
- **Repository 层**：`data/repository/`（业务聚合与数据源协调）
- **数据层**：
  - `data/remote/`（Cloudbase 云服务实现）
  - `data/local/`（Room 数据库实体与 DAO）
  - `data/model/`（核心数据模型）
- **服务层**：`service/`（闹钟触发、通知、推送服务）

## 目录结构

### 根目录结构

| 文件夹/文件 | 作用 |
|------------|------|
| `.trae/` | Trae IDE 相关文件，包含项目开发计划文档 |
| `app/` | 主要应用源码目录，包含所有核心代码 |
| `gradle/` | Gradle 构建工具相关文件 |
| `.gitignore` | Git 忽略规则文件，指定不需要版本控制的文件 |
| `LICENSE` | 项目许可证文件，MIT 许可证 |
| `README.md` | 项目说明文档，包含功能介绍、技术栈、架构设计等 |
| `build.gradle.kts` | 项目级构建配置文件，定义项目依赖和插件 |
| `code_structure_diagram.md` | 代码结构和逻辑关系分析文档，包含 Mermaid 图表 |
| `gradle.properties` | Gradle 配置属性文件 |
| `gradlew`/`gradlew.bat` | Gradle 包装器脚本，用于在不同环境中运行 Gradle 命令 |
| `settings.gradle.kts` | Gradle 设置文件，定义项目模块 |

### `.trae/` 目录

| 文件夹/文件 | 作用 |
|------------|------|
| `documents/` | 存放项目相关文档 |
| `共享闹钟安卓App开发计划.md` | 项目开发计划文档，包含需求分析、功能规划、开发进度等 |

### `app/` 目录

#### `app/build.gradle.kts`
应用级构建配置文件，定义应用的依赖、编译选项、构建类型等。

#### `app/src/main/` 目录
应用主源码目录，包含 Java/Kotlin 代码、资源文件和 Android 配置文件。

##### `app/src/main/java/com/example/sharealarm/` 目录
Java/Kotlin 源码主目录，按照 MVVM 架构组织。

| 文件夹/文件 | 作用 |
|------------|------|
| `data/` | 数据层，包含本地和远程数据源 |
| `data/local/` | 本地数据库相关文件 |
| `AlarmDatabase.kt` | Room 数据库定义，包含表结构和 DAO 接口 |
| `AlarmDatabaseInstance.kt` | 数据库实例管理，提供单例访问 |
| `data/model/` | 数据模型类 |
| `Organization.kt` | 组织数据模型 |
| `Reminder.kt` | 提醒数据模型 |
| `User.kt` | 用户数据模型 |
| `data/remote/` | 远程服务相关文件 |
| `CloudbaseAuthService.kt` | Cloudbase 认证服务实现 |
| `CloudbaseDatabaseService.kt` | Cloudbase 数据库服务实现 |
| `CloudbaseInitializer.kt` | Cloudbase 初始化配置 |
| `data/repository/` | 数据仓库层，协调本地和远程数据源 |
| `AuthRepository.kt` | 认证数据仓库，处理用户登录注册 |
| `OrganizationRepository.kt` | 组织数据仓库，处理组织相关操作 |
| `ReminderRepository.kt` | 提醒数据仓库，处理提醒相关操作 |
| `data/viewmodel/` | ViewModel 层，管理 UI 状态和业务逻辑 |
| `AuthViewModel.kt` | 认证 ViewModel，管理登录注册状态 |
| `OrganizationViewModel.kt` | 组织 ViewModel，管理组织列表和操作 |
| `ReminderViewModel.kt` | 提醒 ViewModel，管理提醒列表和操作 |
| `service/` | 服务层，处理后台任务 |
| `AlarmReceiver.kt` | 闹钟广播接收器，接收系统闹钟触发事件 |
| `AlarmService.kt` | 闹钟服务，处理闹钟触发逻辑 |
| `CloudbaseMessagingService.kt` | Cloudbase 消息推送服务 |
| `NotificationService.kt` | 通知服务，负责显示系统通知 |
| `ui/` | UI 层，使用 Jetpack Compose 构建 |
| `ui/navigation/` | 导航相关文件 |
| `NavHost.kt` | 导航主机，定义应用导航结构 |
| `ui/screen/` | 应用屏幕组件 |
| `CreateOrganizationScreen.kt` | 创建组织页面 |
| `CreateReminderScreen.kt` | 创建提醒页面 |
| `HomeScreen.kt` | 首页，显示提醒和组织列表 |
| `JoinOrganizationScreen.kt` | 加入组织页面 |
| `SignInScreen.kt` | 登录页面 |
| `SignUpScreen.kt` | 注册页面 |
| `ui/theme/` | 应用主题相关文件 |
| `Color.kt` | 颜色定义 |
| `ShareAlarmTheme.kt` | 主题配置 |
| `Type.kt` | 字体样式定义 |
| `ui/MainActivity.kt` | 应用主活动，启动应用并加载 Compose UI |
| `ShareAlarmApplication.kt` | 应用自定义 Application 类，负责初始化应用级组件 |

##### `app/src/main/res/` 目录 - 资源文件

| 文件夹/文件 | 作用 |
|------------|------|
| `values/` | 资源值文件 |
| `colors.xml` | 颜色资源定义 |
| `strings.xml` | 字符串资源定义 |
| `themes.xml` | 主题资源定义 |

##### `app/src/main/AndroidManifest.xml`
Android 应用清单文件，定义应用权限、组件、主题等配置。

### `gradle/` 目录

| 文件夹/文件 | 作用 |
|------------|------|
| `wrapper/` | Gradle 包装器相关文件 |
| `gradle-wrapper.jar` | Gradle 包装器 Jar 文件 |
| `gradle-wrapper.properties` | Gradle 包装器配置文件，指定 Gradle 版本和下载地址 |

## 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 11 或更高版本
- Android SDK API 24 或更高版本
- Cloudbase 账户（用于云服务配置）

### 配置步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd ShareAlarm
   ```

2. **打开项目**
   - 使用 Android Studio 打开项目根目录
   - 等待 Gradle 同步完成

3. **配置 Cloudbase**
   - 登录 [Cloudbase 控制台](https://console.cloud.tencent.com/tcb)
   - 创建应用并获取 `APP_ID` 和 `ENV_ID`
   - 在 `data/remote/CloudbaseInitializer.kt` 中配置：
     ```kotlin
     private const val CLOUDBASE_APP_ID = "your-cloudbase-app-id"
     private const val CLOUDBASE_ENV_ID = "your-cloudbase-env-id"
     ```
   - 确保 `app/build.gradle.kts` 中 Cloudbase 依赖已启用

4. **运行应用**
   - 连接 Android 设备或启动模拟器
   - 选择 `app` 模块并点击运行按钮
   - 或使用命令行：
     ```bash
     ./gradlew assembleDebug
     ```

## 云服务配置详解

### Cloudbase 配置

#### 1. 控制台配置

1. 登录 [Cloudbase 控制台](https://console.cloud.tencent.com/tcb)
2. 创建新应用或使用现有应用
3. 在应用设置中获取 `APP_ID` 和 `ENV_ID`
4. 确保已启用以下服务：
   - 身份认证（支持邮箱登录）
   - 云数据库（用于存储用户、组织和提醒数据）
   - 推送服务（可选，用于消息推送）

#### 2. 数据库集合设计

应用需要在 Cloudbase 控制台创建以下集合：

- `users`：存储用户信息
- `organizations`：存储组织信息
- `reminders`：存储提醒信息

#### 3. 安全规则配置

建议配置以下安全规则：

```javascript
{
  "read": "auth != null",
  "write": "auth != null"
}
```

## 核心功能流程

### 用户认证流程

```
用户 -> SignInScreen/SignUpScreen -> AuthViewModel -> AuthRepository -> CloudbaseAuthService -> Cloudbase SDK
```

### 创建提醒流程

```
用户 -> CreateReminderScreen -> ReminderViewModel -> ReminderRepository -> CloudbaseDatabaseService
  -> 保存到 Cloudbase 数据库
  -> 保存到本地 Room 数据库
  -> 设置系统闹钟
  -> 显示成功通知
```

## 权限说明

应用需要以下权限：

| 权限 | 用途 | 自动授予 |
|------|------|----------|
| `INTERNET` | 网络访问 | 是 |
| `VIBRATE` | 振动提醒 | 是 |
| `RECEIVE_BOOT_COMPLETED` | 设备重启后重设闹钟 | 是 |
| `SCHEDULE_EXACT_ALARM` | 精确闹钟调度 | 否（需用户授权） |
| `POST_NOTIFICATIONS` | 发送通知 | 否（Android 13+ 需用户授权） |

## 页面导航

### 主要页面

- `SignInScreen` / `SignUpScreen`：登录注册页面
- `HomeScreen`：首页，展示组织列表和提醒
- `CreateOrganizationScreen`：创建组织页面
- `JoinOrganizationScreen`：加入组织页面
- `CreateReminderScreen`：创建提醒页面

### 导航结构

```
SignInScreen/SignUpScreen -> HomeScreen
  ├─ CreateOrganizationScreen
  ├─ JoinOrganizationScreen
  └─ CreateReminderScreen
```

## 开发指南

### 代码规范

- 遵循 Kotlin 官方代码风格
- 使用 Compose 函数式编程范式
- 每个 Composable 函数应保持单一职责
- ViewModel 中不包含 UI 逻辑
- Repository 层封装所有数据访问逻辑

### 测试

- 单元测试：使用 JUnit 和 Mockito 测试 ViewModel 和 Repository 层
- UI 测试：使用 Espresso 和 Compose Testing 测试 UI 组件

### 构建变体

- `debug`：开发调试版本
- `release`：发布版本，启用代码混淆

## 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 项目维护者：BoscoGuo
- 项目地址：https://github.com/guobosco/ShareAlarm/tree/main

## 更新日志

### v1.0.0

- 初始版本
- 支持用户认证
- 支持组织管理
- 支持共享提醒创建和同步
- 支持系统闹钟和通知
- 使用 Cloudbase 作为云服务

## 未来计划

- [ ] 支持更多登录方式（手机号、微信等）
- [ ] 支持提醒重复规则
- [ ] 支持提醒完成状态管理
- [ ] 支持组织成员管理
- [ ] 支持消息推送
- [ ] 优化性能和用户体验

---

**感谢使用 ShareAlarm！** 🎉

