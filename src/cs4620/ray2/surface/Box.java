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
		
		// Setup Base Case
		double tempX = this.mesh.getPosition(0).x;
		double tempY = this.mesh.getPosition(0).y;
		double tempZ = this.mesh.getPosition(0).z;
		// w is 1 for points
		Vector4d temp4 = new Vector4d(tempX, tempY, tempZ, 1);
		// Transform Vertices
		this.tMat.mul(temp4);
		
		// Basic setup for minBound
		double tempXmin = temp4.x;
		double tempYmin = temp4.y;
		double tempZmin = temp4.z;
		// Basic setup for maxBound
		double tempXmax = temp4.x;
		double tempYmax = temp4.y;
		double tempZmax = temp4.z;
		// Basic setup for averagePosition
		double tempXavg = temp4.x;
		double tempYavg = temp4.y;
		double tempZavg = temp4.z;
		
		// Loop through remaining cases
		for (int i = 1; i < numVert; i++){
			temp4.setZero();

			tempX = this.mesh.getPosition(i).x;
			tempY = this.mesh.getPosition(i).y;
			tempZ = this.mesh.getPosition(i).z;
			// w is 1 for points
			temp4.set(tempX, tempY, tempZ, 1);
			// Transform Vertices
			this.tMat.mul(temp4);
			
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
			
			tempXavg = tempXavg + temp4.x;
			tempYavg = tempYavg + temp4.y;
			tempZavg = tempZavg + temp4.z;				
		}
				
		// Set minBound and maxBound 
		minBound.set(tempXmin, tempYmin, tempZmin);
		maxBound.set(tempXmax, tempYmax, tempZmax);
        
		// Set average position
		tempXavg = tempXavg / numVert;
		tempYavg = tempYavg / numVert;
		tempZavg = tempZavg / numVert;
		averagePosition.set(tempXavg, tempYavg, tempZavg);
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