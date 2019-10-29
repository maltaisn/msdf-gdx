#ifdef GL_ES
precision mediump float;
#endif

#if __VERSION__ >= 130
#define TEXTURE texture
#else
#define TEXTURE texture2D
#endif

uniform sampler2D u_texture;
uniform vec2 u_textureSize;
varying vec4 v_color;
varying vec2 v_texCoord;

uniform float distanceRange;

uniform vec4 color;
uniform float fontWeight;

uniform float shadowClipped;
uniform vec4 shadowColor;
uniform vec2 shadowOffset;
uniform float shadowSmoothing;

uniform vec4 innerShadowColor;
uniform float innerShadowRange;


float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

vec4 blend(vec4 src, vec4 dst, float alpha) {
    // src OVER dst porter-duff blending
    float a = src.a + dst.a * (1.0 - src.a);
    vec3 rgb = (src.a * src.rgb + dst.a * dst.rgb * (1.0 - src.a)) / (a == 0.0 ? 1.0 : a);
    return vec4(rgb, a * alpha);
}

float linearstep(float a, float b, float x) {
    return clamp((x - a) / (b - a), 0.0, 1.0);
}

void main() {
    // Glyph
    vec4 msdf = TEXTURE(u_texture, v_texCoord);
    float distance = median(msdf.r, msdf.g, msdf.b) + fontWeight - 0.5;
    distance *= dot(distanceRange / u_textureSize, 0.5);
    float glyphAlpha = clamp(distance + 0.5, 0.0, 1.0);
    vec4 glyph = vec4(color.rgb, glyphAlpha * color.a);

    // Shadow
    distance = TEXTURE(u_texture, v_texCoord - shadowOffset / u_textureSize).a + fontWeight;
    float shadowAlpha = linearstep(0.5 - shadowSmoothing, 0.5 + shadowSmoothing, distance) * shadowColor.a;
    shadowAlpha *= 1.0 - glyphAlpha * shadowClipped;
    vec4 shadow = vec4(shadowColor.rgb, shadowAlpha);

    // Inner shadow
    distance = msdf.a + fontWeight;
    float innerShadowAlpha = linearstep(0.5 + innerShadowRange, 0.5, distance) * innerShadowColor.a * glyphAlpha;
    vec4 innerShadow = vec4(innerShadowColor.rgb, innerShadowAlpha);

    gl_FragColor = blend(blend(innerShadow, glyph, 1.0), shadow, v_color.a);
}
