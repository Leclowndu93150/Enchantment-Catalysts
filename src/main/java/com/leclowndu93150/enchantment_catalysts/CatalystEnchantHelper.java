package com.leclowndu93150.enchantment_catalysts;

import com.leclowndu93150.enchantment_catalysts.data.CatalystData;
import com.leclowndu93150.enchantment_catalysts.data.WeightedEnchant;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CatalystEnchantHelper {

    public static List<EnchantmentInstance> selectEnchantments(
            CatalystData data, RegistryAccess registryAccess, ItemStack itemStack,
            int enchantmentCost, RandomSource random) {

        boolean isBook = itemStack.is(Items.BOOK);
        List<WeightedEnchant> compatible = new ArrayList<>();

        for (WeightedEnchant we : data.enchantments()) {
            if (enchantmentCost < we.minCost()) continue;

            Optional<Holder.Reference<Enchantment>> holder = resolve(we, registryAccess);
            if (holder.isEmpty()) continue;

            Enchantment ench = holder.get().value();
            if (ench.isPrimaryItem(itemStack) || isBook) {
                compatible.add(we);
            }
        }

        if (compatible.isEmpty()) {
            return List.of();
        }

        WeightedEnchant selected = weightedPick(compatible, random);

        List<EnchantmentInstance> result = new ArrayList<>();
        WeightedEnchant firstPick = selected;
        resolve(selected, registryAccess).ifPresent(holder ->
                result.add(new EnchantmentInstance(holder, firstPick.level()))
        );

        int modifiedCost = enchantmentCost / 2;
        while (random.nextInt(50) <= modifiedCost && !compatible.isEmpty()) {
            Optional<Holder.Reference<Enchantment>> lastHolder = resolve(selected, registryAccess);
            if (lastHolder.isPresent()) {
                Holder<Enchantment> lastEnch = lastHolder.get();
                compatible.removeIf(we -> {
                    Optional<Holder.Reference<Enchantment>> h = resolve(we, registryAccess);
                    return h.isEmpty() || !Enchantment.areCompatible(lastEnch, h.get());
                });
            }

            if (compatible.isEmpty()) break;

            selected = weightedPick(compatible, random);

            WeightedEnchant pick = selected;
            resolve(selected, registryAccess).ifPresent(holder ->
                    result.add(new EnchantmentInstance(holder, pick.level()))
            );

            modifiedCost /= 2;
        }

        if (isBook && result.size() > 1) {
            result.remove(random.nextInt(result.size()));
        }

        return result;
    }

    private static WeightedEnchant weightedPick(List<WeightedEnchant> entries, RandomSource random) {
        int totalWeight = 0;
        for (WeightedEnchant we : entries) {
            totalWeight += we.weight();
        }

        int roll = random.nextInt(totalWeight);
        int accumulated = 0;
        for (WeightedEnchant we : entries) {
            accumulated += we.weight();
            if (roll < accumulated) {
                return we;
            }
        }
        return entries.get(0);
    }

    private static Optional<Holder.Reference<Enchantment>> resolve(WeightedEnchant we, RegistryAccess registryAccess) {
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse(we.enchantment()));
        return registryAccess.lookupOrThrow(Registries.ENCHANTMENT).get(key);
    }
}
