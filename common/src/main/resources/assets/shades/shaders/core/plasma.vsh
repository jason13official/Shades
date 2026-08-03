#version 330

// unlike our post_effect shaders (screenquad.vsh, always the same 4 screen corners), this one
// actually transforms real 3D vertices - see ShadesPlasmaEffect for what geometry gets fed in
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec2 texCoord0;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexColor = Color;
}
