package dev.tr7zw.exordium.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.renderer.ShaderInstance;

import com.mojang.blaze3d.vertex.MeshData;

public class Model {
    private VertexBuffer toDraw;

    public Model(Vector3f[] modelData, Vector2f[] uvData) {
        // 4 bytes per float, 5 floats per entry
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX); //new BufferBuilder(modelData.length * 4 * 5);
        for (int i = 0; i < modelData.length; i++) {
            Vector3f pos = modelData[i];
            Vector2f uv = uvData[i];
            bufferbuilder.addVertex(pos.x(), pos.y(), pos.z()).setUv(uv.x(), uv.y());
        }
        toDraw = new VertexBuffer(VertexBuffer.Usage.STATIC);
        upload(bufferbuilder.build());
    }

    public void drawWithShader(Matrix4f matrix4f, Matrix4f matrix4f2, ShaderInstance shaderInstance) {
        toDraw.bind();
        toDraw.drawWithShader(matrix4f, matrix4f2, shaderInstance);
    }

    public void draw(Matrix4f matrix4f) {
        drawWithShader(matrix4f, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
    }

    private void upload(MeshData renderedBuffer) {
        RenderSystem.assertOnRenderThread();
        toDraw.bind();
        toDraw.upload(renderedBuffer);
    }

    public void close() {
        toDraw.close();
    }

    public record Vector2f(float x, float y) {
    }

}
