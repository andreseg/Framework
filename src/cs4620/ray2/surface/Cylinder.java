package cs4620.ray2.surface;

import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import egl.math.Vector3d;

public class Cylinder extends Surface {

	/** The center of the bottom of the cylinder x , y ,z components. */
	protected final Vector3d center = new Vector3d();

	public void setCenter(Vector3d center) {
		this.center.set(center);
	}

	/** The radius of the cylinder. */
	protected double radius = 1.0;

	public void setRadius(double radius) {
		this.radius = radius;
	}

	/** The height of the cylinder. */
	protected double height = 1.0;

	public void setHeight(double height) {
		this.height = height;
	}

	public Cylinder() {
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
		    Ray ray = untransformRay(rayIn);

		    // Rename the common vectors so I don't have to type so much
		    Vector3d d = ray.direction;
		    Vector3d c = center;
		    Vector3d o = ray.origin;

		    double tMin = ray.start, tMax = ray.end;
		    // Compute some factors used in computation
		    double qx = o.x - c.x;
		    double qy = o.y - c.y;
		    //double qz = o.z - c.z;
		    double rr = radius * radius;

		    double dd = d.x * d.x + d.y *d.y;
		    double qd = d.x * qx + d.y * qy;
		    double qq =  qx * qx + qy * qy;

		    double t = 0, td1=0, td2=0;
		    double zMin = c.z - height/2;
		    double zMax = c.z + height/2;

		    // z-plane cap calculations
		    if (d.z >= 0) {
		      td1 = (zMin- o.z) / d.z;
		      td2 = (zMax - o.z) / d.z;
		    }
		    else {
		      td1 = (zMax - o.z) / d.z;
		      td2 = (zMin - o.z) / d.z;
		    }
		    if (tMin > td2 || td1 > tMax)
		      return false;
		    if (td1 > tMin)
		      tMin = td1;
		    if (td2 < tMax)
		      tMax = td2;

		    // solving the quadratic equation for t at the pts of intersection
		    // dd*t^2 + (2*qd)*t + (qq-r^2) = 0
		    double discriminantsqr = (qd * qd - dd * (qq - rr));

		    // If the discriminant is less than zero, there is no intersection
		    if (discriminantsqr < 0) {
		      return false;
		    }

		    // Otherwise check and make sure that the intersections occur on the ray (t
		    // > 0) and return the closer one
		    double discriminant = Math.sqrt(discriminantsqr);
		    double t1 = (-qd - discriminant) / dd;
		    double t2 = (-qd + discriminant) / dd;

		    if (t1 > ray.start && t1 < ray.end) {
		      t = t1;
		    }
		    else if (t2 > ray.start && t2 < ray.end) {
		      t = t2;
		    }

		    Vector3d thit1 = new Vector3d(0); 
		    ray.evaluate(thit1, tMin);
		    Vector3d thit2 = new Vector3d(0); 
		    ray.evaluate(thit2, tMax);

		    double dx1 = thit1.x-c.x;  
		    double dy1 = thit1.y-c.y; 
		    double dx2 = thit2.x-c.x;  
		    double dy2 = thit2.y-c.y; 

		    if ((t < tMin || t > tMax) && dx1 * dx1 + dy1 * dy1 > rr && dx2 * dx2 + dy2 * dy2 > rr) {
		      return false;
		    }

		    // There was an intersection, fill out the intersection record
		    if (outRecord != null) {
		      double tside =Math.min( td1, td2);

		      if (t <tside) {
		        outRecord.t = tside;
		        ray.evaluate(outRecord.location, tside);
		        outRecord.normal.set(0, 0, 1);
		      }
		      else {
		        outRecord.t = t;
		        ray.evaluate(outRecord.location, t);        
		        outRecord.normal.set(outRecord.location.x, outRecord.location.y, 0).sub(c.x, c.y, 0);
		      }

		      if (outRecord.normal.dot(ray.direction) > 0)
		        outRecord.normal.negate();

		      outRecord.surface = this;

		      tMat.mulPos(outRecord.location);
		      tMatTInv.mulDir(outRecord.normal).normalize();
		    }

		    return true;
		  }

	public void computeBoundingBox() {
		// TODO#A7: Compute the bounding box and store the result in
		// averagePosition, minBound, and maxBound.
		// Hint: The bounding box may be transformed by a transformation matrix.
		
		//compute bounding box for the untransformed cylinder
		//transform that bounding box
		//compute bounding box for that box

		// Instatiate minBound, maxBound, averagePosition
		this.minBound = new Vector3d();
		this.maxBound = new Vector3d();
		this.averagePosition = new Vector3d();
		// Basic setup
		double centerX = this.center.x;
		double centerY = this.center.y;
		double centerZ = this.center.z;
		double rad = this.radius;
		double halfH = this.height/2;
		Vector3d min = new Vector3d(
				centerX - rad, 
				centerY - rad, 
				centerZ - halfH);
		Vector3d max = new Vector3d(
				centerX + rad, 
				centerY + rad, 
				centerZ + halfH);
		
		// Transform and set averagePosition
		Vector3d c = new Vector3d(centerX, centerY, centerZ);
		this.tMat.mulPos(c);
		this.averagePosition.set(c);
		
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
//		Vector3d tempAvg = new Vector3d(pt0.x, pt0.y, pt0.z);

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

//			// add current x, y, and z to cumulative tempAvg
//			tempAvg.add(tempCur);

		}

		// Set minBound and maxBound
		this.minBound.set(tempMin);
		this.maxBound.set(tempMax);

//		// Average the cumulative tempAvg
//		tempAvg.mul(1 / ptArray.length);
//		// Set averagePosition
//		this.averagePosition.set(tempAvg);
        
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Cylinder " + center + " " + radius + " " + height + " "
				+ shader + " end";
	}
}
