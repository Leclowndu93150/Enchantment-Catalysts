package com.leclowndu93150.enchantment_catalysts.network;

import com.leclowndu93150.enchantment_catalysts.data.CatalystData;
import com.leclowndu93150.enchantment_catalysts.data.RepairOverride;
import com.leclowndu93150.enchantment_catalysts.data.WeightedEnchant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record CatalystSyncPayload(Map<String, CatalystData> catalysts, Map<String, RepairOverride> repairOverrides) implements CustomPacketPayload {

    public static final Type<CatalystSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("enchantment_catalysts", "catalyst_sync"));

    public static final StreamCodec<FriendlyByteBuf, CatalystSyncPayload> STREAM_CODEC =
            CustomPacketPayload.codec(CatalystSyncPayload::write, CatalystSyncPayload::read);

    private static CatalystSyncPayload read(FriendlyByteBuf buf) {
        int mapSize = buf.readVarInt();
        Map<String, CatalystData> map = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            String itemId = buf.readUtf();
            int consume = buf.readVarInt();
            int enchCount = buf.readVarInt();
            List<WeightedEnchant> enchantments = new ArrayList<>();
            for (int j = 0; j < enchCount; j++) {
                String enchId = buf.readUtf();
                int weight = buf.readVarInt();
                int level = buf.readVarInt();
                int minCost = buf.readVarInt();
                enchantments.add(new WeightedEnchant(enchId, weight, level, minCost));
            }
            map.put(itemId, new CatalystData(consume, enchantments));
        }
        int repairSize = buf.readVarInt();
        Map<String, RepairOverride> repairs = new HashMap<>();
        for (int i = 0; i < repairSize; i++) {
            String toolId = buf.readUtf();
            String material = buf.readUtf();
            int amount = buf.readVarInt();
            int cost = buf.readVarInt();
            repairs.put(toolId, new RepairOverride(material, amount, cost));
        }

        return new CatalystSyncPayload(map, repairs);
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(catalysts.size());
        for (Map.Entry<String, CatalystData> entry : catalysts.entrySet()) {
            buf.writeUtf(entry.getKey());
            CatalystData data = entry.getValue();
            buf.writeVarInt(data.consume());
            buf.writeVarInt(data.enchantments().size());
            for (WeightedEnchant we : data.enchantments()) {
                buf.writeUtf(we.enchantment());
                buf.writeVarInt(we.weight());
                buf.writeVarInt(we.level());
                buf.writeVarInt(we.minCost());
            }
        }

        buf.writeVarInt(repairOverrides.size());
        for (Map.Entry<String, RepairOverride> entry : repairOverrides.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeUtf(entry.getValue().material());
            buf.writeVarInt(entry.getValue().amount());
            buf.writeVarInt(entry.getValue().cost());
        }
    }

    @Override
    public Type<CatalystSyncPayload> type() {
        return TYPE;
    }
}
