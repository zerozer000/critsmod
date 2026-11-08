package com.example.critmod;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4fStack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = CritMod.MODID, value = Dist.CLIENT)
public class CritTextRenderer {
    private static final int LIFETIME = 30; // тиков (1.5 секунды)
    private static final ResourceLocation CRIT_TEX =
            ResourceLocation.fromNamespaceAndPath(CritMod.MODID, "textures/gui/crit_text.png");
    private static final int TEX_W = 233;
    private static final int TEX_H = 128;
    private static final int DRAW_W = 50;   // ширина на экране, меняйте под вкус
    private static final int DRAW_H = DRAW_W * TEX_H / TEX_W;
    private static final ResourceLocation CRIT_TEX_RED =
            ResourceLocation.fromNamespaceAndPath(CritMod.MODID, "textures/gui/crit_text_red.png");

    private static final ResourceLocation MINI_TEX =
            ResourceLocation.fromNamespaceAndPath(CritMod.MODID, "textures/gui/mini_crit_text.png");
    private static final int MINI_W = 232;
    private static final int MINI_H = 151;
    private static final int MINI_DRAW_W = 50;
    private static final int MINI_DRAW_H = MINI_DRAW_W * MINI_H / MINI_W;

    private static class FloatingText {
        final double x, y, z;
        final boolean full;
        final float damage;
        int age = 0;

        FloatingText(double x, double y, double z, boolean full, float damage) {
            this.x = x; this.y = y; this.z = z;
            this.full = full;
            this.damage = damage;
        }
    }

    private static final List<FloatingText> TEXTS = new ArrayList<>();

    public static void add(double x, double y, double z, int type, float damage) {
        // Небольшой случайный разброс, чтобы тексты не накладывались друг на друга
        double ox = (Math.random() - 0.5) * 0.4;
        double oz = (Math.random() - 0.5) * 0.4;
        TEXTS.add(new FloatingText(x + ox, y, z + oz, type == 1, damage));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        TEXTS.removeIf(t -> ++t.age > LIFETIME);
    }

    static final Matrix4f VIEW = new Matrix4f();
    static final Matrix4f PROJ = new Matrix4f();
    static Vec3 camPos = Vec3.ZERO;

    // Запоминаем матрицы камеры каждый кадр
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        VIEW.set(event.getModelViewMatrix());
        PROJ.set(event.getProjectionMatrix());
        camPos = event.getCamera().getPosition();
    }

    // Рисуем текст поверх экрана в проекции мировой точки
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (TEXTS.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        GuiGraphics g = event.getGuiGraphics();
        int sw = g.guiWidth();
        int sh = g.guiHeight();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        Matrix4f viewProj = new Matrix4f(PROJ).mul(VIEW);

        for (FloatingText t : TEXTS) {
            float age = t.age + partial;
            double y = t.y + age * 0.04;

            // Мировая точка -> координаты экрана
            Vector4f v = new Vector4f(
                    (float) (t.x - camPos.x),
                    (float) (y - camPos.y),
                    (float) (t.z - camPos.z),
                    1.0f);
            viewProj.transform(v);
            if (v.w <= 0.01f) continue;              // точка позади камеры

            float nx = v.x / v.w;
            float ny = v.y / v.w;
            if (Math.abs(nx) > 1.3f || Math.abs(ny) > 1.3f) continue;  // за краем экрана

            float sx = (nx * 0.5f + 0.5f) * sw;
            float sy = (1.0f - (ny * 0.5f + 0.5f)) * sh;

            // Эффект "выскакивания" и масштаб от расстояния
            float pop = 1.0f + 0.6f * Math.max(0f, 1f - age / 4f);
            float distScale = Mth.clamp(8.0f / v.w, 0.8f, 2.5f);
            float scale = 1.2f * pop * distScale;

            // Затухание (альфа не должна быть меньше 4)
            float fade = Math.min(1f, (LIFETIME - age) / 10f);
            int alpha = Math.max(5, (int) (255 * fade));
            int color = (alpha << 24) | (t.full ? 0xFF3030 : 0xFFD040);
            int dmgColor = (alpha << 24) | 0xFFFFFF;

            PoseStack pose = g.pose();
            pose.pushPose();
            pose.translate(sx, sy, 0);
            pose.scale(scale, scale, 1f);

            if (t.full) {
                // Прогресс перехода: 0 до середины жизни, потом плавно до 1
                float life = age / LIFETIME;
                float red = Mth.clamp((life - 0.35f) / 0.5f, 0f, 1f);

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                // Зелёная, угасает по мере перехода
                RenderSystem.setShaderColor(1f, 1f, 1f, (alpha / 255f) * (1f - red));
                g.blit(CRIT_TEX, -DRAW_W / 2, -DRAW_H, DRAW_W, DRAW_H,
                        0, 0, TEX_W, TEX_H, TEX_W, TEX_H);

                // Красная, проявляется поверх
                RenderSystem.setShaderColor(1f, 1f, 1f, (alpha / 255f) * red);
                g.blit(CRIT_TEX_RED, -DRAW_W / 2, -DRAW_H, DRAW_W, DRAW_H,
                        0, 0, TEX_W, TEX_H, TEX_W, TEX_H);

                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);


            } else {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1f, 1f, 1f, alpha / 255f);

                g.blit(MINI_TEX, -MINI_DRAW_W / 2, -MINI_DRAW_H, MINI_DRAW_W, MINI_DRAW_H,
                        0, 0, MINI_W, MINI_H, MINI_W, MINI_H);

                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }

            pose.popPose();
        }
    }
}