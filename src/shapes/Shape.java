package shapes;
import java.io.Serializable;
import java.util.Random;

import geometry.JVector;


public abstract class Shape implements Serializable {

	private static final long serialVersionUID = 5203336343450059462L;
	protected ShapeTypes type;
	// assumes that this is the center of the shape
	protected JVector location;
	protected JVector[] unitAxes;
	protected static volatile Random rand = new Random();
	
	public Shape(ShapeTypes type, JVector location, JVector[] unitAxes) {
		this.type = type;
		this.location = location;
		this.unitAxes = new JVector[unitAxes.length];
		for(int i = 0; i < unitAxes.length; i++) {
			this.unitAxes[i] = unitAxes[i].unit();
		}
	}
	public String toString() {
		String s = "\nType: " + type.toString();
		s += "\n\tOrigin: " + location.toString();
		s += "\n\tUnit Axes (Orientation):";
		char x = 'x';
		for(int i = 0; i < unitAxes.length; i++) {
			s += "\n\t\t" + x++ + ":\t" + unitAxes[i].toString();
		}
		
		return s;
	}
	public ShapeTypes getType() { return type; }
	public JVector getLocation() { return location; }
	public JVector[] getUnitAxes() { return unitAxes; }
	
	/**
	 * 
	 * @param curAxes <br> Cylindrical: [r,r,h]
	 * <br> Cubic: [a,a,a]
	 * <br> Tetragonal: [a,a,c]
	 * <br> Orthorhombic: [a,b,c]
	 * <br> Spherical: [r,r,r]
	 * @return
	 */
	public abstract JVector[] getPointsOnFace(double[] curAxes);
	
	/**
	 * 
	 * @param curAxes <br> Cylindrical: [r,r,h]
	 * <br> Cubic: [a,a,a]
	 * <br> Tetragonal: [a,a,c]
	 * <br> Orthorhombic: [a,b,c]
	 * <br> Spherical: [r,r,r]
	 * @return
	 */
	public abstract JVector getRandomPointInside(double[] curAxes);
	
	/**
	 * 
	 * @param curAxes <br> Cylindrical: [r,r,h]
	 * <br> Cubic: [a,a,a]
	 * <br> Tetragonal: [a,a,c]
	 * <br> Orthorhombic: [a,b,c]
	 * <br> Spherical: [r,r,r]
	 * @param loc
	 * @return
	 */
	public abstract boolean isInside(double[] curAxes, JVector loc);
	
	/**
	 * 
	 * @param curAxes <br> Cylindrical: [r,r,h]
	 * <br> Cubic: [a,a,a]
	 * <br> Tetragonal: [a,a,c]
	 * <br> Orthorhombic: [a,b,c]
	 * <br> Spherical: [r,r,r]
	 * @return
	 */
	public abstract int calcTotalVolume(double[] curAxes);
}
