#version 330

uniform sampler2D InSampler; // input texture

in vec2 texCoord; // uv coords
out vec4 fragColor; // final pixel color

const float PI = 3.14159265;
const float FLUTE_COUNT = 40.0; // number of glass ridges across the screen
const float DISTORTION_AMOUNT = 0.012;
const vec3 LIGHT_DIR = vec3(-0.4, 0.5, 0.75);

void main(){

    // where we are within a single ridge, looping 0..1 across the screen FLUTE_COUNT times.
    // this describes the SHAPE of the glass: imagine the surface height following a sine wave
    // sin(flutePosition * 2*PI), one full ridge per loop
    float flutePosition = fract(texCoord.x * FLUTE_COUNT);

    // we don't actually need the height itself, we need to know which way the surface is
    // TILTED at each point, since that's what bends light through it. The tilt (slope) of a
    // sine wave is its derivative, which is just a cosine wave; flattest at the peaks and
    // valleys of the ridge, steepest in the middle where the glass curves the most
    float slope = cos(flutePosition * PI * 2.0) * PI;

    // turn that slope into a 3D surface normal: it leans sideways proportional to how steep
    // the glass is here, and mostly points straight at the viewer (+Z) since it's only a
    // gentle curve, not a right angle
    vec3 normal = normalize(vec3(slope * 0.15, 0.0, 1.0));

    // bend (refract) the sample point sideways based on how tilted the glass is at this pixel;
    // this is the actual "looking through glass" distortion.
    // Flat spots (slope near 0) barely shift the image, steep spots shift it the most
    vec2 distortedUV = texCoord + normal.xy * DISTORTION_AMOUNT;
    vec4 diffuseColor = texture(InSampler, distortedUV);

    // standard diffuse lighting: how directly this bit of the ridge faces the light
    float diffuse = max(dot(normal, normalize(LIGHT_DIR)), 0.0);

    // add a narrow specular highlight where the ridge is angled just right to bounce light
    // straight at the camera -> this is what sells "glass" instead of just "wavy image"
    vec3 viewDir = vec3(0.0, 0.0, 1.0);
    vec3 halfVec = normalize(normalize(LIGHT_DIR) + viewDir);
    float specular = pow(max(dot(normal, halfVec), 0.0), 40.0);

    vec3 outColor = diffuseColor.rgb * (0.6 + diffuse * 0.5) + specular * 0.5;

    fragColor = vec4(outColor, 1.0);
}
