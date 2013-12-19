/*******************************************************************************
 * Copyright (c) 2013 Eric Dill -- eddill@ncsu.edu. North Carolina State University. All rights reserved.
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Eric Dill -- eddill@ncsu.edu - initial API and implementation
 * 	James D. Martin -- jdmartin@ncsu.edu - Principal Investigator
 ******************************************************************************/
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
