package com.leclowndu93150.enchantment_catalysts;

import com.leclowndu93150.enchantment_catalysts.network.CatalystSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class Enchantment_catalysts implements ModInitializer {

    @Override
    public void onInitialize() {
        CatalystRegistry.loadConfig();

        PayloadTypeRegistry.playS2C().register(CatalystSyncPayload.TYPE, CatalystSyncPayload.STREAM_CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            CatalystSyncPayload payload = new CatalystSyncPayload(CatalystRegistry.getAllCatalysts());
            ServerPlayNetworking.send(handler.getPlayer(), payload);
        });
    }
}
