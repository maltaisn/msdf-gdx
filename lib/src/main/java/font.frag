#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
varying vec4 v_color;
varying vec2 v_texCoord;

uniform float distanceRange;// Range in pixels used to encode the distance field.

uniform float fontWeight;// Font weight (thickness), from 0 to 1.

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

void main() {
    // Find distance from glyph, see:
    // - Paper: https://github.com/Chlumsky/msdfgen/files/3050967/thesis.pdf
    // - Improved antialiasing: https://github.com/Chlumsky/msdfgen/issues/36
    vec2 msdfUnit = distanceRange / vec2(textureSize(u_texture, 0));
    vec3 samp = texture(u_texture, v_texCoord).rgb;
    float distance = median(samp.r, samp.g, samp.b) + fontWeight - 1;
    distance *= dot(msdfUnit, 0.5 / fwidth(v_texCoord));

    float alpha = clamp(distance + 0.5, 0.0, 1.0);

    gl_FragColor = vec4(v_color.rgb, v_color.a * alpha);
}
