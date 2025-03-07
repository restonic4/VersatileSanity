#version 330 core
uniform sampler2D DiffuseSampler;
uniform vec4 ColorModulate;
uniform float Time; // Seconds
in vec2 texCoord;
uniform float uIntensity;
out vec4 fragColor;

vec2 random2(vec2 st){
    st = vec2(dot(st,vec2(127.1, 311.7)),
    dot(st,vec2(269.5, 183.3)));
    return -1.0 + 2.0 * fract(sin(st) * 43758.5453123);
}

float noise(vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(mix(dot(random2(i + vec2(0.0, 0.0)), f - vec2(0.0, 0.0)),
    dot(random2(i + vec2(1.0, 0.0)), f - vec2(1.0, 0.0)), u.x),
    mix(dot(random2(i + vec2(0.0, 1.0)), f - vec2(0.0, 1.0)),
    dot(random2(i + vec2(1.0, 1.0)), f - vec2(1.0, 1.0)), u.x), u.y);
}

void main() {
    // Parámetros ajustables
    float vignetteStartMin = 0.5;    // Tamaño de viñeta cuando intensidad = 0 (mayor = más pequeña)
    float vignetteStartMax = 0.2;    // Tamaño de viñeta cuando intensidad = 1 (menor = más grande)
    float vignetteEnd = 1.5;         // Dónde termina la viñeta (menor = más intensa)
    float vignetteStrength = 0.95;   // Intensidad del oscurecimiento (0-1)
    float noiseIntensity = 0.2;      // Intensidad del ruido estático (0-1)
    float colorCycleSpeed = 2.0;
    float redIntensityMin = 0.0;
    float redIntensityMax = 0.6;

    // Color original
    vec4 color = texture(DiffuseSampler, texCoord) * ColorModulate;

    // Interpolación de viñeta
    float vignetteStart = mix(vignetteStartMin, vignetteStartMax, uIntensity);
    float redIntensity = mix(redIntensityMin, redIntensityMax, uIntensity);

    // Cálculo de viñeta
    vec2 uv = texCoord * 2.0 - 1.0;
    float vignetteAmount = smoothstep(vignetteStart, vignetteEnd, length(uv));

    // Generación de ruido con variación temporal
    float noiseValue = noise(texCoord * 10.0 + Time);
    float noise = (noiseValue - 0.5) * noiseIntensity;

    // Ciclo de color: de negro a rojo según GameTime
    float colorCycle = (sin(Time * colorCycleSpeed) + 1.0) * 0.5;
    vec3 vignetteColor = mix(vec3(redIntensity, 0.0, 0.0), vec3(0.0), colorCycle);

    // Aplicación de la viñeta
    vec3 baseVignette = color.rgb * (1.0 - vignetteAmount * vignetteStrength);
    vec3 blended = mix(baseVignette, vignetteColor, vignetteAmount);

    // Se suma el ruido de forma aditiva (multiplicado por la cantidad de viñeta)
    vec3 finalColor = clamp(blended + noise * vignetteAmount, 0.0, 1.0);

    // Mezclar con el color original según uIntensity
    fragColor = vec4(mix(color.rgb, finalColor, uIntensity), color.a);
}