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
import java.util.EmptyStackException;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Stack;

import shapes.Shape;
import shapes.ShapeFactory;
import shapes.ShapeTypes;

import geometry.JVector;


public class CrystalFactory implements Observer, Serializable {

	private static final long serialVersionUID = -2491993333643652809L;
	private Sample sample;
	private int numCrystals;
	private SimulSetupParams sp;
	private Random r;
	private Stack<Short> indices;
	
	public CrystalFactory(Sample sample, SimulSetupParams sp) {
		this.sample = sample;
		r = new Random();
		this.sp = sp;
		numCrystals = 0;
		indices = new Stack<Short>();
		for(int i = (sp.getMaxNumCrystals()); i > 0; i--) {
			indices.push((short) i);
		}
//		System.out.println("First xtal idx: " + (int) indices.peek());
	}
	
	public Crystal getNewCrystal(double time) {
		JVector location;
		do {
			location = getCrystalLocation();
		}
		while(!sample.isInside(location));
		NucleationEvent ne = new NucleationEvent(time, location);
		ShapeTypes type = getShapeType();
		Shape shape = ShapeFactory.newShape(type, ne.getLocation(), getShapeOrientation(type));
		numCrystals++;
		short idx = 0;
		try  {
			idx = indices.pop();
			//System.out.println("Popped " + idx);
		} catch(EmptyStackException ese) {
			System.err.println(ese.getStackTrace());
		}
		return new Crystal(shape, ne, sp.getAxialGrowthRates(), idx, this);
	}
	public Crystal getNewCrystal(double time, JVector nucLoc) {
		NucleationEvent ne = new NucleationEvent(time, nucLoc);
		ShapeTypes type = getShapeType();
		Shape shape = ShapeFactory.newShape(type, ne.getLocation(), getShapeOrientation(type));
		numCrystals++;
		short idx = 0;
		try  {
			idx = indices.pop();
			//System.out.println("Popped " + idx);
		} catch(EmptyStackException ese) {
			System.err.println(ese.getStackTrace());
		}
		return new Crystal(shape, ne, sp.getAxialGrowthRates(), idx, this);
	}
	public Crystal getNewCrystal(double time, JVector nucLoc, JVector[] nucOrient) {
		NucleationEvent ne = new NucleationEvent(time, nucLoc);
		ShapeTypes type = getShapeType();
		Shape shape = ShapeFactory.newShape(type, ne.getLocation(), nucOrient);
		numCrystals++;
		short idx = 0;
		try  {
			idx = indices.pop();
			//System.out.println("Popped " + idx);
		} catch(EmptyStackException ese) {
			System.err.println(ese.getStackTrace());
		}
		return new Crystal(shape, ne, sp.getAxialGrowthRates(), idx, this);
	}
	private JVector getCrystalLocation() {
		switch(sp.getNucLoc()) {
		case Random:
			int[] units_i = sample.getUnitsPerAxis();
			double[] units_d = new double[units_i.length];
			for(int i = 0; i < units_d.length; i++) {
				units_d[i] = units_i[i];
			}
			return sample.getShape().getRandomPointInside(units_d);
		case Fixed:
			return sp.getNucLocOptions()[r.nextInt(sp.getNucLocOptions().length)];
		}
		throw new RuntimeException("Simulation Parameter: " + sp.getNucLoc().toString() + " does not have a corresponding " +
		"option in the method: getCrystalLocation() in the class: CrystalFactory");
	}
	private ShapeTypes getShapeType() {
		return sp.getNucShape();
	}
	private JVector[] getShapeOrientation(ShapeTypes type) {
		switch(sp.getNucOrient()) {
		case Random:
			switch(type) {
			case Cubic:
			case Tetragonal:
			case Orthorhombic:
				//return JVector.get100FamilyUnique();
				return JVector.getRandomlyAlignedOrthogonalAxes();
			case Spherical:
				return JVector.getRandomlyAlignedOrthogonalAxes();
			case Cylindrical:
				JVector[] axis = new JVector[1]; 
				axis[0] = new JVector(r.nextDouble(), r.nextDouble(), r.nextDouble()).unit();
				return axis;
			}
		case SetOfOrientations:
			return sp.getNucOrientVal()[r.nextInt(sp.getNucOrientVal().length)];
		}
		throw new RuntimeException("Simulation Parameter: " + sp.getNucOrient().toString() + " does not have a corresponding " +
		"option in the method: getShapeOrientation(ShapeTypes type) in the class: CrystalFactory");
	}
	
	public int getNumCrystals() { return numCrystals; }

	@Override
	public void update(Observable arg0, Object arg1) {
		if(arg0 instanceof Crystal) {
			Crystal c = (Crystal) arg0;
			if(arg1 instanceof Integer) {
				switch((Integer) arg1) {
				case Crystal.BAD_NUCLEATION_LOCATION:
					numCrystals--;
					indices.push(c.getCrystalIdx());
					//System.out.println("Pushed " + c.getCrystalIdx());
				}
			}
		}
		
	}
}
