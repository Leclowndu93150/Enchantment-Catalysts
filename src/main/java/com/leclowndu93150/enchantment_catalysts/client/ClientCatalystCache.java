package com.leclowndu93150.enchantment_catalysts.client;

import com.leclowndu93150.enchantment_catalysts.CatalystRegistry;
import com.leclowndu93150.enchantment_catalysts.data.CatalystData;
import com.leclowndu93150.enchantment_catalysts.data.RepairOverride;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientCatalystCache {

    private static final Map<String, CatalystData> catalysts = new ConcurrentHashMap<>();
    private static volatile boolean dataReceived = false;

    public static void update(Map<String, CatalystData> data, Map<String, RepairOverride> repairs) {
        catalysts.clear();
        catalysts.putAll(data);
        CatalystRegistry.updateRepairOverrides(repairs);
        dataReceived = true;
        JeiPluginBridge.onDataReceived();
    }

    public static Map<String, CatalystData> getCatalysts() {
        return Collections.unmodifiableMap(catalysts);
    }

    public static boolean isDataReceived() {
        return dataReceived;
    }

    public static void clear() {
        catalysts.clear();
        dataReceived = false;
    }
}
