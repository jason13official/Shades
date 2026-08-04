#version 330

// port of a tile-ripple raymarch scene (shadertoy.com/view/tssSDN) - an infinite checkerboard
// floor whose tiles dip down as an orbiting sphere passes overhead, like a ripple pressed into
// the ground. The stepping loop's min() against the distance to the next tile boundary is an
// acceleration trick: it lets the floor extend forever without ever looping over more than one
// tile's worth of geometry per step. Ported from iTime/iMouse to GameTime with a fixed camera (no
// mouse orbit) and every distance/radius scaled up ~10x from the original's tiny macro-camera
// units to something a simple fixed camera can frame without a mouse-driven zoom.
//
// Unlike the original (which just draws the scene as-is), the raymarched surface here is used to
// REFRACT + tint the real background instead of replacing it outright - same "look at the world
// through something" idea as fluted_glass/molten_glass, just with this raymarch's own surface
// normal driving the distortion instead of a sine ridge or metaball field. Real world always
// shows through; the grid reads as a shimmering pane laid over it, brightest right at tile seams
// and near the orb
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, pushed fresh each frame by ShadesClient
layout(std140) uniform GridConfig {
    float Aspect;
};

in vec2 texCoord;
out vec4 fragColor;

#define MAX_DIST 30.0
#define SURF_DIST 0.0005
#define EPS 0.0005
#define PI 3.141592
#define PI2 (PI * 2.0)

const float REP = 0.4;

float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

float sdSphere(vec3 p, float s) {
    return length(p) - s;
}

float sdBox(vec3 p, vec3 b) {
    vec3 q = abs(p) - b;
    return length(max(q, 0.0)) + min(max(q.x, max(q.y, q.z)), 0.0);
}

vec2 minMat(vec2 d1, vec2 d2) {
    return d1.x < d2.x ? d1 : d2;
}

vec2 scene(vec3 p, float t) {

    vec2 d = vec2(100000.0, 0.0);
    vec3 q = p;

    vec3 spo = vec3(sin(t * 1.8) * 2.5, 3.2, cos(t * 2.2) * 3.0);
    d.x = sdSphere(q - spo, 0.75);

    vec2 id = floor(q.xz / REP);
    float hash = rand(id * 0.001);

    q.xz = mod(q.xz, REP) - REP * 0.5;

    vec3 bcp = vec3(0.0);
    bcp.xz = id * REP + REP * 0.5;

    float bsDist = length(spo.xz - bcp.xz);
    float s = smoothstep(0.0, 5.0, bsDist);

    q -= vec3(0.0, 1.25 - (sin(hash * PI2 + t * (2.0 + bsDist * 0.15)) * 0.5) * (1.0 - pow(s, 0.9)), 0.0);

    return minMat(d, vec2(sdBox(q, vec3(REP * 0.5, 1.0, REP * 0.5)), 1.0));
}

vec3 getNormal(vec3 p, float t) {
    vec2 e = vec2(EPS, 0.0);
    return normalize(vec3(
        scene(p + e.xyy, t).x - scene(p - e.xyy, t).x,
        scene(p + e.yxy, t).x - scene(p - e.yxy, t).x,
        scene(p + e.yyx, t).x - scene(p - e.yyx, t).x
    ));
}

vec2 raymarch(vec3 ro, vec3 rd, float t) {

    float accDist = 0.0;
    float mat = 0.0;

    for (int i = 0; i < 96; i++) {
        vec3 p = ro + rd * accDist;
        vec2 result = scene(p, t);
        float dist = result.x;
        vec3 rdi = 1.0 / rd;
        mat = result.y;
        if (abs(dist) < SURF_DIST || accDist > MAX_DIST) {
            break;
        }

        accDist += min(
            min(
                (step(0.0, rd.x) - mod(p.x, REP)) * rdi.x,
                (step(0.0, rd.z) - mod(p.z, REP)) * rdi.z
            ) + 0.001,
            dist
        );
    }

    return vec2(accDist, mat);
}

vec3 getRayDir(vec2 uv, vec3 p, vec3 l, float z) {
    vec3 forward = normalize(l - p);
    vec3 right = normalize(cross(forward, vec3(0.0, 1.0, 0.0)));
    vec3 up = normalize(cross(right, forward));
    return normalize(right * uv.x + up * uv.y + forward * z);
}

void main(){

    float t = GameTime * 2400.0 * 0.5;

    vec2 uv = vec2((texCoord.x * 2.0 - 1.0) * Aspect, texCoord.y * 2.0 - 1.0);

    vec3 ro = vec3(4.0, 3.5, 4.0);
    vec3 ta = vec3(0.0, 0.5, 0.0);
    vec3 rd = getRayDir(uv, ro, ta, 2.2);

    vec2 result = raymarch(ro, rd, t);
    float dist = result.x;
    float mat = result.y;

    vec3 scene = texture(InSampler, texCoord).rgb;

    if (dist >= MAX_DIST) {
        fragColor = vec4(scene, 1.0);
        return;
    }

    vec3 p = ro + rd * dist;
    vec3 l = normalize(vec3(1.0, 1.0, -1.0));
    vec3 n = getNormal(p, t);

    float diffuse = dot(l, n) * 0.5 + 0.5;
    vec3 material;
    if (mat < 0.5) {
        material = diffuse * vec3(1.0, 0.25, 0.15);
    } else {
        material = diffuse * vec3(0.7, 0.85, 1.0);
    }

    // refract the real background through the surface normal, like looking through a rippling
    // glass floor instead of a flat overlay sitting on top of the real image
    vec2 distortedUV = texCoord + n.xy * 0.03;
    vec3 distortedScene = texture(InSampler, distortedUV).rgb;

    // brighter right at tile seams, fading toward barely-there over flat tile faces - reads as a
    // shimmering grid pattern laid over the real world rather than a solid floor blotting it out
    float seamX = abs(fract(p.x / REP) - 0.5);
    float seamZ = abs(fract(p.z / REP) - 0.5);
    float edgeGlow = 1.0 - smoothstep(0.0, 0.08, min(seamX, seamZ));
    float alpha = clamp(0.16 + edgeGlow * 0.5, 0.0, 0.7);

    vec3 outColor = mix(distortedScene, material, alpha);
    fragColor = vec4(outColor, 1.0);
}
