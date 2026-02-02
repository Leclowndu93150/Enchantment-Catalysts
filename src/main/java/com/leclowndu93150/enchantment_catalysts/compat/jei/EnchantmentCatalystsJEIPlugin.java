package com.leclowndu93150.enchantment_catalysts.compat.jei;

import com.leclowndu93150.enchantment_catalysts.client.ClientCatalystCache;
import com.leclowndu93150.enchantment_catalysts.data.CatalystData;
import com.leclowndu93150.enchantment_catalysts.data.WeightedEnchant;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@JeiPlugin
public class EnchantmentCatalystsJEIPlugin implements IModPlugin {

    private static IJeiRuntime jeiRuntime = null;
    private static boolean recipesRegisteredStatically = false;

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath("enchantment_catalysts", "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new CatalystJEICategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (ClientCatalystCache.isDataReceived()) {
            List<CatalystJEIRecipe> recipes = buildRecipes();
            if (!recipes.isEmpty()) {
                registration.addRecipes(CatalystJEICategory.RECIPE_TYPE, recipes);
                recipesRegisteredStatically = true;
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(CatalystJEICategory.RECIPE_TYPE, Items.ENCHANTING_TABLE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        jeiRuntime = runtime;
        if (ClientCatalystCache.isDataReceived() && !recipesRegisteredStatically) {
            pushRecipesToRuntime();
        }
    }

    @Override
    public void onRuntimeUnavailable() {
        jeiRuntime = null;
        recipesRegisteredStatically = false;
    }

    public static void tryPushRecipes() {
        if (jeiRuntime != null) {
            pushRecipesToRuntime();
        }
    }

    private static void pushRecipesToRuntime() {
        if (jeiRuntime == null) return;
        List<CatalystJEIRecipe> recipes = buildRecipes();
        if (!recipes.isEmpty()) {
            jeiRuntime.getRecipeManager().addRecipes(CatalystJEICategory.RECIPE_TYPE, recipes);
        }
    }

    private static List<CatalystJEIRecipe> buildRecipes() {
        Map<String, CatalystData> catalysts = ClientCatalystCache.getCatalysts();
        List<CatalystJEIRecipe> recipes = new ArrayList<>();

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return recipes;

        RegistryAccess registryAccess = mc.level.registryAccess();

        for (Map.Entry<String, CatalystData> entry : catalysts.entrySet()) {
            String itemId = entry.getKey();
            CatalystData data = entry.getValue();

            Item catalystItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
            if (catalystItem == Items.AIR) continue;

            ItemStack catalystStack = new ItemStack(catalystItem, data.consume());

            int totalWeight = 0;
            for (WeightedEnchant we : data.enchantments()) {
                totalWeight += we.weight();
            }
            if (totalWeight == 0) continue;

            List<CatalystJEIRecipe.EnchantmentEntry> enchEntries = new ArrayList<>();
            for (WeightedEnchant we : data.enchantments()) {
                ResourceKey<Enchantment> key = ResourceKey.create(
                        Registries.ENCHANTMENT, Identifier.parse(we.enchantment())
                );
                Optional<Holder.Reference<Enchantment>> holderOpt =
                        registryAccess.lookupOrThrow(Registries.ENCHANTMENT).get(key);
                if (holderOpt.isEmpty()) continue;

                Holder<Enchantment> holder = holderOpt.get();
                ItemStack bookStack = EnchantmentHelper.createBook(new EnchantmentInstance(holder, we.level()));
                double chance = (we.weight() / (double) totalWeight) * 100.0;

                enchEntries.add(new CatalystJEIRecipe.EnchantmentEntry(
                        bookStack, we.weight(), chance, we.minCost()
                ));
            }

            if (!enchEntries.isEmpty()) {
                recipes.add(new CatalystJEIRecipe(catalystStack, enchEntries));
            }
        }

        return recipes;
    }
}
