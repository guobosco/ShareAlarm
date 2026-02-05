# UI 质感提升计划

## 1. 视觉层次增强 (Enhance Visual Hierarchy)
- **微边框回归 (Hairline Border)**:
  - 在 `EventCardContent` 中重新引入边框。
  - **关键调整**: 设置宽度为 `0.5.dp`，颜色使用 `MaterialTheme.colorScheme.outlineVariant` 并设置透明度为 **30% (alpha = 0.3f)**。
  - **目的**: 增加卡片边缘的锐利度和精致感，防止卡片在浅色背景上“晕开”，同时避免旧版边框的生硬感。

## 2. 光影优化 (Lighting & Depth)
- **提升阴影深度**:
  - 将 `Card` 的 `elevation` 从 `2.dp` 提升至 **3.dp**。
  - **目的**: 增强卡片的悬浮感，使其与背景的分离度更高，营造更强的立体质感。

## 3. 验证 (Verification)
- 确认修改后的卡片在亮色模式下既清晰又有层次，不会显得“脏”或“重”。
- 确保代码改动仅限于 `HomeScreen.kt` 的 `EventCardContent` 组件。
