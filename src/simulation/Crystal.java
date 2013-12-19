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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import shapes.Shape;
import geometry.JVector;

public class Crystal extends Observable implements Serializable {

	private static final long serialVersionUID = 3715651800198674004L;
	private Shape shape;
	private NucleationEvent ne;
	private double[] axialGrowthRates;
	/**
	 * curAxes[i] = deltaT * axialGrowthRates[i] + initDims[i]
	 */
	private double[] curAxes;
	private double[] initDims;
	private char idx;
	private int size;
	public final static int BAD_NUCLEATION_LOCATION = 0;
	private Vector<Integer> numTransformed;
	private Vector<JVector> latticePoints;
	
	public Crystal(Shape shape, NucleationEvent ne, double[] axialGrowthRates, char idx, Observer o) {
		addObserver(o);
		this.shape = shape;
		this.ne = ne;
		this.axialGrowthRates = axialGrowthRates;
		this.idx = idx;
		size = 0;
		initDims = new double[] {0, 0, 0};
		numTransformed = new Vector<Integer>();
		curAxes = new double[axialGrowthRates.length];
		latticePoints = new Vector<JVector>();
	}
	
	/**
	 * Trigger the crystal to figure out the new face points based on 
	 * @param time
	 * @return
	 */
	public void grow(double curTime) {
		double deltaT = curTime - ne.getTime();
		if(deltaT >= 0) {
			for(int i = 0; i < axialGrowthRates.length; i++) {
				curAxes[i] = deltaT * axialGrowthRates[i]+initDims[i];
			}
		}
	}
	
	public void addLatticePoint(int i, int j, int k) {
		latticePoints.add(new JVector(i, j, k));
	}
	public JVector[] getLatticePoints() {
		JVector[] points = new JVector[latticePoints.size()];
		points = latticePoints.toArray(points);
		return points;
	}
	public boolean isInside(JVector pos) {
		return shape.isInside(curAxes, pos);
	}
	/**
	 * Increment the volume by additionalVolume
	 * @param additionalVolume	amount of additional volume to add
	 */
	public void addVolume(int additionalVolume) { size += additionalVolume; }
	
	public char getCrystalIdx() { return idx; }
	
	public void setInitDim(double[] dims) {
		initDims = new double[dims.length];
		for(int i = 0; i < dims.length; i++) {
			initDims[i] = dims[i];
		}
	}
	public String toString() {
		String s = "Crystal idx: " + (int) idx;
		s += "\nShape: " + shape.toString();
		s += "\n\tAxial growth rates: " + Arrays.toString(axialGrowthRates);
		s += "\n\tInitial dimensions: " + Arrays.toString(initDims);
		s += "\n\tNucleation time: " + ne.getTime();
		s += "\n\tNucleation location: " + ne.getLocation().toString();
		return s;
	}
	public void registerGrowth(int newGrowth) {
		numTransformed.add(newGrowth);
		addVolume(newGrowth);
	}
	public void incrementGrowth() {
		//size++;
	}
	public Integer[] getGrowth() {
		Integer[] growth = new Integer[numTransformed.size()];
		growth = numTransformed.toArray(growth);
		return growth;
	}
	public void badNucLoc() {
		setChanged();
		notifyObservers(BAD_NUCLEATION_LOCATION);
		//System.out.println("Number of observers: " + countObservers());
	}
	public int getTotalSize() { return size; }
	public double getNucTime() { return ne.getTime(); }
	public JVector getNucLoc() { return ne.getLocation(); }
	public JVector[] getNucOrient() { return shape.getUnitAxes(); }
}
