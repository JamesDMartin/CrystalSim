package simulation;

import geometry.JVector;

import java.io.Serializable;
import java.util.Vector;

public class Lattice implements Serializable {

	private static final long serialVersionUID = -6957910015701592596L;
	private int a, b, c;
	private char[][][] subSystem;
	private int[][] bounds = {{-1, 1}, {-1, 1}, {-1, 1}};
	public Lattice(int numA, int numB, int numC) {
		a = numA;
		b = numB;
		c = numC;
		subSystem = new char[a][b][c];
	}
	public Lattice(int[] units) {
		this(units[0], units[1], units[2]);
	}	
	public Lattice(int num) {
		this(num, num, num);
	}
	/**
	 * 
	 * @param i x coordinate
	 * @param j y coordinate
	 * @param k z coordinate
	 * @return true: coordinates are inside the sample bounding shape and are not occupied. false: coordinate
	 * are either outside the sample bounding shape or are occupied by another crystal
	 */
	public boolean isOccupied(int i, int j, int k) {
		if(subSystem[i][j][k] == 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * A new lattice point can only be part of the crystal if the new <br>
	 * lattice point is directly touching any part of the existing crystal.
	 * @param i x coordinate
	 * @param j y coordinate
	 * @param k z coordinate
	 * @param xtalIdx index of the crystallite
	 * @param totalXtalSize current number of lattice points occupied by the crystallite
	 * @return false: lattice is already occupied or location is outside the bounds of the sample<br>
	 * true: lattice point successfully occupied
	 */
	public boolean occupy(int i, int j, int k, char xtalIdx, int totalXtalSize) {
		if(isOccupied(i, j, k)) { return false; }

		if(totalXtalSize == 0) {
			subSystem[i][j][k] = xtalIdx;
			return true;
		}
		int minI = ((i-1) > 0) ? i-1 : 0;
		int minJ = ((j-1) > 0) ? j-1 : 0;
		int minK = ((k-1) > 0) ? k-1 : 0;
		
		int maxI = ((i+1) < subSystem.length)		? i+1 : 0;
		int maxJ = ((j+1) < subSystem[0].length) 	? j+1 : 0;
		int maxK = ((k+1) < subSystem[0][0].length) ? k+1 : 0;
		
		if(subSystem[minI][j][k] == xtalIdx) {
			subSystem[i][j][k] = xtalIdx;
			return true;
		}
		if(subSystem[maxI][j][k] == xtalIdx) {
			subSystem[i][j][k] = xtalIdx;
			return true;
		}
		if(subSystem[i][minJ][k] == xtalIdx) {
			subSystem[i][j][k] = xtalIdx;
			return true;
		}
		if(subSystem[i][maxJ][k] == xtalIdx) {
			subSystem[i][j][k] = xtalIdx;
			return true;
		}
		if(subSystem[i][j][minK] == xtalIdx) {
			subSystem[i][j][k] = xtalIdx;
			return true;
		}
		if(subSystem[i][j][maxK] == xtalIdx) {
			subSystem[i][j][k] = xtalIdx;
			return true;
		}
		
		return false;
	}
	public void initOccupy(int i, int j, int k, char val) {
		subSystem[i][j][k] = val;
	}
	public char getVal(int i, int j, int k) { return subSystem[i][j][k]; }
	public int getX() { return a; }
	public int getY() { return b; }
	public int getZ() { return c; }
}