# ShareAlarm（小秘书）

一个以组织为单位的共享提醒/闹钟 Android 应用示例，采用 Jetpack Compose 构建界面，并以 ViewModel + Repository 的分层方式组织业务逻辑。项目同时预留了 Cloudbase 与 Firebase 两套云服务实现。

## 功能概览

- 登录与注册界面（Compose UI）
- 组织管理：创建组织、加入组织、退出组织
- 共享提醒：创建提醒（事件名称、描述、地点、事件时间、提醒时间列表、提醒人员）
- 首页入口：组织列表展示、创建/加入组织入口、创建提醒入口
- 闹钟触发与通知：AlarmReceiver/AlarmService + NotificationService

## 技术栈与依赖

- Kotlin + Android Jetpack
- Jetpack Compose（Material3）
- Navigation Compose
- Room（本地数据库结构已定义）
- Cloudbase SDK（依赖当前被注释）
- Firebase（代码存在，依赖需自行补充）

## 架构与模块

- UI 层：`ui/`（Compose 页面与导航）
- ViewModel 层：`data/viewmodel/`（状态管理与业务编排）
- Repository 层：`data/repository/`（业务聚合与数据源协调）
- 数据层：
  - `data/remote/`（Cloudbase/Firebase 云服务实现）
  - `data/local/`（Room 数据库实体与 DAO）
  - `data/model/`（核心数据模型）
- 服务层：`service/`（闹钟触发、通知、推送服务）

## 目录结构

```
app/src/main/java/com/example/sharealarm/
├─ ui/                    # Compose 页面与导航
├─ data/
│  ├─ model/              # Reminder/Organization/User
│  ├─ viewmodel/          # Auth/Organization/Reminder ViewModel
│  ├─ repository/         # Repository 封装
│  ├─ remote/             # Cloudbase/Firebase 数据与认证
│  └─ local/              # Room 数据库与 DAO
├─ service/               # 闹钟与通知服务
└─ ShareAlarmApplication  # 应用入口初始化
```

## 运行与构建

1. 使用 Android Studio 打开项目根目录。
2. 同步 Gradle 依赖。
3. 选择 `app` 模块并运行。

也可以使用命令构建：

```
./gradlew assembleDebug
```

## 云服务配置

### Cloudbase

1. 在 `data/remote/CloudbaseInitializer.kt` 中配置 `CLOUDBASE_APP_ID` 与 `CLOUDBASE_ENV_ID`。
2. 在 `app/build.gradle.kts` 中取消 Cloudbase 依赖的注释。

### Firebase

当前代码包含 Firebase 认证与数据库实现，但 Gradle 依赖与 `google-services.json` 尚未配置。如需使用，请补齐：

- Firebase BOM 与对应模块依赖
- `google-services.json` 与相关插件

## 权限说明

- `INTERNET`：网络访问
- `VIBRATE`：振动提醒
- `RECEIVE_BOOT_COMPLETED`：设备重启后接收系统广播
- `SCHEDULE_EXACT_ALARM`：精确闹钟
- `POST_NOTIFICATIONS`：通知权限（Android 13+）

## 入口与页面

- 应用入口：`MainActivity` + `AppNavHost`
- 主要页面：
  - `SignInScreen` / `SignUpScreen`
  - `HomeScreen`
  - `CreateOrganizationScreen` / `JoinOrganizationScreen`
  - `CreateReminderScreen`

