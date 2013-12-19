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

import email.SendMailTLS;
import geometry.JVector;
import io.StringConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Vector;

import analysis.Analysis;


import simulation.ExperimentalSample;
import simulation.NucleationLocation;
import simulation.NucleationOrientation;
import simulation.SimulSetupParams;
import simulation.Simulation;

public class SimulationController {

	public SimulSetupParams sp;
	private File fName;
	private int sampleGeometry, runDuration, analysisMethod, desiredIterations, whichAspectRatio, volume, whichFitting;
	private double[] alphaBounds;
	public SimulationController() {
		sp = new SimulSetupParams();
		try {
			initialize();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private int whichFitting() {
		System.out.println("Which fitting type?");
		System.out.println("(1) JMAK. (2) Sharp-Hancock.");
		Scanner s = new Scanner(System.in);
		int intVal = s.nextInt();
		switch(intVal) {
		case 1:
		case 2:
			return intVal;
		default:
			System.out.println("Selection: " + intVal + " is not valid.  Please try again.");
			return whichFitting();
		}
	}
	private int whichMethod() {
		System.out.println("Which type of simulation geometries would you like?");
		System.out.println("(1) All Experimental Geometries?\n(2) Select Experimental Geometries?\n(3) Aspect Ratio Variation?");
		Scanner s = new Scanner(System.in);
		int intVal = s.nextInt();
		switch(intVal) {
		case 1:
		case 2: 
			return intVal;
		case 3:
			whichAspectRatio = whichAspectRatio();
			return 3;
		default:
			System.out.println("Selection: " + intVal + " is not valid.  Please try again.");
			return whichMethod();
		}
	}
	private int whichAspectRatio() {
		System.out.println("Which type of simulation geometries would you like?");
		System.out.println("(1) Single crystallite aligned with the cylindrical faces and nucleated in center?" + 
					"\n(2) Single crystallite aligned with the cylindrical faces and randomly nucleated?" + 
					"\n(3) Multiple crystallites aligned with the cylindrical faces and randomly nucleated?" + 
					"\n(4) Single crystallite randomly aligned and nucleated in center?" + 
					"\n(5) Single crystallites randomly aligned and randomly nucleated?" +
					"\n(6) Multiple crystallites randomly aligned and randomly nucleated?" +
					"\n(7) Alpha Range Test (basically (1), but the bounds of alpha are modified based on the user input)?");
		Scanner s = new Scanner(System.in);
		int intVal = s.nextInt();
		switch(intVal) {
		case 1:
		case 2: 
		case 3:
		case 4:
		case 5:
		case 6:
			return intVal;
		case 7:
			alphaRangeTest();
			return 7;
		default:
			System.out.println("Selection: " + intVal + " is not valid.  Please try again.");
			return whichMethod();
		}
	}
	private boolean runAlphaRangeTest() {
		System.out.println("Perform the alpha range test? (1) Yes\n(2) No.");
		Scanner s = new Scanner(System.in);
		switch(s.nextInt()) {
		case 1:
			return true;
		case 2:
			return false;
		default:
			System.out.println("Please enter either (1) for Yes or (2) for No.");
			return runAlphaRangeTest();
		}
	}
	private void alphaRangeTest() {
		System.out.println("Input the following numbers as fractions of 100 with a space between them: alpha_min_min alpha_min_max alpha_min_step");
		Scanner s = new Scanner(System.in);
		double alpha_min_min = s.nextDouble();
		double alpha_min_max = s.nextDouble();
		double alpha_min_step = s.nextDouble();
		

		System.out.println("Input the following numbers as fractions of 100 with a space between them: alpha_max_min alpha_max_max alpha_max_step");
		double alpha_max_min = s.nextDouble();
		double alpha_max_max = s.nextDouble();
		double alpha_max_step = s.nextDouble();
		
		alphaBounds = new double[] {alpha_min_min, alpha_min_max, alpha_min_step, alpha_max_min, alpha_max_max, alpha_max_step};
	}
	private int whichDuration() {
		desiredIterations = 100;
		System.out.println("(1) set number of iterations?\n(2) set length?\n(3)Just run once.");
		Scanner s = new Scanner(System.in);
		int intVal = s.nextInt();
		switch(intVal) {
		case 1:
			System.out.println("How many iterations?");
			desiredIterations = s.nextInt();
			System.out.println(desiredIterations + " iterations");
			return 1;
		case 2: 
			System.out.println("How long do you want this to run for? (in hours)");
			desiredIterations = s.nextInt();
			System.out.println(desiredIterations + " hours");
			return 2;
		case 3:
			desiredIterations = 1;
			return 3;
		default:
			System.out.println("Selection: " + intVal + " is not valid.  Please try again.");
			return whichDuration();
		}
	}
	private int whichAnalysis() {
		System.out.println("Which type of simulation geometries would you like?");
		System.out.println("(1) Fit kA & t0?\n(2) Fit kA & n?\n(3) First fit kA & t0, then fit kA & n?");
		Scanner s = new Scanner(System.in);
		int intVal = s.nextInt();
		switch(intVal) {
		case 1:
		case 2: 
		case 3:
			return intVal;
		default:
			System.out.println("Selection: " + intVal + " is not valid.  Please try again.");
			return whichAnalysis();
		}
	}
	private boolean test() {
		System.out.println("Do you want to test one iteration of your simulation to estimate the time to completion?");
		System.out.println("(1) Yes.\n(2) No.\n");
		Scanner s = new Scanner(System.in);
		int intVal = s.nextInt();
		switch(intVal) {
		case 1:
			return true;
		case 2: 
			return false;
		default:
			System.out.println("Selection: " + intVal + " is not valid.  Please try again.");
			return test();
		}
	}
	private boolean keepRunning() {
		System.out.println("Continue?\n(1) Yes.\n(2) No.\n");
		Scanner s = new Scanner(System.in);
		int intVal = s.nextInt();
		switch(intVal) {
		case 1:
			return true;
		case 2: 
			return false;
		default:
			System.out.println("Selection: " + intVal + " is not valid.  Please try again.");
			return test();
		}
	}
	private String testTime() throws IOException {
		long startMillis;
		long finishMillis;
		long elapsedSeconds;
		String time;
		System.out.println("Testing the iterations to determine approximate simulation length:");
		startMillis = System.currentTimeMillis();
		switch(sampleGeometry) {
		case 1:
			simulate(1);
			break;
		case 2:
			System.out.println("Individual experimental geometries not yet implemented.  Defaulting to all six.");
			simulate(1);
			break;
		case 3:
			simulateAspectRatioVariation(1);
			break;
		}
		
		finishMillis = System.currentTimeMillis();
		elapsedSeconds = finishMillis - startMillis;
		elapsedSeconds /= 1000;
		switch(runDuration) {
		case 2:
			desiredIterations = Math.round((desiredIterations * 60 * 60) / elapsedSeconds);
			break;
		}
		time = longToTime(desiredIterations * elapsedSeconds);
		return time;
	}
	private void run() throws IOException {
		switch(sampleGeometry) {
		case 1:
			simulate(desiredIterations);
			break;
		case 2:
			System.out.println("Individual experimental geometries not yet implemented.  Defaulting to all six.");
			simulate(desiredIterations);
			break;
		case 3:
			simulateAspectRatioVariation(desiredIterations);
			break;
		}
	}
	private int howBig() {
		System.out.println("How large do you want the sample volume to be? (Integers only, please!)");
		Scanner s = new Scanner(System.in);
		return s.nextInt();
	}
	public void initialize() throws IOException, ClassNotFoundException {
		long startMillis, finishMillis, elapsedSeconds;
		String time;
		
		sampleGeometry = whichMethod();
		
		runDuration = whichDuration();
		
		analysisMethod = whichAnalysis();

		volume = howBig();
		
		whichFitting = whichFitting();
		
		if(test()) { 
			time = testTime(); 
			System.out.println("Estimated time to finish " + desiredIterations + " iterations: " + time);
			if(keepRunning()) {
				System.out.println("O.k.  Continuing...");
			} else {
				System.out.println("Exiting...");
				System.exit(1);
			}
		}
		startMillis = System.currentTimeMillis();
		run();
		
		
		finishMillis = System.currentTimeMillis();
		elapsedSeconds = finishMillis - startMillis;
		elapsedSeconds /= 1000;
		time = longToTime(desiredIterations * elapsedSeconds);
		System.out.println("Total time elapsed: " + time);
		SendMailTLS.send("eddill@ncsu.edu", "Simulation results", "Simulation results", new File[] {fName});
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
	private int[][] getAxes(int volume) {
		Vector<int[]> v = new Vector<int[]>();
		
		int h, r;
		int[] axes;
		h = 1;
		do {
			axes = getSampleAxes(ExperimentalSample.CUSTOM_H, volume, h++);
			v.add(axes);
		} while(h <= axes[0]);
		
		r=h;

		do {
			axes = getSampleAxes(ExperimentalSample.CUSTOM_R, volume, r--);
			v.add(axes);
		} while(r >= 1);
		
		int[][] allAxes = new int[v.size()][3];
		allAxes = v.toArray(allAxes);
		return allAxes;
	}
	private void simulateAspectRatioVariation(int iterations) throws IOException {
		Simulation s;
		String excel = "";		
		fName = new File("Compiled Excel Automated Analysis.auto1");
		FileOutputStream fos = new FileOutputStream(fName);
		FileOutputStream fosObj;
		ObjectOutputStream objOut;
		PrintStream ps = new PrintStream(fos);
		SimulSetupParams ssp = new SimulSetupParams();
		
		int numXYZOutput = 0;
		int volumeIncrement = 1;//(int) Math.pow(10, 3);
		ssp.setVolumeIncrement(volumeIncrement);
		int[][] sampleAxes = getAxes(volume);
		ExperimentalSample[] expType = new ExperimentalSample[sampleAxes.length];
		expType[0] = ExperimentalSample.APS_7mm;
		for(int i = 0; i < sampleAxes.length; i++) {
			expType[i] = ExperimentalSample.ASPECT_RATIO_VARIATION;
		}
		ssp.setTransformed(true);
		ssp.setXyz(false);
		ssp.setFit(true);
		ssp.setMovie(false);
		ssp.setAxialGrowthRates(new double[] {1., 1., 1.});
		double[] alphaMin = new double[] {ssp.getAlphaMin()};
		double[] alphaMax = new double[] {ssp.getAlphaMax()};
		if(alphaBounds != null) {
			int numMin = (int) Math.round((alphaBounds[1] - alphaBounds[0]) / alphaBounds[2]);
			int numMax = (int) Math.round((alphaBounds[4] - alphaBounds[3]) / alphaBounds[5]);
			double alphaIncrementer = alphaBounds[0];
			int idx = 0;
			
			alphaMin = new double[numMin];
			alphaMax = new double[numMax];
			
			while(alphaIncrementer < alphaBounds[1]) {
				alphaMin[idx++] = alphaIncrementer;
				alphaIncrementer += alphaBounds[2];
			}

			alphaIncrementer = alphaBounds[3];
			idx = 0;
			while(alphaIncrementer < alphaBounds[4]) {
				alphaMax[idx++] = alphaIncrementer;
				alphaIncrementer += alphaBounds[5];
			}
		}
		for(int min = 0; min < alphaMin.length; min++) {
			for(int max = 0; max < alphaMax.length; max++) {
				if(alphaMin[min] >= alphaMax[max]) { continue; }
				for(int e = 0; e < sampleAxes.length; e++) {
					fosObj = new FileOutputStream(expType[e].toString() + "_" + 
							sampleAxes[e][0] + "_" + sampleAxes[e][1] + "_" + sampleAxes[e][2] + ".obj");
					objOut = new ObjectOutputStream(fosObj);
					ssp.setSampleUnitsPerAxis(sampleAxes[e]);
					System.out.println("Sample axes: " + StringConverter.arrayToTabString(ssp.getSampleUnitsPerAxis()));
					ssp.setExp(expType[e]);
					ssp.setExpVal(volume);
					switch(expType[e]) {
					case DSC:
						ssp.setTimeStep(4);
						break;
					default:
						ssp.setTimeStep(1);
						break;
					}
					JVector nucLoc;
					switch(whichAspectRatio) {
					// single crystal aligned and in center
					case 1:
						ssp.setNucContVal(0);
						ssp.setNucOrient(NucleationOrientation.SetOfOrientations);
						ssp.setNucOrientVal(JVector.axes100U);
						ssp.setNucLoc(NucleationLocation.Fixed);
						nucLoc = new JVector(sampleAxes[e]);
						nucLoc.k /= 2;
						ssp.setNucLocOptions(new JVector[] {nucLoc});
						break;
					// single crystal aligned and not in center
					case 2:
						ssp.setNucContVal(0);
						ssp.setNucOrient(NucleationOrientation.SetOfOrientations);
						ssp.setNucOrientVal(JVector.axes100U);
						ssp.setNucLoc(NucleationLocation.Random);
						break;
		
					// multiple crystals aligned and not in center
					case 3:
						ssp.setNucContVal(0.025);
						ssp.setNucOrient(NucleationOrientation.SetOfOrientations);
						ssp.setNucOrientVal(JVector.axes100U);
						ssp.setNucLoc(NucleationLocation.Random);
						break;
					// single crystal not aligned and in center
					case 4:
						ssp.setNucContVal(0);
						ssp.setNucOrient(NucleationOrientation.Random);
						ssp.setNucLoc(NucleationLocation.Fixed);
						nucLoc = new JVector(sampleAxes[e]);
						nucLoc.k /= 2;
						ssp.setNucLocOptions(new JVector[] {nucLoc});
						break;
					// single crystal not aligned and not in center
					case 5:
						ssp.setNucContVal(0);
						ssp.setNucOrient(NucleationOrientation.Random);
						ssp.setNucLoc(NucleationLocation.Random);
						break;
					// multiple crystals not aligned and not in center
					case 6:
						ssp.setNucContVal(0.025);
						ssp.setNucOrient(NucleationOrientation.Random);
						ssp.setNucLoc(NucleationLocation.Random);
						break;
					// alphaRangeTest
					case 7:
						ssp.setNucContVal(0);
						ssp.setNucOrient(NucleationOrientation.SetOfOrientations);
						ssp.setNucOrientVal(JVector.axes100U);
						ssp.setNucLoc(NucleationLocation.Fixed);
						nucLoc = new JVector(sampleAxes[e]);
						nucLoc.k /= 2;
						ssp.setNucLocOptions(new JVector[] {nucLoc});
						ssp.setAlphaMin(alphaMin[min]);
						ssp.setAlphaMax(alphaMax[max]);
						analysisMethod = 4;
						break;
					}
					for(int i = 0; i < iterations ; i++) {
						if(i < numXYZOutput) {
							ssp.setXyz(true);
						} else {
							ssp.setXyz(false);
						}
						ssp.newFileNames();
						s = new Simulation(ssp);
						s.run();
						switch(analysisMethod) {
						case 1:
							excel = (new Analysis(whichFitting)).analyzeWithOneFixed(s);
							break;
						case 2:
							excel = (new Analysis(whichFitting)).analyzeFixedt0(s);
							break;
						case 3:
							excel = (new Analysis(whichFitting)).analyzeWithOneFixed(s);
							excel += (new Analysis(whichFitting)).analyzeFixedt0(s);
							break;
						case 4:
							excel = (new Analysis(whichFitting)).analyzeFixedAll(s);
						}
						if(excel.compareTo("bad") != 0) {
							ps.print(excel);
						} else {
							ps.println(excel);
						}
						
						ps.flush();
						fos.flush();
						s.getSample().setLatticeToNull();
						objOut.writeObject(s);
						System.out.print("Set: " + (e+1) + "/" + expType.length + " with: " + iterations + " iterations ");
					}
				}
			}
		}
	}
	private void simulate(int iterations) throws IOException {
		Simulation s;
		String excel = "";		
		fName = new File("Compiled Excel Automated Analysis.auto1");
		FileOutputStream fos = new FileOutputStream(fName);
		FileOutputStream fosObj;
		ObjectOutputStream objOut;
		PrintStream ps = new PrintStream(fos);
		SimulSetupParams ssp = new SimulSetupParams();
		
		int numXYZOutput = 0;
		int volumeIncrement = 1;//(int) Math.pow(10, 3);
		ssp.setVolumeIncrement(volumeIncrement);
		int volVal = 1;
		Vector<Integer> volVals = new Vector<Integer>();
		int volStep = 1;
		int initialH = 1;
		//while(initialH < 125) {
		//	volVals.add(initialH++);
		//}
		//Integer[] vol = new Integer[volVals.size()];
		//vol = volVals.toArray(vol);
		double[] vol = new double[] {1, 1, 1, 1, 12.2, 26.5};
		//double[] vol = new double[] {1, 1, 1, 1};
		//double[] vol = new double[] {12.2, 26.5};
		ExperimentalSample[] expType = new ExperimentalSample[vol.length];
		expType[0] = ExperimentalSample.APS_7mm;
		ExperimentalSample[] experimentValues = ExperimentalSample.values();
		for(int i = 0; i < vol.length; i++) {
			if(i < experimentValues.length) {
				expType[i] = experimentValues[i];
			} 
		}
		if(expType.length == experimentValues.length) { expType[5] = ExperimentalSample.DSC; }
		//expType[0] = ExperimentalSample.DSC; 
		//expType[1] = ExperimentalSample.DSC; 
		expType[5] = ExperimentalSample.DSC;
		String dscVal = "";
		ssp.setTransformed(false);
		ssp.setXyz(false);
		ssp.setFit(false);
		ssp.setMovie(false);
		ssp.setAxialGrowthRates(new double[] {1., 1., 1.});
		for(int e = 0; e < expType.length; e++) {
			fosObj = new FileOutputStream(expType[e].toString() + vol[e] + ".obj");
			objOut = new ObjectOutputStream(fosObj);
			//ssp.sampleUnitsPerAxis = axes[e];
			ssp.setSampleUnitsPerAxis(getSampleAxes(expType[e], volume, vol[e]));
			System.out.println("Sample axes: " + StringConverter.arrayToTabString(ssp.getSampleUnitsPerAxis()));
			ssp.setExp(expType[e]);
			if(ssp.getExp() == ExperimentalSample.DSC) { dscVal = vol[e] + ""; } 
			else { dscVal = ""; }
			ssp.setExpVal(vol[e]);
			switch(expType[e]) {
			case DSC:
				ssp.setTimeStep(4);
				break;
			default:
				ssp.setTimeStep(1);
				break;
			}
			ssp.setNucContVal(.025*(ssp.getTimeStep()/.5));
			for(int i = 0; i < iterations ; i++) {
				if(i < numXYZOutput) {
					ssp.setXyz(true);
				} else {
					ssp.setXyz(false);
				}
				ssp.newFileNames();
				s = new Simulation(ssp);
				s.run();
				excel = (new Analysis(whichFitting)).analyzeWithOneFixed(s);
				if(excel.compareTo("bad") != 0) {
					ps.print(excel);
				} else {
					ps.println(excel);
				}
				
				ps.flush();
				fos.flush();
				s.getSample().setLatticeToNull();
				objOut.writeObject(s);
				System.out.print("Set: " + (e+1) + "/" + expType.length + " with: " + iterations + " iterations ");
			}
		}
	}
	
	/**
	 * 
	 * @param type
	 * @param desiredVolume
	 * @param DSCMass in milligrams please!
	 * @return
	 */
	public int[] getSampleAxes(ExperimentalSample type, double desiredVolume, double DSCMass) {
		// 12.5mg DSC sample with the high pressure pans: radius = 0.25, h = 0.0296 => r/h = 8.45
		// 26.2mg DSC sample with the high pressure pans: radius = 0.25, h = 0.0621 => r/h = 4.03
		double CZX1_DENSITY = 2426; // mg/cm^3
		double volumeRatio = 1;
		double h, r;
		int radius, height;
		double a = 1;
		switch(type) {
		case NSLS_5mm:
			a = .48/.15;
			volumeRatio = 1;
			break;
		case NSLS_7mm:
			a = .68/.15;
			volumeRatio = 1.418879056;
			break;
		case APS_5mm:
			a = 1/.24;
			volumeRatio = 5.339233038;
			break;
		case APS_7mm:
			a = 1/.34;
			volumeRatio = 10.7079646;
			break;
		case DSC:
			double volume = DSCMass/CZX1_DENSITY;
			r = .25; // cm
			h = volume/Math.PI/r/r;
			a = h/r;
			desiredVolume *= volume / 0.0000339292006588;
			break;
		case CUSTOM:
			h = DSCMass;
			radius = (int) Math.round(Math.sqrt(desiredVolume / h / Math.PI)); 
			return new int[] {radius, radius, (int) h};
		case CUSTOM_R:
			radius = (int) DSCMass;
			height = (int) Math.round(desiredVolume / (radius * radius) / Math.PI); 
			return new int[] {radius, radius, height};
		case CUSTOM_H:
			height = (int) DSCMass;
			radius = (int) Math.round(Math.sqrt(desiredVolume / height / Math.PI)); 
			return new int[] {radius, radius, height};
		}
		
		
		h = Math.pow(desiredVolume*volumeRatio/Math.PI*a*a, (1./3.));
		r = h/a;
		
		radius = (int) Math.round(r);
		height = (int) Math.round(h);
		return new int[] {radius, radius, height};
	}
	public static void main(String[] args) {
        
		new SimulationController();
	}
}
