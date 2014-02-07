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
import indexing.AlphabeticIndexingSystem;
import indexing.NumericIndexingSystem;
import io.PrintToZip;
import io.StringConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.ZipOutputStream;

import shapes.ShapeTypes;

public class SimulSetupParams implements Serializable, Cloneable {
	/**
	 * 
	 */
	private final int idx;
	private static final long serialVersionUID = 1229179300198841270L;
	private volatile static int instances = 0;
	public SimulSetupParams() {
		idx = instances++;
		System.out.println("total instances of simulParams = " + instances);
	}
	public void nullify() {
		zipFits = null;
	}
	private FittableParametersOptions fittableParameterSelection = FittableParametersOptions.FIT_K_T;
	private DimensionalityOptions dimensionalitySelection = DimensionalityOptions.FIXED_TO_VALUE;
	
	private boolean emailWhenDone = false;
	private String emailAddress = "";
	private String emailUsername = "";
	private String emailPassword = "";
	
	private Nucleation initNuc = Nucleation.Initial;
	private int nucInitVal = 1;
	private int volumeIncrement = 3201277;
	private int numberOfSimulationsToRun = 50;
	private Nucleation contNuc = Nucleation.Continuous;
	private double nucContVal = .025;
	
	private NucleationOrientation nucOrient = NucleationOrientation.Random;
	private JVector[][] nucOrientVal = JVector.axes100;
	private double[] axialGrowthRates = { .5, .5, .5 };
	private double[] initCrystalDimensions = { 0, 0, 0 };
	
	private NucleationLocation nucLoc = NucleationLocation.Random;
	private JVector[] nucLocOptions = JVector.cube_corners;
	
	private ShapeTypes nucShape = ShapeTypes.Cubic;
	
	private Termination term = Termination.FractionComplete;
	private Object termVal = new Double(1);
	
	private ShapeTypes sampleShape = ShapeTypes.Cylindrical;
	private ExperimentalSample exp = ExperimentalSample.APS_5mm;
	private double expVal = 12.2;
	private JVector[] sampleUnitAxes = JVector.get100FamilyUnique();
	private int[] sampleUnitsPerAxis = {50, 50, 50};
	
	private File folderOutput = new File("output");
	private String fileRoot = "CrystallizationSimulation";
	private AlphabeticIndexingSystem ais = new AlphabeticIndexingSystem('a');
	private NumericIndexingSystem nis = new NumericIndexingSystem();
	/**
	 * 0 = alphabetic
	 * <br>1 = numeric
	 * <br>any other value = neither
	 */
	private int indexingSystemInUse = 0;
	/**
	 * 
	 * @param whichToUse0 = alphabetic
	 * <br>1 = numeric
	 * <br>any other value = neither
	 */
	private void setIndexingSystemToUse(int whichToUse) {
		indexingSystemInUse = whichToUse;
	}
	private String currentFileRoot = fileRoot;
	
	private PrintToZip zipFits;
	
	private boolean output = true;
	private boolean fit = true;
	private boolean xyz = true;
	private boolean movie = false;
	private boolean obj = true;
	private boolean transformed = true;
	
	private double timeStep = 0.5;
	private double startTime = 100;
	
	private int maxNumCrystals = Short.MAX_VALUE;	
	
	private int instancesToLaunch = 1;
	
	private int totalVolume;

	private double alphaMin = 0;
	private double alphaMax = 0.5;

	private double init_k_factor = 1;
	private double init_n = 3;
	private double init_tau = 0;
	private FittingType fittingType = FittingType.sigmoidal;
	
	public void copyAndIncrement(SimulSetupParams ssp) {
		copy(ssp);
		
	}
	public void copy(SimulSetupParams ssp) {
		emailWhenDone = ssp.emailWhenDone;
		emailAddress = String.copyValueOf(ssp.emailAddress.toCharArray());
		emailUsername = String.copyValueOf(ssp.emailUsername.toCharArray());
		emailPassword = String.copyValueOf(ssp.emailPassword.toCharArray());
		
		setInitNuc(ssp.getInitNuc());
		setNucInitVal(ssp.getNucInitVal());
		setVolumeIncrement(ssp.getVolumeIncrement());
		
		setContNuc(ssp.getContNuc());
		setNucContVal(ssp.getNucContVal());
		
		setNucOrient(ssp.getNucOrient());
		if(ssp.getNucOrientVal() != null) {
			setNucOrientVal(new JVector[ssp.getNucOrientVal().length][]);
			for(int i = 0; i < getNucOrientVal().length; i++) {
				getNucOrientVal()[i] = new JVector[ssp.getNucOrientVal()[i].length];
				for(int j = 0; j < getNucOrientVal()[i].length; j++) {
					getNucOrientVal()[i][j] = (JVector) ssp.getNucOrientVal()[i][j].clone();
				}
			}
		} else {
			setNucOrientVal(null);
		}
		
		setAxialGrowthRates(Arrays.copyOf(ssp.getAxialGrowthRates(), ssp.getAxialGrowthRates().length));
		setInitCrystalDimensions(Arrays.copyOf(ssp.getInitCrystalDimensions(), ssp.getInitCrystalDimensions().length));
		
		setNucLoc(ssp.getNucLoc());
		if(ssp.getNucLocOptions() != null) {
			setNucLocOptions(new JVector[ssp.getNucLocOptions().length]);
			for(int i = 0; i < getNucLocOptions().length; i++) {
				getNucLocOptions()[i] = (JVector) ssp.getNucLocOptions()[i].clone();
			}
		} else {
			setNucLocOptions(null);
		}
		
		setNucShape(ssp.getNucShape());
		
		setTerm(ssp.getTerm());
		setTermVal(ssp.getTermVal());
		
		setSampleShape(ssp.getSampleShape());
		setExp(ssp.getExp());
		setExpVal(ssp.getExpVal());
		setSampleUnitAxes(new JVector[ssp.getSampleUnitAxes().length]);
		for(int i = 0; i < getSampleUnitAxes().length; i++) {
			getSampleUnitAxes()[i] = (JVector) ssp.getSampleUnitAxes()[i].clone();
		}
		setSampleUnitsPerAxis(Arrays.copyOf(ssp.getSampleUnitsPerAxis(), ssp.getSampleUnitsPerAxis().length));
		
		folderOutput = new File(ssp.folderOutput.getAbsolutePath());
		fileRoot = String.copyValueOf(ssp.fileRoot.toCharArray());
		ais = (AlphabeticIndexingSystem) ssp.ais.clone();
		nis = (NumericIndexingSystem) ssp.nis.clone();
		
		setCurrentFileRoot(String.copyValueOf(ssp.getCurrentFileRoot().toCharArray()));
		

		setOutput(ssp.isOutput());
		setFit(ssp.isFit());
		setXyz(ssp.isOutputtingXyz());
		setMovie(ssp.isOutputtingMovie());
		setObj(ssp.isObj());
		setTransformed(ssp.isTransformed());
		
		
		setTimeStep(ssp.getTimeStep());
		setStartTime(ssp.getStartTime());
		
		setMaxNumCrystals(ssp.getMaxNumCrystals());	
		
		instancesToLaunch = ssp.instancesToLaunch;
		
		totalVolume = ssp.totalVolume;

		setAlphaMin(ssp.getAlphaMin());
		setAlphaMax(ssp.getAlphaMax());

		setInit_k_factor(ssp.getInit_k_factor());
		setInit_n(ssp.getInit_n());
		init_tau = ssp.init_tau;
		
		zipFits = getZipPrinter_Fits();
	}
	public void updateFileRoot() {
		String index = "";
		switch(indexingSystemInUse) {
		case 0: 
			ais.update();
			index = ais.getName();
			break;
		case 1:
			nis.update();
			index = nis.getName();
			break;
		default:
			index = "";
			break;
		}
		setCurrentFileRoot(fileRoot + "_" + index);
	}
	public String getSampleAxes() {
		String s = "";
		switch(sampleShape) {
		case Orthorhombic:
			s += "(a, b, c) = (" + sampleUnitsPerAxis[0] + ", " + sampleUnitsPerAxis[1] + ", " + sampleUnitsPerAxis[2] + ")";
			return s;
		case Tetragonal:
			s += "(a, c) = (" + sampleUnitsPerAxis[0] + ", " + sampleUnitsPerAxis[2] + ")";
			return s;
		case Cubic:
			s += "(a) =(" + sampleUnitsPerAxis[0] + ")";
			return s;
		case Spherical:
			s += "(r) = (" + sampleUnitsPerAxis[0] + ")";
			return s;
		case Cylindrical: 
			s += "(r, h) = (" + sampleUnitsPerAxis[0] + ", " + sampleUnitsPerAxis[2] + ")";
			return s;
		}
		return "";
	}
	public File getAnalysisFile() { 
		File analysis = new File(getFolderOutput() + File.separator + getCurrentFileRoot() + " -- automated fitting.txt");
		return analysis;
	}
	public File getXYZsFolder() { 
		File xyzs = new File(getFolderOutput() + File.separator + getCurrentFileRoot() + " -- structures");
		boolean mkdir = xyzs.mkdir();
		return xyzs;
	}
	public File getMoviesFile() { 
		File movies = new File(getFolderOutput() + File.separator + getCurrentFileRoot() + " -- movie.xyz");
		return movies;
	}
	public File getObjectsFile() { 
		File objects = new File(getFolderOutput() + File.separator + getCurrentFileRoot() + " -- java object.obj");
		return objects;
	}
	public File getTransformationsFolder() { 
		File trans = new File(getFolderOutput() + File.separator + getCurrentFileRoot() + " -- transformation files");
		boolean mkdir = trans.mkdir();
		return trans;
	}
	
	public File getFitsFile(int idx) { 
		File trans = new File(getFolderOutput() + File.separator + getCurrentFileRoot() + " -- " + idx + "-");
		return trans;
	}
	public File getZipFile() {
		File fileZipFits = new File(getFolderOutput() + File.separator + getCurrentFileRoot() + " -- fits.zip");
		return fileZipFits;
	}
	public PrintToZip getZipPrinter_Fits() {
		if(zipFits == null) {
			File fileZipFits = getZipFile();
			FileOutputStream fos = null;
			try {
				if(fileZipFits.exists()) {
					
				} else {
					fileZipFits.createNewFile();
				}
				fos = new FileOutputStream(fileZipFits);
				zipFits = new PrintToZip(new ZipOutputStream(fos), fos);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		return zipFits;
	}
	public File getCurrentTransformationFile(int idx) {
		File fileCurrentTransformation = new File(getTransformationsFolder() + File.separator + 
				getCurrentFileRoot() + " -- " + idx + ".trans");
		return fileCurrentTransformation;
	}
	public File getCurrentXYZFile(int idx) {
		File fileCurrentXYZ = new File(getXYZsFolder() + File.separator + 
				getCurrentFileRoot() + " -- structure -- " + idx + ".xyz");
		return fileCurrentXYZ;
	}
	public String toString() {
		String s = "=========================================";
		s += "\nExperiment Type: " + getExp().toString() + "\t" + getExpVal();
		s += "\nInitial Nucleation Strategy: " + getInitNuc().toString() + "\tval: " + getNucInitVal();

		s += "\nContinuous Nucleation Strategy: " + getContNuc().toString() + "\tval: " + getNucContVal();

		s += "\nNucleation Orientation Strategy: " + getNucOrient().toString();
		switch(getNucOrient()) {
		case Random: break;
		case SetOfOrientations:
			for(int i = 0; i < getNucOrientVal().length; i++) {
				s += "\n\t(" + i + ")"; 
				for(int j = 0; j < getNucOrientVal()[i].length; j++) {
					s += "\t(" + j + ")\t" + getNucOrientVal()[i][j].toString(); 
				}
			}
		}
		s += "\nAxial Growth Rates: " + Arrays.toString(getAxialGrowthRates());
		s += "\nInitial Crystal Dimensions: " + Arrays.toString(getInitCrystalDimensions());
		s += "\nNucleation Orientation Strategy: " + getNucLoc().toString();
		switch(getNucLoc()) {
		case Random: break;
		case Fixed:
			for(int i = 0; i < getNucLocOptions().length; i++) {
				s += "\n\t(" + i + ")\t" + getNucLocOptions()[i].toString();
			}
			break;
		}
		s += "\nCrystal Shape: " + getNucShape().toString();
		s += "\nTermination Strategy: " + getTerm().toString() + ": " + getTermVal();

		s += "\nSample Shape: " + getSampleShape().toString();
		for(int i = 0; i < getSampleUnitAxes().length; i++) {
			s += "\t(" + i + ")\t" + getSampleUnitAxes()[i].toString();
		}
		s += "\nUnits per sample axis: " + StringConverter.arrayToTabString(getSampleUnitsPerAxis());
		s += "\nOutput .xyz? " + isOutputtingXyz();
		s += "\nOutput transformation info? " + isTransformed();
		s += "\nOutput non-linear fitting info? " + isFit();
		s += "\nTime step: " + getTimeStep();
		s += "\nInitial nucleation time: " + getStartTime();
		s += "\nMaximum number of crystals: " + getMaxNumCrystals();
		s += "\nInstances to launch: " + instancesToLaunch;
		
		s += "\n\n";
		s += "\n=========================================\n\n\n";
		s += "\nSimulation started at: " + (new Date()).toString() + "\n";
		return s;
	}
	public int getApproxVolume() {
		int[] sampleAxes = getSampleUnitsPerAxis();
		switch(getSampleShape()) {
		case Orthorhombic:
		case Cubic:
		case Tetragonal:
			return sampleAxes[0] * sampleAxes[1] * sampleAxes[2];
		case Spherical:
			return (int) Math.rint(4./3.*Math.PI * Math.pow(sampleAxes[0], 3));
		case Cylindrical:
			return (int) Math.rint(Math.PI * sampleAxes[0] * sampleAxes[1] * sampleAxes[2]);
		}
		return 0;
	}
	public File getFolderOutput() {
		setFolderOutput(folderOutput);
		return folderOutput;
	}
	public void setFolderOutput(File folderOutput) {
		folderOutput.mkdir();
		this.folderOutput = folderOutput;
	}
	public String getCurrentFileRoot() {
		return currentFileRoot;
	}
	public void setCurrentFileRoot(String currentFileRoot) {
		this.currentFileRoot = currentFileRoot;
	}
	public Termination getTerm() {
		return term;
	}
	public void setTerm(Termination term) {
		this.term = term;
	}
	public Object getTermVal() {
		return termVal;
	}
	public void setTermVal(Object termVal) {
		this.termVal = termVal;
	}
	public boolean isOutputtingXyz() {
		return xyz;
	}
	public void setXyz(boolean xyz) {
		this.xyz = xyz;
	}
	public boolean isOutputtingMovie() {
		return movie;
	}
	public void setMovie(boolean movie) {
		this.movie = movie;
	}
	public boolean isFit() {
		return fit;
	}
	public void setFit(boolean fit) {
		this.fit = fit;
	}
	public boolean isObj() {
		return obj;
	}
	public void setObj(boolean obj) {
		this.obj = obj;
	}
	public boolean isTransformed() {
		return transformed;
	}
	public void setTransformed(boolean transformed) {
		this.transformed = transformed;
	}
	public ShapeTypes getSampleShape() {
		return sampleShape;
	}
	public void setSampleShape(ShapeTypes sampleShape) {
		this.sampleShape = sampleShape;
	}
	public int[] getSampleUnitsPerAxis() {
		return sampleUnitsPerAxis;
	}
	public void setSampleUnitsPerAxis(int[] sampleUnitsPerAxis) {
		this.sampleUnitsPerAxis = sampleUnitsPerAxis;
	}
	public ShapeTypes getNucShape() {
		return nucShape;
	}
	public void setNucShape(ShapeTypes nucShape) {
		this.nucShape = nucShape;
	}
	public boolean isOutput() {
		return output;
	}
	public void setOutput(boolean output) {
		this.output = output;
	}
	public int getNucInitVal() {
		return nucInitVal;
	}
	public void setNucInitVal(int nucInitVal) {
		this.nucInitVal = nucInitVal;
	}
	public int getMaxNumCrystals() {
		return maxNumCrystals;
	}
	public void setMaxNumCrystals(int maxNumCrystals) {
		this.maxNumCrystals = maxNumCrystals;
	}
	public NucleationOrientation getNucOrient() {
		return nucOrient;
	}
	public void setNucOrient(NucleationOrientation nucOrient) {
		this.nucOrient = nucOrient;
	}
	public NucleationLocation getNucLoc() {
		return nucLoc;
	}
	public void setNucLoc(NucleationLocation nucLoc) {
		this.nucLoc = nucLoc;
	}
	public double getNucContVal() {
		return nucContVal;
	}
	public void setNucContVal(double nucContVal) {
		this.nucContVal = nucContVal;
	}
	public double[] getAxialGrowthRates() {
		return axialGrowthRates;
	}
	public void setAxialGrowthRates(double[] axialGrowthRates) {
		this.axialGrowthRates = axialGrowthRates;
	}
	public JVector[] getSampleUnitAxes() {
		return sampleUnitAxes;
	}
	public void setSampleUnitAxes(JVector[] sampleUnitAxes) {
		this.sampleUnitAxes = sampleUnitAxes;
	}
	public double getStartTime() {
		return startTime;
	}
	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}
	public double getTimeStep() {
		return timeStep;
	}
	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
	}
	public double getInit_n() {
		return init_n;
	}
	public void setInit_n(double init_n) {
		this.init_n = init_n;
	}
	public double getInit_k_factor() {
		return init_k_factor;
	}
	public void setInit_k_factor(double init_k) {
		this.init_k_factor = init_k;
	}
	public Nucleation getInitNuc() {
		return initNuc;
	}
	public void setInitNuc(Nucleation initNuc) {
		this.initNuc = initNuc;
	}
	public int getVolumeIncrement() {
		return volumeIncrement;
	}
	public void setVolumeIncrement(int volumeIncrement) {
		this.volumeIncrement = volumeIncrement;
	}
	public Nucleation getContNuc() {
		return contNuc;
	}
	public void setContNuc(Nucleation contNuc) {
		this.contNuc = contNuc;
	}
	public double[] getInitCrystalDimensions() {
		return initCrystalDimensions;
	}
	public void setInitCrystalDimensions(double[] initCrystalDimensions) {
		this.initCrystalDimensions = initCrystalDimensions;
	}
	public ExperimentalSample getExp() {
		return exp;
	}
	public void setExp(ExperimentalSample exp) {
		this.exp = exp;
	}
	public double getExpVal() {
		return expVal;
	}
	public void setExpVal(double expVal) {
		this.expVal = expVal;
	}
	public double getAlphaMin() {
		return alphaMin;
	}
	public void setAlphaMin(double alphaMin) {
		this.alphaMin = alphaMin;
	}
	public double getAlphaMax() {
		return alphaMax;
	}
	public void setAlphaMax(double alphaMax) {
		this.alphaMax = alphaMax;
	}
	public FittableParametersOptions getFittableParameterSelection() {
		return fittableParameterSelection;
	}
	public void setFittableParameterSelection(FittableParametersOptions option) {
		fittableParameterSelection = option;
	}
	public int getNumberOfSimulationsToRun() {
		return numberOfSimulationsToRun;
	}
	public void setNumberOfSimulationsToRun(int numberOfSimulationsToRun) {
		this.numberOfSimulationsToRun = numberOfSimulationsToRun;
	}

	public DimensionalityOptions getDimensionalitySelection() {
		return dimensionalitySelection;
	}
	public void setDimensionalitySelection(DimensionalityOptions dimensionalitySelection) {
		this.dimensionalitySelection = dimensionalitySelection;
	}

	public JVector[] getNucLocOptions() {
		return nucLocOptions;
	}
	public void setNucLocOptions(JVector[] nucLocOptions) {
		this.nucLocOptions = nucLocOptions;
	}

	public JVector[][] getNucOrientVal() {
		return nucOrientVal;
	}
	public void setNucOrientVal(JVector[][] nucOrientVal) {
		this.nucOrientVal = nucOrientVal;
	}

	public FittingType getFittingType() {
		return fittingType;
	}
	public void setFittingType(FittingType fittingType) {
		this.fittingType = fittingType;
	}
	
}
