#version 330

uniform sampler2D InSampler; // input texture

in vec2 texCoord; // uv coords
out vec4 fragColor; // final pixel color

// negative bulges the view outward (fisheye/magnified center), positive would pinch it inward
const float STRENGTH = -0.6;

void main(){

    vec2 fromCenter = texCoord - vec2(0.5);
    float dist = length(fromCenter);

    // classic barrel-distortion polynomial: bend how far out we sample as a cubic function of
    // distance from center, then re-project along the same direction. Flattest right at the
    // center and at the very edge, most curved in between; same shape a real curved lens bends
    // light into
    float distortedDist = dist + STRENGTH * dist * dist * dist;
    vec2 distortedUV = vec2(0.5) + normalize(fromCenter + 1e-6) * distortedDist;

    fragColor = vec4(texture(InSampler, distortedUV).rgb, 1.0);
}
