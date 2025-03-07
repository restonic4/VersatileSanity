#version 330 core

uniform sampler2D DiffuseSampler;
uniform vec4 ColorModulate;
uniform float Time; // Segundos
in vec2 texCoord;

uniform float uIntensity; // Intensidad del efecto (0.0 a 1.0)

out vec4 fragColor;

// Función para calcular el desenfoque gaussiano
/*vec4 gaussianBlur(sampler2D image, vec2 uv, float radius) {
    vec4 sum = vec4(0.0);
    float sigma = radius / 2.0;
    float twoSigmaSq = 2.0 * sigma * sigma;
    float piSigma = 3.14159265359 * sigma;
    float weightSum = 0.0;

    // Tamaño del kernel (ajusta según el rendimiento deseado)
    int kernelSize = int(radius * 2.0);
    for (int x = -kernelSize; x <= kernelSize; x++) {
        for (int y = -kernelSize; y <= kernelSize; y++) {
            vec2 offset = vec2(float(x), float(y));
            float weight = exp(-dot(offset, offset) / twoSigmaSq) / (piSigma);
            sum += texture(image, uv + offset / vec2(textureSize(image, 0))) * weight;
            weightSum += weight;
        }
    }
    return sum / weightSum;
}*/
vec4 gaussianBlur(sampler2D image, vec2 uv, float radius) {
    if (radius <= 0.0) return texture(image, uv);

    vec2 texSize = textureSize(image, 0);
    vec2 texelSize = 1.0 / texSize;
    vec4 sum = texture(image, uv);
    float weightSum = 1.0;

    const vec2 directions[8] = vec2[](
    vec2(1, 0), vec2(0.7071, 0.7071), vec2(0, 1), vec2(-0.7071, 0.7071),
    vec2(-1, 0), vec2(-0.7071, -0.7071), vec2(0, -1), vec2(0.7071, -0.7071)
    );

    const float weights[4] = float[](0.8825, 0.6065, 0.3247, 0.1353);

    for (int i = 0; i < 8; ++i) {
        vec2 dir = directions[i];
        for (int j = 0; j < 4; ++j) {
            float fraction = (j + 1.0) * 0.25;
            vec2 pixelOffset = dir * radius * fraction;
            vec2 uvOffset = pixelOffset * texelSize;
            sum += texture(image, uv + uvOffset) * weights[j];
            weightSum += weights[j];
        }
    }

    return sum / weightSum;
}

void main() {
    // Parámetros configurables
    float MinBlurRadius = 0;   // Radio mínimo del desenfoque
    float MaxBlurRadius = 20;   // Radio máximo del desenfoque
    float MinVignetteDist = 0.5; // Distancia mínima del desenfoque al centro
    float MaxVignetteDist = 0.75; // Distancia máxima del desenfoque al centro
    float MinPulseFreq = 1.0;    // Frecuencia mínima de las pulsaciones
    float MaxPulseFreq = 3.0;    // Frecuencia máxima de las pulsaciones
    float MinPulseIntensity = 0.0;
    float MaxPulseIntensity = 1.5;

    // Calcular la distancia del fragmento al centro de la pantalla
    vec2 center = vec2(0.5, 0.5);
    float dist = distance(texCoord, center);

    // Interpolar la distancia de viñeta según uIntensity
    float vignetteDist = mix(MinVignetteDist, MaxVignetteDist, uIntensity);

    // Calcular la intensidad del desenfoque basada en la distancia (efecto viñeta)
    float blurAmount = smoothstep(0.0, vignetteDist, dist);

    // Interpolar el radio del desenfoque según uIntensity
    float blurRadius = mix(MinBlurRadius, MaxBlurRadius, uIntensity) * blurAmount;

    // Interpolar la frecuencia de pulsación según uIntensity
    float pulseFreq = mix(MinPulseFreq, MaxPulseFreq, uIntensity);

    // Calcular la intensidad de la pulsación
    float pulseIntensity = mix(MinPulseIntensity, MaxPulseIntensity, uIntensity);

    // Aplicar efecto de pulsación con intensidad variable
    float pulse = 1.0 + pulseIntensity * sin(Time * pulseFreq);
    blurRadius *= pulse;

    // Aplicar el desenfoque gaussiano
    vec4 blurredColor = gaussianBlur(DiffuseSampler, texCoord, blurRadius);

    // Mezclar el color original con el desenfoque según la cantidad de desenfoque
    vec4 originalColor = texture(DiffuseSampler, texCoord) * ColorModulate;
    fragColor = mix(originalColor, blurredColor, blurAmount);
}
