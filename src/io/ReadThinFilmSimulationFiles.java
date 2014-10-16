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

import jama.Matrix;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import analysis.Analysis;
import simulation.Crystal;
import simulation.SimulSetupParams;
import simulation.DimensionalityOptions;
import simulation.FittableParametersOptions;
import simulation.Simulation;

public class ReadThinFilmSimulationFiles implements Runnable {
	private String fRoot;
	private PrintStream ps_debug;
	private static PrintStream ps_bulk, ps_individual;
	private static double[][] nVsLayer;
	double n;
	
	
	public ReadThinFilmSimulationFiles(String fRoot, double n) {
		this.fRoot = fRoot;
		this.n = n;
	}
	public static void dispatch(String[] fRoots) throws FileNotFoundException {
		int nThreads = 4;
		Executor e = Executors.newFixedThreadPool(nThreads);
		double n_val = 1;
		Vector<Double> n_vals = new Vector<Double>();
		double step = 0.1;
		while(n_val < 3.5) {
			n_val += step;
			n_vals.add(n_val);
		}
		Double[] n = new Double[n_vals.size()];
		n = n_vals.toArray(n);
		n = new Double[] {3.};
		FileOutputStream fos_all = new FileOutputStream("all_bulk.analyzed");
		FileOutputStream fos_individual = new FileOutputStream("all_individual.analyzed");
		ps_bulk = new PrintStream(fos_all);
		ps_bulk.println("kA\tt0\tn\tkA_err\tt0_err\tsumSq\ttotal_sample_volume");

		ps_individual = new PrintStream(fos_individual);
		ps_individual.println("kA\tt0\tn\tkA_err\tt0_err\tsumSq\txtal_size\ttotal_sample_volume\tnuc_time");
		
		for(int dim = 0; dim < n.length; dim++) {
			for(int i = 0; i < fRoots.length; i++) {
				e.execute(new ReadThinFilmSimulationFiles(fRoots[i], n[dim]));
			}
		}
	}
	public void run() {
		File fName = new File(fRoot + ".obj");
		FileInputStream fis;
		ObjectInputStream ois = null;
		String excel;
		FileOutputStream fos = null;
		FileOutputStream fos_debug = null;
		ps_debug = null;
		try {
			fos = new FileOutputStream(fRoot + "_n=" + n + ".analyzed");
			fos_debug = new FileOutputStream("Debug.txt");
			ps_debug = new PrintStream(fos_debug);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		PrintStream ps = new PrintStream(fos);
		try {
			fis = new FileInputStream(fName);
			ois = new ObjectInputStream(fis);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Object o = null;
		Simulation s;
		Analysis a = new Analysis();
		
		boolean EOF = false;
		int idx = 0;
		Matrix totalCorr = new Matrix(2, 2);
		Matrix totalCovar = new Matrix(2, 2);
		Matrix newCorr, newCovar;
		int numCovarSummed = 0;
		int numCorrSummed = 0;
		int numRead = 0;
		int printHowOften = 1;
		while(!EOF) {
			try {
				o = ois.readObject();
				numRead++;
				if(numRead % printHowOften == 0) {
					print_system("File: " + fRoot + "\tSimulation number: " + idx++ + "\t");
				}
			} catch (IOException e) {
				EOF = true;
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				EOF = true;
				e.printStackTrace();
			} catch (Exception e) {
				EOF = true;
				e.printStackTrace();
			}
			if(!EOF && o instanceof Simulation) {
				s = (Simulation) o;
				long vol = s.getTotalVolume();
				int layers = (int) (vol / (15650/2));
				try {
					int diameter = (int) Math.round(Math.sqrt(4*vol / layers/Math.PI));
					//n = getN_alternate(layers, diameter);
					n = getN(layers);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				if(numRead % printHowOften == 0) {
					println_system("n: " + n + "\tvolume: " + s.getTotalVolume());
				}
				try {
					SimulSetupParams ssp = s.getSSP();
					ssp.setInit_n(n);
					ssp.setFittableParameterSelection(FittableParametersOptions.FIT_K_N);
					ssp.setDimensionalitySelection(DimensionalityOptions.APPROXIMATE_BY_SAMPLE_SHAPE);
					excel = a.analyze(s, ssp.getFittableParameterSelection(), ssp.getDimensionalitySelection());
					printSomeCrystals(s.getSample().getCrystalArray());
					try {
						newCorr = a.getIndividualCorr();
						newCovar = a.getIndividualCovar();
						if(isValid(totalCorr) && isValid(newCorr)) { 
							totalCorr = totalCorr.plus(newCorr);
							numCorrSummed++;
						}
						if(isValid(totalCovar) && isValid(newCovar)) {
							totalCovar = totalCovar.plus(newCovar);
							numCovarSummed++;
						}
						
					} catch (IllegalArgumentException iae) {
						
					}
					ps.print(excel);
					String out = a.getBulkString();
					if(out.compareTo("") != 0) {
						ps_bulk.println(a.getBulkString());
					}
					out = a.getIndividualString();
					if(out.compareTo("") != 0) {
						ps_individual.print(out);
					}
					//ps_debug.println(a.getParams());
					//ps_debug.println(a.getError());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		ps.println("Total correlation matrices summed: " + numCorrSummed);
		ps.println("Total covariance matrices summed: " + numCovarSummed);
		totalCorr = totalCorr.times(1./((double) numCorrSummed));
		totalCovar = totalCovar.times(1./((double) numCovarSummed));
		
		String corrOut = "Correlation matrix:\n";
		double[][] d = totalCorr.getArray();
		for(int i = 0; i < d.length; i++) {
			corrOut += StringConverter.arrayToTabString(d[i]) + "\n";
		}
		
		String covarOut = "Covariance matrix:\n";
		d = totalCovar.getArray();
		for(int i = 0; i < d.length; i++) {
			covarOut += StringConverter.arrayToTabString(d[i]) + "\n";
		}
		println_system(corrOut);
		println_system(covarOut);
	}
	private int numbersTested = 0;
	public boolean isValid(Matrix m) {
		if(m == null) {
			return false;
		}
		double[][] d = m.getArray();
		if(d.length != 2 && d[0].length != 2) {
			return false;
		}
		double val;
		double otherVal;
		for(int i = 0; i < d.length; i++) {
			for(int j = 0; j < d[i].length; j++) {
				val = d[i][j];
				numbersTested++;
				if(numbersTested == 721) {
					otherVal = 0;
				}
				boolean isInf = Double.isInfinite(val);
				boolean isNaN = Double.isNaN(val);
				if(isInf || isNaN) {
					return false;
				}
				ps_debug.println(numbersTested + ": " + val);
			}
		}
		return true;
	}
	public static void main(String[] args) {
		ReadThinFilmSimulationFiles.readLayerVsApparentDimensionality();
		String[] fRoots = { "C:\\Users\\DHD\\Desktop\\$research\\papers\\size correction\\n vs layers\\simulated data\\layers_2-71"};
		try {
			dispatch(fRoots);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized void println_system(String s) {
		System.out.println(s);
	}
	private synchronized void print_system(String s) {
		System.out.print(s);
	}
	private synchronized double getN(int layer) throws Exception {
		for(int i = 0; i < nVsLayer.length; i++) {
			if(nVsLayer[i][0] == layer) {
				return nVsLayer[i][1];
			}
		}
		throw new Exception("Layer: " + layer + " was not found in the nVsLayer array");
	}
	private synchronized double getN_alternate(int layer, int diameter) throws Exception {
		double n = layer + diameter + diameter;
		n /= Math.max(layer, diameter);
		return n;
	}
	private static void readLayerVsApparentDimensionality() {
		File f = new File("C:\\Users\\DHD\\Desktop\\$research\\papers\\size correction\\n vs layers\\simulated data\\num layers vs apparent dimensionality.txt");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scanner s = new Scanner(fis);
		int numLines = 0;
		while(s.hasNextLine()) {
			s.nextLine();
			numLines++;
		}
		
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		s = new Scanner(fis);
		
		double[][] d = new double[numLines][2];
		String[] line;
		for(int i = 0; i < numLines; i++) {
			line = s.nextLine().split("\t");
			d[i][0] = Double.valueOf(line[0]);
			d[i][1] = Double.valueOf(line[1]);
		}
		
		nVsLayer = d;
	}
	private void printSomeCrystals(Crystal[] c) throws FileNotFoundException {
		Integer[] growth;
		FileOutputStream fos;
		PrintStream ps;
		for(int i = 0; i < c.length; i++) {
			growth = c[i].getGrowth();
			fos = new FileOutputStream(i + ".xtal");
			ps = new PrintStream(fos);
			int totalGrowth = 0;
			for(int j = 0; j < growth.length; j++) {
				totalGrowth += growth[j];
				ps.println(j + "\t" + totalGrowth);
			}
		}
	}
	
}
