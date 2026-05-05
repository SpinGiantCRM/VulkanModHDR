package net.vulkanmod.vulkan.framebuffer;

import org.lwjgl.vulkan.VkSurfaceFormatKHR;

record SwapchainOutputState(VkSurfaceFormatKHR format, HdrOutputMode requestedMode, HdrOutputMode activeMode, boolean autoSelected) {
    boolean requiresHdrMetadata() {
        return this.activeMode == HdrOutputMode.HDR10_PQ;
    }
}
