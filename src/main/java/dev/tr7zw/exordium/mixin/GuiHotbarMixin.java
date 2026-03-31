package dev.tr7zw.exordium.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.tr7zw.exordium.ExordiumModBase;
import dev.tr7zw.exordium.components.BufferInstance;
import dev.tr7zw.exordium.components.vanilla.HotbarComponent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;

@Mixin(Gui.class)
public class GuiHotbarMixin {

    @WrapOperation(method = "renderHotbar", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V") })
    private void renderHotbarWrapper(Gui gui, GuiGraphics guiGraphics, DeltaTracker f,
            final Operation<Void> operation) {
        BufferInstance<Void> buffer = ExordiumModBase.instance.getBufferManager()
                .getBufferInstance(HotbarComponent.getId(), Void.class);
        if (!buffer.renderBuffer(null, guiGraphics)) {
            operation.call(gui, guiGraphics, f);
        }
        buffer.postRender(null, guiGraphics);
    }

}
