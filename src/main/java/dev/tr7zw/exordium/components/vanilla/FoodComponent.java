package dev.tr7zw.exordium.components.vanilla;

import dev.tr7zw.exordium.components.BufferComponent;
import dev.tr7zw.exordium.mixin.FoodDataAccessor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;

/**
 * Tracks food level state for buffering the FOOD_LEVEL layer.
 */
public class FoodComponent implements BufferComponent<Void> {

    private static final Minecraft minecraft = Minecraft.getInstance();
    @Getter
    private static final ResourceLocation id = ResourceLocation.fromNamespaceAndPath("minecraft", "food_level");

    private int lastFoodLevel;
    private float lastSaturation;
    private float lastExhaustionLevel;
    private boolean hadHunger;

    @Override
    public void captureState(Void context) {
        lastFoodLevel = minecraft.player.getFoodData().getFoodLevel();
        lastSaturation = minecraft.player.getFoodData().getSaturationLevel();
        lastExhaustionLevel = ((FoodDataAccessor) minecraft.player.getFoodData()).getExhaustionLevel();
        hadHunger = minecraft.player.hasEffect(MobEffects.HUNGER);
    }

    @Override
    public boolean hasChanged(Void context) {
        return lastFoodLevel != minecraft.player.getFoodData().getFoodLevel()
                || lastSaturation != minecraft.player.getFoodData().getSaturationLevel()
                || lastExhaustionLevel != ((FoodDataAccessor) minecraft.player.getFoodData()).getExhaustionLevel()
                || hadHunger != minecraft.player.hasEffect(MobEffects.HUNGER);
    }

}
