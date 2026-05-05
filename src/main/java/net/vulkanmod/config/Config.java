package net.vulkanmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.vulkanmod.config.video.VideoModeManager;
import net.vulkanmod.config.video.VideoModeSet;
import net.vulkanmod.vulkan.framebuffer.HdrOutputMode;
import net.vulkanmod.vulkan.framebuffer.HdrToneMapper;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class Config {
    public VideoModeSet.VideoMode videoMode = VideoModeManager.getFirstAvailable().getVideoMode();
    public int windowMode = 0;

    public int advCulling = 2;
    public boolean indirectDraw = true;

    public boolean uniqueOpaqueLayer = true;
    public boolean entityCulling = true;
    public int device = -1;

    public int ambientOcclusion = 1;
    public int frameQueueSize = 2;
    public int builderThreads = 0;

    public boolean backFaceCulling = true;
    public boolean textureAnimations = true;
    public HdrOutputMode hdrOutputMode = HdrOutputMode.OFF;
    public float paperWhiteNits = 200.0f;
    public float peakNits = 1000.0f;
    public float exposure = 1.0f;
    public float minNits = 0.005f;
    public float maxCLL = 1000.0f;
    public float maxFALL = 400.0f;
    public float saturation = 1.0f;
    public HdrToneMapper toneMapper = HdrToneMapper.REINHARD;

    public void clampHdrSettings() {
        this.paperWhiteNits = clamp(this.paperWhiteNits, 80.0f, 1000.0f);
        this.peakNits = clamp(this.peakNits, 100.0f, 10000.0f);
        this.exposure = clamp(this.exposure, 0.1f, 10.0f);
        this.minNits = clamp(this.minNits, 0.0f, 1.0f);
        this.maxCLL = clamp(this.maxCLL, 100.0f, 10000.0f);
        this.maxFALL = clamp(this.maxFALL, 50.0f, this.maxCLL);
        this.saturation = clamp(this.saturation, 0.0f, 2.0f);
        if (this.toneMapper == null) this.toneMapper = HdrToneMapper.REINHARD;
        if (this.hdrOutputMode == null) this.hdrOutputMode = HdrOutputMode.OFF;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public void write() {
        if (!Files.exists(CONFIG_PATH.getParent())) {
            try {
                Files.createDirectories(CONFIG_PATH);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Files.write(CONFIG_PATH, Collections.singleton(GSON.toJson(this)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path CONFIG_PATH;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();

    public static Config load(Path path) {
        Config config;
        Config.CONFIG_PATH = path;

        if (Files.exists(path)) {
            try (FileReader fileReader = new FileReader(path.toFile())) {
                config = GSON.fromJson(fileReader, Config.class);
            } catch (IOException exception) {
                throw new RuntimeException(exception.getMessage());
            }
        }
        else {
            config = new Config();
        }

        config.clampHdrSettings();
        config.write();

        return config;
    }
}
