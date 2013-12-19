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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Vector;

import geometry.JVector;

public class ParseXYZ {

	private File xyzFile;
	private int passedNumCrystals, calculatedNumCrystals;
	
	/**
	 * 
	 * @param xyzFile	The file name of the xyz file
	 * @param numCrystals	If you don't know, use 0.  This object is clever enough to figure out this parameter if you pass it 0.
	 */
	public ParseXYZ(File xyzFile, int numCrystals) {
		this.xyzFile = xyzFile;
		passedNumCrystals = numCrystals;
		calculatedNumCrystals = 0;
	}
	

	private Scanner getFreshScanner() {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(xyzFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return new Scanner(fis);
	}
	private JVector[][] smartParse() {
		calculatedNumCrystals = 0;
		String line;
		String[] split;
		String delimiter = "\\t";
		int Z, x, y, z;
		JVector pos;
		// if the program isn't told how many crystals are in the xyz file then I have to determine 
		// that myself. This is how I do it:
		if(passedNumCrystals == 0) {
			Scanner s = getFreshScanner();
			// skip the first lines telling how many lattice points
			s.nextLine();
			s.nextLine();
			// figure out how many crystals grew
			while(s.hasNextLine()) {
				line = s.nextLine();
				split = line.split(delimiter);
				Z = Integer.valueOf(split[0]);
				if(Z > calculatedNumCrystals) {
					calculatedNumCrystals = Z;
				}
			}
		}
		
		Scanner s = getFreshScanner();
		// skip the first lines telling how many lattice points
		//System.out.println("Expected total number of lattice points : " + s.nextLine());
		s.nextLine();
		Vector<Vector<JVector>> crystals = new Vector<Vector<JVector>>();
		int numCrystals = Math.max(calculatedNumCrystals, passedNumCrystals);
		for(int i = 0; i < numCrystals; i++) {
			crystals.add(new Vector<JVector>());
		}
		
		while(s.hasNextLine()) {
			line = s.nextLine();
			split = line.split(delimiter);
			if(split.length > 1) {
				Z = Integer.valueOf(split[0])-1;
				if(Z >= 0) {
					x = Integer.valueOf(split[1]);
					y = Integer.valueOf(split[2]);
					z = Integer.valueOf(split[3]);
					pos = new JVector(x, y, z);
					crystals.get(Z).add(pos);
				}
			}
		}
		
		JVector[][] xtals = new JVector[numCrystals][];
		Vector<JVector> cur;
		for(int i = 0; i < xtals.length; i++) {
			cur = crystals.get(i);
			xtals[i] = new JVector[cur.size()];
			xtals[i] = cur.toArray(xtals[i]);
		}
		
		return xtals;
	}
	public JVector[][] getXtals() {
		return smartParse();
	}
	public static void main(String[] args) {
		String xyzName = "C:\\Users\\DHD\\Desktop\\$research\\programming\\workspace\\Kinetics3\\6-7-2012_14-33-26";
		File xyzFile = new File(xyzName + ".xyz");
		ParseXYZ p = new ParseXYZ(xyzFile, 0);
		JVector[][] xtals = p.smartParse();
		System.out.println("There were " + xtals.length + " crystals found in the xyz file: " + xyzFile.toString());
		int numTotal = 0;
		for(int i = 0 ; i < xtals.length; i++) {
			System.out.println("In crystal " + i + " of " + (xtals.length-1) + " there were " + xtals[i].length + " lattice points transformed.");
			numTotal += xtals[i].length;
		}
		System.out.println("total number of lattice points: " + numTotal);
		
	}
}
