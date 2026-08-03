#version 330

// live GameTime, same trick as plasma.fsh - original digital-rain implementation, not ported
// from any external source (see NOTES.md Ideas for the licensing reason that mattered here)
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

const vec3 RAIN_COLOR = vec3(0.15, 1.0, 0.35);
const float COLUMNS = 60.0;
const float ROWS = 40.0;
const float TRAIL_LENGTH = 18.0;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

void main(){

    float t = GameTime * 2400.0;

    vec2 cell = floor(texCoord * vec2(COLUMNS, ROWS));

    // each column falls at its own speed and loops forever; texCoord.y=0 is the bottom of the
    // screen (see screenquad.vsh), so a falling head means its row count decreases over time
    float colSpeed = 4.0 + hash(vec2(cell.x, 0.0)) * 10.0;
    float headRow = mod(ROWS - t * colSpeed * 0.1, ROWS + TRAIL_LENGTH * 2.0) - TRAIL_LENGTH;

    float distBehindHead = headRow - cell.y;

    vec3 color = texture(InSampler, texCoord).rgb * 0.25;

    if (distBehindHead >= 0.0 && distBehindHead < TRAIL_LENGTH) {
        // per-cell glyph flicker, re-rolled every half second, different phase per cell
        float glyph = step(0.5, hash(cell + floor(t * 0.5)));

        float brightness = pow(1.0 - distBehindHead / TRAIL_LENGTH, 1.5);
        vec3 glow = RAIN_COLOR * brightness * glyph;

        // the lead character flashes near-white
        if (distBehindHead < 1.0) {
            glow = mix(glow, vec3(0.8, 1.0, 0.9), 0.6);
        }

        color += glow;
    }

    fragColor = vec4(color, 1.0);
}
