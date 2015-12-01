package cs4620.common.texture;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector2i;
import egl.math.Vector3;
import egl.math.Vector3d;

public class TexGenSphereNormalMap extends ACTextureGenerator {
	// 0.5f means that the discs are tangent to each other
	// For greater values discs intersect
	// For smaller values there is a "planar" area between the discs
	private float bumpRadius;
	public float counter = 0;
	// The number of rows and columns
	// There should be one disc in each row and column
	private int resolution;

	public TexGenSphereNormalMap() {
		this.bumpRadius = 0.5f;
		
		this.resolution = 10;
		this.setSize(new Vector2i(256));
	}

	public void setBumpRadius(float bumpRadius) {
		this.bumpRadius = bumpRadius;
	}

	public void setResolution(int resolution) {
		this.resolution = resolution;
	}

	@Override
	public void getColor(float u, float v, Color outColor) {
		// TODO A4 

		float uRES = u*resolution;
		float vRES = v*resolution;

		float uRound = (float)Math.round(uRES);
		float vRound = (float)Math.round(vRES);

		float uDiv = uRound/resolution;
		float vDiv = vRound/resolution;

		float r = (float) (Math.sin(vDiv)*Math.cos(uDiv));
		float g = (float) (Math.sin(vDiv)*Math.sin(uDiv));
		float b = (float) (Math.cos(vDiv));
		Colord rgb = new Colord(r,g,b);

		counter = counter + 1f;
		if(counter%100 == 0){
			System.out.println("u : " + u + " , v : " + v);
//			System.out.println("vector : " + (counter/100) + " = " + rgb);
			System.out.println("uRound : " + uRound + " , vRound : " + vRound);
			System.out.println("uDiv : " + uDiv + " , vDiv : " + vDiv);
			System.out.println(resolution);
		}

		//remap colors
		rgb.add(1);
		rgb.mul(.5f);
		outColor.set(rgb);
	}
}