package dev.tr7zw.exordium.components.vanilla;

import dev.tr7zw.exordium.components.BufferComponent;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

public class DebugOverlayComponent implements BufferComponent<Void> {

    @Getter
    private static final ResourceLocation id = ResourceLocation.fromNamespaceAndPath("minecraft", "debug_text");

    @Override
    public void captureState(Void context) {

    }

    @Override
    public boolean hasChanged(Void context) {
        return true;
    }

}
