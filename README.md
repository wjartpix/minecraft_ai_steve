# Steve AI 技术架构文档

> **版本**: v1.0  
> **项目**: Steve AI - Minecraft Forge AI Agent Mod  
> **技术栈**: Java 17+, Minecraft Forge 1.20.1-47.2.0, GraalVM Polyglot, Caffeine, Resilience4j  
> **文档日期**: 2026-04-18

---

## 目录

1. [业务需求场景](#一业务需求场景)
2. [业务需求架构](#二业务需求架构)
3. [技术架构](#三技术架构)
4. [实施方案](#四实施方案)

---

## 一、业务需求场景

### 1.1 项目背景

**Steve AI** 是一个 Minecraft Forge mod，灵感来源于 Cursor（AI 代码编辑器），其核心理念是 **"AI 在 Minecraft 中和你一起游玩"**。玩家通过自然语言指令控制 AI 代理 Steve，代理能够自动理解指令、制定计划并执行复杂的游戏任务。系统支持多个 Steve 实例自主协作，共同完成大型建筑项目。

### 1.2 核心痛点与解决方案

#### 1.2.1 资源获取困境

| 维度 | 描述 |
|------|------|
| **痛点** | 采集资源（挖矿、砍树、狩猎）耗时繁琐，重复性操作破坏游戏节奏 |
| **解决方案** | AI 自动执行采矿/采集任务，玩家可专注于探索、建设和创意表达 |
| **技术实现** | `MineBlockAction` (387行) 支持智能深度挖矿、单向隧道模式、8种矿石识别 |

#### 1.2.2 建筑设计困难

| 维度 | 描述 |
|------|------|
| **痛点** | 玩家有建筑构想但难以从零手工建造，特别是复杂结构 |
| **解决方案** | 自然语言描述 → AI 自动生成建筑方案并执行建造 |
| **技术实现** | `BuildStructureAction` (659行) 支持 8 种建筑风格、6 种建筑类型、自动选址 |

#### 1.2.3 多人协作效率低

| 维度 | 描述 |
|------|------|
| **痛点** | 多个 AI 在同一建筑上工作容易产生冲突和重复劳动 |
| **解决方案** | 多 Steve 自动分工，基于空间分区（4象限）实现无冲突并行建造 |
| **技术实现** | `CollaborativeBuildManager` + `BuildSection` 原子操作 + 动态负载均衡 |

#### 1.2.4 战斗自动化

| 维度 | 描述 |
|------|------|
| **痛点** | 清理怪物区域需要反复手动操作，效率低下 |
| **解决方案** | AI 自动识别敌对生物类型，智能选择目标并逐个清除 |
| **技术实现** | `CombatAction` (290行) 支持 6 个生物分组、多目标队列、32格搜索范围 |

#### 1.2.5 探索导航

| 维度 | 描述 |
|------|------|
| **痛点** | 长距离移动和坐标导航耗时 |
| **解决方案** | AI 快速导航到目标坐标，支持跟随玩家模式 |
| **技术实现** | `PathfindAction` + `FollowPlayerAction` + Minecraft 原生路径寻找 |

---

## 二、业务需求架构

### 2.1 功能模块树

```
Steve AI 功能树
│
├── 核心能力层
│   ├── 自然语言理解 (NLP)
│   │   └── LLM 集成: OpenAI / Groq / Gemini / Qwen
│   ├── 任务规划 (Task Planning)
│   │   └── 自然语言 → 结构化任务序列
│   └── 动作执行 (Action Execution)
│       └── 任务 → 游戏操作转换
│
├── 游戏操作域
│   ├── 建筑系统
│   │   ├── 8种建筑风格 (现代/中世纪/东方/未来主义/工业/自然/极简/奇幻)
│   │   ├── 6种建筑类型 (房屋/塔楼/桥梁/围墙/农场/装饰)
│   │   ├── 协作构建 (多Steve并行)
│   │   └── 自动选址 (地形分析)
│   ├── 挖矿系统
│   │   ├── 智能深度挖矿 (螺旋/分支/鱼骨模式)
│   │   ├── 单向隧道 (安全退避)
│   │   └── 8种矿石目标 (钻石/红石/青金石/金/铁/铜/煤/绿宝石)
│   ├── 采集系统
│   │   ├── 5种资源分组 (木材/矿石/作物/动物/杂物)
│   │   ├── 8种木材类型
│   │   └── 32格搜索半径
│   ├── 战斗系统
│   │   ├── 6个生物分组 (敌对/中立/被动/BOSS/水生/飞行)
│   │   ├── 多目标队列管理
│   │   └── 32格范围扫描
│   ├── 移动导航
│   │   ├── 路径寻找 (A*算法)
│   │   └── 跟随玩家 (动态距离保持)
│   └── 方块放置
│       ├── 单方块精确放置
│       └── 复杂结构批量放置
│
├── 协作机制
│   ├── 多代理调度
│   ├── 空间分区 (4象限)
│   ├── 动态负载均衡
│   └── 无锁并发控制
│
├── 配置管理
│   ├── AI Provider 选择
│   ├── API Key 管理
│   ├── 行为参数调节
│   └── 建筑风格定制
│
└── 用户交互
    ├── GUI 面板 (K键, Cursor风格界面)
    ├── 命令系统 (/steve spawn, tell, list, stop)
    └── 聊天反馈 (执行状态/错误报告)
```

### 2.2 核心业务流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  用户输入    │────▶│  LLM理解    │────▶│  任务规划    │────▶│  动作执行    │
│  自然语言   │     │  和推理     │     │  结构化序列  │     │  游戏操作    │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
                                                                   │
                                                                   ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  用户反馈    │◀────│  GUI/聊天   │◀────│  事件总线    │◀────│  世界状态    │
│  结果展示   │     │  状态更新   │     │  动作完成    │     │  变更确认    │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
```

### 2.3 关键业务实体

| 实体 | 职责 | 对应源码 |
|------|------|----------|
| `SteveEntity` | AI 代理在游戏世界中的物理存在 | `entity/SteveEntity.java` |
| `Task` | 可执行的工作单元 | `action/Task.java` |
| `ActionResult` | 动作执行结果封装 | `action/ActionResult.java` |
| `AgentState` | 代理状态枚举 | `execution/AgentState.java` |

---

## 三、技术架构

### 3.1 分层架构（6层）

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           1. 用户交互层 (Presentation)                        │
├─────────────────────────────────────────────────────────────────────────────┤
│  SteveGUI (K键面板)    SteveCommands (/steve命令)    SteveOverlayScreen     │
│  ├─ Cursor风格界面      ├─ spawn/tell/list/stop       └─ 状态覆盖显示        │
│  ├─ 输入历史            └─ 参数解析                                                  │
│  └─ 实时反馈                                                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                           2. 自然语言处理层 (NLP)                             │
├─────────────────────────────────────────────────────────────────────────────┤
│  PromptBuilder                                                               │
│  ├─ 系统提示词 (action定义 + 规则 + few-shot示例)                              │
│  └─ 用户提示词 (位置 + 玩家 + 实体 + 方块 + 生物群系 + 命令)                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                           3. LLM集成层 (异步)                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│  TaskPlanner ──▶ AsyncLLMClient (OpenAI/Groq/Gemini/Qwen)                    │
│       │              │                                                        │
│       │              └─▶ ResilientLLMClient (装饰器模式)                      │
│       │                   ├─ CircuitBreaker (熔断)                           │
│       │                   ├─ Retry (指数退避)                                 │
│       │                   ├─ RateLimiter (限流)                              │
│       │                   └─ Cache (Caffeine, SHA-256 key, 40-60%命中率)      │
│       │                                                                      │
│       └─▶ LLMFallbackHandler (故障转移)                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                           4. 响应解析层 (Parsing)                             │
├─────────────────────────────────────────────────────────────────────────────┤
│  ResponseParser                                                              │
│  ├─ JSON提取 (正则匹配代码块)                                                  │
│  ├─ JSON修复 (处理LLM常见格式错误)                                             │
│  └─ 结构化转换 (ParsedResponse → Task列表)                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                           5. 动作执行引擎层 (Execution)                       │
├─────────────────────────────────────────────────────────────────────────────┤
│  ActionExecutor ──▶ TaskQueue ──▶ ActionRegistry                             │
│       │                              ├─ CoreActionsPlugin (内置)              │
│       │                              └─ 第三方插件扩展点                        │
│       │                                                                      │
│       ├─▶ AgentStateMachine (IDLE→PLANNING→EXECUTING→ERROR)                  │
│       │                                                                      │
│       └─▶ InterceptorChain                                                   │
│            ├─ LoggingInterceptor                                             │
│            ├─ MetricsInterceptor                                             │
│            └─ EventPublishingInterceptor                                     │
│                                                                              │
│  EventBus (SimpleEventBus)                                                   │
│  ├─ ActionStartedEvent                                                       │
│  ├─ ActionCompletedEvent                                                     │
│  └─ StateTransitionEvent                                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                           6. Minecraft集成层 (Game)                           │
├─────────────────────────────────────────────────────────────────────────────┤
│  SteveEntity (extends PathfinderMob)                                         │
│  ├─ 物理存在与动画                                                             │
│  ├─ 物品栏管理                                                                │
│  └─ 与World交互                                                               │
│                                                                              │
│  World交互                                                                   │
│  ├─ setBlock (方块放置/破坏)                                                  │
│  ├─ pathfinding (路径寻找)                                                    │
│  └─ CollaborativeBuildManager (协作构建管理)                                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 核心设计模式

#### 3.2.1 插件架构 (Plugin Architecture)

```java
// ActionPlugin 接口定义
public interface ActionPlugin {
    String getName();
    void registerActions(ActionRegistry registry);
}

// 单例注册表
public class ActionRegistry {
    private static final ActionRegistry INSTANCE = new ActionRegistry();
    private final Map<String, Function<JsonObject, BaseAction>> factories = new HashMap<>();
    
    public void register(String actionType, Function<JsonObject, BaseAction> factory) {
        factories.put(actionType, factory);
    }
}

// 内置实现
public class CoreActionsPlugin implements ActionPlugin {
    @Override
    public void registerActions(ActionRegistry registry) {
        registry.register("build", BuildStructureAction::new);
        registry.register("mine", MineBlockAction::new);
        registry.register("combat", CombatAction::new);
        // ... 其他动作
    }
}
```

#### 3.2.2 状态机 (State Machine)

```
                    ┌─────────────┐
         ┌─────────│    IDLE     │◀────────┐
         │         │   (空闲)     │         │
         │         └──────┬──────┘         │
         │                │ receive command │
         │                ▼                │
         │         ┌─────────────┐         │
         │    ┌────│  PLANNING   │────┐    │
         │    │    │  (规划中)   │    │    │
         │    │    └──────┬──────┘    │    │
         │    │           │ parse ok  │    │
         │    │ error     ▼           │    │
         │    │    ┌─────────────┐    │    │
         │    └───▶│  EXECUTING  │────┘    │
         │         │  (执行中)   │         │
         │         └──────┬──────┘         │
         │                │ complete        │
         │                ▼                │
         │         ┌─────────────┐         │
         └────────▶│    ERROR    │─────────┘
                   │   (错误)     │
                   └─────────────┘
```

**实现**: `AgentStateMachine.java` - 管理 Steve 的生命周期状态转换

#### 3.2.3 拦截器链 (Interceptor Chain)

```
Request ──▶ LoggingInterceptor ──▶ MetricsInterceptor ──▶ EventPublishingInterceptor ──▶ Action
                │                       │                         │
                ▼                       ▼                         ▼
           记录动作日志            收集性能指标              发布领域事件
```

**实现**: `InterceptorChain.java` + `LoggingInterceptor.java` + `MetricsInterceptor.java` + `EventPublishingInterceptor.java`

#### 3.2.4 事件总线 (Event Bus)

```java
// 事件类型层次
Event (基类)
├── ActionStartedEvent    // 动作开始
├── ActionCompletedEvent  // 动作完成 (成功/失败/取消)
└── StateTransitionEvent  // 状态转换

// 发布-订阅模式
public class SimpleEventBus implements EventBus {
    private final Map<Class<?>, List<Consumer<Event>>> subscribers = new ConcurrentHashMap<>();
    
    public <T extends Event> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                   .add((Consumer<Event>) handler);
    }
    
    public void publish(Event event) {
        subscribers.getOrDefault(event.getClass(), Collections.emptyList())
                   .forEach(handler -> handler.accept(event));
    }
}
```

#### 3.2.5 依赖注入容器 (DI Container)

```java
// 服务容器接口
public interface ServiceContainer {
    <T> void register(Class<T> type, T instance);
    <T> T resolve(Class<T> type);
}

// 简单实现
public class SimpleServiceContainer implements ServiceContainer {
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();
    
    @Override
    public <T> void register(Class<T> type, T instance) {
        services.put(type, instance);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> type) {
        return (T) services.get(type);
    }
}
```

### 3.3 动作系统 (Action System)

#### 3.3.1 抽象基类

```java
public abstract class BaseAction {
    protected final SteveEntity steve;
    protected final ActionContext context;
    protected boolean complete = false;
    protected boolean cancelled = false;
    
    // 生命周期方法
    public final void start() {
        onStart();
    }
    
    public final void tick() {
        if (!complete && !cancelled) {
            onTick();
        }
    }
    
    public final void cancel() {
        cancelled = true;
        onCancel();
    }
    
    public boolean isComplete() {
        return complete;
    }
    
    // 子类实现
    protected abstract void onStart();
    protected abstract void onTick();
    protected void onCancel() {}
}
```

#### 3.3.2 动作实现列表

| 动作类 | 代码行数 | 核心功能 | 源码路径 |
|--------|----------|----------|----------|
| `BuildStructureAction` | 659行 | 8种风格建筑生成、协作构建、自动选址 | `action/actions/BuildStructureAction.java` |
| `MineBlockAction` | 387行 | 智能挖矿、隧道模式、矿石识别 | `action/actions/MineBlockAction.java` |
| `CombatAction` | 290行 | 生物分组、目标队列、战斗策略 | `action/actions/CombatAction.java` |
| `GatherResourceAction` | 357行 | 资源采集、搜索算法、物品收集 | `action/actions/GatherResourceAction.java` |
| `PathfindAction` | ~150行 | A*路径寻找、障碍物规避 | `action/actions/PathfindAction.java` |
| `FollowPlayerAction` | ~120行 | 玩家跟随、距离保持 | `action/actions/FollowPlayerAction.java` |
| `PlaceBlockAction` | ~100行 | 单方块放置、方向控制 | `action/actions/PlaceBlockAction.java` |
| `CraftItemAction` | ~200行 | 合成配方、物品栏管理 | `action/actions/CraftItemAction.java` |
| `IdleFollowAction` | ~80行 | 空闲状态跟随 | `action/actions/IdleFollowAction.java` |

### 3.4 协作构建系统

#### 3.4.1 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                  CollaborativeBuildManager                      │
│                     (静态单例管理器)                              │
├─────────────────────────────────────────────────────────────────┤
│  activeBuilds: Map<UUID, CollaborativeBuild>                    │
│  ├─ 注册构建 registerBuild()                                     │
│  ├─ 注销构建 unregisterBuild()                                   │
│  ├─ 分配区域 assignSection()                                     │
│  └─ 负载均衡 rebalanceLoad()                                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     CollaborativeBuild                          │
│                      (单个构建项目)                               │
├─────────────────────────────────────────────────────────────────┤
│  buildId: UUID                                                  │
│  sections: List<BuildSection> (4象限)                           │
│  participants: Map<UUID, SteveEntity>                           │
│  totalBlocks: int                                               │
│  completedBlocks: AtomicInteger                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      BuildSection                               │
│                      (空间分区单元)                               │
├─────────────────────────────────────────────────────────────────┤
│  quadrant: int (1-4)                                            │
│  bounds: BoundingBox                                            │
│  claimedBlocks: AtomicInteger                                   │
│  completedBlocks: AtomicInteger                                 │
│  assignedSteve: UUID                                            │
│                                                                 │
│  claimBlock(): boolean (CAS无锁操作)                            │
│  completeBlock(): void                                          │
│  isComplete(): boolean                                          │
└─────────────────────────────────────────────────────────────────┘
```

#### 3.4.2 4象限空间分区

```
        North (Z-)
            ▲
            │
    ┌───────┼───────┐
    │   2   │   1   │
West◀───────┼───────►East (X+)
    │   3   │   4   │
    └───────┼───────┘
            │
            ▼
        South (Z+)
```

#### 3.4.3 动态负载均衡算法

```java
public void rebalanceLoad(CollaborativeBuild build) {
    List<BuildSection> incompleteSections = build.getIncompleteSections();
    List<SteveEntity> activeSteves = build.getActiveParticipants();
    
    // 计算每个 Steve 的完成速率
    Map<UUID, Double> rates = calculateCompletionRates(build);
    
    // 识别最慢的象限
    BuildSection slowestSection = incompleteSections.stream()
        .min(Comparator.comparingInt(s -> s.getCompletedBlocks().get()))
        .orElse(null);
    
    // 已完成自己象限的 Steve 帮助最慢的象限
    for (SteveEntity steve : activeSteves) {
        if (isSectionComplete(steve.getAssignedSection())) {
            reassignToSection(steve, slowestSection);
        }
    }
}
```

### 3.5 LLM多Provider架构

#### 3.5.1 架构演进

```
阶段1: 同步客户端 (旧)
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ OpenAIClient │     │ GroqClient  │     │ GeminiClient│
│  (阻塞式)   │     │  (阻塞式)   │     │  (阻塞式)   │
└─────────────┘     └─────────────┘     └─────────────┘

阶段2: 异步客户端 + 弹性层 (新)
┌─────────────────────────────────────────────────────────────┐
│                     AsyncLLMClient (接口)                    │
├─────────────────────────────────────────────────────────────┤
│  AsyncOpenAIClient │ AsyncGroqClient │ AsyncGeminiClient    │
│  AsyncQwenClient   │ ...                                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  ResilientLLMClient (装饰器)                 │
├─────────────────────────────────────────────────────────────┤
│  ├─ CircuitBreaker (熔断器)                                  │
│  ├─ Retry (指数退避重试: 1s, 2s, 4s, 8s)                      │
│  ├─ RateLimiter (令牌桶限流)                                 │
│  └─ Cache (Caffeine, SHA-256 key, TTL 5min, 40-60%命中率)    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  LLMFallbackHandler (故障转移)               │
│  Primary (Gemini) ──fail──▶ Secondary (Groq) ──fail──▶ ...  │
└─────────────────────────────────────────────────────────────┘
```

#### 3.5.2 提示词工程

**系统提示词结构**:
```
┌─────────────────────────────────────────────────────────────┐
│ SYSTEM PROMPT                                                │
├─────────────────────────────────────────────────────────────┤
│ 1. 角色定义: "你是Steve, Minecraft中的AI助手..."              │
│ 2. 可用动作列表 (JSON Schema格式)                             │
│    - build: {style, type, size, material}                    │
│    - mine: {target, depth, pattern}                          │
│    - combat: {targetType, range}                             │
│    - ...                                                     │
│ 3. 规则约束                                                  │
│    - 一次最多5个任务                                          │
│    - 优先使用附近资源                                         │
│    - 避免危险区域                                             │
│ 4. Few-shot示例 (3-5个典型场景)                               │
└─────────────────────────────────────────────────────────────┘
```

**用户提示词结构**:
```
┌─────────────────────────────────────────────────────────────┐
│ USER PROMPT                                                  │
├─────────────────────────────────────────────────────────────┤
│ 当前位置: (x=100, y=64, z=-200)                              │
│ 玩家: SteveMaster (距离: 5.2格)                              │
│                                                              │
│ 周围实体:                                                    │
│ - Zombie (距离: 12格, 敌对)                                  │
│ - Cow (距离: 8格, 被动)                                      │
│                                                              │
│ 周围方块 (Top 5):                                            │
│ - Grass Block (x=100, y=63, z=-200)                          │
│ - Oak Log (x=98, y=64, z=-198)                               │
│ - ...                                                        │
│                                                              │
│ 生物群系: Plains                                             │
│                                                              │
│ 玩家命令: "建一个现代风格的小屋"                              │
└─────────────────────────────────────────────────────────────┘
```

#### 3.5.3 LLM响应格式

```json
{
  "reasoning": "玩家想要一个现代风格的小屋。我需要先收集木材，然后建造主体结构。",
  "plan": "1. 采集木材 2. 清理地基 3. 建造墙壁 4. 建造屋顶 5. 添加门窗",
  "tasks": [
    {
      "action": "gather",
      "parameters": {
        "resourceType": "wood",
        "amount": 64,
        "woodType": "oak"
      }
    },
    {
      "action": "build",
      "parameters": {
        "style": "modern",
        "type": "house",
        "size": "small",
        "material": "white_concrete"
      }
    }
  ]
}
```

### 3.6 记忆和感知系统

#### 3.6.1 世界知识 (WorldKnowledge)

```java
public class WorldKnowledge {
    private static final int SCAN_RADIUS = 16;
    private static final int SAMPLE_INTERVAL = 2; // 每2格采样
    
    public WorldScanResult scanAround(BlockPos center, Level level) {
        Map<Block, Integer> blockCounts = new HashMap<>();
        List<EntityInfo> entities = new ArrayList<>();
        
        // 稀疏采样 (避免性能问题)
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx += SAMPLE_INTERVAL) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy += SAMPLE_INTERVAL) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz += SAMPLE_INTERVAL) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    blockCounts.merge(state.getBlock(), 1, Integer::sum);
                }
            }
        }
        
        // 获取Top 5方块
        List<Block> topBlocks = blockCounts.entrySet().stream()
            .sorted(Map.Entry.<Block, Integer>comparingByValue().reversed())
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        // 实体分类
        List<Entity> nearbyEntities = level.getEntities(null, 
            new AABB(center).inflate(SCAN_RADIUS));
        
        return new WorldScanResult(topBlocks, entities, 
            level.getBiome(center).unwrapKey().map(ResourceKey::location).orElse(null));
    }
}
```

#### 3.6.2 Steve记忆 (SteveMemory)

```java
public class SteveMemory {
    private String currentGoal;                    // 当前目标
    private final Queue<String> recentActions;     // 最近20个动作
    private final Map<String, Object> contextData; // 上下文数据
    
    // NBT持久化
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("currentGoal", currentGoal);
        ListTag actionsTag = new ListTag();
        recentActions.forEach(action -> 
            actionsTag.add(StringTag.valueOf(action)));
        tag.put("recentActions", actionsTag);
        return tag;
    }
    
    public void deserializeNBT(CompoundTag tag) {
        this.currentGoal = tag.getString("currentGoal");
        // 恢复recentActions...
    }
}
```

### 3.7 配置系统

#### 3.7.1 TOML配置结构

```toml
# steve-common.toml
[ai]
    # AI Provider选择
    provider = "gemini"  # openai, groq, gemini, qwen
    
    # 默认模型
    defaultModel = "gemini-2.0-flash"

[openai]
    apiKey = ""
    baseUrl = "https://api.openai.com/v1"
    timeoutMs = 30000

[groq]
    apiKey = ""
    baseUrl = "https://api.groq.com/openai/v1"
    timeoutMs = 30000

[gemini]
    apiKey = ""
    baseUrl = "https://generativelanguage.googleapis.com/v1beta"
    timeoutMs = 30000

[qwen]
    apiKey = ""
    baseUrl = "https://dashscope.aliyuncs.com/api/v1"
    timeoutMs = 30000

[behavior]
    maxSteveCount = 5
    autoFollowPlayer = true
    followDistance = 3.0
    workRadius = 32

[network]
    proxyHost = ""
    proxyPort = 0
    proxyType = "NONE"  # NONE, HTTP, SOCKS

[building]
    # 建筑风格开关
    modern = true
    medieval = true
    oriental = true
    futuristic = true
    industrial = true
    natural = true
    minimalist = true
    fantasy = true
```

#### 3.7.2 Forge ConfigSpec 实现

```java
public class SteveConfig {
    public static final ForgeConfigSpec SPEC;
    
    // AI配置
    public static final ForgeConfigSpec.ConfigValue<String> AI_PROVIDER;
    public static final ForgeConfigSpec.ConfigValue<String> DEFAULT_MODEL;
    
    // Provider特定配置
    public static final ForgeConfigSpec.ConfigValue<String> GEMINI_API_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> GEMINI_BASE_URL;
    
    // 行为配置
    public static final ForgeConfigSpec.IntValue MAX_STEVE_COUNT;
    public static final ForgeConfigSpec.DoubleValue FOLLOW_DISTANCE;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        
        builder.push("ai");
        AI_PROVIDER = builder.comment("AI Provider to use")
            .define("provider", "gemini");
        DEFAULT_MODEL = builder.comment("Default LLM model")
            .define("defaultModel", "gemini-2.0-flash");
        builder.pop();
        
        // ... 其他配置
        
        SPEC = builder.build();
    }
}
```

---

## 四、实施方案

### 4.1 关键实施决策

| 决策 | 选择 | 理由 |
|------|------|------|
| **Mod框架** | Forge 1.20.1 | 生态成熟，API稳定，社区资源丰富 |
| **并发模型** | CompletableFuture + tick轮询 | 避免阻塞游戏主线程，零冻结体验 |
| **扩展机制** | 插件架构 | 支持第三方扩展，保持核心精简 |
| **LLM兼容** | 多Provider支持 | 满足不同用户需求（免费/付费/国内/国外） |
| **协作策略** | 空间分区 + 原子操作 | 无锁并发，避免死锁和竞态条件 |

### 4.2 完整数据流

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              完整数据流                                       │
└─────────────────────────────────────────────────────────────────────────────┘

[用户层]
    │
    │ 按K键输入 "建一个现代风格的小屋"
    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ SteveGUI.onInputSubmitted()                                                  │
│ SteveCommands.tellSteve()                                                    │
└─────────────────────────────────────────────────────────────────────────────┘
    │
    ▼
[执行层]
┌─────────────────────────────────────────────────────────────────────────────┐
│ ActionExecutor.processNaturalLanguageCommand()                               │
│  ├─ 取消当前action (如果有)                                                   │
│  ├─ agentStateMachine.transitionTo(PLANNING)                                 │
│  ├─ isPlanning = true                                                        │
│  └─ 发送 "Thinking..." 到聊天                                                  │
└─────────────────────────────────────────────────────────────────────────────┘
    │
    ▼
[规划层 - 异步]
┌─────────────────────────────────────────────────────────────────────────────┐
│ TaskPlanner.planTasksAsync()                                                 │
│  ├─ PromptBuilder.buildSystemPrompt()                                        │
│  │   └─ 加载 action定义 + 规则 + few-shot                                    │
│  ├─ PromptBuilder.buildUserPrompt()                                          │
│  │   └─ WorldKnowledge.scanAround() - 16格半径扫描                            │
│  │   └─ 获取 Top5方块 + 实体分类 + 生物群系                                    │
│  ├─ AsyncLLMClient.sendRequest()                                             │
│  │   └─ 线程池执行 (不阻塞游戏线程)                                            │
│  ├─ ResilientLLMClient 装饰器链                                              │
│  │   ├─ Cache.get() - SHA-256 key                                            │
│  │   ├─ RateLimiter.acquire()                                                │
│  │   ├─ CircuitBreaker.check()                                               │
│  │   ├─ Retry.execute() - 指数退避                                            │
│  │   └─ 实际HTTP请求 (HttpClient)                                            │
│  └─ 返回 CompletableFuture<ParsedResponse>                                   │
└─────────────────────────────────────────────────────────────────────────────┘
    │
    │ (每tick轮询)
    ▼
[游戏Tick循环]
┌─────────────────────────────────────────────────────────────────────────────┐
│ ActionExecutor.tick()                                                        │
│  ├─ 检查 future.isDone()                                                     │
│  └─ 如果完成: ResponseParser.parseAIResponse()                               │
│      ├─ JSON提取 (正则匹配 ```json 代码块)                                    │
│      ├─ JSON修复 (处理常见格式错误)                                            │
│      └─ 转换为 List<Task>                                                    │
└─────────────────────────────────────────────────────────────────────────────┘
    │
    ▼
[任务执行]
┌─────────────────────────────────────────────────────────────────────────────┐
│ Task列表加入 taskQueue                                                       │
│ agentStateMachine.transitionTo(EXECUTING)                                    │
│                                                                              │
│ 逐个执行:                                                                    │
│ while (!taskQueue.isEmpty()) {                                               │
│   Task task = taskQueue.poll();                                              │
│   BaseAction action = ActionRegistry.createAction(task.getType(),            │
│                                                   task.getParams());         │
│                                                                              │
│   // 拦截器链                                                                 │
│   InterceptorChain chain = new InterceptorChain();                           │
│   chain.add(new LoggingInterceptor());                                       │
│   chain.add(new MetricsInterceptor());                                       │
│   chain.add(new EventPublishingInterceptor(eventBus));                       │
│   chain.execute(action);                                                     │
│                                                                              │
│   // 动作生命周期                                                             │
│   action.start();          // onStart()                                      │
│   while (!action.isComplete()) {                                             │
│     action.tick();         // onTick() 每游戏tick调用                         │
│   }                                                                          │
│                                                                              │
│   // 发布事件                                                                 │
│   eventBus.publish(new ActionCompletedEvent(action, result));                │
│ }                                                                            │
└─────────────────────────────────────────────────────────────────────────────┘
    │
    ▼
[反馈层]
┌─────────────────────────────────────────────────────────────────────────────┐
│ EventBus 分发 ActionCompletedEvent                                           │
│  ├─ SteveGUI 更新状态显示                                                    │
│  ├─ SteveOverlayScreen 渲染进度                                              │
│  └─ 发送聊天消息 "已完成建筑任务"                                             │
└─────────────────────────────────────────────────────────────────────────────┘
    │
    ▼
[状态重置]
┌─────────────────────────────────────────────────────────────────────────────┐
│ agentStateMachine.transitionTo(IDLE)                                         │
│ isPlanning = false                                                           │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 4.3 关键源码文件索引

#### 4.3.1 核心模块

| 模块 | 关键文件 | 职责 |
|------|----------|------|
| **入口** | `SteveMod.java` | Mod主类，初始化注册 |
| **配置** | `config/SteveConfig.java` | Forge配置规范 |
| **实体** | `entity/SteveEntity.java` | AI代理实体定义 |
| **实体管理** | `entity/SteveManager.java` | 多Steve实例管理 |

#### 4.3.2 LLM集成

| 模块 | 关键文件 | 职责 |
|------|----------|------|
| **任务规划** | `llm/TaskPlanner.java` | 异步任务规划 |
| **提示词构建** | `llm/PromptBuilder.java` | 系统/用户提示词组装 |
| **响应解析** | `llm/ResponseParser.java` | JSON提取与修复 |
| **同步客户端** | `llm/GeminiClient.java` | Gemini API客户端 |
| | `llm/GroqClient.java` | Groq API客户端 |
| | `llm/OpenAIClient.java` | OpenAI API客户端 |
| | `llm/QwenClient.java` | 通义千问客户端 |
| **异步客户端** | `llm/async/AsyncGeminiClient.java` | 异步Gemini客户端 |
| | `llm/async/AsyncGroqClient.java` | 异步Groq客户端 |
| | `llm/async/AsyncOpenAIClient.java` | 异步OpenAI客户端 |
| | `llm/async/AsyncQwenClient.java` | 异步千问客户端 |
| **弹性层** | `llm/resilience/ResilientLLMClient.java` | 熔断/重试/限流/缓存 |

#### 4.3.3 动作系统

| 模块 | 关键文件 | 职责 |
|------|----------|------|
| **执行器** | `execution/ActionExecutor.java` | 动作调度执行 |
| **状态机** | `execution/AgentStateMachine.java` | 代理状态管理 |
| **拦截器链** | `execution/InterceptorChain.java` | 拦截器编排 |
| **日志拦截器** | `execution/LoggingInterceptor.java` | 动作日志记录 |
| **指标拦截器** | `execution/MetricsInterceptor.java` | 性能指标收集 |
| **事件拦截器** | `execution/EventPublishingInterceptor.java` | 事件发布 |
| **动作基类** | `action/Task.java` | 任务定义 |
| | `action/ActionResult.java` | 结果封装 |
| **建筑动作** | `action/actions/BuildStructureAction.java` | 建筑生成(659行) |
| **挖矿动作** | `action/actions/MineBlockAction.java` | 智能挖矿(387行) |
| **战斗动作** | `action/actions/CombatAction.java` | 战斗逻辑(290行) |
| **采集动作** | `action/actions/GatherResourceAction.java` | 资源采集(357行) |
| **路径动作** | `action/actions/PathfindAction.java` | 路径寻找 |
| **跟随动作** | `action/actions/FollowPlayerAction.java` | 玩家跟随 |
| **放置动作** | `action/actions/PlaceBlockAction.java` | 方块放置 |
| **合成动作** | `action/actions/CraftItemAction.java` | 物品合成 |
| **空闲动作** | `action/actions/IdleFollowAction.java` | 空闲跟随 |

#### 4.3.4 协作系统

| 模块 | 关键文件 | 职责 |
|------|----------|------|
| **协作管理器** | `action/CollaborativeBuildManager.java` | 多Steve协作调度 |

#### 4.3.5 事件系统

| 模块 | 关键文件 | 职责 |
|------|----------|------|
| **事件总线** | `event/EventBus.java` | 事件总线接口 |
| **简单实现** | `event/SimpleEventBus.java` | 发布-订阅实现 |
| **动作开始事件** | `event/ActionStartedEvent.java` | 动作开始通知 |
| **动作完成事件** | `event/ActionCompletedEvent.java` | 动作完成通知 |
| **状态转换事件** | `event/StateTransitionEvent.java` | 状态变更通知 |

#### 4.3.6 DI容器

| 模块 | 关键文件 | 职责 |
|------|----------|------|
| **容器接口** | `di/ServiceContainer.java` | DI容器接口 |
| **简单实现** | `di/SimpleServiceContainer.java` | 简单DI实现 |

#### 4.3.7 用户交互

| 模块 | 关键文件 | 职责 |
|------|----------|------|
| **GUI** | `client/SteveGUI.java` | K键面板 |
| **覆盖层** | `client/SteveOverlayScreen.java` | 状态覆盖显示 |
| **命令** | `command/SteveCommands.java` | /steve命令 |
| **按键绑定** | `client/KeyBindings.java` | 按键注册 |

### 4.4 已知限制

| 限制 | 描述 | 影响 |
|------|------|------|
| **测试覆盖** | 当前测试仅为stub，缺乏集成测试 | 回归风险 |
| **合成系统** | 无完整物品栏管理系统 | 复杂合成任务受限 |
| **记忆系统** | 简单队列实现，无长期记忆 | 上下文长度受限 |
| **向量检索** | 无向量数据库集成 | 无法语义检索历史 |

### 4.5 未来方向

| 方向 | 描述 | 优先级 |
|------|------|--------|
| **语音输入** | 集成Whisper API，支持语音指令 | 高 |
| **自适应规划** | LLM动态调整计划，应对意外情况 | 高 |
| **风格编辑器** | 用户自定义建筑风格，可视化编辑 | 中 |
| **向量记忆** | 集成向量数据库，长期语义记忆 | 中 |
| **可观测性** | OpenTelemetry集成，分布式追踪 | 中 |
| **测试完善** | 单元测试、集成测试、E2E测试 | 高 |

---

## 附录

### A. 技术栈版本

| 组件 | 版本 |
|------|------|
| Java | 17+ |
| Minecraft | 1.20.1 |
| Forge | 47.2.0 |
| Gradle | 8.x |
| Gson | (随Forge) |
| HttpClient | Java 11+ 内置 |
| GraalVM Polyglot | 23.1.0 |
| Caffeine | 3.1.8 |
| Resilience4j | 2.1.0 |
| JUnit | 5 |

### B. 配置文件路径

| 环境 | 路径 |
|------|------|
| 开发环境 | `/config/steve-common.toml` |
| 运行环境 | `/run/config/steve-common.toml` |

### C. 构建命令

```bash
# 构建
./gradlew build

# 运行客户端
./gradlew runClient

# 运行测试
./gradlew test

# 发布
./gradlew publish
```

---

*文档结束*
