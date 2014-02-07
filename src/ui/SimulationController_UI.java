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
package ui;


import io.PrintToZip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.zip.ZipException;

import analysis.Analysis;
import email.SendMailTLS;
import simulation.SimulSetupParams;
import simulation.DimensionalityOptions;
import simulation.FittableParametersOptions;
import simulation.Simulation;

public class SimulationController_UI extends Observable implements Observer {

	public SimulSetupParams sp;
	private File fName;
	private Simulation currentSimulation;
	private int desiredIterations,  whichFitting;
	private long totalVolume, currentVolume;
	private Vector<Simulation> simulations;
	private int simulReturnIndex = 0;
	private Executor e;
	private PrintStream ps_object, ps_analysis;
	private ObjectOutputStream objOut;
	private PrintToZip zip;
	private int totalThreads;
	private int numThreadsFinished = 0;
	private boolean isRunning = false;
	private boolean kill = false;
	public final static String UPDATE_PERCENT = "update percent";
	public final static String MESSAGE = "message: ";
	public final static String THREAD_COMPLETE = "thread complete";
	public final static String ALL_THREADS_COMPLETE = "all threads complete";
	public final static String ALL_THREADS_KILLED = "all simulations terminated";
	public SimulationController_UI(SimulSetupParams ssp) throws IOException {
		sp = ssp;
		initializeStreams();
		simulations = new Stack<Simulation>();
	}
	public long getTotalVolume() {
		if(totalVolume == 0) {
			return sp.getApproxVolume();
		}
		return totalVolume; 
	}
	public long getCurrentVolume() { return currentVolume; }
	
	public boolean isRunning() {
		return isRunning;
	}
	public void initializeStreams() {
		FileOutputStream fos;
		FileOutputStream fosObj;
		try {
			fos = new FileOutputStream(sp.getAnalysisFile());
			ps_analysis = new PrintStream(fos);
			
			fosObj = new FileOutputStream(sp.getObjectsFile());
			objOut = new ObjectOutputStream(fosObj);
			zip = sp.getZipPrinter_Fits();
		} catch (FileNotFoundException e) {
			sp.getFolderOutput();
			e.printStackTrace();
		} catch (IOException e) {
			sp.getFolderOutput();
			e.printStackTrace();
		}
	}
	private String longToTime(long elapsedSeconds) {
		long secondsPerMinute = 60;
		long secondsPerHour = secondsPerMinute * 60;
		long secondsPerDay = secondsPerHour * 24;
		long days = elapsedSeconds / secondsPerDay;
		elapsedSeconds -= days * secondsPerDay;
		long hours = elapsedSeconds / secondsPerHour;
		elapsedSeconds -= hours * secondsPerHour;
		long minutes = elapsedSeconds / secondsPerMinute;
		elapsedSeconds -= minutes * secondsPerMinute;
		long seconds = elapsedSeconds;
		return days + ":" + hours + ":" + minutes + ":" + seconds;
	}
	public void kill() {
		kill = true;
		if(simulations == null) {
			setChanged();
			notifyObservers(ALL_THREADS_KILLED);
		}
		for(int i = 0; i < simulations.size(); i++) {
			simulations.get(i).keepRunning = false;
		}
	}
	public Simulation getNextSimulation() {
		if(simulReturnIndex < simulations.size()) {
			return simulations.get(simulReturnIndex++);
		}
		return null;
	}
	public Simulation peekAtNextSimulation() {
		if(simulReturnIndex < simulations.size()) {
			return simulations.get(simulReturnIndex);
		}
		return null;
	}
	public void initialize() {
		isRunning = true;
		totalThreads = 0;
		SimulSetupParams ssp;
		initializeStreams();
		for(int i = 0; i < sp.getNumberOfSimulationsToRun(); i ++) {
			ssp = new SimulSetupParams();
			ssp.copy(sp);
			Simulation simul = new Simulation(ssp, i);
			simulations.add(simul);
			simul.addObserver(this);
			totalVolume = simul.getTotalVolume();
			totalThreads++;
		}
	}
	public void print(PrintStream ps, String toPrint) {
		ps.print(toPrint);
		ps.flush();
	}
	public void println(PrintStream ps, String toPrint) {
		ps.println(toPrint);
		ps.flush();
	}
	public void writeObject(ObjectOutputStream oos, Object o) throws IOException {
		oos.writeObject(o);
		oos.flush();
	}
	public void closeStreams() throws ZipException, IOException {
		zip.closeStream();
		objOut.close();
		ps_analysis.close();
		sp.nullify();
		for(Simulation simul : simulations)
			simul.closeStreams();
	}
	
	public void analyze(int index) throws IOException {
		Simulation curSimul = simulations.get(index);
		if(curSimul.keepRunning) {
			String excel = "";		
			
			setChanged();		
			notifyObservers(new String[] {MESSAGE, "Analyzing simulation..."});
			
			Analysis a = new Analysis(sp.getFittingType());
			// set up zip printer?
			a.zipPrinter = zip;
			FittableParametersOptions whichFixed = sp.getFittableParameterSelection();
			
			setChanged();
			notifyObservers(new String[] {MESSAGE, "Fitting with: " + whichFixed.name()});
			
			DimensionalityOptions dimensionalitySelection = sp.getDimensionalitySelection();
			
			setChanged();
			notifyObservers(new String[] {MESSAGE, "Fix n method: " + dimensionalitySelection.name()});
			
			a.setPrintStream(curSimul.getPrintStream());
			excel = a.analyze(curSimul, whichFixed, dimensionalitySelection);
			if(excel.compareTo("bad") != 0) {
				print(ps_analysis, excel);
			} else {
				println(ps_analysis, excel);
			}
			notifyObservers("Analysis complete. Written to file: " + sp.getAnalysisFile().getAbsolutePath());
			curSimul.getSample().setLatticeToNull();
			setChanged();
			notifyObservers("Writing simulation object to file: " + sp.getObjectsFile().getAbsolutePath());
			curSimul.nullify();
			curSimul.deleteObservers();
			writeObject(objOut, curSimul);
		}
		numThreadsFinished++;
		checkIsDone();
	}
	private void checkIsDone() {
		if(numThreadsFinished == totalThreads) {
			try {
				closeStreams();
			} catch (ZipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setChanged();
			if(kill) {
				isRunning = false;
				notifyObservers(ALL_THREADS_KILLED);
			} else {
				isRunning = false;
				notifyObservers(ALL_THREADS_COMPLETE);
			}
		}
	}
	@Override
	public void update(Observable arg0, Object arg1) {
		if(arg0 instanceof Simulation) {
			Simulation s = (Simulation) arg0;
			if(arg1 instanceof String[]) {
				String[] complexMessage = (String[]) arg1;
				if(complexMessage[0].compareTo(Simulation.SIMULATION_COMPLETE) == 0) {
					try {
						analyze(s.getSimulationIndex());
					} catch (IOException e) {
						e.printStackTrace();
					}
					setChanged();
					notifyObservers(new String[] {THREAD_COMPLETE, numThreadsFinished + ""});
				}  else if(complexMessage[0].compareTo(Simulation.UPDATE_PERCENT) == 0 && !kill) {
					setChanged();
					notifyObservers(new String[] {UPDATE_PERCENT, complexMessage[1] + ""});
				} else if(complexMessage[0].compareTo(Simulation.MESSAGE) == 0) {
					setChanged();
					notifyObservers(complexMessage);
				}
			} else if(arg1 instanceof String) {
				String message = (String) arg1;
				if(message.compareTo(Simulation.SIMULATION_KILLED) == 0) {
					numThreadsFinished++;
					checkIsDone();
				}
			}
		}
		
	}
}
