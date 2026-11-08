package com.example.critmod;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CritConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue CRIT_MULTIPLIER =
            B.comment("Множитель урона полного крита")
             .defineInRange("critMultiplier", 5.0, 1.0, 20.0);

    public static final ModConfigSpec.DoubleValue MINI_CRIT_MULTIPLIER =
            B.defineInRange("miniCritMultiplier", 2, 1.0, 20.0);

    public static final ModConfigSpec.DoubleValue MELEE_CHANCE =
            B.comment("Шанс крита в ближнем бою")
             .defineInRange("meleeChance", 0.15, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue RANGED_BASE_CHANCE =
            B.defineInRange("rangedBaseChance", 0.02, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue RANGED_MAX_BONUS =
            B.comment("Максимальная прибавка к шансу от нанесённого урона")
             .defineInRange("rangedMaxBonus", 0.10, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue DAMAGE_FOR_MAX_BONUS =
            B.comment("Сколько урона за окно нужно для максимальной прибавки")
             .defineInRange("damageForMaxBonus", 100.0, 1.0, 10000.0);

    public static final ModConfigSpec.IntValue WINDOW_SECONDS =
            B.defineInRange("damageWindowSeconds", 20, 1, 300);

    public static final ModConfigSpec.IntValue STREAK_TICKS =
            B.comment("Длительность серии критов после случайного крита (дальний бой)")
             .defineInRange("streakTicks", 40, 0, 400);

    public static final ModConfigSpec.BooleanValue DISABLE_VANILLA_CRIT =
            B.comment("Отключить ванильный крит при прыжке")
             .define("disableVanillaCrit", true);

    public static final ModConfigSpec.BooleanValue MARKED_BLOCKS_FULL_CRIT =
            B.comment("Помеченная цель получает только мини-криты")
                    .define("markedBlocksFullCrit", true);

    public static final ModConfigSpec SPEC = B.build();
}