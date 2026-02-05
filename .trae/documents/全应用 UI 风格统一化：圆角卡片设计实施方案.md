# UI 风格统一化改造计划 (Unified Design Style)

## 目标
将“我的伙伴”、“伙伴资料”、“个人资料”三个页面重构为与主页一致的 **“圆角悬浮卡片” (Rounded Floating Card)** 风格。

## 核心设计规范 (Design System)
- **容器 (Container)**: `Card` 组件
  - **圆角**: `RoundedCornerShape(16.dp)`
  - **阴影**: `elevation = 3.dp`
  - **边框**: `BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))`
  - **背景**: `MaterialTheme.colorScheme.surface` (纯白)
- **页面背景**: `MaterialTheme.colorScheme.background` (浅灰)
- **间距**: 列表内容水平内边距 `padding(horizontal = 16.dp)`

## 实施步骤

### 1. 改造 `ContactsScreen.kt` (我的伙伴)
- **功能入口区**: 将“新的伙伴”、“群组”、“标签”三个入口封装在一个单独的圆角卡片中。
- **联系人列表**: 按字母分组，每个字母组（Header + 联系人列表）封装在一个圆角卡片中。
- **布局调整**: 为 `LazyColumn` 添加水平内边距，使卡片悬浮于背景之上。

### 2. 改造 `PartnerProfileScreen.kt` (伙伴资料)
- **信息分组**: 将“账号信息”、“备注”、“更多信息”三个板块分别封装为独立的圆角卡片。
- **视觉优化**: 移除全宽分割线，改用卡片内部的内缩分割线；移除硬编码颜色，全面对接 `MaterialTheme`。

### 3. 改造 `UserInfoScreen.kt` (个人资料)
- **组件重构**: 将 `ProfileGroup` 从简单的 `Column` 重构为带有阴影和圆角的 `Card` 组件。
- **细节调整**: 增加卡片间的垂直间距，确保视觉呼吸感。

## 预期效果
所有二级页面将不再是传统的“通栏列表”样式，而是呈现出与主页一致的“悬浮卡片流”视觉，提升应用的整体精致感和现代感。
