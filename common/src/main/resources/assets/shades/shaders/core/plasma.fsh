#version 330

// this is the payoff for the whole custom-pipeline detour: post-processing shaders (everything
// else in this mod) never get this import, since PostPass always builds its pipeline from
// POST_PROCESSING_SNIPPET alone. This one gets composed with GLOBALS_SNIPPET in
// ShadesRenderPipelines, so it's wired to the SAME live uniform buffer every entity/particle/
// portal shader reads from - GameTime updates every real frame, not once at shader-load time
#moj_import <minecraft:globals.glsl>

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {

    // GameTime loops 0..1 once per in-game day (24000 ticks, 20 real minutes at normal speed) -
    // multiply it back up into a fast-moving clock, playing the same role u_time plays in a
    // Book of Shaders sketch (there it's real elapsed seconds; here it's "elapsed ticks", just a
    // different unit for the same idea: a number that climbs every frame, smoothly, forever)
    float t = GameTime * 2400.0;

    // texCoord0 is 0..1 across our single quad (see ShadesPlasmaEffect); scale it up so the
    // pattern repeats a few times instead of stretching one cycle across the whole lens
    vec2 uv = texCoord0 * 6.0;

    // classic "plasma" recipe: sum a handful of sine waves, each reading position/time
    // differently, then let their interference pattern drive the color below. Every term here
    // is one wave; try deleting one, or changing its frequency multiplier, to feel out what
    // each contributes
    float v = 0.0;
    v += sin(uv.x + t);                                     // drifts sideways over time
    v += sin((uv.y + t) * 0.7);                              // drifts up/down, slower
    v += sin((uv.x + uv.y + t) * 0.5);                       // a diagonal wave
    vec2 swirl = uv + 0.5 * vec2(sin(t * 0.3), cos(t * 0.4)); // an orbiting center point
    v += sin(length(swirl) * 2.0 - t);                        // ...driving expanding rings from it
    v *= 0.25; // 4 waves summed to roughly -4..4 -> squash back down to roughly -1..1

    // feed v through three sine waves 120 degrees (2*PI/3) out of phase with each other - as v
    // sweeps through its range, the three channels peak at different moments, which is what
    // makes the color cycle through a full rainbow instead of just fading one flat tint in/out
    vec3 color = vec3(
        sin(v * 3.14159 + 0.0),
        sin(v * 3.14159 + 2.094),
        sin(v * 3.14159 + 4.189)
    ) * 0.5 + 0.5; // sin() ranges -1..1, remap to 0..1 for a color

    fragColor = vec4(color, 0.55) * vertexColor;
}
