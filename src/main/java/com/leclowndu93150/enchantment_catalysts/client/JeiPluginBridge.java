package com.leclowndu93150.enchantment_catalysts.client;

import com.leclowndu93150.enchantment_catalysts.compat.jei.EnchantmentCatalystsJEIPlugin;
import net.fabricmc.loader.api.FabricLoader;

public final class JeiPluginBridge {

    private static final boolean JEI_LOADED = FabricLoader.getInstance().isModLoaded("jei");

    public static void onDataReceived() {
        if (!JEI_LOADED) return;
        EnchantmentCatalystsJEIPlugin.tryPushRecipes();
    }
}
