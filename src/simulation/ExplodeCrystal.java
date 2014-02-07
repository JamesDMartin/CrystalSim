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
import io.MyFileInputStream;
import io.MyPrintStream;

import java.io.File;
import java.util.Scanner;

import chemistry.JAtom;
import chemistry.JAtomTools;

public class ExplodeCrystal {

	private JAtom[] atoms;
	private int numAtomTypes;
	private double boomFactor;
	private String fileRoot = "";
	private JVector sampleCM;
	private int numAtoms;
	
	/**
	 * 
	 * @param file String that points to a .xyz file WITHOUT THE .XYZ EXTENSION
	 * @param boomFactor > 0 indicates that the output will be an exploded view.  < 0 indicates that the output will be an imploded view
	 */
	public ExplodeCrystal(String fileRoot, double boomFactor) {
		this.fileRoot = fileRoot;
		this.boomFactor = boomFactor;
	}
	/**
	 * 
	 * @param atoms List of atoms in the crystal to explode
	 * @param boomFactor > 0 indicates that the output will be an exploded view.  < 0 indicates that the output will be an imploded view
	 */
	public ExplodeCrystal(JAtom[] atoms, double boomFactor) {
		this.atoms = atoms;
		this.boomFactor = boomFactor;
		numAtoms = this.atoms.length;
	}
	public void run() {
		if(fileRoot != "")
			readFile();
		determineNumAtomTypes();
		computeCM();
		explode();
		print();
	}
	private void readFile() {
		Scanner s = (new MyFileInputStream(fileRoot + ".xyz")).getScanner();
		String[] line;
		numAtoms = s.nextInt();
		atoms = new JAtom[numAtoms];
		s.nextLine();
		s.nextLine();
		double x, y, z;
		int Z;
		int idx = 0;
		boolean keepReading = true;
		while (s.hasNextLine() && keepReading) {
			line = s.nextLine().split("\t");
			if(line.length == 4) {
				try {
					Z = Integer.valueOf(line[0]);
				} catch(NumberFormatException nfe) {
					Z = JAtomTools.getZ(line[0]);
					if(Z == 0)
						Z = 1;
				}
				x = Double.valueOf(line[1]);
				y = Double.valueOf(line[2]);
				z = Double.valueOf(line[3]);
				atoms[idx++] = new JAtom(Z, x, y, z);
			} else if(line.length == 1) {
				if(Integer.valueOf(line[0]) != 0)
					keepReading = false;
			}
		}
	}
	private void determineNumAtomTypes() {
		int maxZ = 0;
		for(JAtom atom : atoms) {
			if(atom.getZ() > maxZ) { maxZ = atom.getZ(); }
			
		}
		numAtomTypes = maxZ;
	}
	private void computeCM() {
		sampleCM = new JVector();
		for(JAtom atom : atoms) {
			sampleCM.add(atom.getPosition());
		}
		sampleCM.multiply(1./((double) numAtoms));
	}
	private JVector[] getDisplacementVectors() {
		JVector[] displace = new JVector[numAtomTypes];
		int[] numAtomsPer = new int[numAtomTypes];
		for(int i = 0; i < displace.length; i++) {
			displace[i] = new JVector();
		}
		int Z;
		JVector pos;
		for(int i = 0; i < atoms.length; i++) {
			pos = atoms[i].getPosition();
			Z = atoms[i].getZ();
			displace[Z-1] = JVector.add(displace[Z-1], pos);
			numAtomsPer[Z-1]++;
		}
		for(int i = 0; i < displace.length; i++) {
			displace[i] = JVector.multiply(displace[i], 1./((double) numAtomsPer[i]));
			displace[i] = JVector.subtract(displace[i], sampleCM);
			displace[i] = JVector.multiply(displace[i], boomFactor);
		}
		return displace;
	}
	private void explode() {
		JVector[] displacement = getDisplacementVectors();
		int Z;
		for(int i = 0; i < atoms.length; i++) {
			Z = atoms[i].getZ();
			atoms[i].translate(displacement[Z-1]);
		}
		
	}
	
	public void print() {
		File out = new File(fileRoot + "_" + "explodedBy_" + boomFactor + ".xyz");
		MyPrintStream mps = new MyPrintStream(out);

		mps.println(numAtoms + "\n");
		for(int i = 0; i < atoms.length; i++) {
			mps.println(atoms[i].toStringForXYZ());
		}
		mps.close();
	}
	public JAtom[] getAtoms() { return atoms; }
	public void setAtoms(JAtom[] atoms) { this.atoms = atoms; }
	public static void main(String[] args) {
		String root = "C:\\Users\\Eric\\git\\CrystalSim\\output\\CrystallizationSimulation -- structures\\CrystallizationSimulation -- structure -- 0";
		double boomFactor = 1.5;
		ExplodeCrystal ec = new ExplodeCrystal(root, boomFactor);
		ec.run();
		ec.print();
	}
}
