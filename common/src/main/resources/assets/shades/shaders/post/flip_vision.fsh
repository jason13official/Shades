#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

/// 1.20.1's Flip debug shader did this in flip.vsh (flipping the quad's vertex UVs); simpler to
/// just flip the sample coordinate here and skip a custom vertex shader entirely
void main() {
    fragColor = texture(InSampler, vec2(texCoord.x, 1.0 - texCoord.y));
}
