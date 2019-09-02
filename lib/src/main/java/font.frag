#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
varying vec4 v_color;
varying vec2 v_texCoord;

uniform float distanceRange;

uniform float fontWeight;

uniform bool shadowDrawn;
uniform vec4 shadowColor;
uniform vec2 shadowOffset;
uniform float shadowSmoothing;


float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

void main() {
    vec2 texSize = vec2(textureSize(u_texture, 0));
    vec2 msdfUnit = distanceRange / texSize;

    vec4 samp = texture(u_texture, v_texCoord);
    float distance = median(samp.r, samp.g, samp.b) + fontWeight - 0.5;
    distance *= dot(msdfUnit, 0.5 / fwidth(v_texCoord));
    float glyphAlpha = clamp(distance + 0.5, 0.0, 1.0);
    vec4 glyph = vec4(v_color.rgb, v_color.a * glyphAlpha);

    if (shadowDrawn) {
        distance = texture(u_texture, v_texCoord - shadowOffset / texSize).a + fontWeight;
        float shadowAlpha = smoothstep(0.5 - shadowSmoothing, 0.5 + shadowSmoothing, distance);
        vec4 shadow = vec4(shadowColor.rgb, v_color.a * shadowAlpha * shadowColor.a);

        gl_FragColor = mix(shadow, glyph, glyphAlpha);
    } else {
        gl_FragColor = glyph;
    }
}
