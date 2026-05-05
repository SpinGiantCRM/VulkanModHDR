package net.vulkanmod.vulkan.device;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.glfw.GLFW.GLFW_PLATFORM_WIN32;
import static org.lwjgl.glfw.GLFW.glfwGetPlatform;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK11.vkEnumerateInstanceVersion;
import static org.lwjgl.vulkan.VK11.vkGetPhysicalDeviceFeatures2;

public class Device {
    final VkPhysicalDevice physicalDevice;
    final VkPhysicalDeviceProperties properties;

    private final int vendorId;
    public final String vendorIdString;
    public final String deviceName;
    public final String driverVersion;
    public final String vkVersion;

    public final VkPhysicalDeviceFeatures2 availableFeatures;
    public final VkPhysicalDeviceVulkan11Features availableFeatures11;

    private boolean drawIndirectSupported;
    private final Set<String> availableExtensions;

    public Device(VkPhysicalDevice device) {
        this.physicalDevice = device;
        properties = VkPhysicalDeviceProperties.malloc();
        vkGetPhysicalDeviceProperties(physicalDevice, properties);
        this.vendorId = properties.vendorID();
        this.vendorIdString = decodeVendor(properties.vendorID());
        this.deviceName = properties.deviceNameString();
        this.driverVersion = decodeDvrVersion(properties.driverVersion(), properties.vendorID());
        this.vkVersion = decDefVersion(properties.apiVersion());

        this.availableFeatures = VkPhysicalDeviceFeatures2.calloc();
        this.availableFeatures.sType$Default();
        this.availableFeatures11 = VkPhysicalDeviceVulkan11Features.malloc();
        this.availableFeatures11.sType$Default();
        this.availableFeatures.pNext(this.availableFeatures11);
        vkGetPhysicalDeviceFeatures2(this.physicalDevice, this.availableFeatures);
        this.availableExtensions = getAvailableExtensions();

        if (this.availableFeatures.features().multiDrawIndirect() && this.availableFeatures11.shaderDrawParameters())
            this.drawIndirectSupported = true;
    }
    private static String decodeVendor(int i) { return switch (i) {
        case (0x10DE) -> "Nvidia"; case (0x1022), (0x1002) -> "AMD"; case (0x8086) -> "Intel";
        case (0x1010) -> "Imagination Technologies"; case (0x13B5) -> "ARM"; case (0x5143) -> "Qualcomm";
        case (0x106B) -> "Apple"; case (0x14E4) -> "Broadcom"; case (0x1AE0) -> "Google"; case (0x10005) -> "Mesa";
        default -> "undef";}; }
    static String decDefVersion(int v) { return VK_VERSION_MAJOR(v) + "." + VK_VERSION_MINOR(v) + "." + VK_VERSION_PATCH(v); }
    private static String decodeDvrVersion(int v, int i) { return switch (i) {
        case (0x10DE) -> decodeNvidia(v); case (0x1022), (0x1002) -> decDefVersion(v); case (0x8086) -> decIntelVersion(v); default -> decDefVersion(v);}; }
    public Set<String> getUnsupportedExtensions(Set<String> requiredExtensions) { Set<String> unsupportedExtensions = new HashSet<>(requiredExtensions); unsupportedExtensions.removeAll(this.availableExtensions); return unsupportedExtensions; }
    public boolean supportsExtension(String extensionName) { return this.availableExtensions.contains(extensionName); }
    private Set<String> getAvailableExtensions() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer extensionCount = stack.ints(0);
            vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, extensionCount, null);
            VkExtensionProperties.Buffer extensionsBuffer = VkExtensionProperties.malloc(extensionCount.get(0), stack);
            vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, extensionCount, extensionsBuffer);
            return extensionsBuffer.stream().map(VkExtensionProperties::extensionNameString).collect(toSet());
        }
    }
    public int getVendorId() { return vendorId; }
    public boolean isDrawIndirectSupported() { return drawIndirectSupported; }
    public static int queryInstanceVersion() { try (MemoryStack stack = stackPush()) { IntBuffer a = stack.ints(0); vkEnumerateInstanceVersion(a); return a.get(0);} }
    private static String decodeNvidia(int v) { return ((v >> 22) & 0x3FF) + "." + ((v >> 14) & 0x0FF) + "." + ((v >> 6) & 0x0FF) + "." + (v & 0x03F); }
    private static String decIntelVersion(int v) {
        if (glfwGetPlatform() == GLFW_PLATFORM_WIN32) return ((v >> 14) & 0x3FFFF) + "." + (v & 0x3FFF);
        return decDefVersion(v);
    }
    public static int getCpuThreads() { SystemInfo si = new SystemInfo(); CentralProcessor cp = si.getHardware().getProcessor(); return cp.getLogicalProcessorCount(); }
}
