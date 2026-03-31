package dev.tr7zw.exordium.components.support;

import dev.tr7zw.exordium.components.BufferComponent;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

public class PaperDollComponent implements BufferComponent<Void> {

    @Getter
    private static final ResourceLocation id = ResourceLocation.fromNamespaceAndPath("tr7zw", "paperdoll");

    @Override
    public void captureState(Void context) {
        // do nothing
    }

    @Override
    public boolean hasChanged(Void context) {
        return true;
    }

}
