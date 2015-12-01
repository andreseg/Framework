//DispMapped.vert
#version 120

// Note: We multiply a vector with a matrix from the left side (M * v)!
// mProj * mView * mWorld * pos

// RenderCamera Input
uniform mat4 mViewProjection;

// RenderObject Input
uniform mat4 mWorld;
uniform mat3 mWorldIT;
uniform float dispMagnitude;

// RenderMesh Input
attribute vec4 vPosition; // Sem (POSITION 0)

	//ADDED BY US
	attribute vec3 vNormal; // Sem (NORMAL 0)
	attribute vec2 vUV; // Sem (TEXCOORD 0)

	varying vec2 fUV;
	varying vec3 fN; // normal at the vertex
	varying vec4 worldPos; // vertex position in world-space coordinates

// Shading Information


void main() {
  // TODO A4
	vec4 ncolor = getNormalColor(vUV);
	float disp = (ncolor.x + ncolor.y + ncolor.z) / 3;
	vec3 eatMe = vNormal * disp * dispMagnitude;
	vec4 four = vec4(eatMe, 0.0);
	vec4 newVpos = vPosition + four;

  worldPos = mWorld * newVpos;
  gl_Position = mViewProjection * worldPos;

	fN = normalize((mWorldIT * vNormal).xyz);
	fUV = vUV;
}