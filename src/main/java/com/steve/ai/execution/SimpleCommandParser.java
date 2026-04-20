package com.steve.ai.execution;

import com.steve.ai.action.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simplified command parser that maps natural language to structured tasks.
 * Supports ultra-simple commands like "build", "collect everything", "fight".
 */
public class SimpleCommandParser {
    
    // Command patterns for ultra-simple inputs
    public enum SimpleCommand {
        // Building commands
        BUILD("build", "build a house"),
        BUILD_HOUSE("build house", "build a house"),
        BUILD_HOME("build home", "build a house"),
        BUILD_SHELTER("build shelter", "build a house"),
        
        // Collection commands
        COLLECT_EVERYTHING("collect everything", "gather all resources"),
        GATHER_ALL("gather all", "gather all resources"),
        GET_STUFF("get stuff", "gather all resources"),
        HARVEST_ALL("harvest all", "gather all resources"),
        COLLECT_RESOURCES("collect resources", "gather all resources"),
        
        // Wood collection
        GET_WOOD("get wood", "gather wood logs"),
        CHOP_TREES("chop trees", "gather wood logs"),
        CUT_WOOD("cut wood", "gather wood logs"),
        COLLECT_WOOD("collect wood", "gather wood logs"),
        
        // Mining
        MINE("mine", "mine resources"),
        DIG("dig", "mine resources"),
        
        // Combat
        FIGHT("fight", "attack hostile mobs"),
        KILL_MOBS("kill mobs", "attack hostile mobs"),
        ATTACK("attack", "attack hostile mobs"),
        DEFEND("defend", "attack hostile mobs"),
        PROTECT("protect", "attack hostile mobs"),
        CLEAR_ENEMIES("clear enemies", "attack hostile mobs"),
        
        // Following
        FOLLOW("follow", "follow the player"),
        COME("come", "follow the player"),
        STAY("stay", "stop and wait"),
        STOP("stop", "stop current action"),
        
        // Exploration/Utility
        EXPLORE("explore", "explore the area"),
        LOOK_AROUND("look around", "explore the area"),
        
        // Help
        HELP("help", "show available commands"),
        COMMANDS("commands", "show available commands"),
        WHAT_CAN_YOU_DO("what can you do", "show available commands");
        
        private final String trigger;
        private final String description;
        
        SimpleCommand(String trigger, String description) {
            this.trigger = trigger.toLowerCase();
            this.description = description;
        }
        
        public String getTrigger() {
            return trigger;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Try to parse a simple command into structured tasks.
     * @param input The user's natural language input
     * @return List of tasks if matched, null if should fall back to LLM
     */
    public static List<Task> tryParse(String input) {
        String normalized = input.toLowerCase().trim();
        
        // Remove extra whitespace
        normalized = normalized.replaceAll("\\s+", " ");
        
        // Try exact match first
        for (SimpleCommand cmd : SimpleCommand.values()) {
            if (normalized.equals(cmd.getTrigger())) {
                return createTasks(cmd);
            }
        }
        
        // Try partial match for commands that start with the trigger
        for (SimpleCommand cmd : SimpleCommand.values()) {
            if (normalized.startsWith(cmd.getTrigger())) {
                return createTasks(cmd);
            }
        }
        
        // Try contains match for some flexible commands
        if (containsAny(normalized, "build", "make", "create") && 
            containsAny(normalized, "house", "home", "shelter", "base")) {
            return createBuildingTasks("house");
        }
        
        if (containsAny(normalized, "collect", "gather", "get", "harvest") &&
            containsAny(normalized, "everything", "all", "stuff", "resources")) {
            return createGatherTasks("all", 64);
        }
        
        if (containsAny(normalized, "wood", "log", "tree") &&
            containsAny(normalized, "get", "gather", "collect", "chop", "cut")) {
            return createGatherTasks("logs", 64);
        }
        
        if (containsAny(normalized, "kill", "attack", "fight", "clear", "defend", "protect") &&
            containsAny(normalized, "mob", "monster", "enemy", "hostile")) {
            return createCombatTasks("hostile");
        }
        
        if (containsAny(normalized, "follow", "come with", "come here")) {
            return createFollowTasks();
        }
        
        if (containsAny(normalized, "stop", "halt", "cease")) {
            return createStopTasks();
        }
        
        // No simple match - fall back to LLM
        return null;
    }
    
    private static boolean containsAny(String input, String... keywords) {
        for (String keyword : keywords) {
            if (input.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private static List<Task> createTasks(SimpleCommand cmd) {
        return switch (cmd) {
            case BUILD, BUILD_HOUSE, BUILD_HOME, BUILD_SHELTER -> 
                createBuildingTasks("house");
            case COLLECT_EVERYTHING, GATHER_ALL, GET_STUFF, HARVEST_ALL, COLLECT_RESOURCES -> 
                createGatherTasks("all", 64);
            case GET_WOOD, CHOP_TREES, CUT_WOOD, COLLECT_WOOD -> 
                createGatherTasks("logs", 64);
            case MINE, DIG -> 
                createMineTasks("iron", 16);
            case FIGHT, KILL_MOBS, ATTACK, DEFEND, PROTECT, CLEAR_ENEMIES -> 
                createCombatTasks("hostile");
            case FOLLOW, COME -> 
                createFollowTasks();
            case STAY, STOP -> 
                createStopTasks();
            case EXPLORE, LOOK_AROUND -> 
                createExploreTasks();
            case HELP, COMMANDS, WHAT_CAN_YOU_DO -> 
                createHelpTasks();
        };
    }
    
    private static List<Task> createBuildingTasks(String structureType) {
        List<Task> tasks = new ArrayList<>();
        Task task = new Task("build", Map.of(
            "structure", structureType,
            "blocks", List.of("oak_planks", "cobblestone", "glass_pane"),
            "dimensions", List.of(9, 6, 9)
        ));
        tasks.add(task);
        return tasks;
    }
    
    private static List<Task> createGatherTasks(String resource, int quantity) {
        List<Task> tasks = new ArrayList<>();
        Task task = new Task("gather", Map.of(
            "resource", resource,
            "quantity", quantity
        ));
        tasks.add(task);
        return tasks;
    }
    
    private static List<Task> createMineTasks(String ore, int quantity) {
        List<Task> tasks = new ArrayList<>();
        Task task = new Task("mine", Map.of(
            "block", ore,
            "quantity", quantity
        ));
        tasks.add(task);
        return tasks;
    }
    
    private static List<Task> createCombatTasks(String target) {
        List<Task> tasks = new ArrayList<>();
        Task task = new Task("attack", Map.of(
            "target", target
        ));
        tasks.add(task);
        return tasks;
    }
    
    private static List<Task> createFollowTasks() {
        List<Task> tasks = new ArrayList<>();
        Task task = new Task("follow", Map.of(
            "player", "nearest"
        ));
        tasks.add(task);
        return tasks;
    }
    
    private static List<Task> createStopTasks() {
        List<Task> tasks = new ArrayList<>();
        // Stop is handled at the command level, but we create a no-op task
        Task task = new Task("stop", Map.of());
        tasks.add(task);
        return tasks;
    }
    
    private static List<Task> createExploreTasks() {
        List<Task> tasks = new ArrayList<>();
        // Exploration is a placeholder - could be expanded
        Task task = new Task("pathfind", Map.of(
            "x", 0, "y", 64, "z", 0
        ));
        tasks.add(task);
        return tasks;
    }
    
    private static List<Task> createHelpTasks() {
        List<Task> tasks = new ArrayList<>();
        // Help is handled at the UI level
        Task task = new Task("help", Map.of());
        tasks.add(task);
        return tasks;
    }
    
    /**
     * Get a list of all available simple commands for help display.
     */
    public static String getAvailableCommands() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6=== 简单指令列表 ===§r\n\n");
        
        sb.append("§e建筑指令:§r\n");
        sb.append("  • build - 建造随机风格房屋\n");
        sb.append("  • build house/home - 建造房屋\n\n");
        
        sb.append("§e收集指令:§r\n");
        sb.append("  • collect everything - 收集所有资源\n");
        sb.append("  • get wood - 砍伐树木获取木头\n");
        sb.append("  • gather all - 收集所有物资\n\n");
        
        sb.append("§e战斗指令:§r\n");
        sb.append("  • fight / kill mobs - 清除敌对生物\n");
        sb.append("  • attack - 攻击怪物\n");
        sb.append("  • defend - 保护玩家\n\n");
        
        sb.append("§e跟随指令:§r\n");
        sb.append("  • follow - 跟随玩家\n");
        sb.append("  • come - 过来跟随\n");
        sb.append("  • stop - 停止当前动作\n\n");
        
        sb.append("§7也可以使用自然语言描述，例如:\n");
        sb.append("  '帮我建个房子'、'收集所有东西'、'打怪'§r\n");
        
        return sb.toString();
    }
}
