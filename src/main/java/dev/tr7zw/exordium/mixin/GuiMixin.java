package dev.tr7zw.exordium.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.tr7zw.exordium.access.GuiAccess;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.PlayerTabOverlay;

/**
 * Minimal mixin providing GuiAccess interface for internal field access.
 * HUD layer buffering is handled by GuiLayerEventHandler via NeoForge events.
 */
@Mixin(Gui.class)
public abstract class GuiMixin implements GuiAccess {

    @Shadow
    private ChatComponent chat;
    @Shadow
    private PlayerTabOverlay tabList;
    @Shadow
    protected int tickCount;

    @Override
    public ChatComponent getChatComponent() {
        return chat;
    }

    @Override
    public PlayerTabOverlay getPlayerTabOverlay() {
        return tabList;
    }

    @Override
    public int getTickCount() {
        return tickCount;
    }

}
