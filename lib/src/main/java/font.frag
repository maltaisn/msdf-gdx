#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
varying vec4 v_color;
varying vec2 v_texCoord;

uniform float distanceRange;

uniform float fontWeight;

uniform vec4 shadowColor;
uniform vec2 shadowOffset;
uniform float shadowSmoothing;


float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

vec4 blend(vec4 src, vec4 dst, float alpha) {
    // src OVER dst porter-duff blending
    float a = src.a + dst.a * (1 - src.a);
    vec3 rgb = (src.a * src.rgb + dst.a * dst.rgb * (1 - src.a)) / a;
    return vec4(rgb, a * alpha);
}

void main() {
    vec2 texSize = vec2(textureSize(u_texture, 0));

    // Glyph
    vec4 samp = texture(u_texture, v_texCoord);
    float distance = median(samp.r, samp.g, samp.b) + fontWeight - 0.5;
    distance *= dot(distanceRange / texSize, 0.5 / fwidth(v_texCoord));
    float glyphAlpha = clamp(distance + 0.5, 0.0, 1.0);
    vec4 glyph = vec4(v_color.rgb, glyphAlpha);

    // Shadow
    distance = texture(u_texture, v_texCoord - shadowOffset / texSize).a + fontWeight;
    float shadowAlpha = smoothstep(0.5 - shadowSmoothing, 0.5 + shadowSmoothing, distance) * shadowColor.a;
    vec4 shadow = vec4(shadowColor.rgb, shadowAlpha);

    gl_FragColor = blend(glyph, shadow, v_color.a);
}
