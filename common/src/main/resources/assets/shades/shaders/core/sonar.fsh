#version 330

// live GameTime, same trick as plasma.fsh - see ShadesLiveVision for how this pass gets driven
// by hand instead of through PostChain
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;      // copy of the current frame's color
uniform sampler2D InDepthSampler; // the real depth buffer, bound directly (read-only, no copy needed)

in vec2 texCoord;
out vec4 fragColor;

const vec3 PING_COLOR = vec3(0.3, 0.9, 1.0);

void main(){

    float t = GameTime * 2400.0;
    float depth = texture(InDepthSampler, texCoord).r;

    // an expanding "shell" sweeping from near to far and looping - comparing against the raw
    // depth buffer directly (rather than screen-space distance from center) means the ping
    // follows the actual 3D scene: it sweeps across a flat floor/wall as an approaching band
    // instead of a flat 2D circle drawn over the image, same idea a depth-aware sonar effect
    // uses. Depth isn't linear (denser near the camera) so this loop won't sweep at a perfectly
    // even visual speed, but that's a fine approximation for a stylized ping
    float ping = mod(t * 0.15, 1.0);

    // band width: wide enough to reliably catch on-screen geometry every frame, since real depth
    // values aren't spread evenly across 0..1 - most of a typical view clusters into a fairly
    // narrow slice, so a razor-thin band mostly sweeps through empty space between geometry
    float shellDist = abs(depth - ping);
    float ring = 1.0 - smoothstep(0.0, 0.12, shellDist); // smoothstep needs edge0 < edge1, unlike before
    ring *= 1.0 - ping; // fades out as it expands, like a real ping losing energy

    vec3 color = texture(InSampler, texCoord).rgb;
    color += PING_COLOR * ring * 1.5; // additive glow reads clearly against any background,
                                       // instead of a subtle tint-mix that was easy to miss

    fragColor = vec4(color, 1.0);
}
