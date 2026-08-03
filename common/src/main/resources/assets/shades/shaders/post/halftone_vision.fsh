#version 330

uniform sampler2D InSampler; // input texture

in vec2 texCoord; // uv coords

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor; // final pixel color

const float CELL_PIXELS = 10.0;
const vec4 INK = vec4(0.05, 0.05, 0.08, 1.0);
const vec4 PAPER = vec4(0.92, 0.9, 0.86, 1.0);

void main(){

    // same cell-snapping trick as receipt_vision.fsh; more comments in that file about this
    vec2 cellSize = CELL_PIXELS / OutSize;
    vec2 cellOrigin = cellSize * floor(texCoord / cellSize);
    vec4 diffuseColor = texture(InSampler, cellOrigin);
    float luma = dot(diffuseColor.rgb, vec3(0.299, 0.587, 0.114));

    // this time we re-center cellUV so (0,0) sits in the MIDDLE of the cell instead of a
    // corner (fract() alone gives 0..1, subtracting 0.5 shifts that to -0.5..0.5); that makes
    // "distance from the center" a one-line length() call, which is what a circle is.
    //  signed distance function (SDF)
    vec2 cellUV = fract(texCoord / cellSize) - 0.5;
    float dist = length(cellUV);

    // darker source pixels get a bigger dot -> a halftone/newspaper print uses bigger ink dots
    // to represent shadow and smaller ones (or none) for highlights
    float dotRadius = (1.0 - luma) * 0.7;

    // smoothstep(a, b, x) instead of a hard dist < dotRadius comparison gives the dot a soft,
    // anti-aliased edge -> it fades from 0 to 1 across the [dotRadius-0.08, dotRadius] range
    // rather than snapping instantly, so the circle doesn't look jagged
    float ink = smoothstep(dotRadius, dotRadius - 0.08, dist);

    fragColor = mix(PAPER, INK, ink);
}
