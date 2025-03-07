#version 330 core

uniform sampler2D DiffuseSampler;
uniform vec4 ColorModulate;
uniform float Time; // Segundos
in vec2 texCoord;

uniform float uIntensity; // Intensidad del efecto: 0 = sin efecto, 1 = máximo efecto

out vec4 fragColor;

// Constantes para el efecto (ajusta estos valores según lo deseado)
const float MIN_EFFECT = 0.0; // Efecto mínimo (menos distorsión/ruido)
const float MAX_EFFECT = 0.01;  // Efecto máximo (más distorsión/ruido)

// Función pseudoaleatoria para generar ruido
float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    // Obtiene el color base de la textura
    vec4 baseColor = texture(DiffuseSampler, texCoord) * ColorModulate;

    // Interpolar linealmente entre MIN_EFFECT y MAX_EFFECT en función de uIntensity
    float effectFactor = mix(MIN_EFFECT, MAX_EFFECT, uIntensity);

    // Genera un offset de ruido basado en las coordenadas y el tiempo
    vec2 noiseOffset = vec2(rand(texCoord + Time), rand(texCoord.yx - Time));
    // Centramos el ruido (-0.5 a 0.5) y lo escalamos usando effectFactor
    vec2 distortion = (noiseOffset - 0.5) * effectFactor;

    // Sampleamos la textura usando la coordenada distorsionada
    vec4 distortedColor = texture(DiffuseSampler, texCoord + distortion) * ColorModulate;

    // Genera ruido blanco adicional basado en la posición del fragmento y el tiempo
    float noiseValue = rand(gl_FragCoord.xy + Time);
    vec4 noiseColor = vec4(vec3(noiseValue), 1.0);

    // Mezcla el color distorsionado con el ruido blanco usando effectFactor
    fragColor = mix(distortedColor, noiseColor, effectFactor);
}
