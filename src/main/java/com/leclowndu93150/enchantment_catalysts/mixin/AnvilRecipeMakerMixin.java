package com.leclowndu93150.enchantment_catalysts.mixin;

import com.leclowndu93150.enchantment_catalysts.CatalystRegistry;
import com.leclowndu93150.enchantment_catalysts.data.RepairOverride;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.plugins.vanilla.anvil.AnvilRecipeMaker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(AnvilRecipeMaker.class)
public class AnvilRecipeMakerMixin {

    @Inject(method = "getAnvilRecipes", at = @At("RETURN"), cancellable = true, remap = false)
    private static void modifyAnvilRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager, CallbackInfoReturnable<List<IJeiAnvilRecipe>> cir) {
        Map<String, RepairOverride> overrides = CatalystRegistry.getAllRepairOverrides();
        if (overrides.isEmpty()) return;

        Set<String> overriddenItems = overrides.keySet();
        List<IJeiAnvilRecipe> original = cir.getReturnValue();
        List<IJeiAnvilRecipe> modified = new ArrayList<>(original);


        modified.removeIf(recipe -> {
            Identifier uid = recipe.getUid();
            if (uid == null) return false;
            String path = uid.getPath();
            if (!path.startsWith("anvil.materials_repair.")) return false;
            for (ItemStack left : recipe.getLeftInputs()) {
                String itemId = BuiltInRegistries.ITEM.getKey(left.getItem()).toString();
                if (overriddenItems.contains(itemId)) return true;
            }
            return false;
        });

        for (Map.Entry<String, RepairOverride> entry : overrides.entrySet()) {
            String toolId = entry.getKey();
            RepairOverride override = entry.getValue();

            Item toolItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(toolId));
            if (toolItem == Items.AIR) continue;

            Item materialItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(override.material()));
            if (materialItem == Items.AIR) continue;

            ItemStack damagedTool = new ItemStack(toolItem);
            damagedTool.setDamageValue(damagedTool.getMaxDamage());

            ItemStack repairedTool = new ItemStack(toolItem);

            ItemStack material = new ItemStack(materialItem, override.amount());

            Identifier itemKey = BuiltInRegistries.ITEM.getKey(toolItem);
            IJeiAnvilRecipe recipe = vanillaRecipeFactory.createAnvilRecipe(
                    List.of(damagedTool),
                    List.of(material),
                    List.of(repairedTool),
                    Identifier.fromNamespaceAndPath(itemKey.getNamespace(), "anvil.override_repair." + itemKey.getPath())
            );
            modified.add(recipe);
        }

        cir.setReturnValue(modified);
    }
}
