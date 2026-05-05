package net.vulkanmod.vulkan.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.vulkanmod.Initializer;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.framebuffer.HdrOutputMode;
import net.vulkanmod.vulkan.framebuffer.HdrToneMapper;
import net.vulkanmod.vulkan.shader.layout.Uniform;
import net.vulkanmod.vulkan.util.MappedBuffer;

import java.util.function.Supplier;

public class Uniforms {

    public static Object2ReferenceOpenHashMap<String, Supplier<Integer>> vec1i_uniformMap = new Object2ReferenceOpenHashMap<>();

    public static Object2ReferenceOpenHashMap<String, Supplier<Float>> vec1f_uniformMap = new Object2ReferenceOpenHashMap<>();
    public static Object2ReferenceOpenHashMap<String, Supplier<MappedBuffer>> vec2f_uniformMap = new Object2ReferenceOpenHashMap<>();
    public static Object2ReferenceOpenHashMap<String, Supplier<MappedBuffer>> vec3f_uniformMap = new Object2ReferenceOpenHashMap<>();
    public static Object2ReferenceOpenHashMap<String, Supplier<MappedBuffer>> vec4f_uniformMap = new Object2ReferenceOpenHashMap<>();

    public static Object2ReferenceOpenHashMap<String, Supplier<MappedBuffer>> mat4f_uniformMap = new Object2ReferenceOpenHashMap<>();

    public static void setupDefaultUniforms() {

        //Mat4
        mat4f_uniformMap.put("ModelViewMat", VRenderSystem::getModelViewMatrix);
        mat4f_uniformMap.put("ProjMat", VRenderSystem::getProjectionMatrix);
        mat4f_uniformMap.put("MVP", VRenderSystem::getMVP);
        mat4f_uniformMap.put("TextureMat", VRenderSystem::getTextureMatrix);

        //Vec1i
        vec1i_uniformMap.put("EndPortalLayers", () -> 15);

        //Vec1
        vec1f_uniformMap.put("FogStart", () -> VRenderSystem.getFogData().renderDistanceStart);
        vec1f_uniformMap.put("FogEnd", () -> VRenderSystem.getFogData().renderDistanceEnd);
        vec1f_uniformMap.put("FogEnvironmentalStart", () -> VRenderSystem.getFogData().environmentalStart);
        vec1f_uniformMap.put("FogEnvironmentalEnd", () -> VRenderSystem.getFogData().environmentalEnd);
        vec1f_uniformMap.put("FogRenderDistanceStart", () -> VRenderSystem.getFogData().renderDistanceStart);
        vec1f_uniformMap.put("FogRenderDistanceEnd", () -> VRenderSystem.getFogData().renderDistanceEnd);
        vec1f_uniformMap.put("FogSkyEnd", () -> VRenderSystem.getFogData().skyEnd);
        vec1f_uniformMap.put("FogCloudsEnd", () -> VRenderSystem.getFogData().cloudEnd);
        vec1f_uniformMap.put("LineWidth", RenderSystem::getShaderLineWidth);
        vec1f_uniformMap.put("AlphaCutout", () -> VRenderSystem.alphaCutout);
        vec1f_uniformMap.put("HdrPaperWhiteNits", () -> Initializer.CONFIG.paperWhiteNits);
        vec1f_uniformMap.put("HdrPeakNits", () -> Initializer.CONFIG.peakNits);
        vec1f_uniformMap.put("HdrExposure", () -> Initializer.CONFIG.exposure);
        vec1f_uniformMap.put("HdrSaturation", () -> Initializer.CONFIG.saturation);
        vec1f_uniformMap.put("HdrToneMapper", Uniforms::getHdrToneMapperValue);
        vec1f_uniformMap.put("HdrOutputMode", Uniforms::getHdrOutputModeValue);

        //Vec2
        vec2f_uniformMap.put("ScreenSize", VRenderSystem::getScreenSize);

        //Vec3
        vec3f_uniformMap.put("Light0_Direction", () -> VRenderSystem.lightDirection0);
        vec3f_uniformMap.put("Light1_Direction", () -> VRenderSystem.lightDirection1);
        vec3f_uniformMap.put("ModelOffset", () -> VRenderSystem.modelOffset);
        vec3f_uniformMap.put("ChunkOffset", () -> VRenderSystem.modelOffset);

        //Vec4
        vec4f_uniformMap.put("ColorModulator", VRenderSystem::getShaderColor);
        vec4f_uniformMap.put("FogColor", VRenderSystem::getShaderFogColor);

    }

    private static float getHdrToneMapperValue() {
        HdrToneMapper mapper = Initializer.CONFIG.toneMapper == null ? HdrToneMapper.REINHARD : Initializer.CONFIG.toneMapper;
        return switch (mapper) {
            case REINHARD -> 0.0f;
            case ACES -> 1.0f;
            case LINEAR_CLAMP -> 2.0f;
        };
    }

    private static float getHdrOutputModeValue() {
        Renderer renderer = Renderer.getInstance();
        if (renderer == null || renderer.getSwapChain() == null) {
            return 0.0f;
        }

        HdrOutputMode mode = renderer.getSwapChain().getActiveHdrOutputMode();
        return switch (mode) {
            case HDR10_PQ -> 1.0f;
            case SCRGB_LINEAR -> 2.0f;
            case OFF, AUTO -> 0.0f;
        };
    }

    public static Supplier<MappedBuffer> getUniformSupplier(String type, String name) {
        return switch (type) {
            case "mat4" -> Uniforms.mat4f_uniformMap.get(name);
            case "vec4" -> Uniforms.vec4f_uniformMap.get(name);
            case "vec3" -> Uniforms.vec3f_uniformMap.get(name);
            case "vec2" -> Uniforms.vec2f_uniformMap.get(name);

            default -> null;
        };
    }
}
