package net.vulkanmod.mixin.debug;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.class_2960;
import net.vulkanmod.render.profiling.DebugEntryMemoryStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenEntries.class)
public abstract class DebugScreenEntriesM {

    @Shadow
    public static class_2960 register(class_2960 resourceLocation, DebugScreenEntry debugScreenEntry) {
        return null;
    }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void addEntry(CallbackInfo ci) {
        register(class_2960.fromNamespaceAndPath("vkmod","stats"), new DebugEntryMemoryStats());
    }
}
