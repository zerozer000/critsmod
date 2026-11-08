package com.example.critmod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;

@EventBusSubscriber(modid = CritMod.MODID, value = Dist.CLIENT)
public class MarkedSkullRenderer {
    private static final ResourceLocation SKULL =
            ResourceLocation.fromNamespaceAndPath(CritMod.MODID, "textures/gui/marked_skull.png");
    private static final int TEX = 128;
    private static final float SIZE = 24f;
    private static final double MAX_DIST = 48.0;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) return;

        GuiGraphics g = event.getGuiGraphics();
        int sw = g.guiWidth();
        int sh = g.guiHeight();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        long now = mc.level.getGameTime();
        float time = mc.player.tickCount + partial;

        Matrix4f viewProj = new Matrix4f(CritTextRenderer.PROJ).mul(CritTextRenderer.VIEW);
        Vec3 cam = CritTextRenderer.camPos;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity le)) continue;
            if (!MarkedClient.isMarked(le.getId(), now)) continue;
            if (le == mc.player && mc.options.getCameraType().isFirstPerson()) continue;

            double bob = Math.sin(time * 0.15) * 0.06;
            Vec3 pos = le.getPosition(partial).add(0, le.getBbHeight() + 0.6 + bob, 0);
            if (!canSee(mc, cam, le.getEyePosition(partial)) && !canSee(mc, cam, pos)) continue;
            Vector4f v = new Vector4f(
                    (float) (pos.x - cam.x), (float) (pos.y - cam.y), (float) (pos.z - cam.z), 1f);
            viewProj.transform(v);
            if (v.w <= 0.01f || v.w > MAX_DIST) continue;

            float nx = v.x / v.w;
            float ny = v.y / v.w;
            if (Math.abs(nx) > 1.3f || Math.abs(ny) > 1.3f) continue;

            float sx = (nx * 0.5f + 0.5f) * sw;
            float sy = (1f - (ny * 0.5f + 0.5f)) * sh;

            int size = Math.round(SIZE * Mth.clamp(8f / v.w, 0.35f, 1.6f));

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            g.blit(SKULL, Math.round(sx - size / 2f), Math.round(sy - size / 2f),
                    size, size, 0, 0, TEX, TEX, TEX, TEX);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        MarkedClient.clear();
    }
    private static boolean canSee(Minecraft mc, Vec3 from, Vec3 to) {
        HitResult hit = mc.level.clip(new ClipContext(
                from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, mc.player));
        return hit.getType() == HitResult.Type.MISS;
    }
}