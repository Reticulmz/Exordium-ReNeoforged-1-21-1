package dev.tr7zw.exordium.components.vanilla;

import dev.tr7zw.exordium.components.BufferComponent;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/**
 * Tracks armor level state for buffering the ARMOR_LEVEL layer.
 */
public class ArmorComponent implements BufferComponent<Void> {

    private static final Minecraft minecraft = Minecraft.getInstance();
    @Getter
    private static final ResourceLocation id = ResourceLocation.fromNamespaceAndPath("minecraft", "armor_level");

    private int lastArmorValue;

    @Override
    public void captureState(Void context) {
        lastArmorValue = minecraft.player.getArmorValue();
    }

    @Override
    public boolean hasChanged(Void context) {
        return lastArmorValue != minecraft.player.getArmorValue();
    }

}
