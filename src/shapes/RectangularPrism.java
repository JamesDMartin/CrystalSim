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
package shapes;

import java.awt.List;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import geometry.JVector;

public class RectangularPrism extends Shape {

	private static final long serialVersionUID = 3160645231205931220L;

	public RectangularPrism(ShapeTypes type, JVector location,
			JVector[] unitAxes) {
		super(type, location, unitAxes);
		// TODO Auto-generated constructor stub
	}

	@Override
	public JVector[] getPointsOnFace(double[] curAxes) {
		JVector[] corners = getOppositeCorners(curAxes);
		return null;
	}

	@Override
	public JVector getRandomPointInside(double[] curAxes) {
		Random r = new Random();
		double a, b, c;
		JVector x, y, z, p;
		int count = 0;
		do {
			a = (r.nextDouble()-.5) * curAxes[0];
			b = (r.nextDouble()-.5) * curAxes[1];
			c = (r.nextDouble()-.5) * curAxes[2];
			
			x = JVector.multiply(unitAxes[0], a);
			y = JVector.multiply(unitAxes[1], b);
			z = JVector.multiply(unitAxes[2], c);
			
			p = JVector.add(JVector.add(location, x), JVector.add(y, z));
			count++;
		} while(!isInside(curAxes, p));
//		System.out.println("getRandomPointInside(double[] curAxes) in RectangularPrism.java required " + count + " cycle(s) to " +
//				"find a point inside the shape");
		return p;
	}

	@Override
	/**
	 * The way that this method works is by calculating one corner of the cubic crystal and the total length of the axes.
	 * If the location in question is inside the shape defined by the length of the crystallite axes from the zero point
	 * then return true. else return false.
	 */
	public boolean isInside(double[] curAxes, JVector loc) {
		double dx, dy, dz;
		/*
		// Enhance speed by shortening the number of calculations required if the point is outside the distance from 
		// the crystallite to its corner (i.e. the longest distance in the crystallite)
		dx = curAxes[0] / 2.;
		dy = curAxes[1] / 2.;
		dz = curAxes[2] / 2.;
		double distanceFromCenter = JVector.subtract(loc, location).length();
		double lengthToCorner = new JVector(dx, dy, dz).length();
		if(distanceFromCenter > lengthToCorner) {
			return false;
		}
		*/
		JVector[] corners = getOppositeCorners(curAxes);
		
		// make the cube corner the zero point
		JVector shift = JVector.multiply(corners[0], -1);
		loc = JVector.add(loc, shift);
		
		// get the current max number of units in the three axial directions
		dx = curAxes[0];
		dy = curAxes[1];
		dz = curAxes[2];
		
		// determine the relative number of units from the zero point in the three axial directions
		double x = JVector.dot(loc, unitAxes[0]);
		double y = JVector.dot(loc, unitAxes[1]);
		double z = JVector.dot(loc, unitAxes[2]);
		
		// if the point is less than the relative number of units and greater than 0 then the point is inside the rectangular prism
		if(x < dx && y < dy && z < dz && x >= 0 && y >= 0 && z >= 0) {
			return true;
		}
		
		return false;
	}

	@Override
	public int calcTotalVolume(double[] curAxes) {
		return (int) Math.rint(curAxes[0] * curAxes[1] * curAxes[2]);
	}
	
	/**
	 * <pre>
	 * @param curAxes
	 * @param corners
	 * @return faceCoefficients[0] = x1
	 * faceCoefficients[1] = x2
	 * faceCoefficients[2] = y1
	 * faceCoefficients[3] = y2
	 * faceCoefficients[4] = z1
	 * faceCoefficients[5] = z2
	 *
	public Plane[] getFaces(double[] curAxes, JVector corner) {
		Plane[] p = new Plane[6];
		
		JVector[][] points = new JVector[6][3];
		
		//p43 Dill-10
		points[0][0] = corner;
		points[0][1] = JVector.add(corner, JVector.multiply(unitAxes[1], curAxes[1]));
		points[0][2] = JVector.add(corner, JVector.multiply(unitAxes[2], curAxes[2]));
		
		points[1][0] = JVector.add(points[0][0], JVector.multiply(unitAxes[0], curAxes[0]));
		points[1][1] = JVector.add(points[0][1], JVector.multiply(unitAxes[0], curAxes[0]));
		points[1][2] = JVector.add(points[0][2], JVector.multiply(unitAxes[0], curAxes[0]));
		
		points[2][0] = corner;
		points[2][1] = JVector.add(corner, JVector.multiply(unitAxes[0], curAxes[0]));
		points[2][2] = JVector.add(corner, JVector.multiply(unitAxes[2], curAxes[2]));
		
		points[3][0] = JVector.add(points[2][0], JVector.multiply(unitAxes[1], curAxes[1]));
		points[3][1] = JVector.add(points[2][1], JVector.multiply(unitAxes[1], curAxes[1]));
		points[3][2] = JVector.add(points[2][2], JVector.multiply(unitAxes[1], curAxes[1]));
		
		points[4][0] = corner;
		points[4][1] = JVector.add(corner, JVector.multiply(unitAxes[0], curAxes[0]));
		points[4][2] = JVector.add(corner, JVector.multiply(unitAxes[1], curAxes[1]));
		
		points[5][0] = JVector.add(points[4][0], JVector.multiply(unitAxes[2], curAxes[2]));
		points[5][1] = JVector.add(points[4][1], JVector.multiply(unitAxes[2], curAxes[2]));
		points[5][2] = JVector.add(points[4][2], JVector.multiply(unitAxes[2], curAxes[2]));
		
		
		for(int i = 0; i < points.length; i++) {
			p[i] = new Plane(points[i]);
		}
		
		return p;
	}
	*/
	public JVector[] getOppositeCorners(double[] curAxes) {
		JVector[] corners = new JVector[2];
		// shift from center to corner
		JVector a = JVector.multiply(unitAxes[0], curAxes[0]/2);
		JVector b = JVector.multiply(unitAxes[1], curAxes[1]/2);
		JVector c = JVector.multiply(unitAxes[2], curAxes[2]/2);
		/**
		System.out.println("a = " + a.toString());
		System.out.println("b = " + b.toString());
		System.out.println("c = " + c.toString());
		
		
		System.out.println("length(a) = " + a.length());
		System.out.println("length(b) = " + b.length());
		System.out.println("length(c) = " + c.length());
		
		System.out.println("a dot b = " + JVector.dot(a, b));
		System.out.println("a dot c = " + JVector.dot(a, c));
		System.out.println("b dot c = " + JVector.dot(b, c));
		*/
		/*JVector shift = JVector.add(a, JVector.add(b, c));
		shift = JVector.multiply(shift, .5);
		JVector a1 = JVector.subtract(shift, a);
		JVector b1 = JVector.subtract(shift, b);
		JVector c1 = JVector.subtract(shift, c);*/
		corners[0] = JVector.add(JVector.multiply(a, -1), JVector.add(JVector.multiply(b, -1), JVector.multiply(c, -1)));
		corners[1] = JVector.add(JVector.multiply(a, +1), JVector.add(JVector.multiply(b, +1), JVector.multiply(c, +1)));
		for(int i = 0; i < corners.length; i++) {
			corners[i] = JVector.add(location, corners[i]);
		}
		/*
		double d1 = JVector.distance(corners[0], corners[1]);
		double d2 = Math.sqrt(curAxes[0]*curAxes[0] + curAxes[1]*curAxes[1] + curAxes[2]*curAxes[2]);
		
		double dot1 = JVector.dot(unitAxes[0], unitAxes[1]);
		double dot2 = JVector.dot(unitAxes[0], unitAxes[2]);
		double dot3 = JVector.dot(unitAxes[1], unitAxes[2]);
		*/
		return corners;
	}
	/**
	private double[][] getFaceEquationCoefficients(int[] curAxes, JVector[] corners) {
		double[] axes = new double[curAxes.length];
		for(int i = 0; i < axes.length; i++) {
			axes[i] = curAxes[i];
		}
		return getFaceEquationCoefficients(axes, corners);
	}
	*/

}
