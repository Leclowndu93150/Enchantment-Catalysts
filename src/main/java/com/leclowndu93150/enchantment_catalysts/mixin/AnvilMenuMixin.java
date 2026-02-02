package com.leclowndu93150.enchantment_catalysts.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

    @Shadow private int repairItemCountCost;
    @Shadow private String itemName;
    @Shadow @Final private DataSlot cost;
    @Shadow private boolean onlyRenaming;

    private AnvilMenuMixin() {
        super(null, 0, null, null, null);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void overrideRepair(CallbackInfo ci) {
        ItemStack input = this.inputSlots.getItem(0);
        ItemStack addition = this.inputSlots.getItem(1);

        if (input.isEmpty() || !input.isDamageableItem() || input.getDamageValue() == 0) return;
        if (addition.isEmpty()) return;
        if (!isModdedRepairItem(input, addition)) return;

        ItemStack result = input.copy();
        result.setDamageValue(0);

        if (this.itemName != null && !StringUtil.isBlank(this.itemName)) {
            if (!this.itemName.equals(input.getHoverName().getString())) {
                result.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
            }
        } else if (input.has(DataComponents.CUSTOM_NAME)) {
            result.remove(DataComponents.CUSTOM_NAME);
        }

        this.cost.set(5);
        this.repairItemCountCost = 1;
        this.onlyRenaming = false;

        this.resultSlots.setItem(0, result);
        this.broadcastChanges();
        ci.cancel();
    }

    private static boolean isModdedRepairItem(ItemStack tool, ItemStack material) {
        if (tool.isValidRepairItem(new ItemStack(Items.IRON_INGOT))) {
            return material.is(Items.RAW_IRON);
        }
        if (tool.isValidRepairItem(new ItemStack(Items.GOLD_INGOT))) {
            return material.is(Items.RAW_GOLD);
        }
        return tool.isValidRepairItem(material);
    }
}
