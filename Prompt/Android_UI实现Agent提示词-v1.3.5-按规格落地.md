# Android UI 实现 Agent 提示词 v1.3.5

你负责拳击计时器 Android 项目的 UI 高保真复刻，不负责修改计时规则。

必须读取并执行：

- `D:\WangDao\CodexProject\boxing-timer\Prompt\v1.3.5-ui-promt.md`
- `D:\WangDao\CodexProject\boxing-timer\测试报告\UI视觉规格-v1.3.5.md`
- 参考图：`D:\WangDao\CodexProject\boxing-timer\source\Codex 图像 2026年9月23日 16_12_42.png`

实现边界：

- 只允许修改 UI 层、视觉绘制和版本号。
- 不得修改 `TimerService`、`TimerState`、`CuePolicy`、`StageBellPattern` 的计时规则。
- 不允许把整张参考图当不可交互背景。
- 不允许出现默认 Android 按钮、输入框边框或 Material 卡片外观。
- 交互热区必须和绘制出来的按钮、箭头、输入框对齐。

实现目标：

- 首页还原参考图左侧手机内 UI：像素标题、皇冠、拳套、体育馆灯光、观众席、金属参数面板、四行设置、上下箭头、黄色开始按钮、擂台和两名小拳手。
- 训练页还原参考图右侧手机内 UI：顶部海报/月桂/回合牌、巨型黄色七段数码管、训练/休息状态、背向拳手、擂台、蓝色暂停按钮、红色结束按钮。
- 使用 Canvas 硬边像素绘制，`Paint.setAntiAlias(false)`。
- 版本升级到 `1.3.5` / `versionCode 9`。

交付前必须：

- `gradlew.bat --offline assembleDebug` 成功。
- 保留视觉差异说明。
- 留给 QA agent 执行功能测试和最终打包验收。
