package net.vulkanmod.vulkan.framebuffer;

import net.vulkanmod.Initializer;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.util.List;

import static org.lwjgl.vulkan.EXTSwapchainColorspace.VK_COLOR_SPACE_EXTENDED_SRGB_LINEAR_EXT;
import static org.lwjgl.vulkan.EXTSwapchainColorspace.VK_COLOR_SPACE_HDR10_ST2084_EXT;
import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.VK10.*;

final class SwapchainFormatSelector {

    private SwapchainFormatSelector() {}

    static SwapchainOutputState select(VkSurfaceFormatKHR.Buffer availableFormats, HdrOutputMode requestedMode, boolean extSwapchainColorspaceSupported) {
        List<VkSurfaceFormatKHR> formats = availableFormats.stream().toList();
        List<FormatCandidate> candidates = formats.stream().map(v -> new FormatCandidate(v.format(), v.colorSpace())).toList();
        Selection selection = selectCandidate(candidates, requestedMode, extSwapchainColorspaceSupported);

        VkSurfaceFormatKHR selectedVk = formats.stream()
                .filter(v -> v.format() == selection.format().format() && v.colorSpace() == selection.format().colorSpace())
                .findFirst()
                .orElse(formats.get(0));

        return new SwapchainOutputState(selectedVk, requestedMode, selection.activeMode(), selection.autoSelected());
    }

    static Selection selectCandidate(List<FormatCandidate> formats, HdrOutputMode requestedMode, boolean extSwapchainColorspaceSupported) {
        FormatCandidate sdr = selectSdr(formats);

        if (requestedMode == HdrOutputMode.OFF) {
            return new Selection(sdr, HdrOutputMode.OFF, false);
        }

        if (!extSwapchainColorspaceSupported) {
            Initializer.LOGGER.warn("HDR output requested, but VK_EXT_swapchain_colorspace is unavailable. Falling back to SDR.");
            return new Selection(sdr, HdrOutputMode.OFF, false);
        }

        FormatCandidate hdr10 = selectHdr10(formats);
        FormatCandidate scRgb = selectScRgb(formats);

        return switch (requestedMode) {
            case HDR10_PQ -> hdr10 != null ? new Selection(hdr10, HdrOutputMode.HDR10_PQ, false) : fallback(requestedMode, sdr);
            case SCRGB_LINEAR -> scRgb != null ? new Selection(scRgb, HdrOutputMode.SCRGB_LINEAR, false) : fallback(requestedMode, sdr);
            case AUTO -> {
                if (hdr10 != null) yield new Selection(hdr10, HdrOutputMode.HDR10_PQ, true);
                if (scRgb != null) yield new Selection(scRgb, HdrOutputMode.SCRGB_LINEAR, true);
                yield new Selection(sdr, HdrOutputMode.OFF, true);
            }
            default -> new Selection(sdr, HdrOutputMode.OFF, false);
        };
    }

    private static Selection fallback(HdrOutputMode requestedMode, FormatCandidate sdrFormat) {
        Initializer.LOGGER.warn("Requested HDR output mode {} is unsupported by surface formats. Falling back to SDR.", requestedMode);
        return new Selection(sdrFormat, HdrOutputMode.OFF, false);
    }

    private static FormatCandidate selectSdr(List<FormatCandidate> formats) {
        FormatCandidate selected = formats.get(0);
        for (FormatCandidate format : formats) {
            if (format.format() == VK_FORMAT_R8G8B8A8_UNORM && format.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) return format;
            if (format.format() == VK_FORMAT_B8G8R8A8_UNORM && format.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) selected = format;
        }
        return selected;
    }

    private static FormatCandidate selectHdr10(List<FormatCandidate> formats) {
        for (FormatCandidate format : formats) {
            if (format.colorSpace() == VK_COLOR_SPACE_HDR10_ST2084_EXT &&
                    (format.format() == VK_FORMAT_A2B10G10R10_UNORM_PACK32 || format.format() == VK_FORMAT_A2R10G10B10_UNORM_PACK32)) return format;
        }
        return null;
    }

    private static FormatCandidate selectScRgb(List<FormatCandidate> formats) {
        for (FormatCandidate format : formats) {
            if (format.colorSpace() == VK_COLOR_SPACE_EXTENDED_SRGB_LINEAR_EXT &&
                    (format.format() == VK_FORMAT_R16G16B16A16_SFLOAT || format.format() == VK_FORMAT_B10G11R11_UFLOAT_PACK32)) return format;
        }
        return null;
    }

    record FormatCandidate(int format, int colorSpace) {}
    record Selection(FormatCandidate format, HdrOutputMode activeMode, boolean autoSelected) {}
}
