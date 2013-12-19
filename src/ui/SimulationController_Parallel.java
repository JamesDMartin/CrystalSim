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

import geometry.JVector;
import indexing.AlphabeticIndexingSystem;
import io.CrystalliteAnisotropy;
import io.ParseXYZ;
import io.StringConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import analysis.Analysis;

import email.SendMailTLS;

import shapes.ShapeTypes;
import simulation.ExperimentalSample;
import simulation.Nucleation;
import simulation.NucleationLocation;
import simulation.NucleationOrientation;
import simulation.SimulSetupParams;
import simulation.Simulation;

public class SimulationController_Parallel implements Runnable {

	public SimulSetupParams ssp;
	private File fName;
	private File xyzOutput = new File("xyz");
	private static Executor e;
	private volatile static String NOTEBOOK_NAME = "EDD";
	private volatile static String NOTEBOOK_NUMBER = "10";
	private volatile static String PAGE_NUMBER = "191";
	private volatile static int numThreads = 0;
	private int threadIdx;
	private volatile static AlphabeticIndexingSystem pageIdx = new AlphabeticIndexingSystem("ba");
	private String notebookIdx;
	private volatile static double[] vpb = new double[] {1};
	private int simulIdx, volumeIncrement, vpb_idx;
	private double sampleVolume, expVal;
	private ExperimentalSample sampleGeometry;
	private volatile static boolean aspectRatio = false;
	private volatile static double[] nucRates = new double[] {0.025};
//	private volatile static double[] nucRates = new double[] {0.1};
	private volatile static boolean aspectRatioVariation = false;
	private ShapeTypes sampleShape;
	// 0 = sigmoidal, 1 = linear
	private int whichFitting = 0;
	private int numIterations = 100;
	
	public SimulationController_Parallel(String notebookIdx, int simulIdx, int vpb_idx, 
			double sampleVolume, ExperimentalSample sampleGeometry, 
			int volumeIncrement, ShapeTypes sampleShape) {
		this.sampleGeometry = sampleGeometry;
		this.vpb_idx = vpb_idx;
		this.sampleVolume = sampleVolume;
		this.notebookIdx = notebookIdx;
		this.simulIdx = simulIdx;
		this.volumeIncrement = volumeIncrement;
		this.sampleShape = sampleShape;
		threadIdx = ++numThreads;
	}
	public void run() {
		xyzOutput.mkdir();
		ssp = new SimulSetupParams();
		setupSSP();
		try {
			simulate(numIterations);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void setupSSP() {

		//ssp.timeStep = 2;
		ssp.setExp(sampleGeometry);
		ssp.setExpVal(sampleVolume);
		ssp.setVolumeIncrement(volumeIncrement);
		ssp.setSampleUnitsPerAxis(getSampleAxes(sampleVolume));
		ssp.setTransformed(false);
		ssp.setXyz(true);
		ssp.setFit(false);
		ssp.setMovie(false);
		ssp.setObj(true);
		double rate = vpb[vpb_idx];
		ssp.setAxialGrowthRates(new double[] {rate, rate, rate});
		ssp.setSampleShape(sampleShape);
		ssp.setNucContVal(nucRates[simulIdx]);
	}
	private void simulate(int iterations) throws IOException {
		Simulation s;
		String excel = "";		
		String simulName = NOTEBOOK_NAME + "_" + NOTEBOOK_NUMBER + "-" + PAGE_NUMBER + notebookIdx; 
		fName = new File(simulName + ".out");
		FileOutputStream fos = new FileOutputStream(fName);

		PrintStream ps = new PrintStream(fos);

		/** object output streams */
		FileOutputStream fosObj = new FileOutputStream(simulName + ".obj");
		ObjectOutputStream objOut = new ObjectOutputStream(fosObj);

		/** anisotropy output stream */
		FileOutputStream fosAspectRatio = new FileOutputStream(simulName + ".aspectRatio");
		PrintStream psAspectRatio = new PrintStream(fosAspectRatio);
		psAspectRatio.println("sample axis 1\tsample axis 2\tsample axis 3\txyz analyzed file name\tcrystallite axis 1\tcrystallite axis 2\tcrystallite axis 3\tapproximate dimensionality\taspect ratio\tcrystallite volume");
		
		/** xyz index output stream */
		FileOutputStream fosXYZ = new FileOutputStream(xyzOutput + File.separator + simulName + " -- xyz index.txt");
		PrintStream psXYZ = new PrintStream(fosXYZ);
		
		psXYZ.println("axis 1\taxis 2\taxis 3\titeration #\txyzOutputName");
		int jMax = 1;
		int[][] sampleAxes = null;
		if(aspectRatioVariation) {
			sampleAxes = getAspectRatioVariationAxes(volumeIncrement);
			jMax = sampleAxes.length;
		}
		//ssp.nucContVal = 0;
		CrystalliteAnisotropy ca;
		ParseXYZ parse;
		String info = "";
		for(int i = 0; i < iterations ; i++) {
			for(int j = 0; j < jMax; j++) {
				if(aspectRatioVariation) {
					ssp.setSampleUnitsPerAxis(sampleAxes[j]);
					//System.out.println(StringConverter.arrayToTabString(ssp.sampleUnitsPerAxis));
					info = StringConverter.arrayToTabString(sampleAxes[j]);
				}
				ssp.newFileNames();
				s = new Simulation(ssp, simulIdx, i, iterations);
				s.xyz = new File(xyzOutput + File.separator + simulName +" -- " + i + " -- " + j + ".xyz");
				info += i + "\t" + s.xyz.getName();
				psXYZ.println(info);
				System.out.print("threads index: (" + threadIdx + " / " + numThreads + ") -- iterations index (" + 
						(i+1) + " / " + iterations + ")");
				if(aspectRatioVariation) {
					System.out.println(" -- sample axes index (" + (j+1) + " / " + jMax + ")" + "sample volume: " + s.getSample().getTotalVolume()+ " -- nucleation rate: " + nucRates[simulIdx]);
				} else { 
					System.out.println("sample volume: " + s.getSample().getTotalVolume() + " -- nucleation rate: " + nucRates[simulIdx]); 
				}
				s.run();
				parse = new ParseXYZ(s.xyz, 0);
				ca = new CrystalliteAnisotropy(parse.getXtals());
				ca.findAllPoints();
				ca.writeToFile(psAspectRatio, info);
				Analysis a;
				if(ssp.isFit()) {
					a = (new Analysis(whichFitting));
					a.height = ssp.getSampleUnitsPerAxis()[2];
					a.zipPrinter = ssp.getZipPrinter_Fits();
					excel = a.analyzeWithOneFixed(s, 2, aspectRatio);
					if(excel.compareTo("bad") != 0) {
						ps.print(excel);
					} else {
						ps.println(excel);
					}
				}
				ps.flush();
				fos.flush();
				s.getSample().setLatticeToNull();
				ssp.nullify();
				/**
				 * write the object to file
				 */
				objOut.writeObject(s);
				//System.out.print("Set: " + (simulIdx+1) + "/" + nucRates.length + " with: " + iterations + " iterations ");
			}
		}
		objOut.close();
		psAspectRatio.close();
		fosObj.close();
	}
	
	/**
	 * 
	 * @param type
	 * @param desiredVolume
	 * @param DSCMass in milligrams please!
	 * @return
	 */
	public int[] getSampleAxes(double desiredVolume) {
		// 12.5mg DSC sample with the high pressure pans: radius = 0.25, h = 0.0296 => r/h = 8.45
		// 26.2mg DSC sample with the high pressure pans: radius = 0.25, h = 0.0621 => r/h = 4.03
		double CZX1_DENSITY = 2426; // mg/cm^3
		double volumeRatio = 1;
		double h, r;
		int radius = 0, height = 0;
		double a = 1;
		desiredVolume = ssp.getVolumeIncrement();
		switch(ssp.getExp()) {
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
			double volume = ssp.getExpVal()/CZX1_DENSITY;
			r = .25; // cm
			h = volume/Math.PI/r/r;
			a = h/r;
			desiredVolume = ssp.getVolumeIncrement() * volume / 0.0000339292006588;
			break;
		case CUSTOM:
			h = ssp.getVolumeIncrement();
			switch(ssp.getSampleShape()) {
			case Cylindrical:
				radius = (int) Math.round(Math.sqrt(desiredVolume / ssp.getVolumeIncrement() / Math.PI)); 
				break;
			case Orthorhombic:
				radius = (int) Math.round(Math.sqrt(desiredVolume / ssp.getVolumeIncrement() )); 
				break;
			}
			return new int[] {radius, radius, (int) h};
		case CUSTOM_R:
			radius = (int) ssp.getExpVal();
			switch(ssp.getSampleShape()) {
			case Cylindrical:
				height = (int) Math.round(ssp.getVolumeIncrement() / (ssp.getExpVal() * ssp.getExpVal()) / Math.PI);
				break;
			case Orthorhombic:
				height = (int) Math.round(volumeIncrement / (ssp.getExpVal() * ssp.getExpVal()));
				break;
			}
			return new int[] {radius, radius, height};
		case CUSTOM_H:
			h = (int) ssp.getExpVal();
			switch(ssp.getSampleShape()) {
			case Cylindrical:
				radius = (int) Math.round(Math.sqrt(desiredVolume / ssp.getExpVal() / Math.PI)); 
				break;
			case Orthorhombic:
				radius = (int) Math.round(Math.sqrt(desiredVolume / ssp.getExpVal() )); 
				break;
			}
			return new int[] {radius, radius, (int) h};
		case ASPECT_RATIO_VARIATION:
			break;
		default:
			break;
		}
		
		
		h = Math.pow(desiredVolume*volumeRatio/Math.PI*a*a, (1./3.));
		r = h/a;
		
		radius = (int) Math.round(r);
		height = (int) Math.round(h);
		return new int[] {radius, radius, height};
	}
	
	private int[][] getAspectRatioVariationAxes(int volume) {
		Vector<int[]> v = new Vector<int[]>();
		
		int h, r, vol, idx = 0;
		int[] axes;
		h = 1;
		ssp.setExp(ExperimentalSample.CUSTOM_H);
		do {
			ssp.setExpVal(h++);
			axes = getSampleAxes(volume);
			v.add(axes);
			vol = (int) Math.rint(Math.PI * axes[0] * axes[1] * axes[2]);
			System.out.println(StringConverter.arrayToTabString(axes) + "\t" + vol + "\t" + idx++);
		} while(h <= axes[0]);
		
		r=h;

		ssp.setExp(ExperimentalSample.CUSTOM_R);
		do {
			ssp.setExpVal(r--);
			axes = getSampleAxes(volume);
			v.add(axes);
			vol = (int) Math.rint(Math.PI * axes[0] * axes[1] * axes[2]);
			System.out.println(StringConverter.arrayToTabString(axes) + "\t" + vol + "\t" + idx++);
		} while(r >= 1);
		
		int[][] allAxes = new int[v.size()][3];
		allAxes = v.toArray(allAxes);
		return allAxes;
	}
	private static void initialize() {
		String notebookIdx;
		double[] dscVol = new double[] {26.5};
		int[] volIncrement = new int[] {10000};
		ExperimentalSample[]  expSample = new ExperimentalSample[] {
				ExperimentalSample.DSC,
		};
		SimulationController_Parallel sc_p;
		ShapeTypes[] shapes = new ShapeTypes[] {ShapeTypes.Cylindrical};
		for(int vol = 0; vol < dscVol.length; vol++) {
			for(int v = 0; v < vpb.length; v++) {
				for(int i = 0; i < nucRates.length; i++) {
					notebookIdx = pageIdx.getName();
					sc_p = new SimulationController_Parallel(notebookIdx, i, v, 
							dscVol[vol], expSample[vol], volIncrement[0], shapes[0]);
					e.execute(sc_p);
					pageIdx.update();
		}}}
	}
	public static void main(String[] args) {
		int nThreads = 3;
		e = Executors.newFixedThreadPool(nThreads);
		initialize();
	}
}

