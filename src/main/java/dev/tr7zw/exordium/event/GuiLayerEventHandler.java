package dev.tr7zw.exordium.event;

import dev.tr7zw.exordium.ExordiumModBase;
import dev.tr7zw.exordium.access.BossOverlayAccess;
import dev.tr7zw.exordium.access.ChatAccess;
import dev.tr7zw.exordium.access.TablistAccess;
import dev.tr7zw.exordium.components.BufferInstance;
import dev.tr7zw.exordium.components.vanilla.BossHealthBarComponent;
import dev.tr7zw.exordium.components.vanilla.CrosshairComponent;
import dev.tr7zw.exordium.components.vanilla.DebugOverlayComponent;
import dev.tr7zw.exordium.components.vanilla.ExperienceComponent;
import dev.tr7zw.exordium.components.vanilla.HotbarComponent;
import dev.tr7zw.exordium.components.vanilla.PlayerListComponent;
import dev.tr7zw.exordium.components.vanilla.PlayerListComponent.PlayerListContext;
import dev.tr7zw.exordium.components.vanilla.ScoreboardComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles HUD component buffering using NeoForge's GUI layer event system
 * instead of individual Mixins. Each layer's Pre event decides whether to
 * use the cached buffer (cancel rendering) or capture a fresh frame,
 * and the Post event finalizes any active capture.
 */
public class GuiLayerEventHandler {

    private final Map<ResourceLocation, LayerState> layerStates = new HashMap<>();

    public void register() {
        NeoForge.EVENT_BUS.addListener(this::onLayerPre);
        NeoForge.EVENT_BUS.addListener(this::onLayerPost);
        NeoForge.EVENT_BUS.addListener(this::onRenderGuiPost);
    }

    private void onLayerPre(RenderGuiLayerEvent.Pre event) {
        ResourceLocation name = event.getName();
        LayerHandler handler = getHandler(name);
        if (handler == null) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        BufferInstance<?> buffer = handler.getBuffer();
        if (buffer == null || !buffer.enabled()) return;

        handler.prepareContext();
        boolean buffered = handler.tryRenderBuffer(guiGraphics);
        layerStates.put(name, new LayerState(handler, !buffered));

        if (buffered) {
            event.setCanceled(true);
            handler.postRender(guiGraphics);
        }
    }

    private void onLayerPost(RenderGuiLayerEvent.Post event) {
        LayerState state = layerStates.remove(event.getName());
        if (state != null && state.capturing) {
            state.handler.postRender(event.getGuiGraphics());
        }
    }

    private void onRenderGuiPost(RenderGuiEvent.Post event) {
        ExordiumModBase.instance.getDelayedRenderCallManager().renderComponents();
    }

    private LayerHandler getHandler(ResourceLocation name) {
        if (VanillaGuiLayers.HOTBAR.equals(name)) return new SimpleHandler(HotbarComponent.getId(), Void.class);
        if (VanillaGuiLayers.EXPERIENCE_BAR.equals(name)) return new SimpleHandler(ExperienceComponent.getId(), Void.class);
        if (VanillaGuiLayers.DEBUG_OVERLAY.equals(name)) return new SimpleHandler(DebugOverlayComponent.getId(), Void.class);
        if (VanillaGuiLayers.SCOREBOARD_SIDEBAR.equals(name)) return new SimpleHandler(ScoreboardComponent.getId(), Void.class);
        if (VanillaGuiLayers.CROSSHAIR.equals(name)) return new CrosshairHandler();
        if (VanillaGuiLayers.CHAT.equals(name)) return new ChatHandler();
        if (VanillaGuiLayers.TAB_LIST.equals(name)) return new TabListHandler();
        if (VanillaGuiLayers.BOSS_OVERLAY.equals(name)) return new BossBarHandler();
        return null;
    }

    private record LayerState(LayerHandler handler, boolean capturing) {}

    // --- Handler abstraction ---

    private interface LayerHandler {
        BufferInstance<?> getBuffer();
        void prepareContext();
        boolean tryRenderBuffer(GuiGraphics guiGraphics);
        void postRender(GuiGraphics guiGraphics);
    }

    // --- Null-context handler for simple components ---

    private static class SimpleHandler implements LayerHandler {
        private final ResourceLocation componentId;
        private final Class<?> contextType;

        SimpleHandler(ResourceLocation componentId, Class<?> contextType) {
            this.componentId = componentId;
            this.contextType = contextType;
        }

        @Override
        public BufferInstance<?> getBuffer() {
            return ExordiumModBase.instance.getBufferManager().getBufferInstance(componentId, Void.class);
        }

        @Override
        public void prepareContext() {}

        @Override
        public boolean tryRenderBuffer(GuiGraphics guiGraphics) {
            BufferInstance<Void> buffer = ExordiumModBase.instance.getBufferManager()
                    .getBufferInstance(componentId, Void.class);
            return buffer.renderBuffer(null, guiGraphics);
        }

        @Override
        public void postRender(GuiGraphics guiGraphics) {
            BufferInstance<Void> buffer = ExordiumModBase.instance.getBufferManager()
                    .getBufferInstance(componentId, Void.class);
            buffer.postRender(null, guiGraphics);
        }
    }

    // --- Crosshair: needs DebugScreenOverlay context ---

    private static class CrosshairHandler implements LayerHandler {
        @Override
        public BufferInstance<?> getBuffer() {
            return ExordiumModBase.instance.getBufferManager()
                    .getBufferInstance(CrosshairComponent.getId(), DebugScreenOverlay.class);
        }

        @Override
        public void prepareContext() {}

        @Override
        public boolean tryRenderBuffer(GuiGraphics guiGraphics) {
            Gui gui = Minecraft.getInstance().gui;
            DebugScreenOverlay debugOverlay = gui.getDebugOverlay();
            BufferInstance<DebugScreenOverlay> buffer = ExordiumModBase.instance.getBufferManager()
                    .getBufferInstance(CrosshairComponent.getId(), DebugScreenOverlay.class);
            return buffer.renderBuffer(debugOverlay, guiGraphics);
        }

        @Override
        public void postRender(GuiGraphics guiGraphics) {
            Gui gui = Minecraft.getInstance().gui;
            DebugScreenOverlay debugOverlay = gui.getDebugOverlay();
            BufferInstance<DebugScreenOverlay> buffer = ExordiumModBase.instance.getBufferManager()
                    .getBufferInstance(CrosshairComponent.getId(), DebugScreenOverlay.class);
            buffer.postRender(debugOverlay, guiGraphics);
        }
    }

    // --- Chat: needs ChatAccess context with tickCount ---

    private static class ChatHandler implements LayerHandler {
        @Override
        public BufferInstance<?> getBuffer() {
            return ExordiumModBase.instance.getBufferManager()
                    .getBufferInstance(dev.tr7zw.exordium.components.vanilla.ChatComponent.getId(), ChatAccess.class);
        }

        @Override
        public void prepareContext() {
            Gui gui = Minecraft.getInstance().gui;
            ChatAccess chatAccess = (ChatAccess) gui.getChat();
            chatAccess.setTickCount(gui.getGuiTicks());
        }

        @Override
        public boolean tryRenderBuffer(GuiGraphics guiGraphics) {
            ChatAccess chatAccess = (ChatAccess) Minecraft.getInstance().gui.getChat();
            BufferInstance<ChatAccess> buffer = ExordiumModBase.instance.getBufferManager()
                    .getBufferInstance(dev.tr7zw.exordium.components.vanilla.ChatComponent.getId(), ChatAccess.class);
            return buffer.renderBuffer(chatAccess, guiGraphics);
        }

        @Override
        public void postRender(GuiGraphics guiGraphics) {
            ChatAccess chatAccess = (ChatAccess) Minecraft.getInstance().gui.getChat();
            BufferInstance<ChatAccess> buffer = ExordiumModBase.instance.getBufferManager()
                    .getBufferInstance(dev.tr7zw.exordium.components.vanilla.ChatComponent.getId(), ChatAccess.class);
            buffer.postRender(chatAccess, guiGraphics);
        }
    }

    // --- Tab list: needs PlayerListContext ---

    private static class TabListHandler implements LayerHandler {
        @Override
        public BufferInstance<?> getBuffer() {
            return ExordiumModBase.instance.getBufferManager()
                    .getBufferInstance(PlayerListComponent.getId(), PlayerListContext.class);
        }

        @Override
        public void prepareContext() {}

        @Override
        public boolean tryRenderBuffer(GuiGraphics guiGraphics) {
            PlayerListContext context = buildContext();
            if (context == null) return false;
            BufferInstance<PlayerListContext> buffer = ExordiumModBase.instance.getBufferManager()
                    .getBufferInstance(PlayerListComponent.getId(), PlayerListContext.class);
            return buffer.renderBuffer(context, guiGraphics);
        }

        @Override
        public void postRender(GuiGraphics guiGraphics) {
            PlayerListContext context = buildContext();
            BufferInstance<PlayerListContext> buffer = ExordiumModBase.instance.getBufferManager()
                    .getBufferInstance(PlayerListComponent.getId(), PlayerListContext.class);
            buffer.postRender(context, guiGraphics);
        }

        private PlayerListContext buildContext() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return null;
            Gui gui = mc.gui;
            TablistAccess tablistAccess = (TablistAccess) gui.getTabList();
            Scoreboard scoreboard = mc.level.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);
            return new PlayerListContext(tablistAccess, scoreboard, objective);
        }
    }

    // --- Boss bar: needs BossOverlayAccess context ---

    private static class BossBarHandler implements LayerHandler {
        @Override
        public BufferInstance<?> getBuffer() {
            return ExordiumModBase.instance.getBufferManager()
                    .getBufferInstance(BossHealthBarComponent.getId(), BossOverlayAccess.class);
        }

        @Override
        public void prepareContext() {}

        @Override
        public boolean tryRenderBuffer(GuiGraphics guiGraphics) {
            BossOverlayAccess overlayAccess = (BossOverlayAccess) Minecraft.getInstance().gui.getBossOverlay();
            BufferInstance<BossOverlayAccess> buffer = ExordiumModBase.instance.getBufferManager()
                    .getBufferInstance(BossHealthBarComponent.getId(), BossOverlayAccess.class);
            return buffer.renderBuffer(overlayAccess, guiGraphics);
        }

        @Override
        public void postRender(GuiGraphics guiGraphics) {
            BossOverlayAccess overlayAccess = (BossOverlayAccess) Minecraft.getInstance().gui.getBossOverlay();
            BufferInstance<BossOverlayAccess> buffer = ExordiumModBase.instance.getBufferManager()
                    .getBufferInstance(BossHealthBarComponent.getId(), BossOverlayAccess.class);
            buffer.postRender(overlayAccess, guiGraphics);
        }
    }
}
