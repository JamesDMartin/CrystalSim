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
package simulation;
import java.io.Serializable;

import geometry.JVector;


public class NucleationEvent implements Serializable {

	private static final long serialVersionUID = 4290812620559138427L;
	private double time;
	private JVector location;
	
	public NucleationEvent(double time, JVector location) {
		this.time = time;
		this.location = location;
	}
	
	public double getTime() { return time; }
	public JVector getLocation() { return location; }
}
