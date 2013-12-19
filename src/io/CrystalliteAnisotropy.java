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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Vector;

import simulation.Crystal;
import geometry.JVector;

public class CrystalliteAnisotropy {

	private JVector[][] xtals;
	private JVector[] CM;
	private JVector[][] sixPoints;
	private double[][] actualAngle;
	public int[] crystalliteVolume;
	private int crystalliteIdx;
	private double maxAngle = 5, angleIncrement = 5, startAngle = 5;
	
	/**
	 * 
	 * @param xtals 2d array of lattice points. First dimension is the individual crystallites. Second 
	 * dimension is the lattice points of the individual crystallites.
	 */
	public CrystalliteAnisotropy(JVector[][] xtals) {
		this.xtals = xtals;
		CM = new JVector[xtals.length];
		sixPoints = new JVector[xtals.length][6];
		actualAngle = new double[xtals.length][6];
		crystalliteVolume = new int[xtals.length];
		calcCrystalliteVolume();
	}
	public void calcCrystalliteVolume() {
		for(int i = 0; i < xtals.length; i++) {
			crystalliteVolume[i] = xtals[i].length;
		}
	}
	public void findAllPoints() {
		int numXtals = xtals.length;
		for(int i = 0; i < numXtals ; i++) {
			if(xtals[i].length > 25) {
				crystalliteIdx = i;
				calculateCM();
				calculatePoint1();
				calculatePoint2();
				calculatePoint3();
				calculatePoint4();
				calculatePoint5(0);
				calculatePoint6(0);
			} else {
				CM[i] = new JVector(0, 0, 0);
				for(int j = 0; j < sixPoints[i].length; j++) {
					sixPoints[i][j] = new JVector(0, 0, 0);
				}
			}
		}
	}
	// step 1 of algorithm # 1 (EDD-10 p 87)
	public void calculateCM() {
		maxAngle = startAngle;
		JVector[] coords = xtals[crystalliteIdx];
		JVector total = new JVector();
		for(int i = 0; i < coords.length; i++) {
			total = JVector.add(total, coords[i]);
		}
		JVector CM = JVector.multiply(total, 1./((double) coords.length));
		this.CM[crystalliteIdx] = CM;
	}
	// step 2 of algorithm # 1 (EDD-10 p 87)
	public void calculatePoint1() {
		maxAngle = startAngle;
		double len = 0, curLen;
		JVector furthest = null, curPoint;
		
		JVector[] coords = xtals[crystalliteIdx];
		
		if(CM[crystalliteIdx] == null) {
			calculateCM();
		}
		for(int i = 0; i < coords.length; i++) {
			curPoint = coords[i];
			curLen = JVector.subtract(CM[crystalliteIdx], coords[i]).length();
			if(curLen > len) {
				len = curLen;
				furthest = (JVector) curPoint.clone();
				actualAngle[crystalliteIdx][0] = maxAngle;
			}
		}
		
		if(furthest == null) {
			String failString = "calcPoint1() method failed to find point 2.";
			failString += "\ncoords.length = " + coords.length + " and CM = " + CM.toString();
			throw new RuntimeException(failString);
		} else {
			this.sixPoints[crystalliteIdx][0] = furthest;
		}
	}
	// step 2a of algorithm # 1 (EDD-10 p 87)
	public void calculatePoint2() {
		double len = 0, curLen, angle;
		JVector furthest = null, curPoint;
		
		JVector[] coords = xtals[crystalliteIdx];
		
		if(CM[crystalliteIdx] == null) {
			calculateCM();
		}
		if(sixPoints[crystalliteIdx][0] == null) {
			calculatePoint1();
		}

		for(int i = 0; i < coords.length; i++) {
			curPoint = coords[i];
			curLen = JVector.subtract(CM[crystalliteIdx], coords[i]).length();
			angle = JVector.angleDegrees(
					JVector.subtract(CM[crystalliteIdx], curPoint), 
					JVector.subtract(CM[crystalliteIdx], sixPoints[crystalliteIdx][0])
					);
			if(curLen > len && angle > (180-maxAngle)) {
				len = curLen;
				furthest = (JVector) curPoint.clone();
				actualAngle[crystalliteIdx][1] = angle;
			}
		}
		
		if(furthest == null) {
			/*
			String failString = "calcPoint2() method failed to find point 2.";
			failString += "\ncoords.length = " + coords.length + " and CM = " + CM.toString();
			throw new RuntimeException(failString);
			*/
			maxAngle += angleIncrement;
			calculatePoint2();
			maxAngle -= angleIncrement;
		} else {
			this.sixPoints[crystalliteIdx][1] = furthest;
		}
	}
	// step 3 of algorithm # 1 (EDD-10 p 87)
	public void calculatePoint3() {
		double len = 0, curLen, angle;
		JVector furthest = null, curPoint;
		
		JVector[] coords = xtals[crystalliteIdx];
		
		if(CM[crystalliteIdx] == null) {
			calculateCM();
		}
		if(sixPoints[crystalliteIdx][0] == null) {
			calculatePoint1();
		}
		if(sixPoints[crystalliteIdx][1] == null) {
			calculatePoint2();
		}

		for(int i = 0; i < coords.length; i++) {
			curPoint = coords[i];
			curLen = JVector.subtract(CM[crystalliteIdx], coords[i]).length();
			angle = JVector.angleDegrees(
					JVector.subtract(CM[crystalliteIdx], curPoint), 
					JVector.subtract(CM[crystalliteIdx], sixPoints[crystalliteIdx][0])
					);
			if(curLen > len && angle > (90-maxAngle) && angle < (90 + maxAngle)) {
				len = curLen;
				furthest = (JVector) curPoint.clone();
				actualAngle[crystalliteIdx][2] = angle;
			}
		}
		
		if(furthest == null) {
			/*
			String failString = "calcPoint2() method failed to find point 2.";
			failString += "\ncoords.length = " + coords.length + " and CM = " + CM.toString();
			throw new RuntimeException(failString);
			*/
			maxAngle += angleIncrement;
			calculatePoint3();
			maxAngle -= angleIncrement;
		} else {
			this.sixPoints[crystalliteIdx][2] = furthest;
		}
	}
	// step 3a of algorithm # 1 (EDD-10 p 87)
	public void calculatePoint4() {
		double len = 0, curLen, angle;
		JVector furthest = null, curPoint;
		
		JVector[] coords = xtals[crystalliteIdx];
		
		if(CM[crystalliteIdx] == null) {
			calculateCM();
		}
		if(sixPoints[crystalliteIdx][0] == null) {
			calculatePoint1();
		}
		if(sixPoints[crystalliteIdx][1] == null) {
			calculatePoint2();
		}
		if(sixPoints[crystalliteIdx][2] == null) {
			calculatePoint3();
		}

		for(int i = 0; i < coords.length; i++) {
			curPoint = coords[i];
			curLen = JVector.subtract(CM[crystalliteIdx], coords[i]).length();
			angle = JVector.angleDegrees(
					JVector.subtract(CM[crystalliteIdx], curPoint), 
					JVector.subtract(CM[crystalliteIdx], sixPoints[crystalliteIdx][2])
					);
			if(curLen > len && angle > (180-maxAngle)) {
				len = curLen;
				furthest = (JVector) curPoint.clone();
				actualAngle[crystalliteIdx][3] = angle;
			}
		}
		
		if(furthest == null) {
			/*
			String failString = "calcPoint2() method failed to find point 2.";
			failString += "\ncoords.length = " + coords.length + " and CM = " + CM.toString();
			throw new RuntimeException(failString);
			*/
			maxAngle += angleIncrement;
			calculatePoint4();
			maxAngle -= angleIncrement;
		} else {
			this.sixPoints[crystalliteIdx][3] = furthest;
		}
	}
	// step 4 of algorithm # 1 (EDD-10 p 87)
	public void calculatePoint5(int timesCalculated) {
		double len = 0, curLen, angle;
		JVector furthest = null, curPoint, cross;
		
		JVector[] coords = xtals[crystalliteIdx];
		
		if(CM[crystalliteIdx] == null) {
			calculateCM();
		}
		if(sixPoints[crystalliteIdx][0] == null) {
			calculatePoint1();
		}
		if(sixPoints[crystalliteIdx][1] == null) {
			calculatePoint2();
		}
		if(sixPoints[crystalliteIdx][2] == null) {
			calculatePoint3();
		}
		if(sixPoints[crystalliteIdx][3] == null) {
			calculatePoint4();
		}

		for(int i = 0; i < coords.length; i++) {
			curPoint = coords[i];
			curLen = JVector.subtract(CM[crystalliteIdx], coords[i]).length();
			cross = JVector.cross(
					JVector.subtract(CM[crystalliteIdx], sixPoints[crystalliteIdx][0]), 
					JVector.subtract(CM[crystalliteIdx], sixPoints[crystalliteIdx][2])
					);
			angle = JVector.angleDegrees(JVector.subtract(CM[crystalliteIdx], curPoint), cross);
			if(curLen > len && angle < maxAngle) {
				len = curLen;
				furthest = (JVector) curPoint.clone();
				actualAngle[crystalliteIdx][4] = angle;
			}
		}
		
		if(furthest == null) {
			/*
			String failString = "calcPoint2() method failed to find point 2.";
			failString += "\ncoords.length = " + coords.length + " and CM = " + CM.toString();
			throw new RuntimeException(failString);
			*/
			maxAngle += angleIncrement;
			if(timesCalculated > 10) {
				this.sixPoints[crystalliteIdx][4] = CM[crystalliteIdx];
			} else {
				calculatePoint5(++timesCalculated);
			}
			maxAngle -= angleIncrement;
		} else {
			this.sixPoints[crystalliteIdx][4] = furthest;
		}
	}
	// step 4 of algorithm # 1 (EDD-10 p 87)
	public void calculatePoint6(int timesCalled) {
		double len = 0, curLen, angle;
		JVector furthest = null, curPoint, cross;
		
		JVector[] coords = xtals[crystalliteIdx];
		
		if(CM[crystalliteIdx] == null) {
			calculateCM();
		}
		if(sixPoints[crystalliteIdx][0] == null) {
			calculatePoint1();
		}
		if(sixPoints[crystalliteIdx][1] == null) {
			calculatePoint2();
		}
		if(sixPoints[crystalliteIdx][2] == null) {
			calculatePoint3();
		}
		if(sixPoints[crystalliteIdx][3] == null) {
			calculatePoint4();
		}
		if(sixPoints[crystalliteIdx][3] == null) {
			calculatePoint5(0);
		}

		for(int i = 0; i < coords.length; i++) {
			curPoint = coords[i];
			curLen = JVector.subtract(CM[crystalliteIdx], coords[i]).length();
			cross = JVector.cross(
					JVector.subtract(CM[crystalliteIdx], sixPoints[crystalliteIdx][0]), 
					JVector.subtract(CM[crystalliteIdx], sixPoints[crystalliteIdx][3])
					);
			angle = JVector.angleDegrees(JVector.subtract(CM[crystalliteIdx], curPoint), cross);
			if(curLen > len && angle < maxAngle) {
				len = curLen;
				furthest = (JVector) curPoint.clone();
				actualAngle[crystalliteIdx][4] = angle;
			}
		}
		
		if(furthest == null) {
			/*
			String failString = "calcPoint2() method failed to find point 2.";
			failString += "\ncoords.length = " + coords.length + " and CM = " + CM.toString();
			throw new RuntimeException(failString);
			*/
			maxAngle += angleIncrement;
			if(timesCalled > 10) {
				this.sixPoints[crystalliteIdx][5] = CM[crystalliteIdx];
			} else {
				calculatePoint6(++timesCalled);
			}
			maxAngle -= angleIncrement;
		} else {
			this.sixPoints[crystalliteIdx][5] = furthest;
		}
	}
	public void writeNewXYZFile(File f) {
		MyPrintStream ps = new MyPrintStream(f);
		int numXtals = 0;
		for(int i = 0; i < xtals.length; i++) {
			numXtals += xtals[i].length;
		}
		numXtals += sixPoints.length*7;
		ps.println(numXtals + "");
		ps.println("");
		
		for(int i = 0; i < sixPoints.length; i++) {
			for(int j = 0; j < sixPoints[i].length; j++) {
				ps.println("100\t" + sixPoints[i][j].i + "\t" + sixPoints[i][j].j + "\t" + sixPoints[i][j].k);
			}
			ps.println("100\t" + CM[i].i + "\t" + CM[i].j + "\t" + CM[i].k);
		}
		for(int i = 0; i < xtals.length; i++) {
			for(int j = 0; j < xtals[i].length; j++) {
				ps.println(i + "\t" + xtals[i][j].i + "\t" + xtals[i][j].j + "\t" + xtals[i][j].k);
			}
		}
	}
	public void writeSelectedXYZFile(File f, int i) {
		MyPrintStream ps = new MyPrintStream(f);
		int numXtals = 0;
		numXtals += xtals[i].length;
		numXtals += 7;
		ps.println(numXtals + "");
		ps.println("");
		
		for(int j = 0; j < sixPoints[i].length; j++) {
			ps.println("100\t" + sixPoints[i][j].i + "\t" + sixPoints[i][j].j + "\t" + sixPoints[i][j].k);
		}
		ps.println("100\t" + CM[i].i + "\t" + CM[i].j + "\t" + CM[i].k);
		for(int j = 0; j < xtals[i].length; j++) {
			ps.println(i + "\t" + xtals[i][j].i + "\t" + xtals[i][j].j + "\t" + xtals[i][j].k);
		}
	}
	public double[][] getDistances() {
		double[][] distances = new double[xtals.length][6];
		JVector vec;
		double dist;
		for(int i = 0; i < CM.length; i++) {
			for(int j = 0; j < sixPoints[i].length; j++) {
				vec = JVector.subtract(CM[i], sixPoints[i][j]);
				dist = vec.length();
				if(dist == 0) {
					dist = 1;
				}
				distances[i][j] = dist;
			}
		}
		
		return distances;
	}
	public void writeSelectedXYZFile(File f, int i, int Z) {
		MyPrintStream ps = new MyPrintStream(f);
		int numXtals = 0;
		numXtals += xtals[i].length;
		numXtals += 7;
		ps.println(numXtals + "");
		ps.println("");
		
		for(int j = 0; j < sixPoints[i].length; j++) {
			ps.println("100\t" + sixPoints[i][j].i + "\t" + sixPoints[i][j].j + "\t" + sixPoints[i][j].k);
		}
		ps.println("100\t" + CM[i].i + "\t" + CM[i].j + "\t" + CM[i].k);
		for(int j = 0; j < xtals[i].length; j++) {
			ps.println(Z + "\t" + xtals[i][j].i + "\t" + xtals[i][j].j + "\t" + xtals[i][j].k);
		}
	}
	public double[] getAxisLengths() {
		double[] axisLengths = new double[3];
		double dist = 0;
		JVector v1, v2;
		for(int i = 0; i < 3; i++) {
			try {
				v1 = getSixPoints(2*i);
				v2 = getSixPoints(2*i+1);
				dist = JVector.distance(v1, v2);
			} catch(NullPointerException npe) {
				npe.printStackTrace();
			}
			if(dist == 0) {
				dist = 1;
			}
			axisLengths[i] = dist;
		}
		return axisLengths;
	}
	public double getApproximateDimensionality() {
		double[] axisLengths = getAxisLengths();
		double max = 1;
		max = Math.max(axisLengths[0], axisLengths[1]);
		max = Math.max(max, axisLengths[2]);
		return axisLengths[0]/max + axisLengths[1]/max + axisLengths[2]/max;
	}
	public double getAnisotropy() {
		double[] axisLengths = getAxisLengths();
		double max = 1, min = 1;
		max = Math.max(axisLengths[0], axisLengths[1]);
		max = Math.max(max, axisLengths[2]);
		min = Math.min(axisLengths[0], axisLengths[1]);
		min = Math.min(min, axisLengths[2]);
		return max/min;
	}
	public void setCrystalliteIdx(int idx) { crystalliteIdx = idx; }
	public JVector[] getAllCM() { return CM; }
	public JVector getCM() { return CM[crystalliteIdx]; }
	public JVector[][] getAllSixPoints() { return sixPoints; }
	public JVector[] getSixPoints() { return sixPoints[crystalliteIdx]; }
	public JVector getSixPoints(int pointIdx) { return sixPoints[crystalliteIdx][pointIdx]; }
	public double getMaxAngle(int pointIdx) { return actualAngle[crystalliteIdx][pointIdx]; }
	
	public void writeToFile(PrintStream ps, String sampleInfo) {
		double[] axes;
		double approximateDim, anisotropy;
		int volume;
		for(int j = 0; j < xtals.length; j++) {
			setCrystalliteIdx(j);
			axes = getAxisLengths();
			approximateDim = getApproximateDimensionality();
			anisotropy = getAnisotropy();
			volume = crystalliteVolume[j];
			String s = sampleInfo + "\t" + StringConverter.arrayToTabString(axes);
			s += approximateDim + "\t" + anisotropy + "\t" + volume;
			ps.println(s);
		}
	}
	public static void main(String[] args) {
		File folder = new File("D:\\Documents referenced in lab notebooks\\Dill-10\\189\\189i\\makeXYZoutput");
		File[] files = folder.listFiles();
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(folder + File.separator + "anisotropy calculation.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		PrintStream ps = new PrintStream(fos);
		CrystalliteAnisotropy ca;
		JVector[][] xtals;
		ps.println("sample container aspect ratio\ta\tb\tc\tapproximateDim\tanisotropy\tvolume");
		for(int i = 0; i < files.length; i++) {
			if(files[i].toString().contains("xyz")) {
				System.out.println("Analyzing file: " + files[i].getName());
				String sampleAspectRatio = files[i].getName().substring(0, 7);
				xtals = new ParseXYZ(files[i], 0).getXtals();
				ca = new CrystalliteAnisotropy(xtals);
				ca.findAllPoints();
				ca.writeToFile(ps, sampleAspectRatio);
			}
		}
		/*
		// Testing the CM calculations
		ca.findAllPoints();
		for(int i = 0; i < xtals.length; i++) {
			ca.setCrystalliteIdx(i);
			System.out.println("Crystal " + i + " of " + (xtals.length-1) + ":");
			System.out.println("\t\thas axis lengths: " + StringConverter.arrayToTabString(ca.getAxisLengths()));
			System.out.println("\t\thas an approximate dimensionality of: " + ca.getApproximateDimensionality());
			System.out.println("\t\thas CM: " + ca.getCM().toString(2));
			System.out.println("\t\thas point 1: " + ca.getSixPoints(0).toString(2));
			System.out.println("\t\t\twith a distance of: " +  JVector.subtract(ca.getSixPoints(0), ca.getCM()).length());
			System.out.println("\t\t\tand a max angle of: " +  ca.getMaxAngle(0));
			System.out.println("\t\thas point 2: " + ca.getSixPoints(1).toString(2));
			System.out.println("\t\t\twith a distance of: " +  JVector.subtract(ca.getSixPoints(1), ca.getCM()).length());
			System.out.println("\t\t\tand a max angle of: " +  ca.getMaxAngle(1));
			System.out.println("\t\thas point 3: " + ca.getSixPoints(2).toString(2));
			System.out.println("\t\t\twith a distance of: " +  JVector.subtract(ca.getSixPoints(2), ca.getCM()).length());
			System.out.println("\t\t\tand a max angle of: " +  ca.getMaxAngle(2));
			System.out.println("\t\thas point 4: " + ca.getSixPoints(3).toString(2));
			System.out.println("\t\t\twith a distance of: " +  JVector.subtract(ca.getSixPoints(3), ca.getCM()).length());
			System.out.println("\t\t\tand a max angle of: " +  ca.getMaxAngle(3));
			System.out.println("\t\thas point 5: " + ca.getSixPoints(4).toString(2));
			System.out.println("\t\t\twith a distance of: " +  JVector.subtract(ca.getSixPoints(4), ca.getCM()).length());
			System.out.println("\t\t\tand a max angle of: " +  ca.getMaxAngle(4));
			System.out.println("\t\thas point 6: " + ca.getSixPoints(5).toString(2));
			System.out.println("\t\t\twith a distance of: " +  JVector.subtract(ca.getSixPoints(5), ca.getCM()).length());
			System.out.println("\t\t\tand a max angle of: " +  ca.getMaxAngle(5));
		}
		for(int i = 0; i < xtals.length; i++) {
			ca.writeSelectedXYZFile(new File(xyzName + " -- " + i + ".xyz"), i, 1);
		}
		*/
	}
}
