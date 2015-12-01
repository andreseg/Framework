#version 120

// You May Use The Following Functions As RenderMaterial Input
// vec4 getDiffuseColor(vec2 uv)
// vec4 getNormalColor(vec2 uv)
// vec4 getSpecularColor(vec2 uv)

//added a couple things at the top, outside of the "todo" section

// Lighting Information
const int MAX_LIGHTS = 16;
uniform int numLights;
uniform vec3 lightIntensity[MAX_LIGHTS];
uniform vec3 lightPosition[MAX_LIGHTS];
uniform vec3 ambientLightIntensity;  		// Ia = ambient light intensity

// Camera Information
uniform vec3 worldCam;
uniform float exposure;

// Shading Information
// 0 : smooth, 1: rough
uniform float roughness;

// Shading Information
uniform float shininess;

varying vec2 fUV;
varying vec3 fN; // normal at the vertex
varying vec4 worldPos; // vertex position in world coordinates

void main() {
  // TODO A4
	// Variables needed:

		// F0 = Fresnel Term (specular reflectance when light arrives perpendicular)
	float f0 = 0.04;

		// N = normal vector of surface
	vec3 N = normalize(fN);   //why is N capital? isn't that bad practice

		// V = viewing direction
	vec3 V = normalize(worldCam - worldPos.xyz);

	vec4 fc = vec4(0.0, 0.0, 0.0, 0.0);

for (int i = 0; i < numLights; i++) {

		// L = light direction
	vec3 L = normalize(lightPosition[i] - worldPos.xyz); 

		// H = Half Vector
	vec3 H = normalize(L + V);

		// I = light intensity
	vec3 I = lightIntensity[i];

		// r = shaded distance from the light source
	float r = length(lightPosition[i] - worldPos.xyz);

	vec3 Ioverd2 = lightIntensity[i]/pow(r,2);
	vec4 Ioverd24 = vec4(Ioverd2,0.0);

		// Ks = specular reflectance
	vec4 Ks = getSpecularColor(fUV);

		// Kd = diffuse reflectance
	vec4 Kd = getDiffuseColor(fUV);
	
		// Beta = angle between viewing direction and half vector
	float beta = acos(dot(V,H));

		// Thetah = angle between the normal and half vector
	float thetah = acos(dot(N,H));

	// calculate Fresnel Term
	float fresnel = f0 + (1-f0)*pow((1-(dot(V,H))), 5);
	
	// calculate Microfacet Distribution
		// m = roughness
	float m = roughness;
		// D = Microfacet Distribution
	float D1 = 1/(pow(m,2)*pow(dot(N,H),4));
	float D2 = pow(dot(N,H),2)-1;
	float D3 = 1/(pow(m,2)*pow(dot(N,H),2));
	float D = (D1*exp(D2*D3));

	float Ga;
    float Gb;
	float Gc;
	float G;

	// calculate Geometric Attenuation; G = Geometric Attenuation
	Gb = 2*(dot(N,H))*(dot(N,V))/(dot(V,H));

    Ga = min(1.0,Gb);

	Gc = (2*(dot(N,H))*(dot(N,L)))/(dot(V,H));

	G = min(Ga,Gc);

	// calculate the color using Cook-Torrance Formula
	vec3 Ia = ambientLightIntensity;
	vec4 Ia4 = vec4(Ia,0.0)*Kd;

	float pi = 3.1415926535897932384626433832795;
	//color = (ks*(f(beta)/pi)*(d(thetah)*g)/(n.v)(n.l)))max(n.l,0)(i/r^2)+kd*ia;

    float mnl;
	float frOverpi;
	float dthetag;
	float ndotvndotl;

	mnl = max(dot(N,L),0.0);

 // frOverpiEtc = ((fresnel)/(pi))*((D*thetah*G)/(dot(N,V))*(dot(N,L)));
	frOverpi = (fresnel)/(pi);
	ndotvndotl = (dot(N,V))*(dot(N,L));
//	dthetag = 1000;
	dthetag = ((D*thetah*G)/(ndotvndotl));
	
	fc += vec4((Ks*frOverpi*dthetag) + Kd)*mnl*Ioverd24+Ia4;
	}

    gl_FragColor = fc*exposure;


}
