#version 330 core

uniform sampler2D DiffuseSampler;
uniform vec4 ColorModulate;
in vec2 texCoord;

uniform float uIntensity;

out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord) * ColorModulate;

    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    vec3 grayscale = vec3(gray);

    vec3 result = mix(color.rgb, grayscale, uIntensity);
    fragColor = vec4(result, color.a);
}
