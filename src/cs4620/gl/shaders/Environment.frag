#version 120

// You May Use The Following Functions As RenderMaterial Input
// vec4 getDiffuseColor(vec2 uv)
// vec4 getNormalColor(vec2 uv)
// vec4 getSpecularColor(vec2 uv)
// vec4 getEnvironmentColor(vec3 dir)

varying vec2 fUV;
varying vec4 worldPos; // vertex position in world coordinates

// Lighting Information
uniform vec3 ambientLightIntensity;

// Camera Information
uniform vec3 worldCam;
uniform float exposure;

void main() {
  // TODO A4

	vec3 V = normalize(worldPos.xyz- worldCam);

	vec4 finalColor = vec4(0.0, 0.0, 0.0, 0.0);

	vec4 Iamb = getEnvironmentColor(V);

	gl_FragColor = (finalColor + Iamb)*exposure; 
}