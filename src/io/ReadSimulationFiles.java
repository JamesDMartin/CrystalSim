package io;

import indexing.AlphabeticIndexingSystem;
import jama.Matrix;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import analysis.Analysis;


import simulation.SimulSetupParams;
import simulation.SimulSetupParams.FittingType;
import simulation.Simulation;

public class ReadSimulationFiles implements Runnable {
	private String fRoot;
	private File file;
	private PrintStream ps_debug;
	private PrintStream ps_bulk, ps_individual;
	private PrintStream ps_Error;
	private static PrintStream ps;
	double n;
	private static String analysisType = "t=tnuc";
	private int whichFixed = 1;
	private int numFittable = 2;
	private static Matrix individualTotalCorr, individualTotalCovar, bulkTotalCorr, bulkTotalCovar;
	private double errorVal, alphaMin, alphaMax;
	private String notebookName;
	private static String output = "output\\";
	private static int fittingType = Analysis.NONLINEAR_SIGMOIDAL;
	private boolean aspectRatio = false;
	public ReadSimulationFiles(String fRoot, double n, PrintStream ps_all, PrintStream ps_individual,
			double errorVal, double alphaMin, double alphaMax, String notebookName, PrintStream psError) {
		ps_bulk = ps_all;
		this.ps_individual = ps_individual;
		this.fRoot = fRoot;
		this.file = file;
		this.n = n;
		this.errorVal = errorVal;
		this.alphaMin = alphaMin;
		this.alphaMax = alphaMax;
		this.notebookName = notebookName;
		ps_Error = psError;
	}
	private synchronized void println(String print) {
		ps.println(print);
	}
	private synchronized void print(String print) {
		ps.print(print);
	}
	public static void dispatch(String[] fRoots, double errorVal, double alphaMin, double alphaMax, String notebookName) throws IOException {
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
		FileOutputStream[] fos_all = new FileOutputStream[fRoots.length];
		FileOutputStream[] fos_individual = new FileOutputStream[fRoots.length];
		FileOutputStream fos_error = new FileOutputStream(new File(output + notebookName + ".error"));
		PrintStream ps_Error = new PrintStream(fos_error);
		PrintStream[] ps_all = new PrintStream[fRoots.length];
		PrintStream[] ps_individual = new PrintStream[fRoots.length];
		System.out.println("Create output folder: " + new File(output + notebookName).mkdir());
		File f;
		for(int i = 0; i < fRoots.length; i++) {
			f = new File(output + notebookName + "\\" + fRoots[i] + "_all_bulk.analyzed");
			System.out.println(f.getAbsolutePath());
			System.out.println("Create new file: " + f.createNewFile());
			fos_all[i] = new FileOutputStream(f);
			f = new File(output + notebookName + "\\" + fRoots[i] + "_all_individual.analyzed");
			f.createNewFile();
			fos_individual[i] = new FileOutputStream(f);
			ps_all[i] = new PrintStream(fos_all[i]);
			ps_individual[i] = new PrintStream(fos_individual[i]);
			ps_all[i].println("kA\tt0\tn\tkA_err\tt0_err\tsumSq\ttotal_sample_volume");
			ps_individual[i].println("kA\tt0\tn\tkA_err\tt0_err\tsumSq\txtal_size\ttotal_sample_volume\tnuc_time");
			
		}
		FileOutputStream fos = new FileOutputStream(notebookName + ".analyzed");
		ps = new PrintStream(fos);
		for(int dim = 0; dim < n.length; dim++) {
			for(int i = 0; i < fRoots.length; i++) {
				e.execute(new ReadSimulationFiles(fRoots[i], n[dim], ps_all[i], ps_individual[i], errorVal, alphaMin, alphaMax, 
						notebookName, ps_Error));
				numThreadsLaunched++;
			}
		}
	}
	private synchronized void finished() {
		System.out.println(++numThreadsFinished + " / " + numThreadsLaunched + "\t" + fRoot);
		if(numThreadsFinished == numThreadsLaunched) {
			double[][] individualCorr = individualTotalCorr.times(1./individualTotalCorr.get(0, 0)).getArray();
			double[][] individualCovar = individualTotalCovar.times(1./individualTotalCorr.get(0, 0)).getArray();
			double[][] bulkCorr = bulkTotalCorr.times(1./bulkTotalCorr.get(0, 0)).getArray();
			double[][] bulkCovar = bulkTotalCovar.times(1./bulkTotalCorr.get(0, 0)).getArray();
			String individualCorr_s = "Individual Correlation Matrix:\n";
			String individualCovar_s = "Individual Covariance Matrix:\n";
			String bulkCorr_s = "Bulk Correlation Matrix:\n";
			String bulkCovar_s = "Bulk Covariance Matrix:\n";
			for(int i = 0; i < individualCorr.length; i++) {
				individualCorr_s += StringConverter.arrayToTabString(individualCorr[i]) + "\n";
				individualCovar_s += StringConverter.arrayToTabString(individualCovar[i]) + "\n";
				bulkCorr_s += StringConverter.arrayToTabString(bulkCorr[i]) + "\n";
				bulkCovar_s += StringConverter.arrayToTabString(bulkCovar[i]) + "\n";
			}
			ps_Error.println("Total Correlation");
			ps_Error.println(individualCorr_s);
			ps_Error.println(individualCovar_s);
			ps_Error.println(bulkCorr_s);
			
			ps_Error.println(bulkCovar_s);
		}
	}
	public void run() {
		File fName = new File(fRoot + ".obj");
		FileInputStream fis;
		ObjectInputStream ois = null;
		String excel;
		FileOutputStream fos = null;
		FileOutputStream fos_debug = null;
		FileOutputStream fosZip = null;
		ps_debug = null;
		File zipOutput = new File(output + notebookName + "\\fits");
		boolean regressionFail = false;
//		boolean success = new File(output + notebookName + "\\fits").mkdir();
		if(!zipOutput.isDirectory()) {
			zipOutput.mkdir();
		}
		try {
			fos = new FileOutputStream(output + notebookName + "\\" + fRoot + analysisType + ".analyzed");
			fos_debug = new FileOutputStream("Debug.txt");
			fosZip = new FileOutputStream(zipOutput + "\\" + fRoot + ".zip");
			ps_debug = new PrintStream(fos_debug);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		PrintStream ps = new PrintStream(fos);
		try {
			fis = new FileInputStream("obj files\\" + fName);
			ois = new  ObjectInputStream(fis);
		} catch (FileNotFoundException e1) {
			try {
				fis = new FileInputStream(fName);
				ois = new  ObjectInputStream(fis);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Object o = null;
		Simulation s;
		Analysis a = new Analysis(FittingType.sigmoidal);
		if(fRoot.contains("DSC12")) {
			a.height = 17;
		} else if(fRoot.contains("DSC26")) {
			a.height = 37;
		} else {
			a.height  = 0;
		}
		PrintToZip zipPrinter = new PrintToZip(new ZipOutputStream(fosZip), fosZip);
		a.zipPrinter = zipPrinter;
		a.alphaMin = alphaMin;
		a.fractionError = errorVal;
		a.alphaMax = alphaMax;
		a.estimateN = 2;
		
		
		
		boolean EOF = false;
		int idx = 0;
		Matrix individualCorr = new Matrix(numFittable, numFittable);
		Matrix individualCovar = new Matrix(numFittable, numFittable);
		Matrix bulkCorr = new Matrix(numFittable, numFittable);
		Matrix bulkCovar = new Matrix(numFittable, numFittable);
		Matrix newCorr, newCovar;
		int numIndividualCovarSummed = 0;
		int numIndividualCorrSummed = 0;
		int numBulkCovarSummed = 0;
		int numBulkCorrSummed = 0;
		int numAnalyzed = 0;
		int numMax = Integer.MAX_VALUE;
		while(!EOF && numIndividualCovarSummed < numMax && numIndividualCorrSummed < numMax && numAnalyzed < numMax) {
			try {
				o = ois.readObject(); 
				//System.out.print("File: " + fRoot + "\tSimulation number: " + idx++);
			} catch(EOFException eof) {
				EOF = true;
			} catch (IOException e) {
				EOF = true;
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				EOF = true;
			} catch (Exception e) {
				EOF = true;
			}  catch(Throwable t) {
				EOF = true;
			}
			if(!EOF && o instanceof Simulation) {
				s = (Simulation) o;
				SimulSetupParams sp = s.getSSP();
				if(sp == null) {
					System.out.println("ssp is null");
					continue;
				}
//				a.fName =  fRoot + "_" + idx++;
				a.numAnalyzed = ++numAnalyzed;
				int[] sampleAxes = s.getSample().getUnitsPerAxis();
				s.getSSP().setSampleUnitsPerAxis(s.getSample().getUnitsPerAxis());
				double aspectRatioVal = ((double) sampleAxes[0]) / ((double) sampleAxes[2]); 
				//System.out.print("n: " + n + " ");
				//System.out.print("units: " + StringConverter.arrayToTabString(s.getSSP().sampleUnitsPerAxis));
				try {
					if(aspectRatio) {
						sp.setInit_n(a.linearInterpolate(aspectRatioVal));
					} else {
						sp.setInit_n(n);
					}
					excel = a.analyze(s, sp.getFittableParameterSelection(), sp.getDimensionalitySelection());
					try {
						newCorr = a.getIndividualCorr();
						newCovar = a.getIndividualCovar();
						if(isValid(individualCorr) && isValid(newCorr)) { 
							individualCorr = individualCorr.plus(newCorr);
							numIndividualCorrSummed++;
						}
						if(isValid(individualCovar) && isValid(newCovar)) {
							individualCovar = individualCovar.plus(newCovar);
							numIndividualCovarSummed++;
						}
						newCorr = a.getBulkCorr();
						newCovar = a.getBulkCovar();
						if(isValid(individualCorr) && isValid(newCorr)) { 
							bulkCorr = bulkCorr.plus(newCorr);
							numIndividualCorrSummed++;
						}
						if(isValid(bulkCovar) && isValid(newCovar)) {
							bulkCovar = bulkCovar.plus(newCovar);
							numIndividualCovarSummed++;
						}
						
					} catch (IllegalArgumentException iae) {
						System.out.println("Errored");
						
					} catch(RuntimeException re) {
						System.out.println("Errored");
						regressionFail = true;
					}
					//System.out.print(".");
					ps.print(excel);
					print(excel);
					String bulk = a.getBulkString();
					if(bulk.compareTo("") != 0) {
						ps_bulk.println(a.getBulkString());
					}
					String individual = a.getIndividualString();
					if(individual.compareTo("") != 0) {
						ps_individual.print(individual);
					}
					//ps_debug.println(a.getParams());
					//ps_debug.println(a.getError());
					//System.out.print(".");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Errored");
					e.printStackTrace();
				}
				if(a.printObject) {
					a.printObject = false;
					printObject(s, numAnalyzed);
				}
			}
			//System.out.println("analyzed");
		}
		if(!regressionFail) {
			numIndividualCorrSummed = (int) Math.round(individualCorr.get(0, 0));
			numIndividualCovarSummed = numIndividualCorrSummed;
			numBulkCorrSummed = (int) Math.round(bulkCorr.get(0, 0));
			numBulkCovarSummed = numBulkCorrSummed;
			ps.println("Total correlation matrices summed: " + numIndividualCorrSummed);
			ps.println("Total covariance matrices summed: " + numIndividualCovarSummed);
			addToIndividualCorr(individualCorr);
			addToIndividualCovar(individualCovar);
	
			addToBulkCorr(bulkCorr);
			addToBulkCovar(bulkCovar);
			
			individualCorr = individualCorr.times(1./((double) numIndividualCorrSummed));
			individualCovar = individualCovar.times(1./((double) numIndividualCovarSummed));
	
			bulkCorr = bulkCorr.times(1./((double) numBulkCorrSummed));
			bulkCovar = bulkCovar.times(1./((double) numBulkCovarSummed));
			
			String individualCorrOut = "Individual Correlation matrix:\n";
			double[][] d = individualCorr.getArray();
			for(int i = 0; i < d.length; i++) {
				individualCorrOut += StringConverter.arrayToTabString(d[i]) + "\n";
			}
			
			String individualCovarOut = "Individual Covariance matrix:\n";
			d = individualCovar.getArray();
			for(int i = 0; i < d.length; i++) {
				individualCovarOut += StringConverter.arrayToTabString(d[i]) + "\n";
			}
			
			String bulkCorrOut = "Bulk Correlation matrix:\n";
			d = bulkCorr.getArray();
			for(int i = 0; i < d.length; i++) {
				bulkCorrOut += StringConverter.arrayToTabString(d[i]) + "\n";
			}
			
			String bulkCovarOut = "Bulk Covariance matrix:\n";
			d = bulkCovar.getArray();
			for(int i = 0; i < d.length; i++) {
				bulkCovarOut += StringConverter.arrayToTabString(d[i]) + "\n";
			}
			
			ps_Error.println(fRoot);
			ps_Error.println(individualCorrOut);
			ps_Error.println(individualCovarOut);
			ps_Error.println(bulkCorrOut);
			ps_Error.println(bulkCovarOut);
			
			try {
				zipPrinter.closeStream();
			} catch (ZipException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		finished();
		check();
		
	}
	private void printXYZ(String fRoot, Simulation simul, boolean movie) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fRoot);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream ps = new PrintStream(fos);
		simul.getSample().printXYZ(ps, movie);
		ps.close();
		try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private synchronized void addToIndividualCorr(Matrix corr) {
		if(individualTotalCorr == null) {
			individualTotalCorr = (Matrix) corr.clone();
		} else {
			individualTotalCorr = individualTotalCorr.plus(corr);
		}
	}
	private synchronized void addToIndividualCovar(Matrix covar) {
		if(individualTotalCovar == null) {
			individualTotalCovar = (Matrix) covar.clone();
		} else {
			individualTotalCovar = individualTotalCovar.plus(covar);
		}
	}
	private synchronized void addToBulkCorr(Matrix corr) {
		if(bulkTotalCorr == null) {
			bulkTotalCorr = (Matrix) corr.clone();
		} else {
			bulkTotalCorr = bulkTotalCorr.plus(corr);
		}
	}
	private synchronized void addToBulkCovar(Matrix covar) {
		if(bulkTotalCovar == null) {
			bulkTotalCovar = (Matrix) covar.clone();
		} else {
			bulkTotalCovar = bulkTotalCovar.plus(covar);
		}
	}
	private void printObject(Simulation s, int idx) {
		FileOutputStream fos;
		ObjectOutputStream oos;
		try {
			fos = new FileOutputStream(idx + ".obj");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(s);
			oos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private int numbersTested = 0;
	public boolean isValid(Matrix m) {
		if(m == null) { return false; }
		double[][] d = m.getArray();
		if(d.length != numFittable && d[0].length != numFittable) {
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
	private static int numThreadsLaunched = 0;
	private static int numThreadsFinished = 0;
	private static int errorValIdx = 0;
	private static int alphaMinValIdx = 0;
	private static int alphaMaxValIdx = 0;
	//private static double[] errorVals = new double[] {0.0, 0.01, 0.02, 0.03, 0.04};
	private static double[] errorVals = new double[] {0.00};
	//private static double[] alphaMinVals = new double[] {0, 1, 2, 3, 4};
//	private static double[] alphaMinVals = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
	private static double[] alphaMinVals = new double[] {0};
	private static double[] alphaMaxVals = new double[] {0.5};
//	private static String[] fRoots = { "625_DSC26.5", "625_DSC12.2", "NSLS_5mm1.0", "NSLS_7mm1.0", "APS_5mm1.0", "APS_7mm1.0"};
	private static String[] fRoots = { "CUSTOM1.0" };
	private final static String NOTEBOOK_NAME = "EDD";
	private final static String NOTEBOOK_NUMBER = "10";
	private final static String PAGE_NUMBER = "191";
	private static AlphabeticIndexingSystem pageIdx = new AlphabeticIndexingSystem("u");
	private static String[] get_fRoots() {
		Vector<String> roots = new Vector<String>();
		File folder = new File("D:\\$research\\programming\\workspace\\Kinetics4\\obj files");
		if(folder.isDirectory()) {
			File[] files = folder.listFiles();
			for(int i = 0; i < files.length; i++) {
				if(files[i].isFile() && files[i].toString().contains(".obj") ) {
					roots.add(files[i].toString().substring(0, files[i].toString().length()-4));
				}
			}
			
			String[] tempRoots = new String[roots.size()];
			tempRoots = roots.toArray(tempRoots);
			for(int i = 0; i < tempRoots.length; i++) {
				tempRoots[i] = tempRoots[i].substring(tempRoots[i].lastIndexOf('\\')+1);
			}
			return tempRoots;
		} else { throw new RuntimeException("folder: " + folder.getAbsolutePath() + " is not registering as a folder.");}
	}
	private static String[] getFiles() {
		Vector<String> roots = new Vector<String>();
		File folder = new File("D:\\$research\\programming\\workspace\\Kinetics4\\obj files");
		File[] files;
		if(folder.isDirectory()) {
			files = folder.listFiles();
			for(int i = 0; i < files.length; i++) {
				if(files[i].isFile() && files[i].toString().contains(".obj") ) {
					roots.add(files[i].toString().substring(0, files[i].toString().length()-4));
				}
			}
		}
		return null;
	}
	public static void check() {
		if(numThreadsFinished == numThreadsLaunched && errorValIdx < errorVals.length) {
			String notebookName = NOTEBOOK_NAME + "_" + NOTEBOOK_NUMBER + "-" + PAGE_NUMBER + pageIdx.getName();
			numThreadsFinished = 0;
			numThreadsLaunched = 0;
			double errorVal = errorVals[errorValIdx];
			double alphaMinVal = alphaMinVals[0] * 0.01;
			if(alphaMinVals.length > 1) {
				alphaMinVal = alphaMinVals[alphaMinValIdx] * .01;
			}
			double alphaMaxVal = alphaMaxVals[alphaMaxValIdx];
			//if(errorVal == 0) { alphaMinVal = errorVals[alphaMinValIdx]; }
			System.out.println("ErrorVal: " + errorVal + "\t" + "Alpha min val: " + alphaMinVal + "\t" + "Alpha max val: " + alphaMaxVal);
			fRoots = get_fRoots();
			try {
				dispatch(fRoots, errorVal, alphaMinVal, alphaMaxVal, notebookName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			alphaMinValIdx++;
			alphaMaxValIdx++;
			if(alphaMinVals.length != 1) {
				if(alphaMinValIdx == alphaMinVals.length) {
					alphaMinValIdx = 0;
					errorValIdx++;
				} else if(errorValIdx == errorVals.length && alphaMinValIdx == alphaMinVals.length) {
					System.out.println(pageIdx.getName() + " set is done.");
				}
			}
			if(alphaMaxVals.length != 1) {
				if(alphaMaxValIdx == alphaMaxVals.length) {
					alphaMaxValIdx = 0;
					errorValIdx++;
				} else if(errorValIdx == errorVals.length && alphaMaxValIdx == alphaMaxVals.length) {
					System.out.println(pageIdx.getName() + " set is done.");
				}
			}
			pageIdx.update();
		}
	}
	private static Executor e;
	public static void main(String[] args) {
		int nThreads = 2;
		e = Executors.newFixedThreadPool(nThreads);
		check();
	}
	
}
