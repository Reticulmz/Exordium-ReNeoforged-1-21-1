package dev.tr7zw.exordium;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import dev.tr7zw.exordium.util.ReloadTracker;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

@Mod(value = "exordium", dist = Dist.CLIENT)
public class ExordiumMod extends ExordiumModBase {

    public ExordiumMod(IEventBus modEventBus, ModContainer container) {
        LOGGER.info("Loading Exordium!");
        super.onInitialize();

        modEventBus.addListener(this::onRegisterReloadListeners);
        modEventBus.addListener(this::onRegisterShaders);

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mc, screen) -> createConfigScreen(screen));
    }

    private void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((pPreparationBarrier, pResourceManager, pPreparationsProfiler,
                pReloadProfiler, pBackgroundExecutor, pGameExecutor) -> {
            return pPreparationBarrier.wait(null).thenRunAsync(ReloadTracker::reload, pGameExecutor);
        });
    }

    private void onRegisterShaders(RegisterShadersEvent event) {
        try {
            ShaderInstance shader = new ShaderInstance(event.getResourceProvider(),
                    "position_multi_tex", DefaultVertexFormat.POSITION_TEX);
            event.registerShader(shader,
                    s -> ExordiumModBase.instance.getCustomShaderManager().registerShaderInstance(s));
        } catch (Exception e) {
            throw new RuntimeException("Unable to load Exordium Shader", e);
        }
    }

    @Override
    public void initModloader() {
    }

}
