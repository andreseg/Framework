package cs4620.ray2.shader;

import cs4620.ray2.RayTracer;
import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import cs4620.ray2.Scene;
import egl.math.Colord;
import egl.math.Vector3d;

/**
 * A Phong material.
 *
 * @author ags, pramook
 */
public class Glass extends Shader {

	/**
	 * The index of refraction of this material. Used when calculating Snell's Law.
	 */
	protected double refractiveIndex;
	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }


	public Glass() { 
		refractiveIndex = 1.0;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {    
		return "glass " + refractiveIndex + " end";
	}

	/**
	 * Evaluate the intensity for a given intersection using the Glass shading model.
	 *
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	@Override
	public void shade(Colord outIntensity, Scene scene, Ray ray, IntersectionRecord record, int depth) {
		// TODO#A7: fill in this function.
        // 1) Determine whether the ray is coming from the inside of the surface or the outside.
		
		//get the normal of the surface intersected
		Vector3d n = record.normal;
		
		//get the direction of the incoming view ray
		Vector3d d = ray.direction;
		
		//find dot product between the 2 vectors
		double nDotD = n.dot(d);
		
		//get the reflected ray
		Vector3d r = d.clone().sub(n.clone().mul(nDotD*2));
		
		//if the dot product is less than zero 
		double nt; //refraction index of second (i.e. exit) material
		double n0; //refractive index of first material
		
		if(nDotD < 0){
			//then the ray is traveling out from glass to air
			nt = 1.0;
			n0 = refractiveIndex;
			
		} else {
		//if the dot product is greater than zero
			//then the ray is traveling in from air to material
			nt = refractiveIndex;
			n0 = 1.0;
			
		}
		
        // 2) Determine whether total internal reflection occurs.
		
		//if the number under the square root (tiRef) is negative then total internal reflection occurs
			//nt is the refractive index of the material you're traveling in to
		double tiRef = 1 - (((Math.pow(n0,2)*(1-Math.pow(nDotD,2)))/Math.pow(nt,2)));
		
		//if total internal reflection occurs return the color in out intensity
		if(tiRef  < 0){
			//reflected view Ray  ??HOW 
			double r0 = Math.pow((n0-1),2)/Math.pow((n0+1),2);
			double rTheta = r0+(1-r0)*Math.pow((1-nDotD),5);
			
			Ray rRay = new Ray(record.location, r);
			RayTracer.shadeRay(outIntensity, scene, rRay, depth);
		}
		
        // 3) Compute the reflected ray and refracted ray (if total internal reflection does not occur)
        //    using Snell's law and call RayTracer.shadeRay on them to shade them
	
		// compute the reflected ray	
			//call raytracer shaderay on the reflected ray
		Ray rRay = new Ray(record.location, r);
		RayTracer.shadeRay(outIntensity, scene, rRay, depth);
		
		// compute the refracted ray
		Vector3d t = (d.sub(n.mul((d.dot(n)))).mul(n0).div(nt)).sub(n.mul((Math.sqrt(tiRef))));
		Ray tRay = new Ray(record.location, t);

			//call raytracer shaderay on the refracted ray
		RayTracer.shadeRay(outIntensity, scene, tRay, depth);
		
        
	}
}