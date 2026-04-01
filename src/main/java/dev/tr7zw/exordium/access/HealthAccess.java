package dev.tr7zw.exordium.access;

/**
 * Provides access to Gui's private health-related fields via mixin.
 */
public interface HealthAccess {

    int getDisplayHealth();

    int getLastHealth();

    long getHealthBlinkTime();

}
