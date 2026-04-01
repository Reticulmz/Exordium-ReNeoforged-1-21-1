package dev.tr7zw.exordium.util;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.tr7zw.exordium.ExordiumModBase;
import dev.tr7zw.exordium.render.BufferedComponent;
import dev.tr7zw.exordium.render.Model;
import dev.tr7zw.exordium.util.rendersystem.BlendStateHolder;
import dev.tr7zw.exordium.util.rendersystem.DepthStateHolder;
import dev.tr7zw.exordium.util.rendersystem.MultiStateHolder;
import dev.tr7zw.exordium.util.rendersystem.ShaderColorHolder;

/**
 * Iris causes issues when trying to switch render buffers during world
 * rendering. This class delays the draws to after the world rendering.
 *
 * Components are pre-sorted into normal/crosshair lists on add, avoiding
 * a post-hoc classification pass. GL state is only saved/restored when
 * there are actually components to render.
 *
 * @author tr7zw
 */
public class DelayedRenderCallManager {
    private static final int MAX_TEXTURES_PER_DRAW = 8;
    private final List<BufferedComponent> normalComponents = new ArrayList<>();
    private final List<BufferedComponent> crosshairComponents = new ArrayList<>();
    private final MultiStateHolder stateHolder = new MultiStateHolder(new BlendStateHolder(), new DepthStateHolder(),
            new ShaderColorHolder());

    public void addBufferedComponent(BufferedComponent component) {
        if (((IBufferedComponent) component).getCrosshair()) {
            crosshairComponents.add(component);
        } else {
            normalComponents.add(component);
        }
    }

    public void renderComponents() {
        if (normalComponents.isEmpty() && crosshairComponents.isEmpty()) {
            return;
        }

        stateHolder.fetch();

        CustomShaderManager shaderManager = ExordiumModBase.instance.getCustomShaderManager();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        RenderSystem.setShader(shaderManager::getPositionMultiTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Model model = BufferedComponent.getModel();

        if (!normalComponents.isEmpty()) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            drawBatch(normalComponents, shaderManager, model);
        }
        if (!crosshairComponents.isEmpty()) {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            drawBatch(crosshairComponents, shaderManager, model);
        }

        stateHolder.apply();
        normalComponents.clear();
        crosshairComponents.clear();
    }

    private void drawBatch(List<BufferedComponent> components, CustomShaderManager shaderManager, Model model) {
        int textureId = 0;
        for (int i = 0, size = components.size(); i < size; i++) {
            RenderSystem.setShaderTexture(textureId, components.get(i).getTextureId());
            ++textureId;

            if (textureId == MAX_TEXTURES_PER_DRAW) {
                shaderManager.getPositionMultiTexTextureCountUniform().set(MAX_TEXTURES_PER_DRAW);
                model.draw(RenderSystem.getModelViewMatrix());
                textureId = 0;
            }
        }
        if (textureId > 0) {
            shaderManager.getPositionMultiTexTextureCountUniform().set(textureId);
            model.draw(RenderSystem.getModelViewMatrix());
        }
    }
}
