package simulation;

public enum Termination {
	FractionComplete, TimeElapsed, NumberOfCrystals;
	
	public boolean terminate(SimulSetupParams sp, double val) {
		switch(sp.getTerm()) {
		case FractionComplete:
			if( val < (Double) sp.getTermVal()) {
				return false;
			}
			return true;
		case TimeElapsed:
			if( val < (Double) sp.getTermVal()) {
				return false;
			}
			return true;
		}
		throw new RuntimeException("Terminiation Parameter: " + sp.getTerm().toString() + " does not have a corresponding " +
				"option in the method: terminate(SimulParams sp, Object val) in the class: Termination");
	}
	
}
