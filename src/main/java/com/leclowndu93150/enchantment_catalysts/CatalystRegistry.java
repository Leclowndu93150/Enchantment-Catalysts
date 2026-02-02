package com.leclowndu93150.enchantment_catalysts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.leclowndu93150.enchantment_catalysts.data.CatalystData;
import com.leclowndu93150.enchantment_catalysts.data.RepairOverride;
import com.leclowndu93150.enchantment_catalysts.data.WeightedEnchant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CatalystRegistry {

    private static final Path CONFIG_PATH = Path.of("config", "enchantment_catalysts.json");
    private static final Path REPAIR_CONFIG_PATH = Path.of("config", "anvil_repair_overrides.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type CONFIG_TYPE = new TypeToken<HashMap<String, CatalystData>>() {}.getType();
    private static final Type REPAIR_CONFIG_TYPE = new TypeToken<HashMap<String, RepairOverride>>() {}.getType();

    private static HashMap<String, CatalystData> catalysts = new HashMap<>();
    private static HashMap<String, RepairOverride> repairOverrides = new HashMap<>();

    public static Map<String, CatalystData> getAllCatalysts() {
        return Collections.unmodifiableMap(catalysts);
    }

    public static Map<String, RepairOverride> getAllRepairOverrides() {
        return Collections.unmodifiableMap(repairOverrides);
    }

    public static RepairOverride getRepairOverride(String toolItemId) {
        return repairOverrides.get(toolItemId);
    }

    public static void updateRepairOverrides(Map<String, RepairOverride> synced) {
        repairOverrides = new HashMap<>(synced);
    }

    public static boolean isCatalyst(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return catalysts.containsKey(id.toString());
    }

    public static CatalystData getCatalyst(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return catalysts.get(id.toString());
    }

    public static void loadConfig() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                HashMap<String, CatalystData> loaded = GSON.fromJson(reader, CONFIG_TYPE);
                if (loaded != null) {
                    catalysts = loaded;
                }
            } catch (IOException e) {
                e.printStackTrace();
                saveDefaultConfig();
            }
        } else {
            saveDefaultConfig();
        }
        loadRepairConfig();
    }

    private static void loadRepairConfig() {
        if (Files.exists(REPAIR_CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(REPAIR_CONFIG_PATH)) {
                HashMap<String, RepairOverride> loaded = GSON.fromJson(reader, REPAIR_CONFIG_TYPE);
                if (loaded != null) {
                    repairOverrides = loaded;
                }
            } catch (IOException e) {
                e.printStackTrace();
                saveDefaultRepairConfig();
            }
        } else {
            saveDefaultRepairConfig();
        }
    }

    private static void saveDefaultConfig() {
        catalysts = createDefaults();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(catalysts, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, CatalystData> createDefaults() {
        LinkedHashMap<String, CatalystData> map = new LinkedHashMap<>();

        map.put("minecraft:ancient_debris", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:mending", 10, 1, 25)
        )));

        map.put("minecraft:amethyst_shard", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:fortune", 10, 3, 20),
                new WeightedEnchant("minecraft:looting", 10, 3, 20),
                new WeightedEnchant("minecraft:luck_of_the_sea", 8, 3, 15)
        )));

        map.put("minecraft:echo_shard", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:swift_sneak", 10, 3, 20),
                new WeightedEnchant("minecraft:soul_speed", 8, 3, 20)
        )));

        map.put("minecraft:blaze_powder", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:fire_aspect", 10, 2, 15),
                new WeightedEnchant("minecraft:flame", 10, 1, 10),
                new WeightedEnchant("minecraft:fire_protection", 8, 4, 15)
        )));

        map.put("minecraft:prismarine_shard", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:respiration", 10, 3, 15),
                new WeightedEnchant("minecraft:aqua_affinity", 10, 1, 1),
                new WeightedEnchant("minecraft:depth_strider", 8, 3, 15)
        )));

        map.put("minecraft:ender_pearl", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:silk_touch", 10, 1, 20),
                new WeightedEnchant("minecraft:infinity", 8, 1, 20)
        )));

        map.put("minecraft:heart_of_the_sea", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:channeling", 10, 1, 15),
                new WeightedEnchant("minecraft:loyalty", 10, 3, 15),
                new WeightedEnchant("minecraft:impaling", 8, 5, 20),
                new WeightedEnchant("minecraft:riptide", 6, 3, 20)
        )));

        map.put("minecraft:nether_star", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:sharpness", 8, 5, 20),
                new WeightedEnchant("minecraft:protection", 8, 4, 20),
                new WeightedEnchant("minecraft:efficiency", 8, 5, 20),
                new WeightedEnchant("minecraft:power", 8, 5, 20),
                new WeightedEnchant("minecraft:mending", 5, 1, 25),
                new WeightedEnchant("minecraft:unbreaking", 8, 3, 10),
                new WeightedEnchant("minecraft:silk_touch", 5, 1, 20)
        )));

        map.put("minecraft:redstone", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:quick_charge", 10, 3, 15),
                new WeightedEnchant("minecraft:piercing", 10, 4, 10),
                new WeightedEnchant("minecraft:multishot", 5, 1, 20)
        )));

        map.put("minecraft:ghast_tear", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:protection", 10, 4, 15),
                new WeightedEnchant("minecraft:blast_protection", 8, 4, 15),
                new WeightedEnchant("minecraft:fire_protection", 8, 4, 15),
                new WeightedEnchant("minecraft:projectile_protection", 8, 4, 15)
        )));

        map.put("minecraft:phantom_membrane", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:unbreaking", 10, 3, 10),
                new WeightedEnchant("minecraft:mending", 5, 1, 25)
        )));

        map.put("minecraft:emerald", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:efficiency", 10, 5, 20),
                new WeightedEnchant("minecraft:sharpness", 10, 5, 20),
                new WeightedEnchant("minecraft:protection", 10, 4, 20),
                new WeightedEnchant("minecraft:power", 10, 5, 20),
                new WeightedEnchant("minecraft:unbreaking", 8, 3, 10)
        )));

        map.put("minecraft:raw_gold", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:smite", 10, 5, 15),
                new WeightedEnchant("minecraft:bane_of_arthropods", 10, 5, 15)
        )));

        map.put("minecraft:raw_copper", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:knockback", 10, 2, 1),
                new WeightedEnchant("minecraft:punch", 10, 2, 1)
        )));

        map.put("minecraft:raw_iron", new CatalystData(1, List.of(
                new WeightedEnchant("minecraft:sharpness", 10, 3, 15),
                new WeightedEnchant("minecraft:protection", 10, 3, 15),
                new WeightedEnchant("minecraft:efficiency", 10, 3, 15)
        )));

        return map;
    }

    private static void saveDefaultRepairConfig() {
        repairOverrides = createRepairDefaults();
        try {
            Files.createDirectories(REPAIR_CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(REPAIR_CONFIG_PATH)) {
                GSON.toJson(repairOverrides, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, RepairOverride> createRepairDefaults() {
        LinkedHashMap<String, RepairOverride> map = new LinkedHashMap<>();

        RepairOverride rawIron = new RepairOverride("minecraft:raw_iron", 1, 5);
        map.put("minecraft:iron_sword", rawIron);
        map.put("minecraft:iron_pickaxe", rawIron);
        map.put("minecraft:iron_axe", rawIron);
        map.put("minecraft:iron_shovel", rawIron);
        map.put("minecraft:iron_hoe", rawIron);
        map.put("minecraft:iron_helmet", rawIron);
        map.put("minecraft:iron_chestplate", rawIron);
        map.put("minecraft:iron_leggings", rawIron);
        map.put("minecraft:iron_boots", rawIron);
        map.put("minecraft:chainmail_helmet", rawIron);
        map.put("minecraft:chainmail_chestplate", rawIron);
        map.put("minecraft:chainmail_leggings", rawIron);
        map.put("minecraft:chainmail_boots", rawIron);

        RepairOverride rawGold = new RepairOverride("minecraft:raw_gold", 1, 5);
        map.put("minecraft:golden_sword", rawGold);
        map.put("minecraft:golden_pickaxe", rawGold);
        map.put("minecraft:golden_axe", rawGold);
        map.put("minecraft:golden_shovel", rawGold);
        map.put("minecraft:golden_hoe", rawGold);
        map.put("minecraft:golden_helmet", rawGold);
        map.put("minecraft:golden_chestplate", rawGold);
        map.put("minecraft:golden_leggings", rawGold);
        map.put("minecraft:golden_boots", rawGold);

        return map;
    }
}
