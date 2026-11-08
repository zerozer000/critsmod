package com.example.critmod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

public final class CritManager {
    public enum CritType { NONE, MINI, FULL }

    private record Hit(long tick, float amount) {}

    private static final Map<UUID, Deque<Hit>> HISTORY = new HashMap<>();
    private static final Map<UUID, Long> STREAK_END = new HashMap<>();

    private CritManager() {}

    public static CritType roll(ServerPlayer player, LivingEntity target, boolean melee) {
        if (CritConfig.MARKED_BLOCKS_FULL_CRIT.get() && target.hasEffect(ModEffects.MARKED_FOR_DEATH)) {
            return CritType.MINI;
        }

        long now = player.level().getGameTime();
        UUID id = player.getUUID();

        // Гарантированный крит от эффекта
        if (player.hasEffect(ModEffects.CRIT_BOOST)) return CritType.FULL;

        if (hasCritWeapon(player)) return CritType.FULL;
        // Серия критов после случайного крита (дальний бой)
        if (!melee && now < STREAK_END.getOrDefault(id, 0L)) return CritType.FULL;

        float chance = melee
                ? CritConfig.MELEE_CHANCE.get().floatValue()
                : rangedChance(id, now);

        if (player.getRandom().nextFloat() < chance) {
            if (!melee) STREAK_END.put(id, now + CritConfig.STREAK_TICKS.get());
            return CritType.FULL;
        }

        return CritType.NONE;
    }

    /** Записываем только обычный урон: криты в историю не идут. */
    public static void recordDamage(ServerPlayer player, float amount) {
        HISTORY.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>())
               .addLast(new Hit(player.level().getGameTime(), amount));
    }

    private static float rangedChance(UUID id, long now) {
        float base = CritConfig.RANGED_BASE_CHANCE.get().floatValue();
        Deque<Hit> deque = HISTORY.get(id);
        if (deque == null) return base;

        long window = CritConfig.WINDOW_SECONDS.get() * 20L;
        while (!deque.isEmpty() && now - deque.peekFirst().tick() > window) {
            deque.pollFirst();
        }

        float total = 0;
        for (Hit h : deque) total += h.amount();

        float ratio = Math.min(total / CritConfig.DAMAGE_FOR_MAX_BONUS.get().floatValue(), 1f);
        return base + CritConfig.RANGED_MAX_BONUS.get().floatValue() * ratio;
    }

    public static void clear(UUID id) {
        HISTORY.remove(id);
        STREAK_END.remove(id);
    }

    public static void clearAll() {
        HISTORY.clear();
        STREAK_END.clear();
    }

    private static boolean hasCritWeapon(ServerPlayer player) {
        return player.getMainHandItem().has(ModComponents.GUARANTEED_CRIT.get())
                || player.getOffhandItem().has(ModComponents.GUARANTEED_CRIT.get());
    }
}