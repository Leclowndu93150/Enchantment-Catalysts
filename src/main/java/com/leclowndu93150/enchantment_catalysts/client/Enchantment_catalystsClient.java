package com.leclowndu93150.enchantment_catalysts.client;

import com.leclowndu93150.enchantment_catalysts.network.CatalystSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class Enchantment_catalystsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CatalystSyncPayload.TYPE, (payload, context) -> {
            ClientCatalystCache.update(payload.catalysts(), payload.repairOverrides());
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientCatalystCache.clear();
        });
    }
}
