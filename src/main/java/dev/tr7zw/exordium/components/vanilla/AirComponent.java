package dev.tr7zw.exordium.components.vanilla;

import dev.tr7zw.exordium.components.BufferComponent;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/**
 * Tracks air supply state for buffering the AIR_LEVEL layer.
 */
public class AirComponent implements BufferComponent<Void> {

    private static final Minecraft minecraft = Minecraft.getInstance();
    @Getter
    private static final ResourceLocation id = ResourceLocation.fromNamespaceAndPath("minecraft", "air_level");

    private int lastAirSupply;

    @Override
    public void captureState(Void context) {
        lastAirSupply = minecraft.player.getAirSupply();
    }

    @Override
    public boolean hasChanged(Void context) {
        return lastAirSupply != minecraft.player.getAirSupply();
    }

}
