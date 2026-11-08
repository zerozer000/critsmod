package com.example.critmod;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class CritSparkParticle extends TextureSheetParticle {

    protected CritSparkParticle(ClientLevel level, double x, double y, double z,
                                double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = dx + (random.nextDouble() - 0.5) * 0.05;
        this.yd = dy + (random.nextDouble() - 0.5) * 0.05;
        this.zd = dz + (random.nextDouble() - 0.5) * 0.05;
        this.friction = 0.9f;
        this.gravity = 0f;
        this.hasPhysics = false;
        this.lifetime = 8 + random.nextInt(6);
        this.quadSize = 0.12f + random.nextFloat() * 0.10f;
        this.roll = random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;

        // Цвет искр берётся из CritGlow
        this.rCol = CritGlow.R;
        this.gCol = CritGlow.G;
        this.bCol = CritGlow.B;

        pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = 1f - (float) age / lifetime;     // плавно гаснет
        this.oRoll = this.roll;
        this.roll += 0.15f;                           // слегка вращается
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;                              // всегда яркая, даже в темноте
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new CritSparkParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}