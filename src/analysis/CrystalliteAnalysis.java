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
package analysis;

import java.util.Vector;

import geometry.JVector;
import simulation.Crystal;

public class CrystalliteAnalysis {

	private Crystal c;
	private double tOneHalf = 10;
	private double deltaT = 0.5;
	
	public CrystalliteAnalysis(Crystal c) {
		this.c = c;
		tOneHalf = c.getNucTime();
	}
	public JVector[] get50PercentCrystallizedLattice(double tolerance) {
		JVector[] finalLattice = c.getLatticePoints();
		double vol = finalLattice.length;
		double halfVol = vol/2;
		double fractionDifferent = 0.5;
		double time = tOneHalf;
		JVector[] subLattice = finalLattice;
		int MAX_ATTEMPTS = 5000;
		int attempts = 0;
		double prevIncrementedTime = 0,
				prevDecrementedTime = 0;
		while(Math.abs(fractionDifferent) > tolerance && attempts++ < MAX_ATTEMPTS) {
			subLattice = getSubLattice(time);
			fractionDifferent = (halfVol - subLattice.length) / halfVol;
			if(fractionDifferent > 0) {
				time += deltaT;
				if(time == prevIncrementedTime)
					deltaT *= .75;
				prevIncrementedTime = time;
			} else {
				time -= deltaT;
				if(time == prevDecrementedTime)
					deltaT *= .75;
				prevDecrementedTime = time;
			}
		}
		System.out.println("Final fraction different: " + fractionDifferent + ". Attempts: " + attempts);
		return subLattice;
	}
	
	public JVector[] getSubLattice(double time) {
		JVector[] axes = c.getNucOrient();
		JVector[] finalLattice = c.getLatticePoints();
		c.grow(time);
		Vector<JVector> subLattice = new Vector<JVector>();
		for(int i = 0; i < finalLattice.length; i++) {
			if(c.isInside(finalLattice[i])) {
				subLattice.add((JVector) finalLattice[i].clone());
			}
		}
		return subLattice.toArray(new JVector[subLattice.size()]);
	}
	/**
	 * @return the tOneHalf
	 */
	public double gettOneHalf() {
		return tOneHalf;
	}
	/**
	 * @param tOneHalf the tOneHalf to set
	 */
	public void settOneHalf(double tOneHalf) {
		this.tOneHalf = tOneHalf;
	}
	/**
	 * @return the deltaT
	 */
	public double getDeltaT() {
		return deltaT;
	}
	/**
	 * @param deltaT the deltaT to set
	 */
	public void setDeltaT(double deltaT) {
		this.deltaT = deltaT;
	}
}
