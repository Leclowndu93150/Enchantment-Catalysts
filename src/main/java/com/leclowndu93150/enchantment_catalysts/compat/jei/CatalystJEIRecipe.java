package com.leclowndu93150.enchantment_catalysts.compat.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record CatalystJEIRecipe(
        ItemStack catalystItem,
        List<EnchantmentEntry> enchantments
) {
    public record EnchantmentEntry(
            ItemStack enchantedBook,
            int weight,
            double chancePercent,
            int minCost
    ) {}
}
