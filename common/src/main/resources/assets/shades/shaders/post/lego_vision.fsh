#version 330

uniform sampler2D InSampler; // input texture

in vec2 texCoord; // uv coords

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor; // final pixel color

const float CELL_PIXELS = 14.0;
const float STEPS = 5.0; // how many flat color shades per channel - Lego only ships a limited palette
const vec2 LIGHT_DIR = vec2(-0.5, 0.5); // where the "light" sits, in cell-local xy

void main(){

    // same cell-snapping trick as receipt_vision.fsh
    vec2 cellSize = CELL_PIXELS / OutSize;
    vec2 cellOrigin = cellSize * floor(texCoord / cellSize);
    vec4 diffuseColor = texture(InSampler, cellOrigin);

    // quantize (round down to a fixed step) each color channel instead of leaving it smooth;
    // this is what makes it read as "molded plastic in a few colors" rather than a photo
    vec3 brickColor = floor(diffuseColor.rgb * STEPS) / STEPS;
    brickColor = clamp(brickColor, 0.08, 0.95);

    // cell-local position, re-centered to -0.5..0.5 (see halftone_vision.fsh)
    vec2 cellUV = fract(texCoord / cellSize) - 0.5;
    float distFromCenter = length(cellUV);

    // the "stud" on top of a real 1x1 Lego brick is a lit little cylinder: brightest on the
    // side facing the light, darker on the far side. We fake that by comparing the direction
    // from the cell's center to this pixel against the light's direction; the closer they
    // point the same way, the brighter this pixel gets
    float lighting = dot(normalize(cellUV + 1e-5), normalize(LIGHT_DIR));

    // mask that lighting so it only shows up in a small circle in the middle of the cell,
    // fading smoothly to nothing past radius 0.28; same smoothstep-as-soft-edge idea as the
    // halftone dot
    float stud = smoothstep(0.28, 0.0, distFromCenter);
    brickColor *= 1.0 + stud * lighting * 0.6;

    // thin dark seam around the outer edge of every cell, so bricks read as separate pieces
    // instead of one continuous surface
    float seam = smoothstep(0.46, 0.5, max(abs(cellUV.x), abs(cellUV.y)));
    brickColor = mix(brickColor, brickColor * 0.4, seam);

    fragColor = vec4(brickColor, 1.0);
}
