package simulation;

import io.MyPrintStream;
import io.ParseXYZ;
import io.StringConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Vector;

import geometry.JVector;

public class IndividualCrystalliteAnisotropy {

	private JVector[] points;
	private JVector CM;
	private JVector[] sixPoints;
	private double[] actualAngle;
	public int crystalliteVolume;
	private double maxAngle = 5, angleIncrement = 5, startAngle = 5;
	
	/**
	 * 
	 * @param xtals 2d array of lattice points. First dimension is the individual crystallites. Second 
	 * dimension is the lattice points of the individual crystallites.
	 */
	public IndividualCrystalliteAnisotropy(JVector[] points) {
		this.points = points;
		sixPoints = new JVector[6];
		actualAngle = new double[6];
		crystalliteVolume = points.length;
		findAllPoints();
	}
	public void findAllPoints() {
		calculateCM();
		calculatePoint1();
		calculatePoint2();
		calculatePoint3();
		calculatePoint4();
		calculatePoint5(0);
		calculatePoint6(0);
	}
	// step 1 of algorithm # 1 (EDD-10 p 87)
	public void calculateCM() {
		maxAngle = startAngle;
		JVector[] coords = points;
		JVector total = new JVector();
		for(int i = 0; i < coords.length; i++) {
			total = JVector.add(total, coords[i]);
		}
		CM = JVector.multiply(total, 1./((double) coords.length));
	}
	// step 2 of algorithm # 1 (EDD-10 p 87)
	public void calculatePoint1() {
		maxAngle = startAngle;
		double len = 0, curLen;
		JVector furthest = null, curPoint;
		
		JVector[] coords = points;
		
		if(CM == null) {
			calculateCM();
		}
		for(int i = 0; i < coords.length; i++) {
			curPoint = coords[i];
			curLen = JVector.subtract(CM, coords[i]).length();
			if(curLen > len) {
				len = curLen;
				furthest = (JVector) curPoint.clone();
				actualAngle[0] = maxAngle;
			}
		}
		
		if(furthest == null) {
			String failString = "calcPoint1() method failed to find point 2.";
			failString += "\ncoords.length = " + coords.length + " and CM = " + CM.toString();
			throw new RuntimeException(failString);
		} else {
			this.sixPoints[0] = furthest;
		}
	}
	// step 2a of algorithm # 1 (EDD-10 p 87)
	public void calculatePoint2() {
		double len = 0, curLen, angle;
		JVector furthest = null, curPoint;
		
		JVector[] coords = points;
		
		if(CM == null) {
			calculateCM();
		}
		if(sixPoints[0] == null) {
			calculatePoint1();
		}

		for(int i = 0; i < coords.length; i++) {
			curPoint = coords[i];
			curLen = JVector.subtract(CM, coords[i]).length();
			angle = JVector.angleDegrees(
					JVector.subtract(CM, curPoint), 
					JVector.subtract(CM, sixPoints[0])
					);
			if(curLen > len && angle > (180-maxAngle)) {
				len = curLen;
				furthest = (JVector) curPoint.clone();
				actualAngle[1] = angle;
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
			this.sixPoints[1] = furthest;
		}
	}
	// step 3 of algorithm # 1 (EDD-10 p 87)
	public void calculatePoint3() {
		double len = 0, curLen, angle;
		JVector furthest = null, curPoint;
		
		JVector[] coords = points;
		
		if(CM == null) {
			calculateCM();
		}
		if(sixPoints[0] == null) {
			calculatePoint1();
		}
		if(sixPoints[1] == null) {
			calculatePoint2();
		}

		for(int i = 0; i < coords.length; i++) {
			curPoint = coords[i];
			curLen = JVector.subtract(CM, coords[i]).length();
			angle = JVector.angleDegrees(
					JVector.subtract(CM, curPoint), 
					JVector.subtract(CM, sixPoints[0])
					);
			if(curLen > len && angle > (90-maxAngle) && angle < (90 + maxAngle)) {
				len = curLen;
				furthest = (JVector) curPoint.clone();
				actualAngle[2] = angle;
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
			this.sixPoints[2] = furthest;
		}
	}
	// step 3a of algorithm # 1 (EDD-10 p 87)
	public void calculatePoint4() {
		double len = 0, curLen, angle;
		JVector furthest = null, curPoint;
		
		JVector[] coords = points;
		
		if(CM == null) {
			calculateCM();
		}
		if(sixPoints[0] == null) {
			calculatePoint1();
		}
		if(sixPoints[1] == null) {
			calculatePoint2();
		}
		if(sixPoints[2] == null) {
			calculatePoint3();
		}

		for(int i = 0; i < coords.length; i++) {
			curPoint = coords[i];
			curLen = JVector.subtract(CM, coords[i]).length();
			angle = JVector.angleDegrees(
					JVector.subtract(CM, curPoint), 
					JVector.subtract(CM, sixPoints[2])
					);
			if(curLen > len && angle > (180-maxAngle)) {
				len = curLen;
				furthest = (JVector) curPoint.clone();
				actualAngle[3] = angle;
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
			this.sixPoints[3] = furthest;
		}
	}
	// step 4 of algorithm # 1 (EDD-10 p 87)
	public void calculatePoint5(int timesCalculated) {
		double len = 0, curLen, angle;
		JVector furthest = null, curPoint, cross;
		
		JVector[] coords = points;
		
		if(CM == null) {
			calculateCM();
		}
		if(sixPoints[0] == null) {
			calculatePoint1();
		}
		if(sixPoints[1] == null) {
			calculatePoint2();
		}
		if(sixPoints[2] == null) {
			calculatePoint3();
		}
		if(sixPoints[3] == null) {
			calculatePoint4();
		}

		for(int i = 0; i < coords.length; i++) {
			curPoint = coords[i];
			curLen = JVector.subtract(CM, coords[i]).length();
			cross = JVector.cross(
					JVector.subtract(CM, sixPoints[0]), 
					JVector.subtract(CM, sixPoints[2])
					);
			angle = JVector.angleDegrees(JVector.subtract(CM, curPoint), cross);
			if(curLen > len && angle < maxAngle) {
				len = curLen;
				furthest = (JVector) curPoint.clone();
				actualAngle[4] = angle;
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
				this.sixPoints[4] = CM;
			} else {
				calculatePoint5(++timesCalculated);
			}
			maxAngle -= angleIncrement;
		} else {
			this.sixPoints[4] = furthest;
		}
	}
	// step 4 of algorithm # 1 (EDD-10 p 87)
	public void calculatePoint6(int timesCalled) {
		double len = 0, curLen, angle;
		JVector furthest = null, curPoint, cross;
		
		JVector[] coords = points;
		
		if(CM == null) {
			calculateCM();
		}
		if(sixPoints[0] == null) {
			calculatePoint1();
		}
		if(sixPoints[1] == null) {
			calculatePoint2();
		}
		if(sixPoints[2] == null) {
			calculatePoint3();
		}
		if(sixPoints[3] == null) {
			calculatePoint4();
		}
		if(sixPoints[3] == null) {
			calculatePoint5(0);
		}

		for(int i = 0; i < coords.length; i++) {
			curPoint = coords[i];
			curLen = JVector.subtract(CM, coords[i]).length();
			cross = JVector.cross(
					JVector.subtract(CM, sixPoints[0]), 
					JVector.subtract(CM, sixPoints[3])
					);
			angle = JVector.angleDegrees(JVector.subtract(CM, curPoint), cross);
			if(curLen > len && angle < maxAngle) {
				len = curLen;
				furthest = (JVector) curPoint.clone();
				actualAngle[4] = angle;
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
				this.sixPoints[5] = CM;
			} else {
				calculatePoint6(++timesCalled);
			}
			maxAngle -= angleIncrement;
		} else {
			this.sixPoints[5] = furthest;
		}
	}
	public void writeNewXYZFile(File f) {
		MyPrintStream ps = new MyPrintStream(f);
		int numXtals = points.length;
		
		numXtals += sixPoints.length*7;
		ps.println(numXtals + "");
		ps.println("");
		
		for(int i = 0; i < sixPoints.length; i++) {
			ps.println("100\t" + sixPoints[i].i + "\t" + sixPoints[i].j + "\t" + sixPoints[i].k);
			ps.println("100\t" + CM.i + "\t" + CM.j + "\t" + CM.k);
		}
		for(int i = 0; i < points.length; i++) {
			ps.println(i + "\t" + points[i].i + "\t" + points[i].j + "\t" + points[i].k);
		}
	}
	/**
	 * 
	 * @return the lengths of the three orthogonal axes
	 */
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
	/**
	 * 
	 * @return (a+b+c)/max(a,b,c)
	 */
	public double getApproximateDimensionality() {
		double[] axisLengths = getAxisLengths();
		double max = 1;
		max = Math.max(axisLengths[0], axisLengths[1]);
		max = Math.max(max, axisLengths[2]);
		return axisLengths[0]/max + axisLengths[1]/max + axisLengths[2]/max;
	}
	/**
	 * 
	 * @return max(a,b,c)/min(a,b,c)
	 */
	public double getAnisotropy() {
		double[] axisLengths = getAxisLengths();
		double max = 1, min = 1;
		max = Math.max(axisLengths[0], axisLengths[1]);
		max = Math.max(max, axisLengths[2]);
		min = Math.min(axisLengths[0], axisLengths[1]);
		min = Math.min(min, axisLengths[2]);
		return max/min;
	}
	public JVector getCM() { return CM; }
	public JVector[] getSixPoints() { return sixPoints; }
	public JVector getSixPoints(int pointIdx) { return sixPoints[pointIdx]; }
	public double getMaxAngle(int pointIdx) { return actualAngle[pointIdx]; }
	
}
