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
package io;

import geometry.JVector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;

import simulation.Crystal;
import simulation.Sample;
import simulation.SimulSetupParams;
import simulation.Simulation;

public class ReadSimulationFileCreateXYZMovieOutput {

	private File fName;
	
	public ReadSimulationFileCreateXYZMovieOutput(File fName) {
		this.fName = fName;
	}
	
	public void run() throws FileNotFoundException {
		FileInputStream fis;
		ObjectInputStream ois = null;
		
		try {
			fis = new FileInputStream(fName);
			ois = new ObjectInputStream(fis);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Object o = null;
		try {
			o = ois.readObject(); 
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(o instanceof Simulation) {
			Simulation simul = (Simulation) o;
			Sample samp = simul.getSample();
			Crystal[] cryst = samp.getCrystalArray();
			JVector[] nucleationLocations = new JVector[cryst.length];
			JVector[][] nucleationOrientations = new JVector[cryst.length][3];
			double[] nucleationTimes = new double[cryst.length];
			
			for(int i = 0; i < cryst.length; i++) {
				nucleationLocations[i] = cryst[i].getNucLoc();
				nucleationOrientations[i] = cryst[i].getNucOrient();
				nucleationTimes[i] = cryst[i].getNucTime();
			}
			SimulSetupParams ssp = simul.getSSP();
			ssp.setMovie(true);
			Simulation simul2 = new Simulation(ssp, simul.getSimulationIndex());
			simul2.setRepeatParameters(nucleationLocations, nucleationOrientations, nucleationTimes);
			FileOutputStream fos = new FileOutputStream("movie.xyz");
			simul2.ps1 = new PrintStream(fos);
			simul2.run();
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		File f = new File("C:\\$research\\programming\\workspace\\Kinetics3\\obj files\\NSLS_5mm1.0.obj");
		ReadSimulationFileCreateXYZMovieOutput obj = new ReadSimulationFileCreateXYZMovieOutput(f);
		obj.run();
	}
}
