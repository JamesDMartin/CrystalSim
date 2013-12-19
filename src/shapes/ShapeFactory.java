package shapes;

import java.io.Serializable;

import geometry.JVector;

public class ShapeFactory implements Serializable {

	private static final long serialVersionUID = -6517136892188463749L;

	public ShapeFactory() {
		
	}
	
	public static Shape newShape(ShapeTypes type, JVector location, JVector[] unitAxes) {
		
		switch(type) {
		case Cubic:
			return new Cubic(type, location, unitAxes);
		case Tetragonal:
			return new Tetragonal(type, location, unitAxes);
		case Orthorhombic:
			return new Orthorhombic(type, location, unitAxes);
		case Spherical:
			return new Spherical(type, location, unitAxes);
		case Cylindrical:
			return new Cylindrical(type, location, unitAxes);
		}
		throw new RuntimeException("Shape Type: " + type.toString() + " does not have a corresponding " +
				"option in the method: newShape(ShapeTypes type, JVector location, JVector[] unitAxes) " + 
				"in the class: ShapeFactory");
	}
}
