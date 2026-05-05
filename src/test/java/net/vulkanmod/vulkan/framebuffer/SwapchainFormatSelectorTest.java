package net.vulkanmod.vulkan.framebuffer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.lwjgl.vulkan.EXTSwapchainColorspace.VK_COLOR_SPACE_EXTENDED_SRGB_LINEAR_EXT;
import static org.lwjgl.vulkan.EXTSwapchainColorspace.VK_COLOR_SPACE_HDR10_ST2084_EXT;
import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.VK10.*;

class SwapchainFormatSelectorTest {

    @Test
    void fallsBackToSdrWhenHdrRequestedWithoutExtSupport() {
        List<SwapchainFormatSelector.FormatCandidate> formats = List.of(
                new SwapchainFormatSelector.FormatCandidate(VK_FORMAT_B8G8R8A8_UNORM, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR),
                new SwapchainFormatSelector.FormatCandidate(VK_FORMAT_A2B10G10R10_UNORM_PACK32, VK_COLOR_SPACE_HDR10_ST2084_EXT)
        );

        SwapchainFormatSelector.Selection selection = SwapchainFormatSelector.selectCandidate(formats, HdrOutputMode.HDR10_PQ, false);
        assertEquals(HdrOutputMode.OFF, selection.activeMode());
        assertEquals(VK_COLOR_SPACE_SRGB_NONLINEAR_KHR, selection.format().colorSpace());
    }

    @Test
    void autoPrefersHdr10ThenScrgbThenSdr() {
        List<SwapchainFormatSelector.FormatCandidate> formats = List.of(
                new SwapchainFormatSelector.FormatCandidate(VK_FORMAT_B8G8R8A8_UNORM, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR),
                new SwapchainFormatSelector.FormatCandidate(VK_FORMAT_R16G16B16A16_SFLOAT, VK_COLOR_SPACE_EXTENDED_SRGB_LINEAR_EXT),
                new SwapchainFormatSelector.FormatCandidate(VK_FORMAT_A2B10G10R10_UNORM_PACK32, VK_COLOR_SPACE_HDR10_ST2084_EXT)
        );

        SwapchainFormatSelector.Selection selection = SwapchainFormatSelector.selectCandidate(formats, HdrOutputMode.AUTO, true);
        assertEquals(HdrOutputMode.HDR10_PQ, selection.activeMode());
    }
}
