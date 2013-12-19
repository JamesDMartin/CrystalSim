package io;

import jama.Matrix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import analysis.Analysis;

import simulation.Crystal;
import simulation.Simulation;

public class ReadSimulationFilesOutputTransformationData implements Runnable {
	private String fRoot;
	private PrintStream ps_debug;
	double n;
	
	
	public ReadSimulationFilesOutputTransformationData(String fRoot, double n) {
		this.fRoot = fRoot;
		this.n = n;
	}
	public static void dispatch(String[] fRoots) throws FileNotFoundException {
		int nThreads = 4;
		Executor e = Executors.newFixedThreadPool(nThreads);
		double n_val = 1;
		Vector<Double> n_vals = new Vector<Double>();
		double step = 0.025;
		while(n_val < 4) {
			n_val += step;
			n_vals.add(n_val);
		}
		Double[] n = new Double[n_vals.size()];
		n = n_vals.toArray(n);
		n = new Double[] {3.};
		
		for(int dim = 0; dim < n.length; dim++) {
			for(int i = 0; i < fRoots.length; i++) {
				e.execute(new ReadSimulationFilesOutputTransformationData(fRoots[i], n[dim]));
			}
		}
	}
	public void run() {
		File fName = new File(fRoot + ".obj");
		FileInputStream fis;
		ObjectInputStream ois = null;
		FileOutputStream fos = null;
		PrintStream ps = null;
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
		int numCovarSummed = 0;
		int numCorrSummed = 0;
		int numAnalyzed = 0;
		int numStart = 15;
		int numMax = 100;
		while(!EOF && numCovarSummed < numMax && numCorrSummed < numMax && numAnalyzed < numMax) {
			try {
				o = ois.readObject();
				System.out.print("File: " + fRoot + "\tSimulation number: " + idx++);
				if(numMax < numStart) {
					numMax++;
					System.out.println("Skipped.");
					continue;
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
				double[][] normalizedTimeAndAllData = a.getNormalizedTimeAndAllData(s);
				try {
					fos = new FileOutputStream(fRoot + "-" + numAnalyzed++ + ".xtals");
					ps = new PrintStream(fos);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				Crystal[] c = s.getSample().getCrystalArray();
				int numXtals = 0;
				int vol;
				double t0;
				String out = "";
				for(int i = 0; i < c.length; i++) {
					t0 = c[i].getNucTime();
					vol = c[i].getTotalSize();
					if(vol > 0) {
						numXtals++;
						out += t0 + "\t" + vol + "\n";
					}
				}
				ps.println(numXtals + "");
				ps.print(out);
				for(int i = 0; i < normalizedTimeAndAllData.length; i++) {
					ps.println(StringConverter.arrayToTabString(normalizedTimeAndAllData[i]));
				}
			}
			System.out.println("analyzed");
		}
	}
	private int numbersTested = 0;
	public boolean isValid(Matrix m) {
		double[][] d = m.getArray();
		if(d.length != 2 && d[0].length != 2) {
			return false;
		}
		double val;
		for(int i = 0; i < d.length; i++) {
			for(int j = 0; j < d[i].length; j++) {
				val = d[i][j];
				numbersTested++;
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
		/*String[] fRoots = { "obj files//NSLS_7mm_", "obj files//NSLS_5mm_", 
				"obj files//DSC_12.2_3476_runs", "obj files//DSC_26.5._45 runs", 
				"obj files//DSC_26.5_500runs_", "obj files//APS_7mm_",
				"obj files//APS_5mm_"};*/
		String[] fRoots = { "obj files//CUSTOM1.0" };
		try {
			dispatch(fRoots);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
