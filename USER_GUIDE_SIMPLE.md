# Steve AI 简化指令手册

## 极简指令 - 无需记忆，即说即做

Steve AI 现在支持**极简自然语言指令**，无需记忆复杂的命令格式，像和朋友说话一样简单！

---

## 一句话指令

### 🏠 建筑指令

| 你说 | Steve 做 |
|------|---------|
| `build` | 建造一座随机风格的房屋 |
| `build a house` | 建造房屋 |
| `build home` | 建造家园 |
| `make a castle` | 建造城堡 |
| `create a tower` | 建造塔楼 |

> **智能特性**：每次建造都会随机选择不同的建筑风格（橡木、石砖、沙漠、丛林等20+种风格），确保每个建筑都独一无二！

---

### 🌲 收集指令

| 你说 | Steve 做 |
|------|---------|
| `collect everything` | 收集周围所有资源 |
| `gather all` | 收集所有物资 |
| `get stuff` | 收集所有东西 |
| `get wood` | 砍伐周围所有树木 |
| `chop trees` | 砍树获取木头 |
| `get flowers` | 采集周围所有花朵 |
| `get mushrooms` | 采集所有蘑菇 |

> **智能特性**：自动搜索玩家周围100×100格范围，收集所有可用资源！

---

### ⚔️ 战斗指令

| 你说 | Steve 做 |
|------|---------|
| `fight` | 清除周围所有敌对生物 |
| `kill mobs` | 杀怪保护玩家 |
| `attack` | 攻击怪物 |
| `defend` | 保护玩家 |
| `clear enemies` | 清除敌人 |

> **智能特性**：以保护玩家为核心，自动发现并清除玩家周围所有威胁！

---

### 👣 跟随指令

| 你说 | Steve 做 |
|------|---------|
| `follow` | 跟随玩家 |
| `follow me` | 跟我来 |
| `come` | 过来 |
| `come here` | 来这里 |
| `stop` | 停止当前动作 |

---

### ⛏️ 挖矿指令

| 你说 | Steve 做 |
|------|---------|
| `mine` | 挖掘铁矿石 |
| `dig` | 去挖矿 |
| `mine diamonds` | 挖掘钻石 |
| `find gold` | 寻找金矿 |

---

## 自然语言也支持

Steve 能理解各种自然语言描述：

```
"帮我建个房子"
"收集所有东西"
"打怪保护我"
"跟我走"
"去挖矿"
"砍些树"
```

---

## 快速参考卡

```
┌─────────────────────────────────────┐
│  🏠 build        → 建造房屋         │
│  🌲 collect      → 收集所有资源     │
│  ⚔️ fight        → 清除怪物         │
│  👣 follow       → 跟随玩家         │
│  ⛏️ mine         → 挖矿             │
│  🛑 stop         → 停止动作         │
└─────────────────────────────────────┘
```

---

## 多 Steve 协作

多个 Steve 可以同时工作：

```
/steve spawn Builder1
/steve spawn Builder2
/steve spawn Guard

/steve tell Builder1 build
/steve tell Builder2 build    ← 自动协作建造
/steve tell Guard fight       ← 同时保护玩家
```

---

## 基础控制命令

```
/steve spawn <名字>     - 生成 Steve
/steve tell <名字> <指令> - 发送指令
/steve list             - 查看所有 Steve
/steve stop <名字>      - 停止动作
/steve remove <名字>    - 移除 Steve
```

---

## 使用示例

### 场景1：快速建造
```
/steve spawn Bob
/steve tell Bob build
```
✅ Bob 会立即开始建造一座随机风格的房屋

### 场景2：资源收集
```
/steve spawn Alice
/steve tell Alice collect everything
```
✅ Alice 会收集周围所有有用的资源

### 场景3：战斗保护
```
/steve spawn Guard
/steve tell Guard fight
```
✅ Guard 会清除周围所有敌对生物，保护玩家

### 场景4：组合任务
```
/steve spawn Builder
/steve spawn Miner
/steve spawn Fighter

/steve tell Builder build
/steve tell Miner get wood
/steve tell Fighter fight
```
✅ 三个 Steve 同时工作：建造、收集、保护

---

## 智能特性

### 🎨 建筑风格多样化
- 每次建造自动随机选择风格
- 20+ 种预设风格（橡木、石砖、沙漠、丛林、樱花等）
- 多 Steve 协作时自动分配不同风格

### 🏡 完整建筑结构
- 门、窗、屋顶齐全
- 室内家具（床、工作台、箱子、熔炉）
- 室外花园/院子
- 照明系统

### 🎯 智能搜索
- 建筑：自动寻找合适地点
- 收集：搜索100×100格范围
- 战斗：以保护玩家为中心

### 🤝 自动协作
- 多 Steve 自动分工
- 同时建造不同部分
- 效率大幅提升

---

## 配置文件

如需调整搜索范围等参数，编辑 `config/steve-common.toml`：

```toml
[search]
combatRadius = 50    # 战斗搜索半径（10-200）
gatherRadius = 50    # 采集搜索半径（10-200）

[building]
enableMultipleStyles = true  # 启用多样化风格
```

---

## 常见问题

**Q: 只说 "build" 会建什么？**  
A: 会建造一座随机风格的房屋，每次都不一样！

**Q: "collect everything" 会收集什么？**  
A: 会收集周围所有资源：木头、花朵、蘑菇、浆果、农作物等。

**Q: 多个 Steve 会重复建造吗？**  
A: 不会！多个 Steve 会自动协作，分工建造不同部分。

**Q: 建筑位置在哪里？**  
A: Steve 会在玩家视线方向约12格处自动寻找合适地点建造。

**Q: 需要给 Steve 提供材料吗？**  
A: 不需要！建筑使用预设材料自动生成。

---

> 💡 **提示**：Steve AI 使用大语言模型理解指令，描述越清晰，执行效果越好。试试用自然语言和 Steve 交流吧！
