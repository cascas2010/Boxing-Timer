# Android 开发修复提示词（v1.3.2 首屏分层）

你是 boxing-timer 项目的 Android 开发执行者。针对“应用能启动，但参数控件和开始按钮不在首屏，用户误认为卡死”的问题，必须以真机截图和 Android 测量规则定位，不能只依赖编译成功。

## 诊断重点

1. 检查 `ScrollView`、`fillViewport`、纵向 `LinearLayout`、`FrameLayout` 与 `MATCH_PARENT/WRAP_CONTENT` 的组合。
2. 如果一个 `wrap_content` 容器内含 `MATCH_PARENT` 背景 View，检查它在 ScrollView 的非约束方向是否被测量成整个 viewport，从而把后续控件推到屏幕下方。
3. 从截图核对背景元素比例：若聚光灯延伸整屏、绳索位于屏幕底端而参数区消失，说明品牌头/背景容器实际占满 viewport。
4. 检查背景 View 是否作为 ScrollView 内容、是否位于前景控件之后覆盖内容、是否 clickable/focusable 并拦截触摸。

## 修复要求

1. Activity 根布局使用明确的 `FrameLayout` 两层结构：全屏背景 View 为底层；透明 `ScrollView` 为上层兄弟。
2. 背景 View 不得加入纵向内容容器，不得参与可滚动内容高度；显式设置不可点击、不可聚焦并从无障碍树中排除。
3. 前景 `LinearLayout` 仅包含品牌、参数、计时牌和操作按钮；品牌区内容驱动高度。
4. 首页四个输入项与开始按钮必须属于前景 ScrollView；计时页倒计时、暂停和结束按钮同样属于前景。
5. 在运行时加入布局契约检查：背景与 ScrollView 必须同属根 FrameLayout，内容根必须属于 ScrollView，关键控件必须可见且是内容根的后代。
6. 保留回合、铃声、后台服务、提醒、完成弹窗、服务异常降级等已验收行为。

## 验证与交付

1. 有设备时使用布局检查或截图证明首页与计时页；无设备时明确说明未做真机运行。
2. 执行 `gradlew.bat --offline testDebugUnitTest lintDebug assembleDebug`。
3. 校验包名、版本、签名、ZIP、SHA-256，复制版本化 APK 到交付目录。
