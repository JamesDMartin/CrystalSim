package simulation;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;

import shapes.Shape;
import shapes.ShapeFactory;
import shapes.ShapeTypes;
import geometry.JVector;

public class Sample implements Serializable {

	private static final long serialVersionUID = -5991424869777810615L;
	private Shape shape;
	private int[] unitsPerAxis_i;
	private double[] unitsPerAxis_d;
	private Vector<Crystal> crystals;
	private int totalVolume;
	private Lattice l;
	
	public Sample(ShapeTypes type, JVector[] unitAxes, int[] unitsPerAxis) {
		shape = createSampleShape(type, unitAxes, unitsPerAxis);
		this.unitsPerAxis_i = unitsPerAxis;
		unitsPerAxis_d = new double[unitsPerAxis_i.length];
		for(int i = 0; i < unitsPerAxis_d.length; i++) {
			unitsPerAxis_d[i] = unitsPerAxis_i[i];
		}
		crystals = new Vector<Crystal>(20);
		
		switch(shape.getType()) {
		case Cubic:
		case Orthorhombic:
		case Tetragonal:
			l = new Lattice(unitsPerAxis);
			break;
		case Spherical:
			l = new Lattice(unitsPerAxis[0]*2);
			break;
		case Cylindrical:
			l = new Lattice(unitsPerAxis[1]*2, unitsPerAxis[1]*2, unitsPerAxis[2]);
			break;
		}
		int latticePoints = initLattice();
		totalVolume = calculateTotalVolume();
	}
	private int initLattice() {
		JVector pos = new JVector(); 
		int latticePoints = 0;
		for(int i = 0; i < l.getX(); i++) {
			pos.i = i;
			for(int j = 0; j < l.getY(); j++) {
				pos.j = j;
				for(int k = 0; k < l.getZ(); k++) {
					pos.k = k;
					if(shape.isInside(unitsPerAxis_d, pos)) {
						latticePoints++;
						l.initOccupy(i, j, k, (char) 0);
					} else {
						l.initOccupy(i, j, k, Character.MAX_VALUE);
					}
				}
			}
		}
		return latticePoints;
	}
	
	public void grow(double curTime) {
		for(int i = 0; i < crystals.size(); i++) {
			crystals.get(i).grow(curTime);
		}
	}
	/*
	public int[] check_parallel_ByLattice(double curTime) {
		final int[] totalNewGrowth = new int[crystals.size()+3];
		final Lattice l = this.l;
		final int x_len = l.getX();
		final int y_len = l.getY();
		final int z_len = l.getZ();
		final Shape shape = this.shape;
		final double[] unitsPerAxis_d = this.unitsPerAxis_d;
		final Crystal[] crystals = this.crystals.toArray(new Crystal[this.crystals.size()]);
		final double time = curTime;
		Kernel kernel = new Kernel() {
			@Override
			public void run() {
				int thread_idx = getGlobalId();
				int xy = x_len*y_len;
				
				int z_idx = thread_idx / xy;
				int y_idx = (thread_idx - z_idx * xy) / x_len;
				int x_idx = thread_idx % x_len;
				JVector pos = new JVector(x_idx, y_idx, z_idx);
				
				if(shape.isInside(unitsPerAxis_d, pos)) {
					for(Crystal c : crystals) {
						if(c.getNucTime() <= time && c.isInside(pos)) {
							if(l.occupy(x_idx, y_idx, z_idx, c.getCrystalIdx(), c.getTotalSize())) {
								c.incrementGrowth();
								totalNewGrowth[c.getCrystalIdx()+3]++;
								totalNewGrowth[1]++;
								c.addLatticePoint(x_idx, y_idx, z_idx);
							}
						}
					}
					if(!l.isOccupied(x_idx, y_idx, z_idx)) {
						totalNewGrowth[2]++; //unoccupied lattice points inside bounding shape
					}
				}
			}
		};
		
		Range range = Range.create(x_len * y_len * z_len);
		int simultaneousThreads = 15;
		kernel.execute(range, x_len * y_len * z_len / simultaneousThreads);
		

		for(int i = 0; i < crystals.length; i++) {
			crystals[i].registerGrowth(totalNewGrowth[i + 3]);
		}
		
		return totalNewGrowth;
	}
	public int[] check_parallel_ByCrystals(double curTime) {
		final int[] totalNewGrowth = new int[crystals.size()+3];
		final Lattice l = this.l;
		final int x_len = l.getX();
		final int y_len = l.getY();
		final int z_len = l.getZ();
		final Shape shape = this.shape;
		final double[] unitsPerAxis_d = this.unitsPerAxis_d;
		final Crystal[] crystals = this.crystals.toArray(new Crystal[this.crystals.size()]);
		final double time = curTime;
		Kernel kernel = new Kernel() {
			@Override
			public void run() {
				int thread_idx = getGlobalId();
				int xy = x_len*y_len;
				
				int z_idx = thread_idx / xy;
				int y_idx = (thread_idx - z_idx * xy) / x_len;
				int x_idx = thread_idx % x_len;
				JVector pos = new JVector(x_idx, y_idx, z_idx);
				Crystal c = null;
				try {
					c = crystals[thread_idx];
				} catch(ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
				for(int x = 0; x < l.getX(); x++) {
					pos.setI(x);
					for(int y = 0; y < l.getY(); y++) {
						pos.setJ(y);
						for(int z = 0; z < l.getZ(); z++) {
							pos.setK(z);
							if(shape.isInside(unitsPerAxis_d, pos)) {
								totalNewGrowth[0]++; //total lattice points inside bounding shape
								if(c.getNucTime() <= time && c.isInside(pos)) {
									if(l.occupy(x, y, z, c.getCrystalIdx(), c.getTotalSize())) {
										c.incrementGrowth();
										totalNewGrowth[thread_idx+3]++;
										totalNewGrowth[1]++;
										c.addLatticePoint(x, y, z);
									}
								}
							}
						}
					}
				}
			}
		};
		
		Range range = Range.create(crystals.length);
		
		kernel.execute(range);
		
		for(int x = 0; x < l.getX(); x++) {
			for(int y = 0; y < l.getY(); y++) {
				for(int z = 0; z < l.getZ(); z++) {
					if(!l.isOccupied(x, y, z)) {
						totalNewGrowth[2]++; //unoccupied lattice points inside bounding shape
					}
				}
			}
		}
		for(int i = 0; i < crystals.length; i++) {
			crystals[i].registerGrowth(totalNewGrowth[i + 3]);
		}
		
		return totalNewGrowth;
	}
	public int[] check_Parallel(double curTime) {
		int[] totalNewGrowth = new int[crystals.size()+3];
		Arrays.fill(totalNewGrowth, 0);
		int unoccupied = 0;
		JVector pos = new JVector();
		Crystal curXtal;
		int latticePoints = 0;
		for(int i = 0; i < l.getX(); i++) {
			pos.i = i;
			for(int j = 0; j < l.getY(); j++) {
				pos.j = j;
				for(int k = 0; k < l.getZ(); k++) {
					pos.k = k;
					char curVal = l.getVal(i, j, k);
					if(curVal != Character.MAX_VALUE) {
						totalNewGrowth[0]++; //total lattice points inside bounding shape
						if(curVal == 0) {
							for(int c = 0; c < crystals.size(); c++) {
								curXtal = crystals.get(c);
								if(curXtal.getNucTime() <= curTime && curXtal.isInside(pos)) {
									if(l.occupy(i, j, k, curXtal.getCrystalIdx(), curXtal.getTotalSize())) {
										curXtal.incrementGrowth();
										totalNewGrowth[c+3]++;
										totalNewGrowth[1]++;
										curXtal.addLatticePoint(i, j, k);
									}
								}
							}
						}
						else {
							totalNewGrowth[2]++; //unoccupied lattice points inside bounding shape
						}
					}
				}
			}
		}
		Crystal c;
		for(int i = 0; i < crystals.size(); i++) {
			c = crystals.get(i);
			c.registerGrowth(totalNewGrowth[i+3]);
		}
		return totalNewGrowth;
	}
	*/
	public int[] check(double curTime) {
		int[] totalNewGrowth = new int[crystals.size()+3];
		Arrays.fill(totalNewGrowth, 0);
		int unoccupied = 0;
		JVector pos = new JVector();
		Crystal curXtal;
		int latticePoints = 0;
		for(int i = 0; i < l.getX(); i++) {
			pos.i = i;
			for(int j = 0; j < l.getY(); j++) {
				pos.j = j;
				for(int k = 0; k < l.getZ(); k++) {
					pos.k = k;
					char curVal = l.getVal(i, j, k);
					if(curVal != Character.MAX_VALUE) {
						totalNewGrowth[0]++; //total lattice points inside bounding shape
						if(curVal == 0) {
							for(int c = 0; c < crystals.size(); c++) {
								curXtal = crystals.get(c);
								if(curXtal.getNucTime() <= curTime && curXtal.isInside(pos)) {
									if(l.occupy(i, j, k, curXtal.getCrystalIdx(), curXtal.getTotalSize())) {
										curXtal.incrementGrowth();
										totalNewGrowth[c+3]++;
										totalNewGrowth[1]++;
										curXtal.addLatticePoint(i, j, k);
									}
								}
							}
						}
						else {
							totalNewGrowth[2]++; //unoccupied lattice points inside bounding shape
						}
					}
				}
			}
		}
		Crystal c;
		for(int i = 0; i < crystals.size(); i++) {
			c = crystals.get(i);
			c.registerGrowth(totalNewGrowth[i+3]);
		}
		return totalNewGrowth;
	}
	/**
	 * The box-like shapes have the zero point as (0, 0, 0).  The Spherical shape has the zero point as the center
	 * of the sphere.  The Cylindrical shape has the zero point as halfway along the center of the cylinder.
	 * @param type
	 * @param unitAxes
	 * @return
	 */
	private Shape createSampleShape(ShapeTypes type, JVector[] unitAxes, int[] unitsPerAxis) {
		JVector zero = null;
		double r, r1, r2;
		switch(type) {
		case Cubic:
		case Tetragonal:
		case Orthorhombic:
			zero = new JVector(((double) unitsPerAxis[0])/2., ((double) unitsPerAxis[1])/2., ((double) unitsPerAxis[2])/2.);
			return ShapeFactory.newShape(type, zero, unitAxes);
		case Spherical:
			r = unitsPerAxis[0];
			zero = new JVector(r, r, r);
			return ShapeFactory.newShape(type, zero, unitAxes);
		case Cylindrical:
			if(JVector.dot(unitAxes[2], JVector.z) > 0) {
				r1 = JVector.multiply(unitAxes[0], unitsPerAxis[0]).length();
				r2 = r1;
				zero = new JVector(r1, r2, unitAxes[2].k * 0.5 * unitsPerAxis[2]);
			} 
			if(zero == null) {
				throw new RuntimeException("Cylindrical Axis: " + unitAxes[0].toString() + " must be aligned along the x " + 
						" direction.  Method: createSampleShape(ShapeTypes type, JVector[] unitAxes) Class: Sample.java");
			}
			return ShapeFactory.newShape(type, zero, unitAxes);
		}
		throw new RuntimeException("Simulation Parameter: " + type.toString() + " does not have a corresponding " +
				"option in the method: createSampleShape(ShapeTypes type, JVector[] unitAxes) in the class: Sample.java");
	}
	
	public static final int NUCLEATION_SUCCESSFUL = 0;
	public static final int NUCLEATION_INSIDE_ANOTHER = 1;
	public static final int NUCLEATION_SUCCESSFUL_ADDITION_FAILED = 2;
	
	public int registerNewCrystal(Crystal c) {
		JVector nucLoc = c.getNucLoc();
		Crystal curr;
		for(int i = 0; i < crystals.size(); i++) {
			curr = crystals.get(i);
			if(curr.isInside(nucLoc)) {
				c.badNucLoc();
				return NUCLEATION_INSIDE_ANOTHER;
			}
		}
		if(crystals.add(c)) {
			return NUCLEATION_SUCCESSFUL;
		} else {
			return NUCLEATION_SUCCESSFUL_ADDITION_FAILED;
		}
	}
	public boolean isInside(JVector loc) {
		return shape.isInside(unitsPerAxis_d, loc);
	}
	private int calculateTotalVolume() {
		JVector pos = new JVector();
		int totalV = 0;
		for(int i = 0; i < l.getX(); i++) {
			pos.i = i;
			for(int j = 0; j < l.getY(); j++) {
				pos.j = j;
				for(int k = 0; k < l.getZ(); k++) {
					pos.k = k;
					if(shape.isInside(unitsPerAxis_d, pos)) {
						totalV++;
					}
				}
			}
		}
		return totalV;
	}
	public long calculateTransformedVolume() {
		JVector pos = new JVector();
		long totalV = 0;
		int Z;
		for(int i = 0; i < l.getX(); i++) {
			pos.i = i;
			for(int j = 0; j < l.getY(); j++) {
				pos.j = j;
				for(int k = 0; k < l.getZ(); k++) {
					pos.k = k;
					Z = ((int) l.getVal(i, j, k));
					if(shape.isInside(unitsPerAxis_d, pos) && Z > 0) {
						totalV++;
					}
				}
			}
		}
		return totalV;
	}
	public Shape getShape() { return shape; }
	public int[] getUnitsPerAxis() { return unitsPerAxis_i; }
	
	public int getTotalVolume() {
		if(totalVolume == 0) {
			calculateTotalVolume();
		}
		return totalVolume; 
	}
	
	public void printXYZ(PrintStream ps, boolean movie) {
		JVector pos = new JVector();
		int Z, offset = 0;
		String z = "";
		// zero = the indices of the crystallites whose volume contain less than 10 lattice points
		Integer[] zero = getCrystallitesToZero();
		int[] renumber = getCrystallitesToRenumber(zero);
		for(int i = 0; i < l.getX(); i++) {
			pos.i = i;
			String lines = "";
			for(int j = 0; j < l.getY(); j++) {
				pos.j = j;
				for(int k = 0; k < l.getZ(); k++) {
					pos.k = k;
					Z = ((int) l.getVal(i, j, k));
					if(shape.isInside(unitsPerAxis_d, pos)) {
						offset = 0;
						if(Z > 0) {
							offset = 40;
						}
						if(movie) {
							z = intToZ(Z);
						} else {
							z = "" + getNewZVal(renumber, zero, Z);
						}
						lines += z + "\t" + (i+offset) + "\t" + j + "\t" + k + "\n";
						
					}
				}
			}
			ps.print(lines);
		}
	}
	
	public int getNewZVal(int[] renumber, Integer[] zero, int Z) {
		for(int i = 0; i < zero.length; i++) {
			if(Z == zero[i]) {
				return 0;
			}
		}
		for(int i = 0; i < renumber.length; i++) {
			if(Z == renumber[i]) {
				return i+1;
			}
		}
		if(zero.length == 0 && renumber.length == 0) {
			return Z;
		}
		return 0;
	}
	/**
	 * 
	 * @param toZero Any crystallies whose total volume is
	 * @return
	 */
	public int[] getCrystallitesToRenumber(Integer[] toZero) {
		// max = the largest crystallite index. since crystallites indices are incremented from 1, this also
		// serves as the total number of crystallites
		int max = getMaxIdx();
		int[] toRenumber = new int[max - toZero.length-1];
		boolean add = false;
		int idx = 0;
		for(int i = 1; i <= max; i++, add = true) {
			for(int j = 0; j < toZero.length; j++) {
				if(i == toZero[j]) {
					add = false;
					break;
				}
			}
			if(add) {
				try {
					toRenumber[idx++] = i;
				} catch(ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		}
		for(int i = 0; i < toRenumber.length; i++) {
			//System.out.println("Need to renumber crystallite #: " + toRenumber[i] + " to: " + i);
		}
		return toRenumber;
	}
	/**
	 * Zero out crystallites with less than 10 total lattice points in their transformed volume
	 * @return
	 */
	public Integer[] getCrystallitesToZero() {
		int max = getMaxIdx();
		JVector pos = new JVector();
		int Z;
		int[] numPerIdx = new int[max+1];
		for(int i = 0; i < l.getX(); i++) {
			pos.i = i;
			for(int j = 0; j < l.getY(); j++) {
				pos.j = j;
				for(int k = 0; k < l.getZ(); k++) {
					pos.k = k;
					Z = ((int) l.getVal(i, j, k));
					if(Z > 0 && Z != Character.MAX_VALUE) {
						numPerIdx[Z]++;
					}
				}
			}
		}
		
		// zero out the ones with less than 10 points
		int maxXtals = 80;
		int currentXtals = max;
		int minIdx = 0;
		int minVal = 0;
		Stack<Integer> small = new Stack<Integer>();
		while(currentXtals >= maxXtals) {
			minIdx = 0;
			minVal = l.getX() * l.getY() * l.getZ();
			for(int i = 1; i < numPerIdx.length; i++) {
				if(numPerIdx[i] > 0 && numPerIdx[i] < minVal) {
					minVal = numPerIdx[i];
					minIdx = i;
				}
			}
			numPerIdx[minIdx] = -1;
			//System.out.println("minIdx: " + minIdx);
			small.push(minIdx);
			currentXtals--;
		}
		Integer[] toZeroArray = new Integer[small.size()];
		toZeroArray = small.toArray(toZeroArray);

		for(int i = 0; i < toZeroArray.length; i++) {
			//System.out.println("Need to zero crystallite #: " + toZeroArray[i] + " to: " + i);
		}
		return toZeroArray;
	}
	public int getMaxIdx() {
		JVector pos = new JVector();
		int Z;
		String z = "";
		int maxIdx = 0;
		for(int i = 0; i < l.getX(); i++) {
			pos.i = i;
			for(int j = 0; j < l.getY(); j++) {
				pos.j = j;
				for(int k = 0; k < l.getZ(); k++) {
					pos.k = k;
					Z = ((int) l.getVal(i, j, k));
					if(Z > maxIdx && Z != Character.MAX_VALUE) {
						maxIdx = Z;
					}
				}
			}
		}
		return maxIdx;
	}
	private String intToZ(int i) {
		String s = "";
		switch(i) {
		case 1: s = "H"; break;
		case 2: s = "He"; break;
		case 3: s = "Li"; break;
		case 4: s = "Be"; break;
		case 5: s = "B"; break;
		default: s = "Fe"; break;
		}
		return s;
	}
	public Iterator<Crystal> getCrystalIterator() {
		return crystals.iterator();
	}
	public Crystal[] getCrystalArray() {
		Crystal[] c = new Crystal[crystals.size()];
		c = crystals.toArray(c);
		return c;
	}
	
	public JVector getRandomNucLoc() {
		Vector<JVector> locations = new Vector<JVector>();
		JVector pos = new JVector();
		for(int i = 0; i < l.getX(); i++) {
			pos.i = i;
			for(int j = 0; j < l.getY(); j++) {
				pos.j = j;
				for(int k = 0; k < l.getZ(); k++) {
					pos.k = k;
					if(shape.isInside(unitsPerAxis_d, pos) && !l.isOccupied(i, j, k)) {
						locations.add((JVector) pos.clone());
					}
				}
			}
		}
		
		Random r = new Random();
		int idx = r.nextInt(locations.size());
		return locations.get(idx);
	}
	
	public void setLatticeToNull() {
		l = null;
	}
	public JVector[][] getLatticePoints() {
		JVector[][] points = new JVector[crystals.size()][];
		Crystal cur;
		for(int i = 0; i < crystals.size(); i++) {
			cur = crystals.get(i);
			points[i] = cur.getLatticePoints();
		}
		return points;
	}
	/**
	 * @deprecated
	 * @return
	 */
	public JVector[][] getLatticePoints_old() {
		Vector<Vector<JVector>> locations = new Vector<Vector<JVector>>();
		for(int i = 0; i < crystals.size(); i++) {
			locations.add(new Vector<JVector>());
		}
		int Z;
		JVector pos = new JVector();
		for(int i = 0; i < l.getX(); i++) {
			pos.i = i;
			for(int j = 0; j < l.getY(); j++) {
				pos.j = j;
				for(int k = 0; k < l.getZ(); k++) {
					pos.k = k;
					if(shape.isInside(unitsPerAxis_d, pos)) {
						Z = l.getVal(i, j, k)-1;
						locations.get(Z).add((JVector) pos.clone());
					}
				}
			}
		}
		
		JVector[][] latticePoints = new JVector[crystals.size()][];
		for(int i = 0; i < latticePoints.length; i++) {
			latticePoints[i] = new JVector[locations.get(i).size()];
			latticePoints[i] = locations.get(i).toArray(latticePoints[i]);
		}
		return latticePoints;
	}
}
