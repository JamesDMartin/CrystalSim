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

public enum Termination {
	FractionComplete, TimeElapsed, NumberOfCrystals;
	
	public boolean terminate(SimulSetupParams sp, double val) {
		switch(sp.getTerm()) {
		case FractionComplete:
			if( val < (Double) sp.getTermVal()) {
				return false;
			}
			return true;
		case TimeElapsed:
			if( val < (Double) sp.getTermVal()) {
				return false;
			}
			return true;
		}
		throw new RuntimeException("Terminiation Parameter: " + sp.getTerm().toString() + " does not have a corresponding " +
				"option in the method: terminate(SimulParams sp, Object val) in the class: Termination");
	}
	
}
