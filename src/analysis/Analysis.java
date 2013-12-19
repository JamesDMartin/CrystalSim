package analysis;

import io.MyFileInputStream;
import io.MyPrintStream;
import io.PrintToZip;
import io.StringConverter;
import jama.Matrix;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Observable;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.zip.ZipException;

import simulation.Crystal;
import simulation.IndividualCrystalliteAnisotropy;
import simulation.Sample;
import simulation.SimulSetupParams;
import simulation.SimulSetupParams.DimensionalityOptions;
import simulation.SimulSetupParams.FittableParametersOptions;
import simulation.SimulSetupParams.FittingType;
import simulation.Simulation;
import _TESTING_regression.RegressionException;
import _TESTING_regression.RegressionNonLinear;
import _TESTING_regression.RegressionXY;
import _TESTING_regressionModel.Bounds;
import _TESTING_regressionModel.Bounds.BoundingCondition;
import _TESTING_regressionModel.RegressionModel;
import _TESTING_regressionModel.RegressionModel.ParameterOptions;
import _TESTING_regressionModel.SharpHancockModel;
import _TESTING_regressionModel.XJMatrixPlugin_SharpHancockNonLinear;
import equations_1d.Avrami;
import equations_1d.Equation1;
import equations_1d.SharpHancock;
import fitting.NonLinearFitting;
import geometry.JVector;

public class Analysis extends Observable {

	private Matrix individualCorr, individualCovar, bulkCorr, bulkCovar;
	public Matrix getIndividualCorr() {
		return individualCorr; 
	}
	public Matrix getIndividualCovar() {
		return individualCovar; 
	}
	public Matrix getBulkCorr() {
		return bulkCorr; 
	}
	public Matrix getBulkCovar() {
		return bulkCovar; 
	}
	public final static int NONLINEAR_LINEAR = 1;
	public final static int NONLINEAR_SIGMOIDAL = 0;
	private NonLinearFitting nlf;
	private RegressionXY r;
	private RegressionModel model;
	private Simulation simul;
	private String bulkInfo, individualInfo;
	public String getBulkString() { return bulkInfo; }
	public String getIndividualString() { return individualInfo; }
	public boolean printObject = false;
	public int numAnalyzed=0;
	private int delayNucleationTimeBy = 50;
	private Equation1 e = new SharpHancock(new double[] {1, 1, 1});
	private Random rand = new Random();
	private int numFittable = 0;
	public double fractionError = 0.0;
	public double alphaMin = 0.00;
	public double alphaMax = 0.5;
	private double t0Offset = +0;
	private double nOffset = 0;
	private double kAValue = .01;
	private double tolerance = 0.05;
	private RegressionXY reg = new RegressionNonLinear();
	private double[][] nAsFuncOfAspectRatio;
	public PrintToZip zipPrinter;
	public int height = 0;
	public Analysis() {}
	private MyPrintStream mps;
	private String standardHeader;
	/**
	 * 0 = no
	 * 1 = use the sample height to calculate the anisotropy
	 * 2 = use the IndividualCrystalliteAnisotropy class to do a more precise evaluation of the dimensionality
	 */
	public int estimateN = 0;
	private void initHeaders() {
		standardHeader = "Nucleation value,number of initial nucleation events,volume increment for new nucleation event,nucleation value,probability of continuous nucleation,nucleation orientation strategy,nucleation axis 1 growth rate,nucleation axis 2 growth rate,nucleation axis 3 growth rate,crystal axis 1 initial size,crystal axis 2 initial size,crystal axis 3 initial size,nucleation location strategy,nucleation shape,termination strategy,termination strategy value,Experimental setup,experimental value,Sample container shape,container shape axis 1,container shape axis 2,container shape axis 3,natural log of d/h,xyz output file,xyz output?,transformation output file,transformation output?,time step,start time,max num crystals,alpha min,alpha max,which parameters are fit,number of fittable crystallites,this file name,alpha min fit,fractional error imposed,crystal index or bulk,nuc loc x,nuc loc y,nuc loc z,nuc time,kA,t0,n,kA err,t0 err,n err,sum of squares,zipped file name for fit output,degrees of freedom,crystallite size,fraction of whole,nucleation shape ordinal,total sample volume,crystallite axis 1,crystallite axis 2,crystallite axis 3,CM x,CM y,CM z,approximate dimensionality,anisotropy"; 
		String[] arr = standardHeader.split(",");
		standardHeader = StringConverter.arrayToTabString(arr) + "\n";
	}
	/**
	 * 
	 * @param whichFitting<br>0 = sigmoidal fits<br>1 = sharp-hancock
	 */
	public Analysis(FittingType fittingType) {
		switch(fittingType) {
		case sigmoidal: reg = new NonLinearFitting();
			break;
		case sharp_hancock: reg = new RegressionNonLinear();
			break;
		}
		initHeaders();
	}
	public double[][] getNormalizedTimeAndAllData(Simulation s) {
		simul = s;
		Sample sample = s.getSample();
		SimulSetupParams ssp = s.getSSP();
		double totalTime = s.getTime();
		int timeSteps = (int) Math.round(totalTime / ssp.getTimeStep());
		double[] time = new double[timeSteps];
		// set up time array
		for(int i = 0; i < time.length; i++) {
			time[i] = i * ssp.getTimeStep();
		}
		// set up data arrays
		// find how many crystals grew
		int grew = 0;
		Iterator<Crystal> iter = sample.getCrystalIterator();
		Crystal c;
		while(iter.hasNext()) {
			c = iter.next();
			if(c.getTotalSize() > 0) {
				grew++;
			}
			
		}
		double[][] data = getTimeAndAllData(timeSteps, grew, sample, ssp);
		integrate(data);
		normalize(data);
		double[][] timeAndAllData = new double[data.length][data[0].length+1];
		for(int i = 0; i < timeAndAllData.length; i++) {
			timeAndAllData[i][0] = time[i];
			for(int j = 1; j < timeAndAllData[i].length; j++) {
				timeAndAllData[i][j] = data[i][j-1];
			}
		}
		return timeAndAllData;
	}
	private void initializeRegression(double[] time, double[] data, boolean[] isFittable, double[] parameters, RegressionXY reg) {
 		double[][] timeAndData = new double[time.length][2];
		for(int i= 0; i < time.length; i++) {
			timeAndData[i][0] = time[i];
			timeAndData[i][1] = data[i];
		}
		Bounds[] xyBounds = new Bounds[2];
		xyBounds[0] = new Bounds();
		xyBounds[1] = new Bounds();
		xyBounds[1].addBound(alphaMin, BoundingCondition.GREATER_THAN);
		xyBounds[1].addBound(alphaMax, BoundingCondition.LESS_THAN_OR_EQUAL_TO);
		r = reg;
		model = new SharpHancockModel(parameters, isFittable, null);
		r.setModel(model);
		r.setData(timeAndData);
		r.setBounds(xyBounds);
	}
	private String dataTimeFitToString(double[][] converted, double[] fit, double[] params) {
		String longString = StringConverter.arrayToTabString(params) + "\n";

		if(converted.length < converted[0].length) { 
			for(int i = 0; i < converted[0].length; i++) {
				longString += converted[0][i] + "\t" + converted[1][i] + "\t" + fit[i] + "\n";
			}
		} else { 
			for(int i = 0; i < converted.length; i++) {
				longString += converted[i][0] + "\t" + converted[i][1] + "\t" + fit[i] + "\n";
			}
		}
		
		return longString;
	}
	private void readNVals() {
		File f = new File("D:\\Documents referenced in lab notebooks\\Dill-10\\178\\EDD_10-178i.txt");
		MyFileInputStream mfis = new MyFileInputStream(f);
		Scanner s = mfis.getScanner();
		Vector<double[]> vals = new Vector<double[]>();
		
		String[] line;
		double val1, val2;
		while(s.hasNextLine()) {
			line = s.nextLine().split("\t");
			val1 = Double.valueOf(line[0]);
			val2 = Double.valueOf(line[1]);
			vals.add(new double[] {val1, val2});
		}
		
		nAsFuncOfAspectRatio = new double[vals.size()][2];
		nAsFuncOfAspectRatio = vals.toArray(nAsFuncOfAspectRatio);
	}
	public double linearInterpolate(double aspectRatio) {
		double n1, n2, navg, asp1, asp2;

		
		if(nAsFuncOfAspectRatio == null) {
			readNVals();
		}
		
		double[][] nfunc = nAsFuncOfAspectRatio;
		
		int i = 0;
		while(aspectRatio > nfunc[i][0]) {
			i++;
		}
		if(i != 0) {
			n1 = nfunc[i-1][1];
			n2 = nfunc[i][1];
			asp1 = nfunc[i-1][0];
			asp2 = nfunc[i][0];
		
			double x = (aspectRatio - asp1) / (asp2 - asp1);
			double n = n1 * (1-x) + n2 * x;
			
			return n;
		} 
		return nfunc[0][1];
	}
	private double getN(double volume, double h) {
		double a = Math.sqrt(volume/h);
		double aspectRatio = a/h;
		
		return linearInterpolate(aspectRatio);
	}
	/**
	 * 
	 * @param s
	 * @param fittingSelection <br> 0 = k <br> 1 = tau <br> 2 = n <br> 3 = none <br> 4 = k/tau fixed<br>5 = k/n fixed
	 * <br>6 = tau/n fixed<br>7 = all fixed
	 * @param nAsFnAspectRatio true = use n as f(d/h) <br> false = use n=ssp.init_n
	 * @return
	 * @throws IOException
	 */
	public String analyze(Simulation s, 
			FittableParametersOptions fittingSelection, 
			DimensionalityOptions dimensionalitySelection) 
					throws IOException {
		simul = s;
		int analysisNumber = simul.getSimulationIndex();
		Sample sample = s.getSample();
		SimulSetupParams ssp = s.getSSP();
		double totalTime = s.getTime() + delayNucleationTimeBy;
		int timeSteps = (int) Math.round(totalTime / ssp.getTimeStep());
		double[] time = new double[timeSteps];
		// set up time array
		for(int i = 0; i < time.length; i++) {
			time[i] = i * ssp.getTimeStep();
		}
		boolean output = true;

		String zipFolderName = ssp.getZipFile().getAbsolutePath();
		zipFolderName = zipFolderName.substring(0, zipFolderName.lastIndexOf("."));
		File fitFolder = new File(zipFolderName);
		fitFolder.mkdirs();
		
		Vector<Crystal> fittableXtals = new Vector<Crystal>();
		// set up data arrays
		// find how many crystals grew
		int grew = 0;
		Iterator<Crystal> iter = sample.getCrystalIterator();
		Crystal c = null;
		while(iter.hasNext()) {
			c = iter.next();
			if(c.getTotalSize() > 0) {
				grew++;
				fittableXtals.add(c);
			}
			
		}
		/*JVector[][] sortedList = simul.getSample().getLatticePoints();
		CrystalliteAnisotropy ca = new CrystalliteAnisotropy(sortedList);
		ca.findAllPoints();
		double[][] anisotropyDistances = ca.getDistances();
		JVector[] anisotropyCM = ca.getAllCM();
		*/
		double[][] data = getTimeAndAllData(timeSteps, grew, sample, ssp);
		integrate(data);
		double[] max = Arrays.copyOf(data[data.length-1], data[data.length-1].length);
		normalize(data);
		addError(data, fractionError);
		/*
		for(int i = 0; i < data.length; i++) {
			System.out.println(Arrays.toString(data[i]));
		}
		 */
		double t0_est=10+delayNucleationTimeBy, kA_est = ssp.getInit_k_factor(), n_est=ssp.getInit_n();//n=(77.+77.+8.)/77.;
		boolean[] isFittable = {true, true, true};
		switch(fittingSelection) {
		case FIT_T_N: 
			isFittable = new boolean[] {false, true, true};
			break;
		case FIT_K_N: 
			isFittable = new boolean[] {true, false, true};
			break;
		case FIT_K_T: 
			isFittable = new boolean[] {true, true, false};
			break;
		case FIT_K_T_N:
			isFittable = new boolean[] {true, true, true};
			break;
		case FIT_N:
			isFittable = new boolean[] {false, false, true};
			break;
		case FIT_T:
			isFittable = new boolean[] {false, true, false};
			break;
		case FIT_K:
			isFittable = new boolean[] {true, false, false};
			break;
		case FIT_NONE:
			isFittable = new boolean[] {false, false, false};
			break;
		}
		numFittable = numFittable(isFittable);
		individualCorr = new Matrix(numFittable, numFittable);
		individualCovar = new Matrix(numFittable, numFittable);
		double[] params = new double[] {kA_est, t0_est, n_est};
		e.setParams(params);
		//nlf = new NonLinearFitting(null, time, e, null, isFittable, ssp.alphaMin, ssp.alphaMax);
		double[] stdDev;
		String excel = standardHeader;
		Matrix m = new Matrix(data);
		Matrix mT = m.transpose();
		String prefix, xtalInfo, simulInfo;
		double[][] transposed = mT.getArray();
		int vol;
		int startVal = 0;
		if(transposed.length == 2) {
			startVal = 1;
		}
		int bulkVol = 0;
		bulkInfo = "";
		individualInfo = "";
		int[] axes = sample.getUnitsPerAxis();
		Matrix tempCorr, tempCovar;
		String printFitName;
		IndividualCrystalliteAnisotropy ica = null;
		CrystalliteAnalysis ca = null;
		long totalVolume = s.getTotalVolume();
		JVector[] latticePoints;
		int curVol = 0;
		for(int i = transposed.length-1; i >= 0; i--) {
			
			vol = (int)max[i];
			kA_est = Math.pow(vol, -1./3.)*ssp.getAxialGrowthRates()[0] * ssp.getInit_k_factor();
			//kA_est = 0.01;
			//e.setParam(kA_est, 0);
			
			//n = ((double) (4*axes[0]+axes[2])) /((double) (Math.max(2*axes[0], axes[2])));
			//e.setParam(n, 2);
			
			if(i != transposed.length-1) {
				c = fittableXtals.get(i);
				latticePoints = c.getLatticePoints();
				curVol = latticePoints.length;
				double fractionalCrystalliteSize = ((double) curVol) / ((double) totalVolume);
				if(fractionalCrystalliteSize >= 0.05) {
					ca = new CrystalliteAnalysis(c);
					ca.get50PercentCrystallizedLattice(tolerance);
					ica = new IndividualCrystalliteAnisotropy(latticePoints);
				} else {
					ica = null;
				}
				t0_est = fittableXtals.get(i).getNucTime()+this.delayNucleationTimeBy;
				switch(dimensionalitySelection) {
				case FIXED_TO_VALUE:
					n_est = ssp.getInit_n();
					break;
				case APPROXIMATE_BY_ACTUAL_SHAPE:
					if(ica != null) {
						n_est = ica.getApproximateDimensionality();
						break;
					} else {
						n_est = 3;
						break;
					}
				case APPROXIMATE_BY_SAMPLE_SHAPE:
					double h = (double) sample.getUnitsPerAxis()[2];
					double d = 2*Math.sqrt(vol/Math.PI/h);
					n_est = linearInterpolate(d/h);
					break;
				}
					//System.out.println("Crystallite volume: " + vol + " h: " + h + " d: " + d + " n_est: " + n_est);
			} else {
				t0_est = 10+this.delayNucleationTimeBy;
				switch(dimensionalitySelection) {
				case FIXED_TO_VALUE:
					n_est = ssp.getInit_n();
					break;
				case APPROXIMATE_BY_ACTUAL_SHAPE:
				case APPROXIMATE_BY_SAMPLE_SHAPE:
					double h = (double) sample.getUnitsPerAxis()[2];
					double d = (double) sample.getUnitsPerAxis()[0];
					n_est = linearInterpolate(d/h);
					break;
					//System.out.println("Total sample volume: " + vol + " h: " + h + " d: " + d + " n_est: " + n_est);
				default:
					n_est = 3;
					break;
				}
			}
			t0_est += t0Offset;
			n_est += nOffset;
			//kA_est = kAValue;
			initializeRegression(time, transposed[i], isFittable, new double[] {kA_est, t0_est, n_est}, reg);
			if(r instanceof NonLinearFitting) {
				((NonLinearFitting) r).setIsFittable(Arrays.copyOf(isFittable, isFittable.length));
				((NonLinearFitting) r).setEquation(new Avrami(new double[] {kA_est, t0_est, n_est}));
				((NonLinearFitting) r).setData(transposed[i]);
				((NonLinearFitting) r).setTime(time);
				((NonLinearFitting) r).setAlphaMax(alphaMax);
				((NonLinearFitting) r).setAlphaMin(alphaMin);
			}
			r.setXJMatrixPlugin(new XJMatrixPlugin_SharpHancockNonLinear());
			//nlf.setData(transposed[i]);
			int fittingProblems = 0;
			try {
				r.doFit();
			} catch(RegressionException re) {
				fittingProblems = 1;
			}
			//int fittingProblems = nlf.go();
			double percentOfWhole;
			String anisotropy = "";
			
			switch(fittingProblems) {
			case 0:
				double[] fit = r.getFit();
				params = model.getAllParameters();
				stdDev = model.getAllStandardDeviations();
				tempCovar = r.getCovariance();
				tempCorr = r.getCorrelation();
				double[][] converted = r.getConvertedData();
				if(r instanceof NonLinearFitting) {
					converted = new double[2][];
					converted[0] = ((NonLinearFitting) r).getTime();
					converted[1] = ((NonLinearFitting) r).getData();
					params = ((NonLinearFitting) r).getParams();
					stdDev = ((NonLinearFitting) r).getStdDev();
					if(stdDev.length != isFittable.length) {
						double[] oldStdDev = stdDev;
						stdDev = new double[isFittable.length];
						int idxNew = 0, idxOld = 0;
						for(boolean b : isFittable) {
							if(b)
								stdDev[idxNew++] = oldStdDev[idxOld++];
							else
								stdDev[idxNew++] = -1;
						}
					}
					tempCovar = ((NonLinearFitting) r).getCovar();
					tempCorr = ((NonLinearFitting) r).getCorr();
					fit = ((NonLinearFitting) r).getUnboundedFit();
				}
				printFitName = ssp.getFitsFile(s.getSimulationIndex()).getName() + "_";
				// if individual crystallite 
				if(i != transposed.length-1) {
					
					printFitName += i;
					if(isValid(tempCovar)) {
						individualCovar = individualCovar.plus(tempCovar);
					}
					if(isValid(tempCorr)) {
						individualCorr = individualCorr.plus(tempCorr);
					}
					if(output) { mps.println("Crystal: " + i); }
					prefix = i + "";
					xtalInfo = getXtalInfo(fittableXtals.get(i));
					percentOfWhole = ((double) curVol) / ((double) totalVolume);
					String out = StringConverter.arrayToTabString(params);
					out += StringConverter.arrayToTabString(stdDev);
					if(stdDev.length != model.getAreParametersFittable().length) {
						int numTabsToAdd = 2 - stdDev.length;
						for(int idx = 0; idx < numTabsToAdd; idx++) {
							out+= "\t";
						}
					}
					out += getSumSq() + "\t" + curVol + "\t";
					out += totalVolume + "\t" + fittableXtals.get(i).getNucTime() + "\n";
					individualInfo += out;
					if(ica != null) {
						anisotropy = StringConverter.arrayToTabString(ica.getAxisLengths());
						anisotropy += StringConverter.arrayToTabString(ica.getCM().toArray());
						anisotropy += ica.getApproximateDimensionality() + "\t";
						anisotropy += ica.getAnisotropy() + "\t";
					} else {
						anisotropy = "";
					}
					double t0 = model.getSpecificParameter(ParameterOptions.t0);
					double tnuc = fittableXtals.get(i).getNucTime();
					if((t0-tnuc)/r.getDegreesOfFreedom() > .6) {
						String print = getSimulInfo(ssp) + "\t" + fittableXtals.size() + "\t" + ssp.getFitsFile(s.getSimulationIndex()).getName() + "\t" + alphaMin + "\t" + fractionError + "\t" +  
								prefix + "\t" + xtalInfo + "\t" + StringConverter.arrayToTabString(params) + "\t" + 
								StringConverter.arrayToTabString(stdDev) + "\t" + getSumSq() + "\t" + r.getDegreesOfFreedom() + "\t" + max[i] + "\t" + percentOfWhole + "\t" + 
								ssp.getNucShape().ordinal() + "\t" + bulkVol + "\t" + anisotropy + "\n";
						//printFit(numAnalyzed + ".txt", nlf.getTimeDataFit(), print);
					}
				} 
				// if bulk
				else {
					printFitName += "bulk";
					if(isValid(tempCovar)) {
						bulkCovar = individualCovar.plus(tempCovar);
					}
					if(isValid(tempCorr)) {
						bulkCorr = individualCorr.plus(tempCorr);
					}
					if(output) {mps.println("Bulk: "); }
					prefix = "bulk";
					xtalInfo = getXtalInfo(null);
					percentOfWhole = 1;
					String out = StringConverter.arrayToTabString(params);
					out += StringConverter.arrayToTabString(stdDev);
					out += getSumSq() + "\t" + totalVolume;
					bulkInfo = out;
					bulkVol = (int) totalVolume;
				}
				try {
					// make new print stream to output the fits to a file
					MyPrintStream mps = new MyPrintStream(new File(fitFolder + File.separator + printFitName));
					
					// make a new entry in the zipped folder
					zipPrinter.newEntry(printFitName);
					
					// get the data to print to file/zip
					String dataTimeFit = dataTimeFitToString(converted, fit, params);
					
					// print the data to the zip file
					zipPrinter.print(dataTimeFit);
					
					// print the data to the raw file
					mps.println(dataTimeFit);
					
					// close&flush the file print stream
					mps.close();
					
					// and close the zip printer entry
					zipPrinter.closeEntry();
				} catch(ZipException e) {
					e.printStackTrace();
				} catch(IOException e) {
					e.printStackTrace();
				}
				String sumsq = getSumSq() + "";
				if(sumsq.compareTo("") == 0) {
					System.err.println("get sum of squares failed");
				}
				simulInfo = getSimulInfo(ssp);
				excel += simulInfo + fittableXtals.size() + "\t" + ssp.getFitsFile(s.getSimulationIndex()).getName() + "\t" + alphaMin + "\t" + fractionError + "\t" + 
						prefix + "\t" + xtalInfo + "\t" + StringConverter.arrayToTabString(params) + "\t" + 
						StringConverter.arrayToTabString(stdDev) + "\t" + sumsq + "\t" + printFitName + "\t" + r.getDegreesOfFreedom() + "\t" + max[i] + "\t" + 
						percentOfWhole + "\t" + ssp.getNucShape().ordinal() + "\t" + bulkVol + "\t" + anisotropy + "\n";
				if(output) {
					mps.println(ssp.getFittableParameterSelection().toString() + "\t" + StringConverter.arrayToTabString(params)); 
				}
				if(output) {
					mps.println("\tStandard Deviation of " + ssp.getFittableParameterSelection().toString() + "\t" + StringConverter.arrayToTabString(stdDev)); 
				}
				break;
			default:
				
			}
			
		}
		if(output) {mps.println("kA\tt0\tn\tkA-stdDev\tt0-stdDev\tsize\tnucShape"); }
		if(output) {mps.println(excel); }
		return excel;
	}
	private int numFittable(boolean[] isFittable) {
		int numFittable = 0;
		for(int i = 0; i < isFittable.length; i++) {
			if(isFittable[i]) { numFittable++; }
		}
		return numFittable;
	}
	public boolean isValid(Matrix m) {
		if(m == null) { return false; }
		double[][] d = m.getArray();
		if(d.length != numFittable && d[0].length != numFittable) {
			return false;
		}
		double val;
		for(int i = 0; i < d.length; i++) {
			for(int j = 0; j < d[i].length; j++) {
				val = d[i][j];
				boolean isInf = Double.isInfinite(val);
				boolean isNaN = Double.isNaN(val);
				if(isInf || isNaN) {
					return false;
				}
			}
		}
		return true;
	}
	private String getXtalInfo(Crystal c) {
		String xtal = "";
		// nucLoc
		if(c == null) 
			xtal += "NA\tNA\tNA";
		else
			xtal += StringConverter.arrayToTabString(c.getNucLoc().toArray());
		// nuc time
		if(c == null) 
			xtal += "\t10"; 
		else 
			xtal += "\t" + c.getNucTime(); 
		
		return xtal;
		
	}
	public double getSumSq() { return r.getSumOfSquares(); }
	public double getN() { return nlf.getParams()[2]; }
	public double getVolume() { return simul.getTotalVolume(); }
	private String getSimulInfo(SimulSetupParams ssp) {
		String simul = "";
		// initial nucleation strategy
		simul += ssp.getInitNuc().toString() + "\t" + ssp.getNucInitVal() + "\t" + ssp.getVolumeIncrement() + "\t";
		// continuous nucleation strategy
		simul += ssp.getContNuc().toString() + "\t" + ssp.getNucContVal() + "\t";
		// nucleation orientation strategy
		simul += ssp.getNucOrient().toString() + "\t";
		// axial growth rates
		simul += StringConverter.arrayToTabString(ssp.getAxialGrowthRates()) + "\t";
		// initial nucleated crystalline dimensions
		simul += StringConverter.arrayToTabString(ssp.getInitCrystalDimensions()) + "\t";
		// nucleated crystal orientation strategy
		simul += ssp.getNucLoc().toString() + "\t";
		// nucleation shape
		simul += ssp.getNucShape().toString() + "\t";
		// termination strategy
		simul += ssp.getTerm().toString() + "\t" + ssp.getTermVal().toString() + "\t";
		// experimental sample type
		simul += ssp.getExp().toString() + "\t";
		// experimental value
		simul += ssp.getExpVal() + "\t";
		// sample shape
		simul += ssp.getSampleShape().toString() + "\t";
		// sample units per axis
		simul += StringConverter.arrayToTabString(ssp.getSampleUnitsPerAxis()) + "\t";
		// ln(d/h)
		simul += (Math.log(ssp.getSampleUnitsPerAxis()[0]*2/ssp.getSampleUnitsPerAxis()[2])) + "\t";
		// xyz file
		simul += ssp.getXYZsFolder().getAbsolutePath() + "\t" + ssp.isXyz() + "\t";
		// trans file
		simul += ssp.getTransformationsFolder().getAbsolutePath() + "\t" + ssp.isTransformed() + "\t";
		// delta t
		simul += ssp.getTimeStep() + "\t";
		// start of simulation
		simul += ssp.getStartTime() + "\t";
		// max num xtals
		simul += ssp.getMaxNumCrystals() + "\t";
		// min value of alpha
		simul += ssp.getAlphaMin() + "\t";
		// max value of alpha
		simul += ssp.getAlphaMax() + "\t";
		// parameters that are being fit
		simul += ssp.getFittableParameterSelection().toString() + "\t";
		return simul;
	}
	private void normalize(double[][] integrated) {
		double[] max = integrated[integrated.length-1];
		for(int i = 1; i < integrated.length; i++) {
			for(int j = 0; j < integrated[i].length; j++) {
				integrated[i][j] /= max[j];
			}
		}
	}
	private void addError(double[][] normalized, double percentError) {
		double val;
		for(int i = 1; i < normalized.length; i++) {
			for(int j = 0; j < normalized[i].length; j++) {
				val = rand.nextGaussian() * percentError;
				normalized[i][j] += val; 
				if(normalized[i][j] < 0) { normalized[i][j] = 0; }
				if(normalized[i][j] > 1) { normalized[i][j] = 1; }
			}
		}
	}
	private void integrate(double[][] data) {
		for(int i = 1; i < data.length; i++) {
			for(int j = 0; j < data[i].length; j++) {
				data[i][j] += data[i-1][j];
			}
		}
	}
	private double[][] getTimeAndAllData(int timeSteps, int grew, Sample sample, SimulSetupParams ssp) {
		double[][] data = new double[timeSteps][grew+1];
		double curTime = 0;
		int xtalIdx = 0;
		Crystal[] allXtals = sample.getCrystalArray();
		Integer[] growth;
		Vector<Crystal> fittableXtals = new Vector<Crystal>();
		Crystal c;
		for(int j = 0; j < data[0].length-1; j++) {
			do {
				c = allXtals[xtalIdx++];
			} while(c.getTotalSize() == 0);
			growth = c.getGrowth();
			fittableXtals.add(c);
			int row = 0;
			for(int i = 0; i < data.length; i++) {
				curTime = i*ssp.getTimeStep();
				if(curTime < (c.getNucTime()+delayNucleationTimeBy)) {
					data[i][j] = 0;
				} else {
					data[i][j] = growth[row++];
				}
			}
		}
		
		// compile all rows into the total row in the last column
		int bulkIdx = data[0].length-1;
		int bulk = 0;
//		int[] size = new int[data[0].length-1];
		for(int i = 0; i < data.length; i++) {
			bulk = 0;
			for(int j = 0; j < data[0].length-1; j++) {
				bulk += data[i][j];
//				size[j] += data[i][j];
			}
			data[i][bulkIdx] = bulk;
//			size[data.length-1] += bulk;
		}
		
		for(int i = 0; i < data.length; i++) {
			//System.out.println(Arrays.toString(data[i]));
		}
		return data;
	}
	public String getParams() { return nlf.getParameters(); }
	public String getError() { return nlf.getError(); }
	public MyPrintStream getPs() {
		return mps;
	}
	public void setPrintStream(MyPrintStream ps) {
		this.mps = ps;
	}
}
