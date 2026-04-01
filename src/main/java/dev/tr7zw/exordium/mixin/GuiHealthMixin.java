package dev.tr7zw.exordium.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.tr7zw.exordium.access.HealthAccess;
import lombok.Getter;
import net.minecraft.client.gui.Gui;

/**
 * Minimal mixin providing HealthAccess interface for internal field access.
 * Health layer buffering is handled by GuiLayerEventHandler via NeoForge events.
 */
@Mixin(Gui.class)
public abstract class GuiHealthMixin implements HealthAccess {

    @Shadow
    @Getter
    private long healthBlinkTime;
    @Shadow
    @Getter
    private int lastHealth;
    @Shadow
    @Getter
    private int displayHealth;

}
