#version 450

layout(binding = 0) uniform sampler2D DiffuseSampler;
layout(binding = 1) uniform UBO {
    float HdrPaperWhiteNits;
    float HdrPeakNits;
    float HdrExposure;
    float HdrOutputMode;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    // blit final output of compositor into displayed back buffer
    if (HdrOutputMode < 0.5) {
        fragColor = color;
        return;
    }

    vec3 sceneLinear = max(color.rgb, vec3(0.0)) * HdrExposure;

    float peakNits = max(HdrPeakNits, 1.0);
    float paperWhiteNits = clamp(HdrPaperWhiteNits, 1.0, peakNits);
    float paperWhiteScale = paperWhiteNits / peakNits;

    vec3 displayLinear = sceneLinear * paperWhiteScale;
    displayLinear = displayLinear / (displayLinear + vec3(1.0));

    if (HdrOutputMode < 1.5) {
        vec3 pqNits = displayLinear * peakNits;
        vec3 pqN = clamp(pqNits / 10000.0, vec3(0.0), vec3(1.0));

        const float m1 = 2610.0 / 16384.0;
        const float m2 = 2523.0 / 32.0;
        const float c1 = 3424.0 / 4096.0;
        const float c2 = 2413.0 / 128.0;
        const float c3 = 2392.0 / 128.0;

        vec3 p = pow(pqN, vec3(m1));
        vec3 pq = pow((c1 + c2 * p) / (1.0 + c3 * p), vec3(m2));
        fragColor = vec4(pq, color.a);
        return;
    }

    fragColor = vec4(displayLinear, color.a);
}
