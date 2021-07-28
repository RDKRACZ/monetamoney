package github.pitbox46.monetamoney;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final String CATEGORY_GENERAL = "general";

    public static ForgeConfigSpec SERVER_CONFIG;

    public static ForgeConfigSpec.DoubleValue KILL_MONEY;
    public static ForgeConfigSpec.LongValue INITIAL_BAL;
    public static ForgeConfigSpec.LongValue DAILY_REWARD;
    public static ForgeConfigSpec.LongValue BASE_CHUNKLOADER;
    public static ForgeConfigSpec.DoubleValue MULTIPLIER_CHUNKLOADER;
    public static ForgeConfigSpec.LongValue OVERDRAFT_FEE;
    public static ForgeConfigSpec.LongValue ADVANCEMENT_REWARD;

    static {
        ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

        SERVER_BUILDER.comment("General Settings").push(CATEGORY_GENERAL);

        INITIAL_BAL = SERVER_BUILDER.comment("Initial player balance")
                .defineInRange("init_bal", 1000, 0, Long.MAX_VALUE);
        KILL_MONEY = SERVER_BUILDER.comment("Percent of account balance that is transferred on kill if the killed player has no physical money on person")
                .defineInRange("kill_money", 0.1d, 0, 1);
        DAILY_REWARD = SERVER_BUILDER.comment("Reward amount for players logging in every 24 hours")
                .defineInRange("daily_reward", 100, 0, Long.MAX_VALUE);
        ADVANCEMENT_REWARD = SERVER_BUILDER.comment("Reward that a player gets per advancement")
                .defineInRange("advancement_reward", 100, 0, Long.MAX_VALUE);
        BASE_CHUNKLOADER = SERVER_BUILDER.comment("Base chunk loader cost per 24 hours")
                .defineInRange("base_chunkloader", 100, 0, Long.MAX_VALUE);
        MULTIPLIER_CHUNKLOADER = SERVER_BUILDER.comment("The amount that chunk loader cost scales by. " +
                "The cost for a new loader is this multiplier multiplied by the previous cost")
                .defineInRange("multi_chunkloader", 1.1d, 0, Double.MAX_VALUE);
        OVERDRAFT_FEE = SERVER_BUILDER.comment("If a chunk for some reason is still force loaded when a team has no money, an overdraft fee is incurred")
                .defineInRange("overdraft_fee", 100, 0, Long.MAX_VALUE);

        SERVER_BUILDER.pop();
        SERVER_CONFIG = SERVER_BUILDER.build();
    }
}
