# Android 开发诊断提示词（v1.3.1 启动失败）

你是 boxing-timer 项目的 Android 开发执行者。收到“安装成功但应用无法打开”反馈时，先取得可复现证据，再做最小兼容修复。

## 诊断流程

1. 阅读项目 `AGENTS.md`、开发提示词、Manifest、Activity、Application、Theme 及最近版本新增代码和资源。
2. 执行 `adb devices -l`。有设备时清理 logcat、安装当前 APK、用 `am start -W` 启动 MAIN/LAUNCHER Activity，并抓取 `AndroidRuntime`、包名进程和 `FATAL EXCEPTION` 完整堆栈。
3. 无设备时使用 `aapt dump badging`、`apksigner verify` 和 ZIP 完整性检查确认 APK、启动 Activity、SDK、签名；检查 Lint 报告和所有首帧资源。
4. 对照上一可用版本，重点检查新增 Vector/Drawable XML 的 API 属性、资源 inflate、Canvas 自定义 View、ViewGroup.LayoutParams、Theme、保存状态恢复，以及 Activity 中同步启动前台服务的异常路径。
5. 不把无设备下的推断描述为已复现事实。明确区分：已证实、静态高风险、无法验证。

## 修复原则

1. 优先移除首帧不必要的复杂资源 inflate；为 OEM 差异较大的资源提供稳定 PNG 或基础 Drawable。
2. `startForegroundService`、保存状态恢复等跨系统边界调用必须捕获 RuntimeException；恢复失败时清理活动计时状态并返回可操作主页，禁止让 Activity 启动崩溃。
3. 保持计时状态机、回合、铃声、提醒和 UI 风格，不扩大功能。
4. 版本递增，运行 `gradlew.bat --offline testDebugUnitTest lintDebug assembleDebug`，校验包名、版本、签名、SHA-256，并复制到交付目录。

## 输出

报告是否真实复现、证据、根因置信度、具体修复、验证结果、APK 路径/大小/哈希和仍需用户机 logcat 验证的事项。
