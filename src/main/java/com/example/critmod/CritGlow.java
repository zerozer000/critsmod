package com.example.critmod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid = CritMod.MODID, value = Dist.CLIENT)
public class CritGlow {
    // Цвет эффекта (0..1). Красный. Для синего: R=0.3, G=0.5, B=1.0
    public static final float R = 1.0f;
    public static final float G = 0.15f;
    public static final float B = 0.10f;

    // --- Настройки положения искр в первом лице (подгоняйте под вкус) ---
    private static final double ITEM_SIDE = 0.56;    // в сторону от центра экрана
    private static final double ITEM_UP = -0.30;     // вверх/вниз (минус = ниже)
    private static final double ITEM_FWD = 0.95;     // вперёд от камеры
    private static final double HAND_SIDE = 0.64;    // то же, но для пустой руки
    private static final double HAND_UP = -0.45;
    private static final double HAND_FWD = 0.85;

    private static final float SOUND_VOLUME = 0.4f;
    private static final int SOUND_CHANCE = 15;      // раз в ~10 тиков на руку (больше = реже)

    // ---------- Оттенок предмета и руки ----------

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.hasEffect(ModEffects.CRIT_BOOST)) return;

        float t = mc.player.tickCount + event.getPartialTick();
        float k = 0.75f + 0.25f * (0.5f + 0.5f * Mth.sin(t * 0.25f));
        RenderSystem.setShaderColor(1.0f, 0.30f * k, 0.25f * k, 1.0f);
    }

    @SubscribeEvent
    public static void onGuiPre(RenderGuiEvent.Pre event) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    // ---------- Искры и звук ----------

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null || mc.isPaused()) return;

        for (Player p : level.players()) {
            if (!p.hasEffect(ModEffects.CRIT_BOOST)) continue;
            if (p.distanceToSqr(mc.player) > 48 * 48) continue;

            boolean firstPersonSelf = p == mc.player && mc.options.getCameraType().isFirstPerson();
            int mainSide = p.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;

            // Основная рука (предмет, блок или пустая рука)
            boolean emptyMain = p.getMainHandItem().isEmpty();
            Vec3 mainPos = firstPersonSelf
                    ? firstPersonHand(mc, mainSide, emptyMain)
                    : bodyHand(p, mainSide);
            emit(level, mainPos, 2 + level.random.nextInt(2));

            // Вторая рука: только если в ней что-то есть
            if (!p.getOffhandItem().isEmpty()) {
                Vec3 offPos = firstPersonSelf
                        ? firstPersonHand(mc, -mainSide, false)
                        : bodyHand(p, -mainSide);
                emit(level, offPos, 1 + level.random.nextInt(2));
            }
        }
    }

    // Позиция руки/предмета от первого лица (по камере)
    private static Vec3 firstPersonHand(Minecraft mc, int side, boolean empty) {
        Camera cam = mc.gameRenderer.getMainCamera();
        Vector3f look = cam.getLookVector();
        Vector3f up = cam.getUpVector();
        Vector3f left = cam.getLeftVector();

        // Рука рисуется с фиксированным FOV 70°. Поправка на ваш FOV
        double fovK = Math.tan(Math.toRadians(mc.options.fov().get() / 2.0))
                / Math.tan(Math.toRadians(35.0));

        double sx = (empty ? HAND_SIDE : ITEM_SIDE) * side * fovK;
        double sy = (empty ? HAND_UP : ITEM_UP) * fovK;
        double sz = empty ? HAND_FWD : ITEM_FWD;

        Vec3 c = cam.getPosition();
        return new Vec3(
                c.x + look.x * sz + up.x * sy - left.x * sx,
                c.y + look.y * sz + up.y * sy - left.y * sx,
                c.z + look.z * sz + up.z * sy - left.z * sx);
    }

    // Позиция руки для третьего лица и других игроков (по телу)
    private static Vec3 bodyHand(Player p, int side) {
        float yaw = p.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw));
        Vec3 right = new Vec3(-Mth.cos(yaw), 0, -Mth.sin(yaw));
        double height = p.isCrouching() ? 0.8 : 1.0;
        return p.position()
                .add(0, height, 0)
                .add(right.scale(0.42 * side))
                .add(forward.scale(0.45));
    }

    private static void emit(ClientLevel level, Vec3 pos, int count) {
        for (int i = 0; i < count; i++) {
            level.addParticle(ModParticles.CRIT_SPARK.get(),
                    pos.x + (level.random.nextDouble() - 0.5) * 0.25,
                    pos.y + (level.random.nextDouble() - 0.5) * 0.25,
                    pos.z + (level.random.nextDouble() - 0.5) * 0.25,
                    0, 0.01, 0);
        }

        if (level.random.nextInt(SOUND_CHANCE) == 0) {
            level.playLocalSound(pos.x, pos.y, pos.z,
                    ModSounds.CRIT_SPARK.get(), SoundSource.PLAYERS,
                    SOUND_VOLUME, 0.8f + level.random.nextFloat() * 0.4f, false);
        }
    }
}