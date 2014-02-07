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
import geometry.JVector;
import io.MyPrintStream;
import io.StringConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.Observable;
import java.util.Random;

import ui.SimulationController_UI;


public class Simulation extends Observable implements Runnable, Serializable {

	private static final long serialVersionUID = 3062285315959996919L;
	private static int numSimulations = 0;
	private SimulSetupParams sp;
	private CrystalFactory cf;
	private Sample sample;
	private Random r;
	private double time;
	private double fractionComplete;
	private double boomFactor = 1.5;
	public final static int FINISHED = 1;
	private final static int MAX_ATTEMPTS = 50;
	private boolean output;
	private MyPrintStream mps;
	private MyPrintStream mps_movie;
	public PrintStream ps1;
	public boolean keepRunning = true;
	public File xyz;
	private int simulationIndex;
	public final static String UPDATE_PERCENT = SimulationController_UI.UPDATE_PERCENT;
	public final static String MESSAGE = SimulationController_UI.MESSAGE;
	public final static String SIMULATION_COMPLETE = "simulation complete";
	public final static String SIMULATION_KILLED = " simulation killed";
	public Simulation(SimulSetupParams sp, int simulationIndex) {
		this.simulationIndex = simulationIndex;
		this.sp = sp;
		output = sp.isTransformed();
		sample = new Sample(sp.getSampleShape(), sp.getSampleUnitAxes(), sp.getSampleUnitsPerAxis());
		cf = new CrystalFactory(sample, sp);
		r = new Random();
		time = 0;
		fractionComplete = 0;
		if(output) {
			mps = new MyPrintStream(sp.getCurrentTransformationFile(simulationIndex));
		}
		if(sp.isOutputtingMovie())
			mps_movie = new MyPrintStream(sp.getMoviesFile());
	}
	
	@Override
	public void run() {
		if(output) {mps.print(sp.toString());}
		int totalTransformed = 0;
		int noGrowth = 0;
		double unoccupiedPercent;
		int[] newVolume;
		int unoccupied = 0;
		time = sp.getStartTime();
		for(int i = 0; i < sp.getNucInitVal(); i++) {
			int val = sample.registerNewCrystal(cf.getNewCrystal(time));
			//int[] axes = sample.getUnitsPerAxis();
			//int val = sample.registerNewCrystal(cf.getNewCrystal(time, new JVector(axes[0], axes[0], axes[2]/2)));
			while(val == Sample.NUCLEATION_INSIDE_ANOTHER) {
				val = sample.registerNewCrystal(cf.getNewCrystal(time));
				//val = sample.registerNewCrystal(cf.getNewCrystal(time, new JVector(axes[0], axes[0], axes[2]/2)));
			}
		}
		
		int lastPercent = 0;
		String msg;
		try {
			while(!sp.getTerm().terminate(sp, fractionComplete) && keepRunning) {// && noGrowth < 30) {
				int attempts = 0;
	 			if(nucleate()) {
	 				int val = sample.registerNewCrystal(cf.getNewCrystal(time));
	 				while(val == Sample.NUCLEATION_INSIDE_ANOTHER && attempts < MAX_ATTEMPTS) {
	 					val = sample.registerNewCrystal(cf.getNewCrystal(time));
	 	 				attempts++;
	 				}
	 				if(attempts == MAX_ATTEMPTS) {
	 					JVector location = sample.getRandomNucLoc();
	 					if(location != null)
	 						val = sample.registerNewCrystal(cf.getNewCrystal(time, location));
	 				}
				}	
	 			attempts = 0;
				sample.grow(time);
				newVolume = sample.check(time);
				//newVolume = sample.check_parallel_ByLattice(time);
				totalTransformed += newVolume[1];
				unoccupied = newVolume[2];
				if(newVolume[1] == 0) {
					noGrowth++;
				} else 
					noGrowth = 0;
				if(output) {mps.println(time + "\t" + StringConverter.arrayToTabString(newVolume));}
				fractionComplete = ( (double) totalTransformed ) / ( (double) newVolume[0] );
				unoccupiedPercent = ( (double) unoccupied ) / ( (double) newVolume[0] );
				setChanged();
				notifyObservers(new String[] {UPDATE_PERCENT, fractionComplete + ""});
	//			setChanged();
	//			notifyObservers(new String[] {"update", msg});
				if(sp.isOutputtingMovie()) {
					outputMovie();
//					outputMovie2(time);
				}
				time += sp.getTimeStep();
				setChanged();
				String message = "Current simulation time: " + time;
				message += "\tNumber of crystals: " + sample.getCrystalArray().length;
				notifyObservers(new String[] {MESSAGE, message});
			}
		} catch(Exception e) {
			keepRunning = false;
			String errormsg = StringConverter.arrayToNewLineString(e.getStackTrace());
			notifyObservers(new String[] {MESSAGE, "Error:" + e.getLocalizedMessage()});
		}
		if(!keepRunning) {
			setChanged();
			mps.close();
			notifyObservers(SIMULATION_KILLED);
		} else {
			if(output) {
				mps.print("\n\nSimulation Finished at: " + (new Date()).toString());
			}
			try {
				setChanged();
				notifyObservers(new String[] {MESSAGE, "Writing XYZ to file: " + sp.getCurrentXYZFile(simulationIndex).getAbsolutePath()});
				printXYZ();
			} catch(Exception e) {
				
			}
			if(output) {mps.print(finished());}
			//(new Analysis()).analyze(this);
			setChanged();
			notifyObservers(new String[] {SIMULATION_COMPLETE, simulationIndex + ""});
		}
	}
	public String finished() {
		String s = "\n\nCrystal Information: \n";
		Iterator<Crystal> iter = sample.getCrystalIterator();
		while(iter.hasNext()) {
			s += "\n" + iter.next().toString() + "\n";
		}
		
		return s;
	}
	private boolean nucleate() {
		
		if(sp.getNucContVal() > 0 && 
				cf.getNumCrystals() < (char) sp.getMaxNumCrystals() && 
				r.nextDouble() < (sp.getNucContVal() * sp.getTimeStep())) {
			return true;
		}
		return false;
	}
	
	public long getTotalVolume() { return sample.getTotalVolume(); }

	private void printXYZ() {
		if(sp.isOutputtingXyz()) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(sp.getCurrentXYZFile(simulationIndex));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			PrintStream ps = new PrintStream(fos);
			
			ps.println(sample.getTotalVolume());
			ps.println("0");
			sample.printXYZ(ps, false, boomFactor);
//			sample.explode(ps, boomFactor);
			ps.close();
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public double getTime() { return time; }
	public Sample getSample() { return sample; }
	public SimulSetupParams getSSP() { return sp; }
	
	public void setRepeatParameters(JVector[] nucLocations, 
			JVector[][] nucOrientations, 
			double[] nucTimes) {
		sp.setNucInitVal(0);
		sp.setNucContVal(0);
		for(int i = 0; i < nucLocations.length; i++) {
			sample.registerNewCrystal(cf.getNewCrystal(nucTimes[i], nucLocations[i], nucOrientations[i]));
		}
	}
	private void outputMovie() {
		PrintStream ps = mps_movie.getPrintStream();
		ps.println(sample.getTotalVolume());
		ps.println(idx++);
		sample.printXYZ(ps, sp.isOutputtingMovie());
	}
	private void outputMovie2(double time) {
		ps1.println(sample.getTotalVolume());
		ps1.println(" " + idx++);
		sample.printXYZ(ps1, sp.isOutputtingMovie());
	}
	private int idx = 0;
	public int getSimulationIndex() { return simulationIndex; }
	public void nullify() {
		closeStreams();
		sp.nullify();
		mps = null;
	}
	public void closeStreams() {
		if(output) {
			if(mps != null) {
				mps.close();
				mps = null;
			}
			if(mps_movie != null) {
				mps_movie.close();
				mps_movie = null;
			}
		}
	}

	public MyPrintStream getPrintStream() {
		return mps;
	}

	public void setPrintStream(MyPrintStream ps) {
		this.mps = ps;
	}
}
