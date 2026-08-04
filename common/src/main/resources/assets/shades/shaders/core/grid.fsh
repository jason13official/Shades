#version 330

// reworked per feedback: was a raymarched fake floor + orbiting sphere replacing the whole
// screen; now uses real depth-reconstructed world position instead (same worldPos() technique
// sonar.fsh uses - ShadesClient pushes the same combined inverse-projection*view matrix + camera
// position, see buildGridRayUniform). Every real block on screen gets grid-snapped by its actual
// world XZ column and bounces up and down in place, screen-space-displaced by its real distance
// from the camera - this modifies how the real world looks instead of replacing it with an
// unrelated scene, unlike this file's first version
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

// combined inverse-projection*view matrix + camera position, pushed fresh each frame by
// ShadesClient - same worldPos() reconstruction sonar.fsh uses, just no ping origin needed
layout(std140) uniform GridRay {
    mat4 InverseTransformMatrix;
    vec3 CameraPosition;
};

in vec2 texCoord;
out vec4 fragColor;

const float CELL_SIZE = 1.0; // one Minecraft block
const float BOUNCE_AMOUNT = 0.4; // in blocks
const float BOUNCE_SPEED = 1.6;
const vec3 SEAM_COLOR = vec3(0.6, 0.85, 1.0);

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

vec3 worldPos(vec3 screenPoint) {
    vec3 ndc = screenPoint * 2.0 - 1.0;
    vec4 homPos = InverseTransformMatrix * vec4(ndc, 1.0);
    return homPos.xyz / homPos.w + CameraPosition;
}

void main(){

    float t = GameTime * 2400.0 * 0.5;
    float rawDepth = texture(InDepthSampler, texCoord).r;

    // sky/no real geometry here -> nothing to grid-snap, leave the pixel untouched
    if (rawDepth >= 0.9999) {
        fragColor = texture(InSampler, texCoord);
        return;
    }

    vec3 surfacePos = worldPos(vec3(texCoord, rawDepth));

    // which real block column this pixel belongs to, and a phase unique to that column so
    // neighbouring blocks don't all bounce in lockstep
    vec2 cell = floor(surfacePos.xz / CELL_SIZE);
    float phase = hash(cell) * 6.2831853;
    float height = sin(t * BOUNCE_SPEED + phase) * BOUNCE_AMOUNT;

    // the same world-space bounce needs to shrink in screen space the farther the block is from
    // the camera, same intuition as real perspective; texCoord.y=0 is the bottom of the screen
    // (see screenquad.vsh), so a positive world-space rise shifts the sample DOWN to pull the
    // block's true appearance UP into view
    float camDist = max(length(surfacePos - CameraPosition), 0.5);
    float screenShift = height / camDist * 0.4;

    vec2 distortedUV = texCoord - vec2(0.0, screenShift);
    vec3 color = texture(InSampler, distortedUV).rgb;

    // thin bright seam right at each column's edge, so the grid itself reads clearly instead of
    // just being a blurry bounce
    vec2 cellUV = fract(surfacePos.xz / CELL_SIZE);
    float edgeDist = min(min(cellUV.x, 1.0 - cellUV.x), min(cellUV.y, 1.0 - cellUV.y));
    float seam = 1.0 - smoothstep(0.0, 0.06, edgeDist);

    vec3 outColor = mix(color, SEAM_COLOR, seam * 0.35);
    fragColor = vec4(outColor, 1.0);
}
