#version 330

#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D PrevFrameSampler;

in vec2 texCoord;
out vec4 fragColor;

/// ported from 1.20.1's phosphor.fsh: keeps whichever is brighter between the current frame and
/// the previous frame decayed by PHOSPHOR_DECAY, so motion drags a fading afterimage behind it -
/// needs a real cross-frame feedback target (see ShadesClient.getPhosphorFeedback), a plain
/// post_effect JSON pass can't carry state between frames
const vec3 PHOSPHOR_DECAY = vec3(0.85, 0.82, 0.78);

void main() {

    vec3 current = texture(InSampler, texCoord).rgb;
    vec3 previous = texture(PrevFrameSampler, texCoord).rgb;

    fragColor = vec4(max(previous * PHOSPHOR_DECAY, current), 1.0);
}
