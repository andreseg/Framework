package cs4620.anim;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import cs4620.common.Scene;
import cs4620.common.SceneObject;
import cs4620.common.event.SceneTransformationEvent;
import egl.math.Matrix3;
import egl.math.Matrix4;
import egl.math.Quat;
import egl.math.Vector3;

/**
 * A Component Resting Upon Scene That Gives Animation Capabilities
 * 
 * @author Cristian
 *
 */
public class AnimationEngine {
	/**
	 * The First Frame In The Global Timeline
	 */
	private int frameStart = 0;
	/**
	 * The Last Frame In The Global Timeline
	 */
	private int frameEnd = 100;
	/**
	 * The Current Frame In The Global Timeline
	 */
	private int curFrame = 0;
	/**
	 * Scene Reference
	 */
	private final Scene scene;
	/**
	 * Animation Timelines That Map To Object Names
	 */
	public final HashMap<String, AnimTimeline> timelines = new HashMap<>();

	/**
	 * An Animation Engine That Works Only On A Certain Scene
	 * 
	 * @param s
	 *            The Working Scene
	 */
	public AnimationEngine(Scene s) {
		scene = s;
	}

	/**
	 * Set The First And Last Frame Of The Global Timeline
	 * 
	 * @param start
	 *            First Frame
	 * @param end
	 *            Last Frame (Must Be Greater Than The First
	 */
	public void setTimelineBounds(int start, int end) {
		// Make Sure Our End Is Greater Than Our Start
		if (end < start) {
			int buf = end;
			end = start;
			start = buf;
		}

		frameStart = start;
		frameEnd = end;
		moveToFrame(curFrame);
	}

	/**
	 * Add An Animating Object
	 * 
	 * @param oName
	 *            Object Name
	 * @param o
	 *            Object
	 */
	public void addObject(String oName, SceneObject o) {
		timelines.put(oName, new AnimTimeline(o));
	}

	/**
	 * Remove An Animating Object
	 * 
	 * @param oName
	 *            Object Name
	 */
	public void removeObject(String oName) {
		timelines.remove(oName);
	}

	/**
	 * Set The Frame Pointer To A Desired Frame (Will Be Bounded By The Global
	 * Timeline)
	 * 
	 * @param f
	 *            Desired Frame
	 */
	public void moveToFrame(int f) {
		if (f < frameStart)
			f = frameStart;
		else if (f > frameEnd)
			f = frameEnd;
		curFrame = f;
	}

	/**
	 * Looping Forwards Play
	 * 
	 * @param n
	 *            Number Of Frames To Move Forwards
	 */
	public void advance(int n) {
		curFrame += n;
		if (curFrame > frameEnd)
			curFrame = frameStart + (curFrame - frameEnd - 1);
	}

	/**
	 * Looping Backwards Play
	 * 
	 * @param n
	 *            Number Of Frames To Move Backwards
	 */
	public void rewind(int n) {
		curFrame -= n;
		if (curFrame < frameStart)
			curFrame = frameEnd - (frameStart - curFrame - 1);
	}

	public int getCurrentFrame() {
		return curFrame;
	}

	public int getFirstFrame() {
		return frameStart;
	}

	public int getLastFrame() {
		return frameEnd;
	}

	public int getNumFrames() {
		return frameEnd - frameStart + 1;
	}

	/**
	 * Adds A Keyframe For An Object At The Current Frame Using The Object's
	 * Transformation - (CONVENIENCE METHOD)
	 * 
	 * @param oName
	 *            Object Name
	 */
	public void addKeyframe(String oName) {
		AnimTimeline tl = timelines.get(oName);
		if (tl == null)
			return;
		tl.addKeyFrame(getCurrentFrame(), tl.object.transformation);
	}

	/**
	 * Removes A Keyframe For An Object At The Current Frame Using The Object's
	 * Transformation - (CONVENIENCE METHOD)
	 * 
	 * @param oName
	 *            Object Name
	 */
	public void removeKeyframe(String oName) {
		AnimTimeline tl = timelines.get(oName);
		if (tl == null)
			return;
		tl.removeKeyFrame(getCurrentFrame(), tl.object.transformation);
	}

	/**
	 * Loops Through All The Animating Objects And Updates Their Transformations
	 * To The Current Frame - For Each Updated Transformation, An Event Has To
	 * Be Sent Through The Scene Notifying Everyone Of The Change
	 */

	// TODO A6 - Animation

	public void updateTransformations() {

		// Loop Through All The Timelines
		// And Update Transformations Accordingly
		// (You WILL Need To Use this.scene)
		for (Iterator it = this.timelines.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, AnimTimeline> pair = (Map.Entry<String, AnimTimeline>) it.next();
			String a1k = pair.getKey();
			AnimTimeline a1v = pair.getValue();
			System.out.println(a1k + " = " + a1v);

			// get pair of surrounding frames
			// (function in AnimTimeline)
			int frame = 0;
			AnimKeyframe[] outPair = new AnimKeyframe[2];
			a1v.getSurroundingFrames(curFrame, outPair);
			AnimKeyframe f1 = outPair[0];
			AnimKeyframe f2 = outPair[1];

			// get interpolation ratio
			float ratio = (float) getRatio(f1.frame, f2.frame, curFrame);
			float ratioMinus = (float) 1
					- getRatio(f1.frame, f2.frame, curFrame);
			// if min = max return 0

			Matrix4 xform1 = f1.transformation;
			Matrix4 xform2 = f2.transformation;

			// interpolate translations linearly
			Vector3 xform1vec = new Vector3();
			Vector3 xform2vec = new Vector3();
			xform1.getTrans(xform1vec);
			xform2.getTrans(xform2vec);
			// translation with lerp in vector 3
			xform1vec.lerp(xform2vec, ratio);
			Matrix4 xformTrans = Matrix4.createTranslation(xform1vec);

			// polar decompose axis matrices
			Matrix3 threeXform1 = new Matrix3(xform1);
			Matrix3 threeXform2 = new Matrix3(xform2);
			Matrix3 outR1 = new Matrix3();
			Matrix3 outS1 = new Matrix3();
			threeXform1.polar_decomp(outR1, outS1);
//			System.out.println("outR1 = \n" + outR1 + "\noutS1 = \n" + outS1);
			Matrix3 outR2 = new Matrix3();
			Matrix3 outS2 = new Matrix3();
			threeXform2.polar_decomp(outR2, outS2);
//			System.out.println("outR2 = \n" + outR2 + "\noutS2 = \n" + outS2);

			// slerp rotation matrix
			Quat q1 = new Quat(outR1);
			Quat q2 = new Quat(outR2);
			Quat q3 = Quat.slerp(q1, q2, ratio);

			// linearly interpolate scales
			Matrix3 interpScale = new Matrix3();
			interpScale.interpolate(outS1, outS2, ratio);

			// combine interpolated TRS
			Matrix4 xformScale = new Matrix4(interpScale);
			Matrix4 xformRot = new Matrix4();
			q3.toRotationMatrix(xformRot);
			
			Matrix4 matrixTRS = xformTrans.clone().mulBefore(xformRot).clone().mulBefore(xformScale);
			a1v.object.transformation.set(matrixTRS);
			
			
			
			// notification to scene that update has been made
			this.scene.sendEvent(new SceneTransformationEvent(a1v.object));
		}
	}

	public static float getRatio(int min, int max, int cur) {
		if (min == max)
			return 0f;
		float total = max - min;
		float diff = cur - min;
		return diff / total;
	}
}