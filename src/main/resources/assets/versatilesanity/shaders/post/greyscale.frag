#version 330 core

// Coordenadas de textura que provienen del renderizado de la pantalla.
in vec2 vTexCoord;

// El renderizado actual, ya sea la pantalla entera u otro target.
uniform sampler2D uTexture;

// Uniform que controla la intensidad del efecto:
// 0.0 -> sin efecto (color original)
// 1.0 -> 100% gris (escala de grises)
uniform float uIntensity;

out vec4 fragColor;

void main() {
    vec4 color = texture(uTexture, vTexCoord);
    // Calcula la luminancia usando coeficientes de percepción
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    vec3 grayscale = vec3(gray);
    // Mezcla el color original con el gris según uIntensity
    vec3 result = mix(color.rgb, grayscale, uIntensity);
    fragColor = vec4(result, color.a);
}
