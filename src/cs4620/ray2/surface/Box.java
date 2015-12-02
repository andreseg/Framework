package cs4620.ray2.surface;

import java.util.ArrayList;
import java.util.Iterator;

import org.lwjgl.BufferUtils;

import cs4620.mesh.MeshData;
import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import edu.cornell.graphics.exr.ilmbaseto.Vector3;
import egl.math.Vector3d;
import egl.math.Vector4;
import egl.math.Vector4d;

/**
 * A class that represents an Axis-Aligned box. When the scene is built, the Box
 * is split up into a Mesh of 12 Triangles.
 * 
 * @author sjm324
 *
 */
public class Box extends Surface {

	/* The mesh that represents this Box. */
	private Mesh mesh;

	/* The corner of the box with the smallest x, y, and z components. */
	protected final Vector3d minPt = new Vector3d();

	public void setMinPt(Vector3d minPt) {
		this.minPt.set(minPt);
	}

	/* The corner of the box with the largest x, y, and z components. */
	protected final Vector3d maxPt = new Vector3d();

	public void setMaxPt(Vector3d maxPt) {
		this.maxPt.set(maxPt);
	}

	/* Generate a Triangle mesh that represents this Box. */
	private void buildMesh() {
		// Create the OBJMesh
		MeshData box = new MeshData();

		box.vertexCount = 8;
		box.indexCount = 36;

		// Add positions
		box.positions = BufferUtils.createFloatBuffer(box.vertexCount * 3);
		box.positions.put(new float[] { (float) minPt.x, (float) minPt.y,
				(float) minPt.z, (float) minPt.x, (float) maxPt.y,
				(float) minPt.z, (float) maxPt.x, (float) maxPt.y,
				(float) minPt.z, (float) maxPt.x, (float) minPt.y,
				(float) minPt.z, (float) minPt.x, (float) minPt.y,
				(float) maxPt.z, (float) minPt.x, (float) maxPt.y,
				(float) maxPt.z, (float) maxPt.x, (float) maxPt.y,
				(float) maxPt.z, (float) maxPt.x, (float) minPt.y,
				(float) maxPt.z });

		box.indices = BufferUtils.createIntBuffer(box.indexCount);
		box.indices.put(new int[] { 0, 1, 2, 0, 2, 3, 0, 5, 1, 0, 4, 5, 0, 7,
				4, 0, 3, 7, 4, 6, 5, 4, 7, 6, 2, 5, 6, 2, 1, 5, 2, 6, 7, 2, 7,
				3 });
		this.mesh = new Mesh(box);
		
		//set transformations and absorptioins
		this.mesh.setTransformation(this.tMat, this.tMatInv, this.tMatTInv);
		
		this.mesh.shader = this.shader;
	}

	public void computeBoundingBox() {
		// TODO#A7: NEEDS TESTING!! 
		// Compute the bounding box and store the result in
		// averagePosition, minBound, and maxBound.
		// Hint: The bounding box is not the same as just minPt and maxPt,
		// because
		// this object can be transformed by a transformation matrix.
		
		
		int numVert = 8;
		this.minBound = new Vector3d();
		this.maxBound = new Vector3d();
		this.averagePosition = new Vector3d();
		
		// you have to construct the box from the minPt and the maxPt
		
		Vector4d pt0 = new Vector4d(this.minPt.x, this.minPt.y, this.minPt.z, 1);
		Vector4d pt1 = new Vector4d(this.maxPt.x, this.minPt.y, this.minPt.z, 1);
		Vector4d pt2 = new Vector4d(this.maxPt.x, this.maxPt.y, this.minPt.z, 1);
		Vector4d pt3 = new Vector4d(this.minPt.x, this.maxPt.y, this.minPt.z, 1);
		Vector4d pt4 = new Vector4d(this.minPt.x, this.minPt.y, this.maxPt.z, 1);
		Vector4d pt5 = new Vector4d(this.maxPt.x, this.minPt.y, this.maxPt.z, 1);
		Vector4d pt6 = new Vector4d(this.maxPt.x, this.maxPt.y, this.maxPt.z, 1);
		Vector4d pt7 = new Vector4d(this.minPt.x, this.maxPt.y, this.maxPt.z, 1);
		
		this.tMat.mul(pt0);
		this.tMat.mul(pt1);
		this.tMat.mul(pt2);
		this.tMat.mul(pt3);
		this.tMat.mul(pt4);
		this.tMat.mul(pt5);
		this.tMat.mul(pt6);
		this.tMat.mul(pt7);
		
		Vector4d[] ptArray = { 
				pt0, pt1, pt2, pt3, 
				pt4, pt5, pt6, pt7
			};

		// Setup Base Case
		double tempX = ptArray[0].x;
		double tempY = ptArray[0].y;
		double tempZ = ptArray[0].z;
		
		// Basic setup for minBound
		double tempXmin = tempX;
		double tempYmin = tempY;
		double tempZmin = tempZ;
		// Basic setup for maxBound
		double tempXmax = tempX;
		double tempYmax = tempY;
		double tempZmax = tempZ;
		// Basic setup for averagePosition
		double tempXavg = tempX;
		double tempYavg = tempY;
		double tempZavg = tempZ;
		
		Vector4d temp4 = pt0;
		
		// Loop through remaining cases
		for (int i = 1; i < numVert; i++){

			tempX = ptArray[i].x;
			tempY = ptArray[i].y;
			tempZ = ptArray[i].z;
			// w is 1 for points
			temp4.set(tempX, tempY, tempZ, 1);
			// Transform Vertices
			
			// if new x is less than minBound.x, store new minBound.x
			if (temp4.x < tempXmin) {
				tempXmin = temp4.x;
			// else if new x is more than maxBound.x, store new maxBound.x
			} else if (temp4.x > tempXmax) {
				tempXmax = temp4.x;
			}

			// if new y is less than minBound.y, store new minBound.y
			if (temp4.y < tempYmin) {
				tempYmin = temp4.y;
			// else if new y is more than maxBound.y, store new maxBound.y
			} else if (temp4.y > tempYmax) {
				tempYmax = temp4.y;
			}

			// if new z is less than minBound.z, store new minBound.z
			if (temp4.z < tempZmin) {
				tempZmin = temp4.z;
			// else if new x is more than maxBound.x, store new maxBound.x
			} else if (temp4.z > tempZmax) {
				tempZmax = temp4.z;
			}
			
			tempXavg = tempXavg + tempX;
			tempYavg = tempYavg + tempY;
			tempZavg = tempZavg + tempZ;			
		}
				
		// Set minBound and maxBound 
// UNCLEAR - should we use this?
		this.minBound.set(tempXmin, tempYmin, tempZmin);
		this.maxBound.set(tempXmax, tempYmax, tempZmax);
        
		// Set average position
		tempXavg = tempXavg / numVert;
		tempYavg = tempYavg / numVert;
		tempZavg = tempZavg / numVert;
		this.averagePosition.set(tempXavg, tempYavg, tempZavg);
	}

	public boolean intersect(IntersectionRecord outRecord, Ray ray) {
		return false;
	}

	public void appendRenderableSurfaces(ArrayList<Surface> in) {
		buildMesh();
		mesh.appendRenderableSurfaces(in);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Box ";
	}

}