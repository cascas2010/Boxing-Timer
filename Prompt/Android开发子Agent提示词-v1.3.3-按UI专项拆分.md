# Android 开发子 Agent 提示词 v1.3.3：按 UI 专项拆分接入

你是拳击计时器 Android 项目的开发 agent。本轮你不负责重新设计 UI 风格，必须严格执行 UI 专项 agent 的结构方案：

`D:\WangDao\CodexProject\boxing-timer\Prompt\Android_UI专项Agent提示词-v1.3.3.md`

## 本轮目标

把当前集中在 `MainActivity` 里的 UI 创建逻辑拆到独立 UI 类中，并保证首页进入后就是可操作主界面，视觉结构贴近参考图左侧，不再出现只有背景、logo、标题的封面式页面。

参考图：

`C:\Users\casca\.codex\generated_images\01a0c312-38d2-7370-b665-a3cc0c0b5eda\exec-99dbefcb-6c42-491b-a94b-1f38a3ebcba6.png`

## 必须遵守

1. 不改 `TimerState`、`TimerService`、`StageBellPattern`、`CuePolicy` 的业务语义。
2. 新建 UI 包：`com.wangdao.boxingtimer.ui`。
3. 至少拆出：
   - `ArcadeTimerTheme`
   - `ArcadeHomeView`
   - `ArcadeTimerView`
   - `ArenaBackdropView` 移入 ui 包或保持等价独立背景类
4. `MainActivity` 只负责：
   - 创建首页/倒计时页 UI view。
   - 读取 UI 输入并调用已有计时业务。
   - 把计时状态渲染给 UI view。
5. 首页必须直接显示四行输入和“开始训练”按钮，不能做启动页或封面页。
6. 背景 View 必须作为 `FrameLayout` 背景层，不能作为 `ScrollView` 内容。
7. 倒计时页必须直接显示回合、倒计时、状态、暂停、结束。

## 建议接口

`ArcadeHomeView`：

- `String getTrainingText()`
- `String getRestText()`
- `String getRoundsText()`
- `String getReminderText()`
- `void setOnStartTrainingClickListener(View.OnClickListener listener)`
- `void bindFromState(TimerState state)` 或等价方法

`ArcadeTimerView`：

- `void renderRound(int currentRound, int roundLimit)`
- `void renderTimer(String timeText, boolean training, boolean running)`
- `void setPauseText(String text)`
- `void setOnToggleClickListener(View.OnClickListener listener)`
- `void setOnEndClickListener(View.OnClickListener listener)`

`ArcadeTimerTheme`：

- 统一颜色、字号、dp 工具、按钮/文本样式。

## 验收前必须执行

在项目目录执行：

```powershell
.\gradlew.bat --offline testDebugUnitTest lintDebug assembleDebug
```

如果本机没有真机或模拟器，不要声称“真机 UI 验收通过”，只能写“自动检查通过，真机截图待确认”。

## 交付

1. 新 APK 输出到：`D:\WangDao\CodexProject\boxing-timer\交付\boxing-timer-v1.3.3-debug.apk`
2. 更新版本号为 `1.3.3`。
3. 输出 SHA-256。
4. 报告改动文件清单、UI 拆分方式、测试命令结果。
