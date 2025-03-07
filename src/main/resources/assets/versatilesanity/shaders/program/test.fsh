#version 330 core

uniform sampler2D DiffuseSampler;
uniform vec4 ColorModulate;
uniform float Time; // Segundos
in vec2 texCoord;

uniform float uIntensity; // Intensidad, cuanto mas cerca a 0 menos  se notan los efectos (hasta no aplicarse), cuanto mas cerca a 1 mas se aplican

out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord) * ColorModulate; // Original color

    fragColor = color;
}