#version 330

// 7 orbiting metaball blobs rendered as a soft distance field (log-density trick: accumulate
// exp(-k*(shape-radius)) per blob, then d = -log(sum)/k recovers an approximate signed distance),
// refracting the real background with real chromatic aberration (each color channel sampled at a
// slightly different offset) plus specular/rim lighting. Composites over InSampler natively -
// everywhere farther than the soft edge threshold is a pure passthrough of the real scene
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, pushed fresh each frame by ShadesClient
layout(std140) uniform LensConfig {
    float Aspect;
};

in vec2 texCoord;
out vec4 fragColor;

// each color channel refracted at a slightly different strength, for real chromatic aberration
// instead of a flat single-channel bend
vec3 getRefr(vec2 uv, vec2 n, float e) {
    float d = 0.05 * e;
    return vec3(
        texture(InSampler, uv - n * d).r,
        texture(InSampler, uv - n * d * 1.1).g,
        texture(InSampler, uv - n * d * 1.2).b
    );
}

void main(){

    float t = GameTime * 2400.0 * 0.5 * 0.6;
    vec2 p = (texCoord - 0.5) * vec2(Aspect, 1.0);

    float m = 0.0;
    for (float i = 0.0; i < 7.0; i++) {
        vec2 pos = vec2(sin(t + i * 1.3), cos(t * 0.8 + i * 2.5)) * 0.6;

        float a = t + i;
        vec2 dir = p - pos;
        dir *= mat2(cos(a), -sin(a), sin(a), cos(a));

        float shape = length(dir * vec2(1.0 + sin(t + i) * 0.3, 1.0 + cos(t * 0.5 + i) * 0.3));
        float radius = 0.15 + 0.05 * sin(t + i * 0.5);

        m += exp(-35.0 * (shape - radius));
    }

    float d = -log(m) / 35.0;
    vec3 bg = texture(InSampler, texCoord).rgb;
    vec3 fc = bg;

    if (d < 0.05) {
        vec2 n = normalize(vec2(
            (-log(m + dFdx(m)) / 35.0) - d,
            (-log(m + dFdy(m)) / 35.0) - d
        ));

        float edge = smoothstep(0.05, -0.02, d);
        vec3 glass = getRefr(texCoord, n, edge);

        float sp = pow(max(0.0, dot(n, normalize(vec2(-1.0, 1.5)))), 25.0);

        // constant rim term (0.4); folded down from a per-pixel rim calc that always evaluated to
        // the same value given this lens's 2D normal
        fc = mix(bg, glass + sp + 0.4, edge);
        fc += sp * 0.7 * edge;
    }

    fragColor = vec4(fc, 1.0);
}
