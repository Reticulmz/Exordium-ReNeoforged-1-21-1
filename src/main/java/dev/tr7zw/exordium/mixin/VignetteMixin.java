package dev.tr7zw.exordium.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.tr7zw.exordium.ExordiumModBase;
import dev.tr7zw.exordium.components.BufferInstance;
import dev.tr7zw.exordium.components.vanilla.VignetteComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;

@Mixin(Gui.class)
public class VignetteMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    private static final ResourceLocation FAST_VIGNETTE_LOCATION = ResourceLocation.fromNamespaceAndPath("exordium",
            "textures/misc/fast_vignette.png");
    private static final ResourceLocation FAST_VIGNETTE_DARK_LOCATION = ResourceLocation.fromNamespaceAndPath("exordium",
            "textures/misc/fast_vignette_dark.png");

    @WrapOperation(method = "renderCameraOverlays", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderVignette(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/Entity;)V"), })
    private void renderVignetteWrapper(Gui gui, GuiGraphics guiGraphics, Entity entity,
            final Operation<Void> operation) {

        BufferInstance<Float> buffer = ExordiumModBase.instance.getBufferManager()
                .getBufferInstance(VignetteComponent.getId(), Float.class);
        float brightness = ((Gui) (Object) this).vignetteBrightness;
        if (!buffer.renderBuffer(brightness, guiGraphics)) {
            if (buffer.enabled()) {
                renderCustomVignette(guiGraphics);
            } else {
                operation.call(gui, guiGraphics, entity);
            }
        }
        buffer.postRender(brightness, guiGraphics);
    }

    public void renderCustomVignette(GuiGraphics guiGraphics) {
        WorldBorder worldBorder = minecraft.level.getWorldBorder();
        float f = 0.0F;
        if (minecraft.getCameraEntity() != null) {
            float f1 = (float) worldBorder.getDistanceToBorder(minecraft.getCameraEntity());
            double d = Math.min(worldBorder.getLerpSpeed() * (double) worldBorder.getWarningTime() * 1000.0,
                    Math.abs(worldBorder.getLerpTarget() - worldBorder.getSize()));
            double e = Math.max((double) worldBorder.getWarningBlocks(), d);
            if ((double) f1 < e) {
                f = 1.0F - (float) ((double) f1 / e);
            }
        }
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        ExordiumModBase.correctBlendMode();
        ResourceLocation texture = FAST_VIGNETTE_DARK_LOCATION;
        float brightness = Mth.clamp(((Gui) (Object) this).vignetteBrightness, 0.0F, 1.0F);
        if (f > 0.0F) {
            f = Mth.clamp(f, 0.0F, 1.0F);
            brightness = Math.max(brightness, f);
            guiGraphics.setColor(f, 0.0F, 0.0F, brightness);
            texture = FAST_VIGNETTE_LOCATION;
        } else {
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, brightness);
        }

        guiGraphics.blit(texture, 0, 0, -90, 0.0F, 0.0F, guiGraphics.guiWidth(), guiGraphics.guiHeight(),
                guiGraphics.guiWidth(), guiGraphics.guiHeight());

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
    }

}
