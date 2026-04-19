package com.steve.ai.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SteveConfig {
    public static final ForgeConfigSpec SPEC;
    
    // AI Provider
    public static final ForgeConfigSpec.ConfigValue<String> AI_PROVIDER;
    
    // OpenAI Configuration
    public static final ForgeConfigSpec.ConfigValue<String> OPENAI_API_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> OPENAI_MODEL;
    public static final ForgeConfigSpec.IntValue OPENAI_MAX_TOKENS;
    public static final ForgeConfigSpec.DoubleValue OPENAI_TEMPERATURE;
    
    // Gemini Configuration
    public static final ForgeConfigSpec.ConfigValue<String> GEMINI_API_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> GEMINI_MODEL;
    public static final ForgeConfigSpec.IntValue GEMINI_MAX_TOKENS;
    public static final ForgeConfigSpec.DoubleValue GEMINI_TEMPERATURE;
    
    // Groq Configuration
    public static final ForgeConfigSpec.ConfigValue<String> GROQ_API_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> GROQ_MODEL;
    public static final ForgeConfigSpec.IntValue GROQ_MAX_TOKENS;
    public static final ForgeConfigSpec.DoubleValue GROQ_TEMPERATURE;
    
    // Qwen Configuration
    public static final ForgeConfigSpec.ConfigValue<String> QWEN_API_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> QWEN_MODEL;
    public static final ForgeConfigSpec.IntValue QWEN_MAX_TOKENS;
    public static final ForgeConfigSpec.DoubleValue QWEN_TEMPERATURE;
    
    // Behavior Configuration
    public static final ForgeConfigSpec.IntValue ACTION_TICK_DELAY;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CHAT_RESPONSES;
    public static final ForgeConfigSpec.IntValue MAX_ACTIVE_STEVES;
    
    // Network/Proxy Configuration
    public static final ForgeConfigSpec.ConfigValue<String> API_BASE_URL;
    public static final ForgeConfigSpec.ConfigValue<String> PROXY_HOST;
    public static final ForgeConfigSpec.IntValue PROXY_PORT;

    // Building configuration
    public static final ForgeConfigSpec.BooleanValue BUILDING_STYLES_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<String> BUILDING_STYLES;

    // Search radius configuration for Combat and Gather actions
    public static final ForgeConfigSpec.IntValue COMBAT_SEARCH_RADIUS;
    public static final ForgeConfigSpec.IntValue GATHER_SEARCH_RADIUS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("AI API Configuration").push("ai");
        
        AI_PROVIDER = builder
            .comment("AI provider to use: 'groq' (FASTEST, FREE), 'openai', 'gemini', or 'qwen'")
            .define("provider", "groq");
        
        builder.pop();

        builder.comment("OpenAI API Configuration").push("openai");
        
        OPENAI_API_KEY = builder
            .comment("Your OpenAI API key")
            .define("apiKey", "");
        
        OPENAI_MODEL = builder
            .comment("OpenAI model to use")
            .define("model", "gpt-4-turbo-preview");
        
        OPENAI_MAX_TOKENS = builder
            .comment("Maximum tokens per API request")
            .defineInRange("maxTokens", 8000, 100, 65536);
        
        OPENAI_TEMPERATURE = builder
            .comment("Temperature for AI responses")
            .defineInRange("temperature", 0.7, 0.0, 2.0);
        
        builder.pop();

        builder.comment("Google Gemini API Configuration").push("gemini");
        
        GEMINI_API_KEY = builder
            .comment("Your Gemini API key")
            .define("apiKey", "");
        
        GEMINI_MODEL = builder
            .comment("Gemini model to use")
            .define("model", "gemini-2.5-flash-latest");
        
        GEMINI_MAX_TOKENS = builder
            .comment("Maximum tokens per API request")
            .defineInRange("maxTokens", 8000, 100, 65536);
        
        GEMINI_TEMPERATURE = builder
            .comment("Temperature for AI responses")
            .defineInRange("temperature", 0.7, 0.0, 2.0);
        
        builder.pop();

        builder.comment("Groq API Configuration").push("groq");
        
        GROQ_API_KEY = builder
            .comment("Your Groq API key")
            .define("apiKey", "");
        
        GROQ_MODEL = builder
            .comment("Groq model to use")
            .define("model", "llama-3.1-8b-instant");
        
        GROQ_MAX_TOKENS = builder
            .comment("Maximum tokens per API request")
            .defineInRange("maxTokens", 500, 100, 65536);
        
        GROQ_TEMPERATURE = builder
            .comment("Temperature for AI responses")
            .defineInRange("temperature", 0.7, 0.0, 2.0);
        
        builder.pop();

        builder.comment("Qwen API Configuration").push("qwen");
        
        QWEN_API_KEY = builder
            .comment("Your Qwen API key")
            .define("apiKey", "");
        
        QWEN_MODEL = builder
            .comment("Qwen model to use")
            .define("model", "qwen-plus");
        
        QWEN_MAX_TOKENS = builder
            .comment("Maximum tokens per API request")
            .defineInRange("maxTokens", 8000, 100, 32000);
        
        QWEN_TEMPERATURE = builder
            .comment("Temperature for AI responses")
            .defineInRange("temperature", 0.7, 0.0, 2.0);
        
        builder.pop();

        builder.comment("Steve Behavior Configuration").push("behavior");
        
        ACTION_TICK_DELAY = builder
            .comment("Ticks between action checks (20 ticks = 1 second)")
            .defineInRange("actionTickDelay", 20, 1, 100);
        
        ENABLE_CHAT_RESPONSES = builder
            .comment("Allow Steves to respond in chat")
            .define("enableChatResponses", true);
        
        MAX_ACTIVE_STEVES = builder
            .comment("Maximum number of Steves that can be active simultaneously")
            .defineInRange("maxActiveSteves", 10, 1, 50);
        
        builder.pop();

        builder.comment("Network Configuration").push("network");
        
        API_BASE_URL = builder
            .comment("Custom API Base URL for Gemini (leave empty to use default). " +
                     "Useful for proxy servers in regions where Google API is blocked.")
            .define("apiBaseUrl", "");
        
        PROXY_HOST = builder
            .comment("Proxy host for HTTP connections (leave empty to use system proxy or no proxy)")
            .define("proxyHost", "");
        
        PROXY_PORT = builder
            .comment("Proxy port for HTTP connections (0 to disable)")
            .defineInRange("proxyPort", 0, 0, 65535);
        
        builder.pop();

        builder.comment("Building Configuration").push("building");

        BUILDING_STYLES_ENABLED = builder
            .comment("Enable multiple building styles for variety")
            .define("enableMultipleStyles", true);

        BUILDING_STYLES = builder
            .comment("Comma-separated list of enabled building styles:\n" +
                "Original: oak_classic,spruce_cabin,birch_cottage,stone_fortress,sandstone_desert,dark_oak_manor,brick_house,jungle_hut\n" +
                "New: acacia_outpost,cherry_blossom,bamboo_retreat,mangrove_swamp,deepslate_cavern,nether_brick_fortress,warped_forest,crimson_hunting_lodge")
            .define("styles", "oak_classic,spruce_cabin,birch_cottage,stone_fortress,sandstone_desert,dark_oak_manor,brick_house,jungle_hut,acacia_outpost,cherry_blossom,bamboo_retreat,mangrove_swamp,deepslate_cavern,nether_brick_fortress,warped_forest,crimson_hunting_lodge");

        builder.pop();

        builder.comment("Search Radius Configuration").push("search");

        COMBAT_SEARCH_RADIUS = builder
            .comment("Search radius for combat actions (kill mobs, attack).\n" +
                "Default: 50 blocks radius = 100x100 area around player.\n" +
                "Larger values cover more area but may impact performance.")
            .defineInRange("combatRadius", 50, 10, 200);

        GATHER_SEARCH_RADIUS = builder
            .comment("Search radius for gather actions (get wood, flowers, etc.).\n" +
                "Default: 50 blocks radius = 100x100 area around player.\n" +
                "Larger values cover more area but may impact performance.")
            .defineInRange("gatherRadius", 50, 10, 200);

        builder.pop();

        SPEC = builder.build();
    }
}

