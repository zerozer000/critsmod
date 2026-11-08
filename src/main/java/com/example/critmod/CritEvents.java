package com.example.critmod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = CritMod.MODID)
public class CritEvents {

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getSource().is(DamageTypes.THORNS)) return;

        LivingEntity target = event.getEntity();
        if (target == player) return;

        Entity direct = event.getSource().getDirectEntity();
        boolean melee = direct == player;
        boolean ranged = direct instanceof Projectile;
        if (!melee && !ranged) return;

        CritManager.CritType type = CritManager.roll(player, target, melee);

        switch (type) {
            case FULL -> {
                event.setAmount(event.getAmount() * CritConfig.CRIT_MULTIPLIER.get().floatValue());
                playEffects(target, true, event.getAmount());
            }
            case MINI -> {
                event.setAmount(event.getAmount() * CritConfig.MINI_CRIT_MULTIPLIER.get().floatValue());
                playEffects(target, false, event.getAmount());
            }
            case NONE -> CritManager.recordDamage(player, event.getAmount());
        }
    }

    private static void playEffects(LivingEntity target, boolean full, float damage) {
        if (!(target.level() instanceof ServerLevel level)) return;

        SoundEvent sound = (full ? ModSounds.CRIT_HIT : ModSounds.MINI_CRIT_HIT).get();
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                sound, SoundSource.PLAYERS, 1.0f, 1.0f);

        level.sendParticles(
                full ? ParticleTypes.CRIT : ParticleTypes.ENCHANTED_HIT,
                target.getX(), target.getY(0.5), target.getZ(),
                full ? 40 : 15, 0.4, 0.5, 0.4, 0.3);

        CritMod.LOGGER.info("[crit] sending text packet, damage={}", damage);

        // Текст над головой цели, виден игрокам в радиусе 64 блоков
        PacketDistributor.sendToPlayersNear(level, null,
                target.getX(), target.getY(), target.getZ(), 64,
                new CritTextPayload(target.getX(), target.getY() + target.getBbHeight() + 0.3,
                        target.getZ(), full ? 1 : 0, damage));
    }

    // Убираем ванильный крит, чтобы не было двойного бонуса
    @SubscribeEvent
    public static void onVanillaCrit(CriticalHitEvent event) {
        if (CritConfig.DISABLE_VANILLA_CRIT.get()) {
            event.setCriticalHit(false);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        CritManager.clear(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        CritManager.clearAll();
    }
}