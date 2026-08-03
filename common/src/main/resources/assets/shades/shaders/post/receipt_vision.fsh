#version 330

uniform sampler2D InSampler; // input texture

in vec2 texCoord; // uv coords

// screen size in real pixels; so our "cells" stay square/consistent no matter the
// window size, instead of stretching with the aspect ratio
layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor; // final pixel color

const float CELL_PIXELS = 8.0; // size of one cell, in real screen pixels
const vec4 INK = vec4(0.05, 0.05, 0.05, 1.0);
const vec4 PAPER = vec4(0.85, 0.83, 0.78, 1.0);

void main(){

    // size of one cell in UV space (0..1 units) rather than pixels, so the grid below lines
    // up the same way regardless of resolution
    vec2 cellSize = CELL_PIXELS / OutSize;

    // classic pixelation trick: divide texCoord by cellSize to get "which cell am I in"
    // (as a fractional coordinate), floor() it to snap to the cell's bottom-left corner, then
    // multiply back by cellSize to land back in UV space -> every pixel inside one cell now
    // samples from the exact same spot
    vec2 cellOrigin = cellSize * floor(texCoord / cellSize);
    vec4 diffuseColor = texture(InSampler, cellOrigin);
    float luma = dot(diffuseColor.rgb, vec3(0.299, 0.587, 0.114));

    // fract() of the same division gives the OPPOSITE piece of information: not which cell
    // we're in, but WHERE inside that cell (0..1 on both axes) -> lets us draw a
    // shape inside each cell instead of just filling it with one flat color
    vec2 cellUV = fract(texCoord / cellSize);

    // darker source pixels -> longer ink bar, like a receipt printer pressing harder/longer
    // on shadowed detail and barely touching bright highlights
    float barWidth = 1.0 - luma;

    bool insideBar = cellUV.x < barWidth && cellUV.y > 0.15 && cellUV.y < 0.85;
    fragColor = insideBar ? INK : PAPER;
}
