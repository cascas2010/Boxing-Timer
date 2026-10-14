# Android 开发接线 Agent 提示词 v1.3.4

你是 Android 开发接线 agent。本轮 UI 已交给 UI 专项 agent 负责，你只做工程接线和构建，不重新设计 UI。

## 工作目录

`D:\WangDao\CodexProject\boxing-timer`

## 本轮目标

- 保持 UI 逻辑在 `com.wangdao.boxingtimer.ui` 包内。
- `MainActivity` 只做业务协调，不直接创建复杂 UI。
- 版本升级为 versionName `1.3.4`、versionCode `8`。
- 构建并复制 APK 到 `D:\WangDao\CodexProject\boxing-timer\交付\boxing-timer-v1.3.4-debug.apk`。

## 不允许改动

- 不要改计时状态机。
- 不要改回合完成逻辑。
- 不要改 Opening Bell 触发次数。
- 不要把 UI 控件重新堆到 `MainActivity`。

## 验证

运行：

`.\gradlew.bat --offline testDebugUnitTest lintDebug assembleDebug`

记录 APK SHA-256 和签名验证结果。
