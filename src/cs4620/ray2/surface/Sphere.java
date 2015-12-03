package cs4620.ray2.surface;

import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import egl.math.Vector3d;

/**
 * Represents a sphere as a center and a radius.
 *
 * @author ags
 */
public class Sphere extends Surface {

	/** The center of the sphere. */
	protected final Vector3d center = new Vector3d();

	public void setCenter(Vector3d center) {
		this.center.set(center);
	}

	/** The radius of the sphere. */
	protected double radius = 1.0;

	public void setRadius(double radius) {
		this.radius = radius;
	}

	protected final double M_2PI = 2 * Math.PI;

	public Sphere() {
	}

	/**
	 * Tests this surface for intersection with ray. If an intersection is found
	 * record is filled out with the information about the intersection and the
	 * method returns true. It returns false otherwise and the information in
	 * outRecord is not modified.
	 *
	 * @param outRecord
	 *            the output IntersectionRecord
	 * @param ray
	 *            the ray to intersect
	 * @return true if the surface intersects the ray
	 */
	public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
		// transform the ray into object space
		Ray ray = untransformRay(rayIn);

		// Rename the common vectors so I don't have to type so much
		Vector3d d = ray.direction;
		Vector3d c = center;
		Vector3d o = ray.origin;

		// Compute some factors used in computation
		double qx = o.x - c.x;
		double qy = o.y - c.y;
		double qz = o.z - c.z;
		double dd = d.lenSq();
		double qd = qx * d.x + qy * d.y + qz * d.z;
		double qq = qx * qx + qy * qy + qz * qz;

		// solving the quadratic equation for t at the pts of intersection
		// dd*t^2 + (2*qd)*t + (qq-r^2) = 0
		double discriminantsqr = (qd * qd - dd * (qq - radius * radius));

		// If the discriminant is less than zero, there is no intersection
		if (discriminantsqr < 0) {
			return false;
		}

		// Otherwise check and make sure that the intersections occur on the ray
		// (t
		// > 0) and return the closer one
		double discriminant = Math.sqrt(discriminantsqr);
		double t1 = (-qd - discriminant) / dd;
		double t2 = (-qd + discriminant) / dd;
		double t = 0;
		if (t1 > ray.start && t1 < ray.end) {
			t = t1;
		} else if (t2 > ray.start && t2 < ray.end) {
			t = t2;
		} else {
			return false; // Neither intersection was in the ray's half line.
		}

		// There was an intersection, fill out the intersection record
		if (outRecord != null) {
			outRecord.t = t;
			ray.evaluate(outRecord.location, t);
			outRecord.surface = this;
			outRecord.normal.set(outRecord.location).sub(center).normalize();
			double theta = Math.asin(outRecord.normal.y);
			double phi = Math.atan2(outRecord.normal.x, outRecord.normal.z);
			double u = (phi + Math.PI) / (2 * Math.PI);
			double v = (theta - Math.PI / 2) / Math.PI;
			outRecord.texCoords.set(u, v);

			// transform location and normal back to world space
			tMat.mulPos(outRecord.location);
			tMatTInv.mulDir(outRecord.normal);
		}

		return true;
	}

	public void computeBoundingBox() {
		// TODO#A7: NEEDS TESTING Compute the bounding box and store the result
		// in
		// averagePosition, minBound, and maxBound.

		// Basic setup
		this.minBound = new Vector3d();
		this.maxBound = new Vector3d();
		this.averagePosition = new Vector3d();
		Vector3d min = new Vector3d(
				this.center.x - this.radius, 
				this.center.y - this.radius, 
				this.center.z - this.radius);
		Vector3d max = new Vector3d(
				this.center.x + this.radius, 
				this.center.y + this.radius, 
				this.center.z + this.radius);

		// Construct a bounding box in object space from the mins and the maxs
		Vector3d pt0 = new Vector3d(min.x, min.y, min.z);
		Vector3d pt1 = new Vector3d(max.x, min.y, min.z);
		Vector3d pt2 = new Vector3d(min.x, max.y, min.z);
		Vector3d pt3 = new Vector3d(min.x, min.y, max.z);
		Vector3d pt4 = new Vector3d(max.x, max.y, min.z);
		Vector3d pt5 = new Vector3d(max.x, min.y, max.z);
		Vector3d pt6 = new Vector3d(min.x, max.y, max.z);
		Vector3d pt7 = new Vector3d(max.x, max.y, max.z);
		// Transform the eight points
		this.tMat.mulPos(pt0);
		this.tMat.mulPos(pt1);
		this.tMat.mulPos(pt2);
		this.tMat.mulPos(pt3);
		this.tMat.mulPos(pt4);
		this.tMat.mulPos(pt5);
		this.tMat.mulPos(pt6);
		this.tMat.mulPos(pt7);

		Vector3d[] ptArray = { pt0, pt1, pt2, pt3, pt4, pt5, pt6, pt7 };

		// Basic setup for minBound, maxBound, averagePosition
		// Set everything to pt0
		Vector3d tempCur = new Vector3d(pt0.x, pt0.y, pt0.z);
		Vector3d tempMin = new Vector3d(pt0.x, pt0.y, pt0.z);
		Vector3d tempMax = new Vector3d(pt0.x, pt0.y, pt0.z);
		Vector3d tempAvg = new Vector3d(pt0.x, pt0.y, pt0.z);

		// Loop through remaining cases
		for (int i = 1; i < ptArray.length; i++) {

			tempCur.set(ptArray[i].x, ptArray[i].y, ptArray[i].z);

			// if current x is less than tempMin.x, replace tempMin.x
			if (tempCur.x < tempMin.x) {
				tempMin.x = tempCur.x;
				// else if current x is more than tempMax.x, replace tempMax.x
			} else if (tempCur.x > tempMax.x) {
				tempMax.x = tempCur.x;
			}

			// if current y is less than tempMin.y, replace tempMin.y
			if (tempCur.y < tempMin.y) {
				tempMin.y = tempCur.y;
				// else if current y is more than tempMax.y, replace tempMax.y
			} else if (tempCur.y > tempMax.y) {
				tempMax.y = tempCur.y;
			}

			// if current z is less than tempMin.z, replace tempMin.z
			if (tempCur.z < tempMin.z) {
				tempMin.z = tempCur.z;
				// else if current z is more than tempMax.z, replace tempMax.z
			} else if (tempCur.z > tempMax.z) {
				tempMax.z = tempCur.z;
			}

			// add current x, y, and z to cumulative tempAvg
			tempAvg.add(tempCur);

		}

		// Set minBound and maxBound
		this.minBound.set(tempMin);
		this.maxBound.set(tempMax);

		// Average the cumulative tempAvg
		tempAvg.mul(1 / ptArray.length);
		// Set averagePosition
		this.averagePosition.set(tempAvg);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "sphere " + center + " " + radius + " " + shader + " end";
	}

}