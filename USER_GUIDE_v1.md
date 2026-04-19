# Steve AI 游戏指令手册

## 一、简介和配置说明

### Steve AI Mod 是什么

Steve AI 是一个 Minecraft Forge Mod，为游戏引入了智能 AI 助手 Steve。这些 AI 助手能够理解自然语言指令，自动执行各种任务，包括建筑、采集资源、挖矿、战斗等。

### 基本工作流程

```
生成 Steve → 发送自然语言指令 → AI 解析并自动执行
```

1. **生成 Steve**：使用 `/steve spawn <名字>` 命令召唤一个 Steve
2. **发送指令**：使用 `/steve tell <名字> <指令>` 让 Steve 执行任务
3. **自动执行**：Steve 会自动解析指令并完成相应动作

> **提示**：Steve 使用大语言模型（LLM）理解指令，支持自然语言描述，无需记忆复杂的命令格式。

### 配置文件说明

Steve AI 使用 TOML 格式的配置文件 `config/steve-common.toml` 进行设置。

#### AI Provider 配置

支持以下 AI 提供商：

| Provider | 说明 | 推荐度 |
|---------|------|-------|
| **groq** | 免费、速度快 | ⭐⭐⭐⭐⭐ |
| **openai** | GPT 模型，稳定可靠 | ⭐⭐⭐⭐ |
| **gemini** | Google AI，免费额度充足 | ⭐⭐⭐⭐ |
| **qwen** | 通义千问，国内访问友好 | ⭐⭐⭐⭐ |

#### 通义千问（Qwen）配置示例

```toml
[ai]
provider = "qwen"

[qwen]
# 从阿里云 DashScope 控制台获取 API Key
# https://dashscope.aliyun.com/
apiKey = "your-api-key-here"

# 模型选择
model = "qwen3.6-flash"

# 最大 Token 数
maxTokens = 10000000

# 温度参数（0.0-2.0，越低越确定）
temperature = 0.7
```

**获取 API Key**：
1. 访问 [阿里云 DashScope](https://dashscope.aliyun.com/)
2. 注册/登录阿里云账号
3. 创建 API Key
4. 将 Key 复制到配置文件中

#### 搜索半径配置

在 `[search]` 节中可以配置战斗和采集的搜索范围：

```toml
[search]
# 战斗搜索半径（默认 50，范围 10-200）
# 50 = 100×100 区域
combatRadius = 50

# 采集搜索半径（默认 50，范围 10-200）
gatherRadius = 50
```

#### 建筑风格配置

在 `[building]` 节中可以自定义建筑风格：

```toml
[building]
# 是否启用多风格（默认 true）
enableMultipleStyles = true

# 启用的风格列表（逗号分隔）
styles = "oak_classic,spruce_cabin,birch_cottage,stone_fortress,sandstone_desert,dark_oak_manor,brick_house,jungle_hut"
```

> **提示**：修改配置后需要重启游戏生效。

---

## 二、基础命令

### 1. `/steve spawn <name>` - 生成 Steve

在玩家前方生成一个名为 `<name>` 的 Steve。

**参数说明**：
- `name`：Steve 的名字（唯一标识，不能重复）

**功能描述**：
- Steve 会生成在玩家视线方向约 3 格远的位置
- 每个 Steve 都有独立的名字，用于后续指令
- 受最大数量限制（默认 10 个）

**命令样例**：
```
/steve spawn Alice
/steve spawn Builder1
/steve spawn Miner
```

---

### 2. `/steve remove <name>` - 移除 Steve

移除指定名字的 Steve。

**参数说明**：
- `name`：要移除的 Steve 名字

**功能描述**：
- 立即移除指定 Steve 实体
- 该 Steve 正在执行的任务会被取消

**命令样例**：
```
/steve remove Alice
/steve remove Builder1
```

---

### 3. `/steve list` - 查看所有 Steve

列出当前世界中所有活跃的 Steve。

**功能描述**：
- 显示所有 Steve 的名字列表
- 显示当前 Steve 数量

**命令样例**：
```
/steve list
```

**输出示例**：
```
Active Steves (3): Alice, Builder1, Miner
```

---

### 4. `/steve stop <name>` - 停止动作

停止指定 Steve 的当前动作并清空任务队列。

**参数说明**：
- `name`：要停止的 Steve 名字

**功能描述**：
- 立即停止当前正在执行的动作
- 清空该 Steve 的任务队列
- Steve 会保持在原地

**命令样例**：
```
/steve stop Alice
/steve stop Builder1
```

---

### 5. `/steve tell <name> <command>` - 发送指令（核心命令）

向指定 Steve 发送自然语言指令，这是最常用的核心命令。

**参数说明**：
- `name`：Steve 的名字
- `command`：自然语言指令（支持多词描述）

**功能描述**：
- Steve 使用 AI 解析指令内容
- 自动选择合适的动作执行
- 支持建筑、采集、挖矿、跟随、战斗等多种任务
- 指令在后台线程异步执行，不会阻塞游戏

**命令样例**：
```
/steve tell Alice build a house
/steve tell Builder1 mine 10 diamonds
/steve tell Miner gather 20 wood
/steve tell Alice follow me
/steve tell Guard attack zombies
```

> **重要提示**：指令执行是异步的，发送后不会立即看到反馈。Steve 会在后台处理并执行。

---

## 三、跟随玩家

### 跟随指令

**功能**：Steve 跟随指定玩家移动

**示例命令**：
```
/steve tell Alice follow me
/steve tell Guard follow Steve
/steve tell Bob follow player
```

**特性**：
- 距离超过 3 格时开始跟随
- 距离小于 2 格时停止移动
- 持续 5 分钟后自动停止
- 如果指定玩家不存在，会跟随最近的玩家

---

## 四、建筑系统

### 建筑风格多样化功能

Steve AI 支持多种建筑风格，让 AI 建造的建筑更加丰富多样。

#### 功能说明

- **自动随机选择**：AI 角色建造房屋、城堡等建筑时会自动随机选择不同的建筑风格
- **多角色去重**：多个 AI 角色同时建造时，每个角色会自动分配不同的风格，确保建筑多样性
- **预定义 16 种风格**：内置 16 种经典 Minecraft 建筑风格

#### 预定义风格列表

| 风格名称 | 标识 | 主要材料 | 视觉特征 |
|---------|------|---------|---------|
| **经典橡木屋** | `oak_classic` | 橡木木板 | 温馨经典小屋 |
| **云杉小木屋** | `spruce_cabin` | 云杉木板 | 北欧风木屋 |
| **白桦小屋** | `birch_cottage` | 白桦木板 | 明亮田园风 |
| **石砖堡垒** | `stone_fortress` | 石砖/圆石 | 坚固石质堡垒 |
| **沙岩沙漠风** | `sandstone_desert` | 沙岩 | 沙漠风格建筑 |
| **深色橡木庄园** | `dark_oak_manor` | 深色橡木 | 庄园/哥特风 |
| **砖瓦房** | `brick_house` | 砖块 | 经典砖房 |
| **丛林小屋** | `jungle_hut` | 丛林木板 | 热带风格 |
| **金合欢前哨** | `acacia_outpost` | 金合欢木板 | 热带草原风格 |
| **樱花庄园** | `cherry_blossom` | 樱花木板 | 日式樱花风格 |
| **竹林隐居** | `bamboo_retreat` | 竹板 | 东方竹林风格 |
| **红树林沼泽** | `mangrove_swamp` | 红树林木板 | 沼泽湿地风格 |
| **深板岩洞穴** | `deepslate_cavern` | 深板岩 | 地下洞穴风格 |
| **下界砖堡垒** | `nether_brick_fortress` | 下界砖 | 下界地狱风格 |
| **诡异森林** | `warped_forest` | 诡异木板 | 诡异森林风格 |
| **绯红狩猎屋** | `crimson_hunting_lodge` | 绯红木板 | 绯红森林风格 |

### 支持的建筑类型

| 建筑类型 | 别名 | 描述 | 默认材料 |
|---------|------|------|---------|
| **房屋** | `house`, `home` | 带地板、墙壁、门、窗户和金字塔屋顶的完整房屋 | 橡木木板、圆石、玻璃板 |
| **城堡** | `castle`, `fort` | 四角塔楼、锯齿墙头的防御性建筑 | 石砖、圆石、玻璃板 |
| **塔楼** | `tower` | 圆形塔身、金字塔顶的独立塔楼 | 石砖、錾制石砖、深色橡木楼梯 |
| **谷仓** | `barn`, `shed` | 带尖顶的大型木质建筑 | 橡木木板、橡木原木、云杉木板 |
| **现代建筑** | `modern`, `modern_house` | 使用石英和玻璃的现代风格建筑 | 石英块、平滑石头、玻璃 |
| **墙壁** | `wall` | 直线型墙壁 | 指定材料 |
| **平台** | `platform` | 平面平台 | 指定材料 |
| **立方体** | `box`, `cube` | 实心立方体结构 | 指定材料 |

### 建筑特性

- **预留充足内部空间**：最小 6×6 格内部区域，可自由走动和放置家具
- **丰富内部装饰**：
  - 家具：工作台、箱子、熔炉、床（双层房屋）
  - 灯具：吊灯、壁灯、灯笼
  - 装饰：地毯、花盆、书架、告示牌
- **外部装饰**：
  - 门廊灯笼、花箱
  - 花园小径（石砖路）
  - 两侧树苗装饰
  - 门口欢迎垫

### 建筑尺寸说明

**默认尺寸**：9 × 6 × 9（宽 × 高 × 深）

**尺寸参数用法**：
- 可以通过自然语言指定尺寸，如 `build a large house` 或 `build a small castle`
- 也可以通过 `width`、`height`、`depth` 参数精确控制

**示例**：
```
/steve tell Alice build a large house
/steve tell Builder1 build a small tower
/steve tell Bob build a castle with width 15 height 10 depth 15
```

> **注意**：建筑会在玩家视线方向约 12 格远的位置生成。Steve 会自动寻找合适的地面开始建造。

### 智能选址功能

Steve 在建造前会自动搜索合适的建造地点：

**搜索范围**：以目标位置为中心，半径 20 格

**选址标准**：
- 地形相对平坦（高度差不超过 2 格）
- 地面坚实（非水域、岩浆等）
- 上方空间充足（无障碍物阻挡）

**自动清理**：
- 建造前会自动清除区域内的植被障碍（草、花、树叶等）
- 无需玩家手动清理场地

---

## 五、战斗模式

### 战斗指令

**功能**：攻击敌对生物，保护玩家安全，支持大范围多目标清除

**搜索范围**：可配置（默认 100×100 格，以**玩家**为中心，50 格半径）

> **配置方法**：在 `config/steve-common.toml` 中修改 `[search]` 节的 `combatRadius` 参数（范围 10-200，默认 50）

> **重要**：`kill mobs` 和 `attack mobs` 以保护玩家为核心目标，搜索区域以**玩家所在位置**为中心，而不是 Steve 的位置。

### 怪物分组功能

| 分组名称 | 包含怪物 | 说明 |
|---------|---------|------|
| **undead** | 僵尸、骷髅、尸壳、流浪者、溺尸、僵尸村民、凋零骷髅、幻翼、僵尸疣猪兽 | 亡灵生物 |
| **flying** | 幻翼、恶魂、恼鬼、旋风人 | 飞行生物 |
| **nether** | 烈焰人、恶魂、凋零骷髅、岩浆怪、猪灵蛮兵、疣猪兽、僵尸疣猪兽 | 下界生物 |
| **raid** | 掠夺者、卫道士、唤魔者、劫掠兽、恼鬼 | 劫掠生物 |
| **boss** | 末影龙、凋灵、远古守卫者、监守者 | 首领级生物 |
| **ranged** | 骷髅、流浪者、掠夺者、烈焰人、恶魂、潜影贝 | 远程攻击生物 |
| **slimes** | 史莱姆、岩浆怪 | 史莱姆类生物 |
| **hostile** | 所有敌对生物 | 全部敌对生物 |

### 多目标清除机制

- **玩家中心搜索**：AI 以**玩家位置**为中心，搜索可配置范围内的所有敌对生物
- **保护性清除**：Steve 会主动保护玩家，清除玩家周围大范围内的所有威胁
- **队列系统**：发现的目标按距离排序，从近到远逐个清除
- **动态补充**：每 2 秒重新扫描一次区域，新出现或遗漏的怪物会被自动加入队列
- **直到清完**：持续战斗直到玩家周围搜索范围内没有符合条件的怪物
- **超时保护**：最长执行 6 分钟

### 战斗示例命令

```
/steve tell Guard attack mobs           # 清除所有敌对生物
/steve tell Alice kill mobs             # "杀怪"清除周围所有怪物
/steve tell Bob clear enemies           # 清除所有敌人
/steve tell Defender attack undead      # 攻击亡灵生物
/steve tell Guard attack flying         # 攻击飞行生物
/steve tell Hunter attack nether        # 攻击下界生物
/steve tell Defender attack raid        # 攻击劫掠者
/steve tell Hero attack boss            # 攻击首领级生物
/steve tell Archer attack ranged        # 攻击远程怪物
/steve tell Guard attack zombies        # 攻击僵尸
/steve tell Guard attack skeletons      # 攻击骷髅
/steve tell Guard attack creepers       # 攻击爬行者
/steve tell Guard attack slimes         # 攻击史莱姆
/steve tell Guard attack magma_cubes    # 攻击岩浆怪
```

### 战斗特性

- 搜索范围：可配置（默认 100×100 格，50 格半径）
- **玩家保护优先**：以保护玩家为核心，搜索区域跟随玩家位置
- 自动冲刺接近目标
- 被困时自动传送靠近目标
- 不会攻击其他 Steve 或玩家
- 战斗时启用无敌状态
- **智能落地**：如果 Steve 在建筑上，会自动传送到地面再开始战斗

---

## 六、创意物品

### 创造载具和装备

**功能**：创造火箭、铁路、飞机、大炮、活动装备等特殊结构和载具

**支持的载具类型**：

| 载具类型 | 别名 | 描述 | 结构特点 |
|---------|------|------|---------|
| **火箭** | `rocket`, `spaceship`, `missile` | 太空火箭 | 白色主体、橙色尾翼、红色锥形顶部、玻璃窗 |
| **铁路** | `railway`, `rail`, `track`, `train_track` | 铁路轨道 | 20格长轨道、动力铁轨、红石火把供电 |
| **飞机** | `aircraft`, `plane`, `jet`, `airplane` | 飞机 | 白色机身、机翼、尾翼、驾驶舱玻璃窗 |
| **大炮** | `cannon`, `artillery`, `tnt_cannon` | TNT大炮 | 黑色炮管、黑曜石底座、TNT弹仓、拉杆触发 |
| **活动装备** | `event_gear`, `party`, `fireworks`, `celebration` | 庆典舞台 | 橡木平台、围栏支柱、红色屋顶、烟花发射器 |

### 创造示例命令

```
/steve tell Alice create a rocket              # 创建火箭
/steve tell Bob build a spaceship              # 创建太空船
/steve tell Engineer make a railway            # 创建铁路轨道
/steve tell Pilot create an aircraft           # 创建飞机
/steve tell Soldier build a cannon             # 创建大炮
/steve tell Party create event gear            # 创建庆典舞台
```

### 创造特性

- 在玩家前方 **15 格**处建造（给玩家足够空间观赏）
- Steve 启用飞行模式建造
- 建造时有粒子效果和音效
- 完成后有烟花庆祝效果
- 10 分钟超时保护
- 自动检测并适应地面高度

---

## 七、收集物资

### 采集资源

**功能**：收集木头、花、蘑菇等自然资源

**搜索范围**：可配置（默认 100×100 格，以**玩家**为中心，50 格半径）

> **配置方法**：在 `config/steve-common.toml` 中修改 `[search]` 节的 `gatherRadius` 参数（范围 10-200，默认 50）

> **重要**：采集任务以保护玩家为核心，搜索区域以**玩家所在位置**为中心。

### 资源分组功能

| 分组名称 | 包含资源 | 说明 |
|---------|---------|------|
| **logs** | 橡木、云杉、白桦、丛林、金合欢、深色橡木、红树林、樱花原木 | 任意原木类型 |
| **flowers** | 虞美人、蒲公英、向日葵、兰花等 9 种花 | 任意花朵 |
| **mushrooms** | 红色蘑菇、棕色蘑菇 | 任意蘑菇 |
| **ores** | 煤炭、铁、金、钻石、铜、红石、青金石、绿宝石矿石 | 任意矿石 |
| **stones** | 石头、圆石、深板岩、花岗岩、闪长岩、安山岩 | 任意石头类型 |
| **all** | 所有有用资源（原木、花朵、蘑菇、浆果、仙人掌、竹子、农作物等） | **一键收集所有物资** |

### 超级简化指令（推荐）

```
/steve tell Alice collect everything      # 收集周围所有有用物资
/steve tell Alice gather all              # 收集所有资源
/steve tell Alice harvest resources       # 收割所有资源
/steve tell Alice get stuff               # 收集所有东西
```

**收集范围**：玩家周围可配置区域（默认 100×100 格）

**包含资源**：
- 所有类型原木（橡木、云杉、白桦、丛林、金合欢、深色橡木、红树林、樱花）
- 所有花朵（虞美人、蒲公英、向日葵、兰花等）
- 蘑菇（红色、棕色）
- 浆果丛
- 仙人掌
- 甘蔗
- 竹子
- 农作物（小麦、胡萝卜、土豆、甜菜）

### 分类简化指令

```
/steve tell Alice get wood           # 采集玩家周围所有木头（64个）
/steve tell Alice get flowers        # 采集玩家周围所有花朵（64个）
/steve tell Bob get mushrooms        # 采集玩家周围所有蘑菇（64个）
/steve tell Farmer get berries       # 采集玩家周围所有浆果（64个）
```

### 标准采集指令

```
/steve tell Alice gather 20 wood      # 采集 20 个任意原木
/steve tell Alice gather 10 oak       # 采集 10 个橡木原木
/steve tell Alice gather 10 flowers   # 采集 10 朵任意花
/steve tell Bob gather mushrooms      # 采集蘑菇（默认 32 个）
/steve tell Farmer gather berries     # 采集甜浆果
```

### 采集特性

- **默认数量**：32 个（简化指令模式下为 64 个）
- **采集延迟**：每 0.5 秒采集一个（10 ticks）
- **超时时间**：6 分钟
- 使用分组名称时，Steve 会搜索周围任意类型的资源

### 挖矿

**功能**：智能深度挖矿，自动寻找并挖掘指定矿石

**支持的矿石类型**：
- 煤矿石 (`coal_ore`)
- 铁矿石 (`iron_ore`, `deepslate_iron_ore`)
- 铜矿石 (`copper_ore`)
- 金矿石 (`gold_ore`, `deepslate_gold_ore`)
- 钻石矿石 (`diamond_ore`, `deepslate_diamond_ore`)
- 红石矿石 (`redstone_ore`, `deepslate_redstone_ore`)
- 青金石矿石 (`lapis_ore`, `deepslate_lapis_ore`)
- 绿宝石矿石 (`emerald_ore`)

**智能特性**：
- 根据矿石类型自动前往合适深度（钻石 Y=-59，铁 Y=64 等）
- 单向直线隧道挖掘模式（根据玩家视线方向）
- 自动放置火把照明（每 5 秒检查光线）
- 装备铁镐进行挖掘
- 飞行模式挖掘（快速移动）
- 20 分钟超时保护

**示例命令**：
```
/steve tell Miner mine 10 diamonds
/steve tell Bob mine 20 iron
/steve tell Alice mine coal
/steve tell Digger mine 5 gold
```

**默认数量**：8 个

---

## 八、多角色协作

### 工作原理

Steve AI 支持多个 Steve 协作建造同一建筑，大幅提高建造效率：

1. **象限划分**：建筑被自动分为 4 个象限
   - 西北（North-West）
   - 东北（North-East）
   - 西南（South-West）
   - 东南（South-East）

2. **分工合作**：
   - 每个 Steve 负责一个象限
   - 从地面向上逐层建造
   - 当一个象限完成后，Steve 自动协助其他象限

3. **动态分配**：
   - 系统优先分配未分配的象限给新加入的 Steve
   - 如果某个象限工作量较大，多个 Steve 可以共同完成

### 协作构建完整示例

以下是 3 个 Steve 合作建造城堡的完整命令序列：

```
/steve spawn Builder1
/steve spawn Builder2
/steve spawn Builder3

/steve tell Builder1 build a castle
/steve tell Builder2 help build the castle
/steve tell Builder3 help build the castle
```

**执行过程**：
1. Builder1 开始建造城堡，系统自动创建协作构建项目
2. Builder2 加入同一建筑，被分配到第二个象限
3. Builder3 加入，被分配到第三个象限
4. 三个 Steve 同时从各自象限的地面开始向上建造
5. 完成后，所有 Steve 收到成功提示

> **提示**：协作构建时，Steve 会启用飞行模式并无敌状态，确保建造过程顺畅。

---

## 九、其他指令说明

### 放置方块

**功能**：在指定坐标放置特定方块

**示例命令**：
```
/steve tell Alice place stone at 100 64 200
/steve tell Builder place oak_planks at x 50 y 70 z 100
```

**参数**：
- 方块类型：`stone`, `oak_planks`, `dirt` 等
- 坐标：`x y z`

---

### 寻路

**功能**：移动到指定坐标

**示例命令**：
```
/steve tell Alice go to 100 64 200
/steve tell Bob pathfind to x 50 y 70 z 100
/steve tell Scout go to coordinates 200 70 300
```

**特性**：
- 自动寻路到目标位置
- 到达 2 格范围内视为成功
- 30 秒超时

---

### 合成物品

> **注意**：此功能尚未实现（预留接口）

**示例命令**：
```
/steve tell Alice craft a pickaxe
/steve tell Bob craft 10 planks
```

当前执行会返回失败提示："Crafting not yet implemented"

---

## 十、常见问题与注意事项

### Steve 数量上限

- **默认上限**：10 个 Steve
- 可在配置文件中修改 `maxActiveSteves` 参数
- 达到上限后无法生成新的 Steve

### 配置文件位置

- **开发环境**：`config/steve-common.toml`
- **运行环境**：`run/config/steve-common.toml`

配置更改后需要重启游戏生效。

### 命令异步执行说明

- 所有 `/steve tell` 命令都在后台线程异步执行
- 发送命令后不会立即看到游戏内反馈
- Steve 会在后台处理指令并开始执行
- 可以通过观察 Steve 的行为判断指令是否生效

### 建筑材料说明

- Steve 建造时**不需要**玩家提供材料
- 建筑使用预设材料生成
- 可以通过指令中的材料描述影响建筑外观（如 `stone house`）

### 常见问题解答

**Q：Steve 不执行指令怎么办？**
A：检查 Steve 名字是否正确，使用 `/steve list` 确认 Steve 存在。

**Q：建筑生成位置不对？**
A：Steve 会在玩家视线方向约 12 格远的位置生成建筑。调整视角后重新发送指令。

**Q：协作建造时 Steve 不工作？**
A：确保第一个 Steve 已经开始建造后，再让其他 Steve 加入。使用 `help build` 关键词加入协作。

**Q：Steve 飞走了？**
A：建筑任务会启用飞行模式，任务完成后会自动关闭。使用 `/steve stop` 可以强制停止并关闭飞行。

**Q：如何移除所有 Steve？**
A：需要逐个使用 `/steve remove <名字>` 移除，或重启游戏。

**Q：Steve 会死亡吗？**
A：建筑时 Steve 处于无敌状态，战斗时也会启用无敌，正常情况下不会死亡。

**Q：为什么砍树指令会采集不同种类的原木？**
A：现在 "砍树"、"gather wood"、"get wood" 等泛化指令会搜索 `logs` 分组，自动采集玩家周围 100×100 范围内任意类型的原木（橡木、云杉、白桦等 8 种）。如需特定树种，请明确指定，如 `gather oak`。

**Q：采集指令的搜索范围有多大？**
A：采集任务以**玩家位置**为中心，搜索可配置的范围（默认 100×100，可在配置中调整）。

**Q：如何调整搜索范围？**
A：编辑 `config/steve-common.toml` 文件，在 `[search]` 节中修改：
- `combatRadius`：战斗搜索半径（默认 50，范围 10-200）
- `gatherRadius`：采集搜索半径（默认 50，范围 10-200）

**Q："kill mobs" 会攻击多少怪物？**
A：AI 会以**玩家位置**为中心，自动发现搜索范围内的所有敌对生物。Steve 以保护玩家为首要目标，会按距离排序逐个清除威胁。

**Q：Steve 在建筑上时会不会下来打怪？**
A：会。CombatAction 现在会检测 Steve 是否在地面上。如果 Steve 在建筑上或空中，会自动寻找下方的地面并传送到那里，然后再开始清除怪物。

**Q：史莱姆和岩浆怪能被正确识别吗？**
A：是的，现在 Steve 可以正确识别并攻击史莱姆（Slime）和岩浆怪（Magma Cube）。

**Q：如何切换 AI Provider？**
A：编辑 `config/steve-common.toml` 文件，修改 `[ai]` 节中的 `provider` 值（可选：`groq`、`openai`、`gemini`、`qwen`），保存后重启游戏。

---

> **提示**：Steve AI 使用大语言模型理解指令，描述越清晰，执行效果越好。尝试使用不同的自然语言描述来达到你想要的效果！
