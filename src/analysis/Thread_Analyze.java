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
package analysis;

import java.io.IOException;
import java.util.Observable;

import simulation.Simulation;
import simulation.SimulSetupParams.DimensionalityOptions;
import simulation.SimulSetupParams.FittableParametersOptions;

public class Thread_Analyze extends Observable implements Runnable  {

	private Simulation curSimul;
	public Thread_Analyze(Simulation curSimul) {
		this.curSimul = curSimul;
	}
	public void analyze() throws IOException {
		if(curSimul.keepRunning) {
			String excel = "";		
			
			Analysis a = new Analysis(sp.getFittingType());
			// set up zip printer?
			a.zipPrinter = zip;
			setChanged();
			notifyObservers("Analyzing...");
			FittableParametersOptions whichFixed = sp.getFittableParameterSelection();
			DimensionalityOptions dimensionalitySelection = sp.getDimensionalitySelection();
			a.setPrintStream(curSimul.getPrintStream());
			excel = a.analyze(curSimul, whichFixed, dimensionalitySelection);
			if(excel.compareTo("bad") != 0) {
				print(ps_analysis, excel);
			} else {
				println(ps_analysis, excel);
			}
			curSimul.getSample().setLatticeToNull();
			setChanged();
			notifyObservers("Writing simulation object to file...");
			curSimul.nullify();
			curSimul.deleteObservers();
			writeObject(objOut, curSimul);
		}
		numThreadsFinished++;
		checkIsDone();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
