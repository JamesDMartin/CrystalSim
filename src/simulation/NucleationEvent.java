package simulation;
import java.io.Serializable;

import geometry.JVector;


public class NucleationEvent implements Serializable {

	private static final long serialVersionUID = 4290812620559138427L;
	private double time;
	private JVector location;
	
	public NucleationEvent(double time, JVector location) {
		this.time = time;
		this.location = location;
	}
	
	public double getTime() { return time; }
	public JVector getLocation() { return location; }
}
