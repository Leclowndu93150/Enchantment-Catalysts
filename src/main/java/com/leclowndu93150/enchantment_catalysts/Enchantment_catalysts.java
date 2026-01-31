package com.leclowndu93150.enchantment_catalysts;

import net.fabricmc.api.ModInitializer;

public class Enchantment_catalysts implements ModInitializer {

    @Override
    public void onInitialize() {
        CatalystRegistry.loadConfig();
    }
}
