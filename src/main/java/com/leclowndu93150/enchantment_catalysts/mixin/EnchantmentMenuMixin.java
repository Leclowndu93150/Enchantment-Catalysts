package com.leclowndu93150.enchantment_catalysts.mixin;

import com.leclowndu93150.enchantment_catalysts.CatalystEnchantHelper;
import com.leclowndu93150.enchantment_catalysts.CatalystRegistry;
import com.leclowndu93150.enchantment_catalysts.data.CatalystData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {

    @Shadow @Final private Container enchantSlots;
    @Shadow @Final private RandomSource random;
    @Shadow @Final private DataSlot enchantmentSeed;

    private static final Identifier EMPTY_SLOT_LAPIS_LAZULI = Identifier.withDefaultNamespace("container/slot/lapis_lazuli");

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void replaceLapisSlot(CallbackInfo ci) {
        EnchantmentMenu self = (EnchantmentMenu) (Object) this;
        Slot oldSlot = self.slots.get(1);
        Slot newSlot = new Slot(this.enchantSlots, 1, 35, 47) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.is(Items.LAPIS_LAZULI) || CatalystRegistry.isCatalyst(itemStack);
            }

            @Override
            public Identifier getNoItemIcon() {
                return EMPTY_SLOT_LAPIS_LAZULI;
            }
        };
        newSlot.index = oldSlot.index;
        self.slots.set(1, newSlot);
    }

    @Inject(method = "getEnchantmentList", at = @At("HEAD"), cancellable = true)
    private void catalystEnchantmentList(RegistryAccess registryAccess, ItemStack itemStack, int slot, int enchantmentCost, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        ItemStack catalystStack = this.enchantSlots.getItem(1);
        if (!CatalystRegistry.isCatalyst(catalystStack)) return;

        CatalystData data = CatalystRegistry.getCatalyst(catalystStack);
        this.random.setSeed(this.enchantmentSeed.get() + slot);
        List<EnchantmentInstance> result = CatalystEnchantHelper.selectEnchantments(data, registryAccess, itemStack, enchantmentCost, this.random);
        if (!result.isEmpty()) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "getGoldCount", at = @At("HEAD"), cancellable = true)
    private void catalystGoldCount(CallbackInfoReturnable<Integer> cir) {
        ItemStack catalystStack = this.enchantSlots.getItem(1);
        if (CatalystRegistry.isCatalyst(catalystStack)) {
            cir.setReturnValue(catalystStack.getCount());
        }
    }

    @Redirect(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", ordinal = 0))
    private boolean allowCatalystShiftClick(ItemStack stack, Item item) {
        return stack.is(Items.LAPIS_LAZULI) || CatalystRegistry.isCatalyst(stack);
    }
}
