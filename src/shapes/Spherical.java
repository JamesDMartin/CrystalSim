package shapes;

import java.util.Vector;

import geometry.JVector;

public class Spherical extends Shape {

	private static final long serialVersionUID = -6199770338914829647L;
	public Spherical(ShapeTypes type, JVector location, JVector[] unitAxes) {
		super(type, location, JVector.v100sU);
		// TODO Auto-generated constructor stub
	}

	@Override
	public JVector[] getPointsOnFace(double[] curAxes) {
		Vector<JVector> facePoints = new Vector<JVector>();
		double r = JVector.distance(location, JVector.add(location, new JVector(curAxes[0], 0, 0)));
		double rMin = r - Math.sqrt(3);
		
		int[][] inscribedCube = getInscribedCubeMinAndMax(rMin);
		int[] inscribedCubeMin = inscribedCube[0];
		int[] inscribedCubeMax = inscribedCube[1];

		int[][] circumscribedCube = getCircumscribedCubeMinAndMax(r);
		int[] circumscribedCubeMin = circumscribedCube[0];
		int[] circumscribedCubeMax = circumscribedCube[1];

		JVector curLoc = new JVector();
		double rCalc;
		for(int i = circumscribedCubeMin[0]; i < circumscribedCubeMax[0]; i++) {
			curLoc.i = i;
			for(int j = circumscribedCubeMin[1]; j < circumscribedCubeMax[1]; j++) {
				curLoc.j = j;
				for(int k = circumscribedCubeMin[2]; k < circumscribedCubeMax[2]; k++) {
					curLoc.k = k;
					rCalc = JVector.distance(curLoc, location);
					if(rCalc > rMin && rCalc < r) {
						facePoints.add((JVector) curLoc.clone());
					}
					if(k > inscribedCubeMin[2] && k < inscribedCubeMax[2] &&
							j > inscribedCubeMin[1] && j < inscribedCubeMax[1] &&
							i > inscribedCubeMin[0] && i < inscribedCubeMax[0]
									) { k = inscribedCubeMax[2]; }
				}
			}
		}
		
		
		JVector[] points = new JVector[facePoints.size()];
		points = facePoints.toArray(points);
		return points;
	}

	@Override
	public JVector getRandomPointInside(double[] curAxes) {
		double r;
		
		JVector loc = new JVector();
		
		r = JVector.distance(location, new JVector(location.i+curAxes[0], location.j, location.k));
		
		do {
			loc.i = rand.nextDouble()*2;
			loc.j = rand.nextDouble()*2;
			loc.k = rand.nextDouble()*2;
			
			loc.i -= 1;
			loc.j -= 1;
			loc.k -= 1;
			
			loc.i *= r;
			loc.j *= r;
			loc.k *= r;
			
			loc.i += location.i;
			loc.j += location.j;
			loc.k += location.k;
		} while(!isInside(curAxes, loc));
		
		return loc;
	}

	@Override
	public boolean isInside(double[] curAxes, JVector loc) {
		double r = JVector.distance(location, new JVector(location.i+curAxes[0], location.j, location.k));
		//JVector curLoc = new JVector(curAxes[0], curAxes[1], curAxes[2]);
		double curDist = JVector.distance(location, loc);
		if(curDist < r) { return true; }
		return false;
	}

	@Override
	public int calcTotalVolume(double[] curAxes) {
		double r = JVector.distance(location, JVector.add(location, new JVector(curAxes[0], 0, 0)));
		int[][] circumscribedCube = getCircumscribedCubeMinAndMax(r);
		int[] circumscribedCubeMin = circumscribedCube[0];
		int[] circumscribedCubeMax = circumscribedCube[1];

		JVector curLoc = new JVector();
		double rCalc;
		int volume = 0;
		for(int i = circumscribedCubeMin[0]; i < circumscribedCubeMax[0]; i++) {
			curLoc.i = i;
			for(int j = circumscribedCubeMin[1]; j < circumscribedCubeMax[1]; j++) {
				curLoc.j = j;
				for(int k = circumscribedCubeMin[2]; k < circumscribedCubeMax[2]; k++) {
					curLoc.k = k;
					rCalc = JVector.distance(curLoc, location);
					if(rCalc < r) {
						volume++;
					}
				}
			}
		}
		return volume;
	}
	
	private int[][] getInscribedCubeMinAndMax(double rMin) {
		double aSmall = rMin / Math.sqrt(3);
		JVector rSmall = new JVector(aSmall, aSmall, aSmall);
		int[] inscribedCubeMin = JVector.subtract(location, rSmall).toInt();
		int[] inscribedCubeMax = JVector.add(location, rSmall).toInt();
		int[][] minAndMax = {inscribedCubeMin, inscribedCubeMax};
		
		for(int i = 0; i < 3; i++) {
			if(inscribedCubeMin[i] > inscribedCubeMax[i] ||
					inscribedCubeMax[i] < inscribedCubeMin[i]) {
				throw new RuntimeException("\n\nCurrent location of this Shape (" + type + ") prohibits Volume or Surface determination" +
					" because inscribedCubeMin[" + i + "] = " + inscribedCubeMin[i] + " and inscribedCubeMax[" + i + 
					"] = " + inscribedCubeMax[i] + "\n");
			}
		}
		
		return minAndMax;
	}
	private int[][] getCircumscribedCubeMinAndMax(double r) {
		JVector rBig = new JVector(r, r, r);
		int[] circumscribedCubeMin = JVector.subtract(location, rBig).toInt();
		int[] circumscribedCubeMax = JVector.add(location, rBig).toInt();
		int[][] minAndMax = {circumscribedCubeMin, circumscribedCubeMax};
		
		for(int i = 0; i < 3; i++) {
			if(circumscribedCubeMin[i] > circumscribedCubeMax[i] ||
					circumscribedCubeMax[i] < circumscribedCubeMin[i]) {
				throw new RuntimeException("\n\nCurrent location of this Shape (" + type + ") prohibits Volume or Surface determination" +
					" because circumscribedCubeMin[" + i + "] = " + circumscribedCubeMin[i] + " and circumscribedCubeMax[" + i + 
					"] = " + circumscribedCubeMax[i] + "\n");
			}
		}
		return minAndMax;
	}
}
