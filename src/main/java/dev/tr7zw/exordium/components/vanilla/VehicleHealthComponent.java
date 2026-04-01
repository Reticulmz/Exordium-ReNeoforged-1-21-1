package dev.tr7zw.exordium.components.vanilla;

import dev.tr7zw.exordium.components.BufferComponent;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Tracks vehicle (mount) health state for buffering the VEHICLE_HEALTH layer.
 * Separated from HealthComponent to align with NeoForge's layer architecture.
 */
public class VehicleHealthComponent implements BufferComponent<Void> {

    private static final Minecraft minecraft = Minecraft.getInstance();
    @Getter
    private static final ResourceLocation id = ResourceLocation.fromNamespaceAndPath("minecraft", "vehicle_health");

    private float lastVehicleHealth = -1;
    private float lastVehicleMaxHealth = 0;
    private boolean hadVehicle = false;

    @Override
    public void captureState(Void context) {
        LivingEntity vehicle = getVehicle();
        hadVehicle = vehicle != null;
        lastVehicleHealth = vehicle != null ? vehicle.getHealth() : -1;
        lastVehicleMaxHealth = vehicle != null ? vehicle.getMaxHealth() : 0;
    }

    @Override
    public boolean hasChanged(Void context) {
        LivingEntity vehicle = getVehicle();
        boolean hasVehicle = vehicle != null;
        if (hadVehicle != hasVehicle) return true;
        if (!hasVehicle) return false;
        return lastVehicleHealth != vehicle.getHealth()
                || lastVehicleMaxHealth != vehicle.getMaxHealth();
    }

    private static LivingEntity getVehicle() {
        if (minecraft.player == null) return null;
        if (minecraft.player.getVehicle() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }
}
