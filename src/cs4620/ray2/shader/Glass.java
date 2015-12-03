package cs4620.ray2.shader;

import cs4620.ray2.RayTracer;
import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import cs4620.ray2.Scene;
import egl.math.Color;
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
		
		//initialize an empty color variable 
		//to cumulate the colors from the rays
		Colord cumColor = new Colord(0,0,0);
		Colord refrColor = new Colord(0,0,0);
		Colord reflColor = new Colord(0,0,0);

		//get the normal of the surface intersected
		Vector3d n = record.normal;

		//get the direction of the incoming view ray
		Vector3d d = ray.direction;
		
		//find dot product between the 2 vectors
		n.normalize();
		d.normalize();
		double nDotD = n.dot(d);

		//get the reflected ray
		Vector3d r = d.clone().sub(n.clone().mul(nDotD*2));

		//if the dot product is less than zero 
		double nt; //refraction index of second (i.e. exit) material
		double n0; //refractive index of first material
		
		//increment depth
		depth++;

		if(nDotD < 0){
			//then the ray is traveling out from glass to air
			nt = 1.0;
//			n0 = refractiveIndex;
			n0 = 1.5;


		} else {
			//if the dot product is greater than zero
			//then the ray is traveling in from air to material
//			nt = refractiveIndex;
			nt = 1.5;
			n0 = 1.0;
			n.negate();

		}

		// 2) Determine whether total internal reflection occurs.

		//if the number under the square root (tiRef) is negative then total internal reflection occurs
		//nt is the refractive index of the material you're traveling in to
		double tiRef = 1 - (
				(n0*n0*(1 - (nDotD*nDotD)))/
				(nt*nt));

		//if total internal reflection occurs return the color in out intensity
		if(tiRef  < 0){
			Ray rRay = new Ray(record.location, r);
			rRay.makeOffsetRay();
			RayTracer.shadeRay(outIntensity, scene, rRay, depth);
		} else {

			// 3) Compute the reflected ray and refracted ray (if total internal reflection does not occur)
			//    using Snell's law and call RayTracer.shadeRay on them to shade them

			// compute the reflected ray	
			//call raytracer shaderay on the reflected ray
			Ray rRay = new Ray(record.location, r);
			rRay.makeOffsetRay();
			RayTracer.shadeRay(reflColor, scene, rRay, depth);
			
			// compute the refracted ray
			// -----MATH------
			double DdotN = d.dot(n);
			Vector3d NDdotN = n.clone().mul(DdotN);
			Vector3d DminusNDdotN = d.clone().sub(NDdotN);
			DminusNDdotN.mul(n0);
			DminusNDdotN.div(nt);
			Vector3d N_sqrt_tiref = n.clone().mul(Math.sqrt(tiRef));
			DminusNDdotN.sub(N_sqrt_tiref);
			// -----MATH------
			
			//call raytracer shaderay on the reflected ray
			Ray tRay = new Ray(record.location, DminusNDdotN);
			tRay.makeOffsetRay();
			RayTracer.shadeRay(refrColor, scene, tRay, depth);

			//add the colors from the rays
			reflColor.mul(.5);
			refrColor.mul(.5);
//			cumColor.add(reflColor);
			cumColor.add(refrColor);
			outIntensity.set(cumColor);
		}


	}
}