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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class WriteSimulParams {

	public static void main(String[] args) throws IOException {
		FileOutputStream fos = new FileOutputStream("ssp\\1.ssp");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		int stride = 50;
		SimulSetupParams ssp = new SimulSetupParams();
		int units;
		String s;
		for(int i = 1; i < 8; i++) {
			units = stride;
			ssp.setSampleUnitsPerAxis(new int[] {units, units, units});
			s = "ssp\\" + i + ".ssp";
			fos = new FileOutputStream(s);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ssp);
		}
		
	}
}
