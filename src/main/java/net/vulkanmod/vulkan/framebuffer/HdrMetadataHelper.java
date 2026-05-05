package net.vulkanmod.vulkan.framebuffer;

import net.vulkanmod.Initializer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTHdrMetadata;
import org.lwjgl.vulkan.VkHdrMetadataEXT;
import org.lwjgl.vulkan.VkXYColorEXT;

import static org.lwjgl.vulkan.EXTHdrMetadata.vkSetHdrMetadataEXT;

final class HdrMetadataHelper {

    private HdrMetadataHelper() {}

    static void applyMetadata(long swapchainId, HdrOutputMode mode, boolean hdrMetadataSupported) {
        if (mode != HdrOutputMode.HDR10_PQ || !hdrMetadataSupported || swapchainId == 0L) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkHdrMetadataEXT.Buffer metadataBuffer = VkHdrMetadataEXT.calloc(1, stack);
            VkHdrMetadataEXT metadata = metadataBuffer.get(0);
            metadata.sType(EXTHdrMetadata.VK_STRUCTURE_TYPE_HDR_METADATA_EXT);

            setColor(metadata.displayPrimaryRed(), 0.680f, 0.320f);
            setColor(metadata.displayPrimaryGreen(), 0.265f, 0.690f);
            setColor(metadata.displayPrimaryBlue(), 0.150f, 0.060f);
            setColor(metadata.whitePoint(), 0.3127f, 0.3290f);

            metadata.maxLuminance(1000.0f);
            metadata.minLuminance(0.001f);
            metadata.maxContentLightLevel(1000.0f);
            metadata.maxFrameAverageLightLevel(400.0f);

            vkSetHdrMetadataEXT(net.vulkanmod.vulkan.Vulkan.getVkDevice(), stack.longs(swapchainId), metadataBuffer);
            Initializer.LOGGER.info("Applied HDR10 metadata to swapchain.");
        }
    }

    private static void setColor(VkXYColorEXT color, float x, float y) {
        color.x(x);
        color.y(y);
    }
}
