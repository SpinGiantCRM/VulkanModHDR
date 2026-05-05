package net.vulkanmod.vulkan.framebuffer;

final class DisplayOutputManager {
    private final boolean hdrMetadataSupported;
    private SwapchainOutputState swapchainOutputState;

    DisplayOutputManager(boolean hdrMetadataSupported) {
        this.hdrMetadataSupported = hdrMetadataSupported;
    }

    void onSwapchainCreated(long swapchainId, SwapchainOutputState state) {
        this.swapchainOutputState = state;
        if (state.requiresHdrMetadata()) {
            HdrMetadataHelper.applyMetadata(swapchainId, state.activeMode(), this.hdrMetadataSupported);
        }
    }

    HdrOutputMode getActiveMode() {
        return this.swapchainOutputState == null ? HdrOutputMode.OFF : this.swapchainOutputState.activeMode();
    }
}
