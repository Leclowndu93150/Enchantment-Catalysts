package com.leclowndu93150.enchantment_catalysts.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class CatalystJEICategory extends AbstractRecipeCategory<CatalystJEIRecipe> {

    public static final IRecipeType<CatalystJEIRecipe> RECIPE_TYPE =
            IRecipeType.create("enchantment_catalysts", "catalyst", CatalystJEIRecipe.class);

    private static final int ITEMS_PER_ROW = 3;

    private final IDrawableStatic arrow;

    public CatalystJEICategory(IGuiHelper guiHelper) {
        super(RECIPE_TYPE,
                Component.literal("Enchantment Catalysts"),
                guiHelper.createDrawableItemLike(Items.ENCHANTING_TABLE),
                160, 120);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CatalystJEIRecipe recipe, IFocusGroup focuses) {
        int inputX = (this.getWidth() - 18) / 2;
        builder.addInputSlot(inputX, 2)
                .add(recipe.catalystItem())
                .setStandardSlotBackground();

        int outputCount = recipe.enchantments().size();
        int outputStartY = 46;

        for (int i = 0; i < outputCount; i++) {
            CatalystJEIRecipe.EnchantmentEntry entry = recipe.enchantments().get(i);

            int row = i / ITEMS_PER_ROW;
            int col = i % ITEMS_PER_ROW;
            int itemsInRow = Math.min(ITEMS_PER_ROW, outputCount - row * ITEMS_PER_ROW);
            int rowStartX = (this.getWidth() - itemsInRow * 18) / 2;

            builder.addOutputSlot(rowStartX + col * 18, outputStartY + row * 18)
                    .add(entry.enchantedBook())
                    .setStandardSlotBackground()
                    .addRichTooltipCallback((slotView, tooltip) -> {
                        String chanceText;
                        if (entry.chancePercent() >= 10) {
                            chanceText = String.format("%.0f%%", entry.chancePercent());
                        } else if (entry.chancePercent() >= 1) {
                            chanceText = String.format("%.1f%%", entry.chancePercent());
                        } else {
                            chanceText = String.format("%.2f%%", entry.chancePercent());
                        }
                        tooltip.add(Component.literal("Chance: " + chanceText)
                                .withStyle(ChatFormatting.GOLD));
                        tooltip.add(Component.literal("Min Cost: " + entry.minCost() + " levels")
                                .withStyle(ChatFormatting.GRAY));
                    });
        }
    }

    @Override
    public void draw(CatalystJEIRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int centerX = this.getWidth() / 2;
        int centerY = 32;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(centerX, centerY);
        guiGraphics.pose().rotate((float)(Math.PI / 2.0));
        arrow.draw(guiGraphics, -arrow.getWidth() / 2, -arrow.getHeight() / 2);
        guiGraphics.pose().popMatrix();
    }
}
