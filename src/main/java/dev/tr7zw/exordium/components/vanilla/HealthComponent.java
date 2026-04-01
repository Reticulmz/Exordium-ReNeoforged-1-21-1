package dev.tr7zw.exordium.components.vanilla;

import dev.tr7zw.exordium.access.HealthAccess;
import dev.tr7zw.exordium.components.BufferComponent;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;

/**
 * Tracks player health hearts state for buffering the PLAYER_HEALTH layer.
 * Reads internal Gui fields via HealthAccess mixin interface.
 */
public class HealthComponent implements BufferComponent<Void> {

    private static final Minecraft minecraft = Minecraft.getInstance();
    @Getter
    private static final ResourceLocation id = ResourceLocation.fromNamespaceAndPath("minecraft", "player_health");

    private boolean healthBlinking;
    private int lastRenderedHealth;
    private int lastDisplayHealth;
    private float lastPlayerHealth;
    private float lastPlayerAbsorption;
    private boolean hadRegeneration;

    private HealthAccess getHealthAccess() {
        return (HealthAccess) minecraft.gui;
    }

    @Override
    public void captureState(Void context) {
        HealthAccess access = getHealthAccess();
        healthBlinking = (access.getHealthBlinkTime() > minecraft.gui.getGuiTicks()
                && (access.getHealthBlinkTime() - minecraft.gui.getGuiTicks()) / 3L % 2L == 1L);
        lastRenderedHealth = access.getLastHealth();
        lastDisplayHealth = access.getDisplayHealth();
        lastPlayerHealth = minecraft.player.getHealth();
        lastPlayerAbsorption = minecraft.player.getAbsorptionAmount();
        hadRegeneration = minecraft.player.hasEffect(MobEffects.REGENERATION);
    }

    @Override
    public boolean hasChanged(Void context) {
        HealthAccess access = getHealthAccess();
        boolean blinking = (access.getHealthBlinkTime() > minecraft.gui.getGuiTicks()
                && (access.getHealthBlinkTime() - minecraft.gui.getGuiTicks()) / 3L % 2L == 1L);
        boolean hasRegeneration = minecraft.player.hasEffect(MobEffects.REGENERATION);
        return healthBlinking != blinking
                || lastRenderedHealth != access.getLastHealth()
                || lastDisplayHealth != access.getDisplayHealth()
                || lastPlayerHealth != minecraft.player.getHealth()
                || Mth.ceil(lastPlayerHealth) <= 4
                || lastPlayerAbsorption != minecraft.player.getAbsorptionAmount()
                || hadRegeneration != hasRegeneration;
    }

}
