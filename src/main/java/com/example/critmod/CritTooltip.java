package com.example.critmod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = CritMod.MODID, value = Dist.CLIENT)
public class CritTooltip {
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().has(ModComponents.GUARANTEED_CRIT.get())) {
            event.getToolTip().add(Component.translatable("critmod.tooltip.guaranteed_crit")
                    .withStyle(ChatFormatting.RED));
        }
    }
}