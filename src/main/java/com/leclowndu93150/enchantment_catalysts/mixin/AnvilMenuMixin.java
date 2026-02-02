package com.leclowndu93150.enchantment_catalysts.mixin;

import com.leclowndu93150.enchantment_catalysts.CatalystRegistry;
import com.leclowndu93150.enchantment_catalysts.data.RepairOverride;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringUtil;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.Item;
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

        String toolId = BuiltInRegistries.ITEM.getKey(input.getItem()).toString();
        RepairOverride override = CatalystRegistry.getRepairOverride(toolId);

        if (override != null) {
            Item requiredMaterial = BuiltInRegistries.ITEM.getValue(Identifier.parse(override.material()));
            if (requiredMaterial != Items.AIR && addition.is(requiredMaterial)) {
                applyRepair(input, addition, override.cost(), override.amount(), ci);
            } else if (input.isValidRepairItem(addition)) {
                blockRepair(ci);
            }
        } else if (isVanillaSwappedRepair(input, addition)) {
            applyRepair(input, addition, 5, 1, ci);
        } else if (isBlockedVanillaIngot(input, addition)) {
            blockRepair(ci);
        }
    }

    private void applyRepair(ItemStack input, ItemStack addition, int levelCost, int materialCost, CallbackInfo ci) {
        ItemStack result = input.copy();
        result.setDamageValue(0);

        if (this.itemName != null && !StringUtil.isBlank(this.itemName)) {
            if (!this.itemName.equals(input.getHoverName().getString())) {
                result.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
            }
        } else if (input.has(DataComponents.CUSTOM_NAME)) {
            result.remove(DataComponents.CUSTOM_NAME);
        }

        this.cost.set(levelCost);
        this.repairItemCountCost = materialCost;
        this.onlyRenaming = false;

        this.resultSlots.setItem(0, result);
        this.broadcastChanges();
        ci.cancel();
    }

    private void blockRepair(CallbackInfo ci) {
        this.resultSlots.setItem(0, ItemStack.EMPTY);
        this.broadcastChanges();
        ci.cancel();
    }

    private static boolean isVanillaSwappedRepair(ItemStack tool, ItemStack material) {
        if (tool.isValidRepairItem(new ItemStack(Items.IRON_INGOT))) {
            return material.is(Items.RAW_IRON);
        }
        if (tool.isValidRepairItem(new ItemStack(Items.GOLD_INGOT))) {
            return material.is(Items.RAW_GOLD);
        }
        return false;
    }

    private static boolean isBlockedVanillaIngot(ItemStack tool, ItemStack material) {
        if (tool.isValidRepairItem(new ItemStack(Items.IRON_INGOT)) && material.is(Items.IRON_INGOT)) {
            return true;
        }
        if (tool.isValidRepairItem(new ItemStack(Items.GOLD_INGOT)) && material.is(Items.GOLD_INGOT)) {
            return true;
        }
        return false;
    }
}
