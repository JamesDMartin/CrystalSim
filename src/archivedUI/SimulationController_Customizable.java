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
package archivedUI;

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

import email.SendMailTLS;

import shapes.ShapeTypes;
import simulation.ExperimentalSample;
import simulation.Nucleation;
import simulation.NucleationLocation;
import simulation.NucleationOrientation;
import simulation.SimulSetupParams;
import simulation.Simulation;

public class SimulationController_Customizable {

	public SimulSetupParams ssp;
	private File fName;
	public SimulationController_Customizable() {
		ssp = new SimulSetupParams();
		try {
			simulate(50);	
		} catch (Exception e) {
			e.printStackTrace();
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
		int fixedParameters = 2;
		boolean analyze = true;
		
		int numXYZOutput = 3;
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
		//double[] vol = new double[] {1, 1, 1, 1};
		double[] nucRates = new double[] {0.01, 0.025, 0.05, 0.1, 0.2, 0.4};
		double[] vol = new double[nucRates.length];
		ExperimentalSample[] expType = new ExperimentalSample[nucRates.length];
		for(int i = 0; i < nucRates.length; i++) {
			vol[i] = 12.2;
			expType[i] = ExperimentalSample.DSC;
		}
		//expType[0] = ExperimentalSample.DSC; 
		//expType[1] = ExperimentalSample.DSC; 
		//expType[5] = ExperimentalSample.DSC;
		String dscVal = "";
		ssp.setTransformed(false);
		ssp.setXyz(false);
		ssp.setFit(true);
		ssp.setMovie(false);
		ssp.setObj(true);
		ssp.setAxialGrowthRates(new double[] {1., 1., 1.});
		// 147 for the big dsc sample, 113 for the small one
		int axisLength = 113;
		int volume = 10000;
		//int[] axes = new int[] {axisLength, axisLength, axisLength};
		int[] axes = new int[] {166,166,17};
		ssp.setSampleShape(ShapeTypes.Cylindrical);
		boolean aspectRatio = false;
		//ssp.nucLoc = NucleationLocation.Fixed;
		//ssp.nucLocOptions = new JVector[] {new JVector(axes[0]/2., axes[1]/2., axes[2]/2.)};
		//ssp.nucShape = ShapeTypes.Spherical;
		int whichFitting = 0;
		Analysis a;
		for(int e = 1; e < vol.length; e++) {
			fosObj = new FileOutputStream(iterations + "-" + expType[e].toString() + vol[e] + "_" + nucRates[e] + ".obj");
			objOut = new ObjectOutputStream(fosObj);
			//ssp.sampleUnitsPerAxis = axes[e];
			ssp.setSampleUnitsPerAxis(getSampleAxes(expType[e], volume, vol[e]));
			//ssp.sampleUnitsPerAxis = axes;
			System.out.println("Sample axes: " + StringConverter.arrayToTabString(ssp.getSampleUnitsPerAxis()));
			ssp.setExp(expType[e]);
			if(ssp.getExp() == ExperimentalSample.DSC) { dscVal = vol[e] + ""; } 
			else { dscVal = ""; }
			ssp.setExpVal(vol[e]);
			switch(expType[e]) {
			case DSC:
				ssp.setTimeStep(1);
				break;
			default:
				ssp.setTimeStep(1);
				break;
			}
			ssp.setNucContVal(.025*(ssp.getTimeStep()/.5));
			ssp.setNucContVal(nucRates[e]);
			//ssp.nucContVal = 0;
			for(int i = 0; i < iterations ; i++) {
				if(i < numXYZOutput) {
					ssp.setXyz(true);
				} else {
					ssp.setXyz(false);
				}
				ssp.newFileNames();
				s = new Simulation(ssp, i, e, iterations);
				s.run();
				if(analyze) {
					a = (new Analysis(whichFitting));
					a.height = axes[2];
					a.zipPrinter = ssp.getZipPrinter_Fits();
					excel = a.analyzeWithOneFixed(s, fixedParameters, aspectRatio);
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
		new SimulationController_Customizable();
	}
}
