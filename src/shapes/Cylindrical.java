package shapes;

import geometry.JVector;

public class Cylindrical extends Shape {

	private static final long serialVersionUID = -7583910939546454098L;

	/**
	 * 
	 * @param type
	 * @param location
	 * @param unitAxes First vector specifies the orientation axis of the cylinder (needs to be the z-axis)
	 */
	public Cylindrical(ShapeTypes type, JVector location, JVector[] unitAxes) {
		super(type, location, unitAxes);
		// TODO Auto-generated constructor stub
	}

	@Override
	public JVector[] getPointsOnFace(double[] curAxes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JVector getRandomPointInside(double[] curAxes) {
		// currently the implementation of this method only works if unitAxes[0] is JVector.z
		// curaxes[0] is the height of the cylinder
		double height = curAxes[2];
		// curaxes[1] is the radius of the cylinder
		double r = 2*curAxes[0];
		
		double z = rand.nextDouble() - 0.5;
		z *= height;
		z += location.k;
		
		double x, y;
		JVector pos = new JVector(0, 0, z);
		do {
			pos.i = rand.nextDouble() - 0.5;
			pos.j = rand.nextDouble() - 0.5;
			pos.i *= r;
			pos.j *= r;
			pos.i += location.i;
			pos.j += location.j;
		} while(!isInside(curAxes, pos));
		System.out.println(pos.toString(4));
		return pos;
	}

	@Override
	public boolean isInside(double[] curAxes, JVector loc) {
		// currently the implementation of this method only works if unitAxes[0] is JVector.z
		// curaxes[2] is the height of the cylinder
		double height = curAxes[2];
		// curaxes[0] is the radius of the cylinder
		double r = curAxes[0];
		double curZDist = loc.k - location.k;
		if(height <= Math.abs(curZDist)) {
			return false;
		}
		
		JVector xyLocProj = (JVector) loc.clone();
		JVector xyLocationProj = (JVector) location.clone();
		xyLocProj.k = 0;
		xyLocationProj.k = 0; 
		
		if(JVector.distance(xyLocProj, xyLocationProj) < r) {
			return true;
		}
		return false;
	}

	@Override
	public int calcTotalVolume(double[] curAxes) {
		double h = curAxes[2];
		double r = curAxes[0];
		int minX = (int) Math.round(location.i - r);
		int minY = (int) Math.round(location.j - r);
		int maxX = (int) Math.round(location.i + r);
		int maxY = (int) Math.round(location.j + r);
		
		JVector curLoc = new JVector(0, 0, 0);
		JVector centerXYProj = (JVector) location.clone();
		centerXYProj.k = 0;
		int volume = 0;
		for(int i = minX; i < maxX; i++) {
			curLoc.i = i;
			for(int j = minY; j < maxY; j++) {
				curLoc.j = j;
				if(JVector.distance(curLoc, centerXYProj) < r) {
					volume++;
				}
			}
		}
		volume = (int) Math.round(volume*h);
		return volume;
	}

}
