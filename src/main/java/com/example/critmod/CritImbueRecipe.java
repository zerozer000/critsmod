package com.example.critmod;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class CritImbueRecipe extends CustomRecipe {

    public CritImbueRecipe(CraftingBookCategory category) {
        super(category);
    }

    /** Что считается оружием. Добавляйте сюда свои типы. */
    public static boolean isWeapon(ItemStack s) {
        return s.is(ItemTags.SWORDS) || s.is(ItemTags.AXES)
                || s.getItem() instanceof BowItem
                || s.getItem() instanceof CrossbowItem
                || s.getItem() instanceof TridentItem
                || s.getItem() instanceof MaceItem;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean essence = false;
        boolean weapon = false;

        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.isEmpty()) continue;

            if (s.is(ModItems.CRIT_ESSENCE.get())) {
                if (essence) return false;                 // только одна эссенция
                essence = true;
            } else if (isWeapon(s) && !s.has(ModComponents.GUARANTEED_CRIT.get())) {
                if (weapon) return false;                  // только одно оружие
                weapon = true;
            } else {
                return false;                              // лишний предмет
            }
        }
        return essence && weapon;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (isWeapon(s)) {
                ItemStack result = s.copy();               // сохраняем зачарования и износ
                result.setCount(1);
                result.set(ModComponents.GUARANTEED_CRIT.get(), true);
                result.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CRIT_IMBUE.get();
    }
}