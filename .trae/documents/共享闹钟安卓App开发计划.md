# 共享闹钟安卓App开发计划

## 1. 技术栈选择

* **开发语言**：Kotlin

* **开发框架**：Jetpack Compose（用于UI）

* **后端服务**：Firebase（认证、数据库、云消息推送）

* **本地存储**：Room（本地数据库缓存）

* **依赖管理**：Gradle

* **UI设计**：Material Design 3 + NASA蓝主题

## 2. 项目架构设计

* **架构模式**：MVVM（Model-View-ViewModel）

* **分层结构**：

  * Presentation Layer（UI + ViewModel）

  * Domain Layer（业务逻辑）

  * Data Layer（数据访问）

## 3. 数据库设计

### Firebase Firestore 集合

* **users**：存储用户信息

  * uid: String

  * email: String

  * name: String

  * organizations: List<String>（加入的组织ID）

* **organizations**：存储组织信息

  * orgId: String

  * name: String

  * members: List<String>（成员UID）

  * creator: String

* **reminders**：存储提醒信息

  * reminderId: String

  * orgId: String

  * title: String

  * description: String

  * eventTime: Timestamp

  * location: String

  * alertTimes: List<Timestamp>（多个提醒时间点）

  * participants: List<String>（涉及人员UID）

  * creator: String

  * createTime: Timestamp

### Room 本地数据库

* **LocalReminder**：缓存提醒信息，用于本地闹钟触发

* **LocalUser**：缓存用户信息

* **LocalOrganization**：缓存组织信息

## 4. 主要功能模块

### 4.1 用户认证模块

* 邮箱/密码注册登录

* 谷歌账号登录

* 用户信息管理

### 4.2 组织管理模块

* 创建组织

* 加入组织（通过邀请码或搜索）

* 组织成员管理

* 退出组织

### 4.3 提醒管理模块

* 创建提醒

  * 事件名称、描述

  * 选择涉及人员（勾选）

  * 设置事件时间、地点

  * 添加多个提醒时间点

* 查看提醒列表

* 编辑/删除提醒

### 4.4 闹钟提醒模块

* 本地闹钟调度（使用AlarmManager）

* 多个时间点提醒支持

* 提醒通知显示

* 提醒响铃功能

### 4.5 消息通知模块

* 实时消息推送（使用Firebase Cloud Messaging）

* 新提醒通知

* 组织邀请通知

## 5. UI设计方案

### 5.1 主题色彩

* **主色调**：NASA蓝（#0B3D91）

* **辅助色**：浅蓝（#4A90E2）

* **背景色**：白色（#FFFFFF）

* **文字色**：深灰（#333333）、浅灰（#666666）

### 5.2 主要页面

#### 5.2.1 登录/注册页

* 简洁的表单设计

* NASA蓝主题按钮

* 第三方登录选项

#### 5.2.2 组织列表页

* 顶部导航栏（NASA蓝）

* 组织卡片列表

* 悬浮创建/加入组织按钮

#### 5.2.3 组织详情页

* 组织信息展示

* 成员列表

* 提醒列表

* 发布提醒按钮

#### 5.2.4 提醒创建页

* 分步表单设计

* 时间选择器（支持多个时间点）

* 人员选择器（勾选式）

* 地点选择器（地图集成）

#### 5.2.5 提醒详情页

* 提醒信息完整展示

* 编辑/删除按钮

* 参与人员列表

#### 5.2.6 闹钟提醒页

* 醒目的提醒内容

* 暂停/关闭按钮

* 重复提醒选项

## 6. 开发步骤

### 6.1 项目初始化

* 创建Android Studio项目

* 配置Gradle依赖

* 设置Firebase项目

* 配置Jetpack Compose

### 6.2 基础架构搭建

* 实现MVVM架构

* 创建基础数据模型

* 配置Room本地数据库

* 实现Firebase数据访问层

### 6.3 用户认证功能

* 实现登录/注册UI

* 集成Firebase Authentication

* 实现用户信息管理

### 6.4 组织管理功能

* 实现组织创建/加入UI

* 实现组织CRUD操作

* 实现成员管理

### 6.5 提醒管理功能

* 实现提醒创建/编辑UI

* 实现提醒CRUD操作

* 实现多个时间点选择功能

### 6.6 闹钟提醒功能

* 集成AlarmManager

* 实现本地闹钟调度

* 实现提醒通知

* 实现响铃功能

### 6.7 消息推送功能

* 配置Firebase Cloud Messaging

* 实现实时消息接收

* 实现通知显示

### 6.8 UI美化与测试

* 应用NASA蓝主题

* 优化UI交互

* 进行功能测试

* 性能优化

## 7. 预期交付物

* 完整的安卓App源码

* 编译好的APK文件

* 开发文档

## 8. 关键注意事项

* 确保闹钟在后台能正常触发

* 处理网络离线情况

* 优化电池使用

* 确保数据安全

* 提供良好的用户体验

