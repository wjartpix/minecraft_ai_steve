# Steve AI 技术架构文档

> **版本**: v2.0  
> **项目**: Steve AI - Minecraft Forge AI Agent Mod  
> **技术栈**: Java 17+, Minecraft Forge 1.20.1-47.2.0, Caffeine, Resilience4j  
> **文档日期**: 2026-04-20

---

## 快速开始

```bash
/steve spawn Bob            # 召唤 AI 助手
/steve tell Bob build       # 建造房屋
/steve tell Bob collect everything  # 收集资源
/steve tell Bob fight       # 清除怪物
/steve list                 # 查看所有 Steve
/steve stop Bob             # 停止 Bob 的任务
```

按 **K 键** 打开 Cursor 风格的交互面板，输入自然语言即可。

> 详细指令: [USER_GUIDE_SIMPLE.md](USER_GUIDE_SIMPLE.md)

---

## 目录

1. [项目概览](#一项目概览)
2. [系统架构](#二系统架构)
3. [模块系统](#三模块系统)
4. [核心子系统](#四核心子系统)
5. [源码索引](#五源码索引)
6. [配置说明](#六配置说明)

---

## 一、项目概览

Steve AI 是一个 Minecraft Forge Mod，核心目标是 **"AI 在 Minecraft 中和你一起游玩"**。玩家通过自然语言指令控制 AI 代理 Steve，代理能自动理解指令、制定计划并执行复杂的游戏任务。

### 核心能力

| 能力 | 说明 |
|------|------|
| 自然语言理解 | LLM 驱动，支持中英文指令 |
| 多 Provider | OpenAI / Groq / Gemini / Qwen，自动故障转移 |
| 异步非阻塞 | CompletableFuture，游戏线程零冻结 |
| 多代理协作 | 空间分区并行建造，动态负载均衡 |
| 建筑多样化 | 20+ 种风格，10 种建筑类型 |
| 模块化架构 | 松耦合、可插拔、可配置 |

---

## 二、系统架构

### 分层架构

```
┌─────────────────────────────────────────────────────────────────┐
│                     用户交互层 (Presentation)                     │
│  SteveGUI (K键)  │  SteveCommands (/steve)  │  OverlayScreen   │
├─────────────────────────────────────────────────────────────────┤
│                     模块管理层 (Module)                           │
│  ModuleManager → CoreModule → LLMModule → ActionModule → ...   │
├─────────────────────────────────────────────────────────────────┤
│                     LLM 集成层 (Async + Resilience)              │
│  TaskPlanner → AsyncLLMClient → ResilientLLMClient              │
│                 ├─ CircuitBreaker  ├─ Retry  ├─ RateLimiter     │
│                 └─ Cache (Caffeine) └─ FallbackHandler          │
├─────────────────────────────────────────────────────────────────┤
│                     执行引擎层 (Execution)                        │
│  ActionExecutor → ActionRegistry → InterceptorChain             │
│  ├─ AgentStateMachine (IDLE→PLANNING→EXECUTING→ERROR)          │
│  ├─ LoggingInterceptor / MetricsInterceptor                     │
│  └─ EventPublishingInterceptor → EventBus                      │
├─────────────────────────────────────────────────────────────────┤
│                     Minecraft 集成层 (Game)                       │
│  SteveEntity (PathfinderMob)  │  SteveManager  │  WorldKnowledge│
└─────────────────────────────────────────────────────────────────┘
```

### 核心数据流

```
用户输入 → SteveGUI/Commands → ActionExecutor.processNaturalLanguageCommand()
    → TaskPlanner.planTasksAsync() [异步, 不阻塞游戏线程]
        → PromptBuilder (系统+用户提示词)
        → AsyncLLMClient → ResilientLLMClient (熔断/重试/限流/缓存)
        → ResponseParser (JSON提取与修复)
    ← CompletableFuture<ParsedResponse>
    → Task队列 → ActionRegistry.createAction() → BaseAction.start/tick
    → EventBus 发布事件 → GUI 反馈
```

---

## 三、模块系统

### 模块架构

系统采用模块化架构，通过 `ModuleManager` 管理模块生命周期：

```
ModuleManager
├── CoreModule (priority: 1000)   ← EventBus, ActionRegistry, SteveManager
├── LLMModule  (priority: 800)    ← LLMClientRegistry, LLMCache
├── ActionModule (priority: 400)  ← 拦截器链, 状态机
└── BuildingModule (priority: 300, depends: core) ← StructureGeneratorRegistry, BuildingStyleRegistry
```

### 生命周期

每个 `SteveModule` 实现以下生命周期：

1. `onInit(ServiceContainer)` — 注册服务到 DI 容器
2. `onStart()` — 启动模块运行
3. `onStop()` — 停止并清理
4. `onReload()` — 热重载配置（可选）

### 依赖注入

```java
ServiceContainer container = new SimpleServiceContainer();
container.register(EventBus.class, eventBus);
EventBus bus = container.resolve(EventBus.class);
```

---

## 四、核心子系统

### 4.1 LLM 集成

**仅使用异步客户端**，同步客户端已移除。

```
AsyncLLMClient (接口, extends LLMClient)
├── AsyncOpenAIClient
├── AsyncGroqClient
├── AsyncGeminiClient
└── AsyncQwenClient
        │
        ▼
ResilientLLMClient (装饰器模式)
├── CircuitBreaker (熔断器)
├── Retry (指数退避)
├── RateLimiter (令牌桶限流)
└── Cache (Caffeine, SHA-256 key, 5min TTL)
        │
        ▼
LLMFallbackHandler (自动故障转移)
```

`LLMClientRegistry` 管理客户端注册与查询，`LLMModule` 在初始化时根据配置自动注册可用客户端。

### 4.2 动作系统

```
ActionPlugin (SPI 接口)
    │ META-INF/services/com.steve.ai.plugin.ActionPlugin
    ▼
CoreActionsPlugin (内置插件, 注册所有动作)
    │
    ▼
ActionRegistry (单例注册表, 工厂模式)
    ├── pathfind → PathfindAction
    ├── mine → MineBlockAction
    ├── place → PlaceBlockAction
    ├── craft → CraftItemAction
    ├── attack → CombatAction
    ├── follow → FollowPlayerAction
    ├── gather → GatherResourceAction
    ├── build → BuildStructureAction
    └── (可扩展第三方插件)
```

**BaseAction 生命周期**: `start()` → `tick()` → `isComplete()` / `cancel()`

### 4.3 事件系统

```
EventBus (接口) → SimpleEventBus (实现)
├── ActionStartedEvent   (implements DomainEvent)
├── ActionCompletedEvent (implements DomainEvent)
└── StateTransitionEvent (implements DomainEvent)

事件标记接口:
├── DomainEvent     — 领域事件
├── SystemEvent     — 系统事件
└── IntegrationEvent — 集成事件
```

### 4.3 状态机

```
IDLE ──receive command──▶ PLANNING ──parse ok──▶ EXECUTING ──complete──▶ IDLE
                            │                       │
                            └──error──▶ ERROR ──────┘
```

### 4.4 建筑系统

```
StructureGenerator (策略接口)
    ├── StructureGenerators (基础生成器)
    ├── EnhancedStructureGenerators (增强生成器)
    └── CreativeStructureGenerators (创意生成器)

StructureGeneratorRegistry (注册表, 查找匹配生成器)
BuildingStyleRegistry (20+ 种建筑风格管理)
```

### 4.5 世界感知

```
WorldPerception (接口)
├── MinecraftWorldPerception (真实 Minecraft 世界查询)
└── SimulatedWorldPerception (测试模拟)
```

### 4.6 协作构建

```
CollaborativeBuildManager (静态管理器)
└── CollaborativeBuild (单个构建项目)
    └── BuildSection (4象限空间分区, CAS无锁并发)
```

---

## 五、源码索引

### 入口与配置

| 文件 | 职责 |
|------|------|
| `SteveMod.java` | Mod 主类，初始化 ModuleManager 和 ServiceContainer |
| `config/SteveConfig.java` | Forge 配置规范 (API Key, 行为参数) |
| `config/ConfigManager.java` | 模块化配置管理器 |
| `config/ModuleConfig.java` | 模块配置接口 |

### 模块系统

| 文件 | 职责 |
|------|------|
| `module/SteveModule.java` | 模块接口定义 |
| `module/ModuleManager.java` | 模块生命周期管理，依赖排序 |
| `module/CoreModule.java` | 核心基础设施模块 (EventBus, Registry, Manager) |
| `module/LLMModule.java` | LLM 集成模块 |
| `module/ActionModule.java` | 动作执行模块 |
| `module/BuildingModule.java` | 建筑模块 |

### DI 容器

| 文件 | 职责 |
|------|------|
| `di/ServiceContainer.java` | DI 容器接口 |
| `di/SimpleServiceContainer.java` | 简单实现，支持生命周期 |
| `di/Lifecycle.java` | 生命周期接口 |
| `di/ServiceLifecycleManager.java` | 服务生命周期管理器 |

### LLM 集成

| 文件 | 职责 |
|------|------|
| `llm/TaskPlanner.java` | 异步任务规划 (仅异步) |
| `llm/PromptBuilder.java` | 系统/用户提示词组装 |
| `llm/ResponseParser.java` | JSON 提取与修复 |
| `llm/LLMClientRegistry.java` | LLM 客户端注册表 |
| `llm/async/AsyncLLMClient.java` | 异步 LLM 客户端接口 |
| `llm/async/AsyncOpenAIClient.java` | OpenAI 异步客户端 |
| `llm/async/AsyncGroqClient.java` | Groq 异步客户端 |
| `llm/async/AsyncGeminiClient.java` | Gemini 异步客户端 |
| `llm/async/AsyncQwenClient.java` | 通义千问异步客户端 |
| `llm/async/LLMCache.java` | Caffeine 缓存 |
| `llm/async/LLMResponse.java` | LLM 响应封装 |
| `llm/async/LLMException.java` | LLM 异常层次 |
| `llm/async/LLMExecutorService.java` | 线程池管理 |
| `llm/resilience/ResilientLLMClient.java` | 熔断/重试/限流/缓存装饰器 |
| `llm/resilience/LLMFallbackHandler.java` | 自动故障转移 |
| `llm/resilience/ResilienceConfig.java` | 弹性配置 |

### 执行引擎

| 文件 | 职责 |
|------|------|
| `execution/ActionContext.java` | 动作上下文 (DI + EventBus + StateMachine) |
| `execution/ActionInterceptor.java` | 拦截器接口 |
| `execution/InterceptorChain.java` | 拦截器链编排 |
| `execution/InterceptorContext.java` | 拦截器上下文 (属性 + 取消) |
| `execution/AgentState.java` | 代理状态枚举 |
| `execution/AgentStateMachine.java` | 状态机实现 |
| `execution/LoggingInterceptor.java` | 日志拦截器 |
| `execution/MetricsInterceptor.java` | 指标拦截器 |
| `execution/EventPublishingInterceptor.java` | 事件发布拦截器 |

### 动作实现

| 文件 | 职责 |
|------|------|
| `action/Task.java` | 任务定义 |
| `action/ActionResult.java` | 结果封装 |
| `action/ActionExecutor.java` | 动作调度与执行 |
| `action/CollaborativeBuildManager.java` | 多 Steve 协作调度 |
| `action/actions/BaseAction.java` | 动作抽象基类 |
| `action/actions/BuildStructureAction.java` | 建筑生成 |
| `action/actions/MineBlockAction.java` | 智能挖矿 |
| `action/actions/CombatAction.java` | 战斗逻辑 |
| `action/actions/GatherResourceAction.java` | 资源采集 |
| `action/actions/PathfindAction.java` | 路径寻找 |
| `action/actions/FollowPlayerAction.java` | 玩家跟随 |
| `action/actions/PlaceBlockAction.java` | 方块放置 |
| `action/actions/CraftItemAction.java` | 物品合成 |
| `action/actions/IdleFollowAction.java` | 空闲跟随 |
| `action/actions/CreateVehicleAction.java` | 载具创建 |

### 插件系统

| 文件 | 职责 |
|------|------|
| `plugin/ActionPlugin.java` | 插件接口 (SPI) |
| `plugin/ActionFactory.java` | 动作工厂接口 |
| `plugin/ActionRegistry.java` | 动作注册表 (单例) |
| `plugin/CoreActionsPlugin.java` | 内置动作插件 |
| `plugin/PluginManager.java` | SPI 插件发现与加载 |

### 事件系统

| 文件 | 职责 |
|------|------|
| `event/EventBus.java` | 事件总线接口 |
| `event/SimpleEventBus.java` | 发布-订阅实现 (优先级 + 异步) |
| `event/DomainEvent.java` | 领域事件标记接口 |
| `event/SystemEvent.java` | 系统事件标记接口 |
| `event/IntegrationEvent.java` | 集成事件标记接口 |
| `event/ActionStartedEvent.java` | 动作开始事件 |
| `event/ActionCompletedEvent.java` | 动作完成事件 |
| `event/StateTransitionEvent.java` | 状态转换事件 |
| `event/ServerEventHandler.java` | Forge 事件处理 |

### 建筑系统

| 文件 | 职责 |
|------|------|
| `structure/BlockPlacement.java` | 方块放置数据 |
| `structure/BuildingStyle.java` | 20+ 种建筑风格枚举 |
| `structure/BuildingStyleRegistry.java` | 风格注册与轮换 |
| `structure/StructureGenerator.java` | 结构生成器接口 |
| `structure/StructureGeneratorRegistry.java` | 生成器注册表 |
| `structure/StructureGenerators.java` | 基础生成器 |
| `structure/EnhancedStructureGenerators.java` | 增强生成器 |
| `structure/CreativeStructureGenerators.java` | 创意生成器 |
| `structure/StructureTemplateLoader.java` | NBT 模板加载 |

### 感知系统

| 文件 | 职责 |
|------|------|
| `api/perception/WorldPerception.java` | 世界感知接口 |
| `perception/MinecraftWorldPerception.java` | 真实世界实现 |
| `perception/SimulatedWorldPerception.java` | 测试模拟实现 |

### 记忆系统

| 文件 | 职责 |
|------|------|
| `memory/SteveMemory.java` | Steve 记忆 (NBT 持久化) |
| `memory/WorldKnowledge.java` | 世界知识扫描 |
| `memory/StructureRegistry.java` | 已建结构注册 |

### 用户交互

| 文件 | 职责 |
|------|------|
| `client/SteveGUI.java` | K 键交互面板 |
| `client/SteveOverlayScreen.java` | 状态覆盖显示 |
| `client/KeyBindings.java` | 按键注册 |
| `client/ClientEventHandler.java` | 客户端事件 |
| `client/ClientSetup.java` | 客户端初始化 |
| `command/SteveCommands.java` | /steve 命令系统 |

---

## 六、配置说明

### 配置文件路径

| 环境 | 路径 |
|------|------|
| 开发环境 | `config/steve-common.toml` |
| 运行环境 | `run/config/steve-common.toml` |

### 核心配置项

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `ai.provider` | `gemini` | LLM Provider (openai/groq/gemini/qwen) |
| `openai.apiKey` | `""` | OpenAI API Key |
| `groq.apiKey` | `""` | Groq API Key |
| `gemini.apiKey` | `""` | Gemini API Key |
| `qwen.apiKey` | `""` | 通义千问 API Key |
| `behavior.maxActiveSteves` | `10` | 最大活跃 Steve 数 |
| `behavior.followDistance` | `3.0` | 跟随距离 |
| `building.buildingStylesEnabled` | `true` | 启用多样化建筑风格 |
| `building.buildingStyles` | `""` | 指定风格 (空=全部) |

### 构建命令

```bash
./gradlew build          # 构建
./gradlew runClient      # 运行客户端
./gradlew test           # 运行测试
```

---

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 17+ |
| Minecraft | 1.20.1 |
| Forge | 47.2.0 |
| Caffeine | 3.1.8 |
| Resilience4j | 2.1.0 |
| Gson | (随 Forge) |
| JUnit | 5 |
