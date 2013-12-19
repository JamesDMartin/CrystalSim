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
package tests;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;

import shapes.RectangularPrism;
import shapes.Shape;
import shapes.ShapeFactory;
import shapes.ShapeTypes;
import geometry.Geom;
import geometry.JVector;

public class ShapeTests {

	private static void volumeTest(JVector[] centers, double[][] curAxes, JVector[] alignmentAxes, ShapeTypes shape) {
		Shape s;
		double calcVol, expectedVol, calcSurf, expectedSurf;
		for(int i = 0; i < centers.length; i++) {
			for(int j = 0; j < curAxes.length; j++) {
				System.out.println("Spherical crystal center: " + centers[i].toString() + 
						"\twith dimensions: " + Arrays.toString(curAxes[j]));
				s = ShapeFactory.newShape(shape, centers[i], alignmentAxes);
				calcVol = s.calcTotalVolume(curAxes[j]);
				System.out.println("Total volume for a " + shape.toString() + " of dimensions: " + 
						Arrays.toString(curAxes[j]) + " is:\t" + calcVol);
				
				expectedVol = (Math.PI)*Math.pow(curAxes[j][1], 2)*curAxes[j][0];
				System.out.println("\tExpected volume is:\t\t\t\t\t\t" + expectedVol);
				System.out.println("\tGiving a percent difference of:\t\t\t\t\t" + 
						(expectedVol - calcVol) / calcVol * 100 + "%\n");
			}

		}
	}
	
	private static void surfaceTest(JVector[]centers, double[][] curAxes, JVector[] alignmentAxes, ShapeTypes shape) {
		Shape s;
		double calcSurf, expectedSurf;
		JVector[] surf;
		for(int i = 0; i < centers.length; i++) {
			for(int j = 0; j < curAxes.length; j++) {
				System.out.println(shape.toString() + " crystal center: " + centers[i].toString() + "\twith radius: " + curAxes[j][0]);
				s = ShapeFactory.newShape(shape, centers[i], alignmentAxes);
				surf = s.getPointsOnFace(curAxes[j]);

				alignmentAxes = Geom.randomlyRotateOrthogonalAxes(alignmentAxes);
				surf = ((RectangularPrism) s).getOppositeCorners(curAxes[0]);
				if(surf == null) { continue; }
				printToXYZ(surf);
				calcSurf = surf.length;
				System.out.println("Total surface for a sphere of radius " + Arrays.toString(curAxes[j]) + ": " + calcSurf);
				
				expectedSurf = (4*Math.PI)*Math.pow(curAxes[j][0], 2);
				System.out.println("\tExpected surface area is: " + expectedSurf);
				System.out.println("\tGiving a percent difference of: " + (expectedSurf - calcSurf) / expectedSurf * 100 + "%\n");
			}

		}
	}
	
	private static void getRandomPointInsideTest(JVector[]centers, double[][] curAxes, JVector[] alignmentAxes, ShapeTypes shape) {
		Shape s;
		JVector pointInside;
		for(int i = 0; i < centers.length; i++) {
			for(int j = 0; j < curAxes.length; j++) {
				System.out.println(shape.toString() + " crystal center: " + centers[i].toString() + "\twith axes: " + Arrays.toString(curAxes[j]));
				s = ShapeFactory.newShape(shape, centers[i], alignmentAxes);
				pointInside = s.getRandomPointInside(curAxes[j]);
				System.out.println(pointInside);
			}

		}
	}
	private static void printToXYZ(JVector[] vec) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("test.xyz");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream ps = new PrintStream(fos);
		
		ps.println(vec.length + "\n");
		
		for(int i = 0; i < vec.length; i++) {
			ps.println("1\t" + vec[i].i + "\t" + vec[i].j + "\t" + vec[i].k);
		}
		if(ps != null) {
			ps.close();	
		}
		try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static JVector getRandomAxis(Random r) {
		return new JVector(r.nextDouble()-.5, r.nextDouble()-.5, r.nextDouble()-.5).unit();
	}
	public static void main(String[] args) {
		JVector[] centers =  
			{JVector.zero };
		
		int[][] curAxes = {{100}};
		double[][] dAxes = {{75, 25, 25}};
		
		Random r = new Random();
		JVector[] alignmentAxes = {JVector.z, JVector.x, JVector.y};
		ShapeTypes[] shapes = {ShapeTypes.Cylindrical};
		
		for(int i = 0; i < shapes.length; i++) {
			//volumeTest(centers, curAxes, alignmentAxes, shapes[i]);
			volumeTest(centers, dAxes, alignmentAxes, shapes[i]);
		}
	}
}
