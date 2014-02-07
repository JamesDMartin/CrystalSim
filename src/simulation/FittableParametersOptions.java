package simulation;

public enum FittableParametersOptions {
	FIT_T_N,
	FIT_K_N,
	FIT_K_T,
	FIT_K_T_N,
	FIT_N,
	FIT_T,
	FIT_K,
	FIT_NONE,
	;
	
	public String toString() {
		switch(this) {
		case FIT_K:
			return "Fit k";
		case FIT_K_N:
			return "Fit k n";
		case FIT_K_T:
			return "Fit k t";
		case FIT_K_T_N:
			return "Fit k t n";
		case FIT_N:
			return "Fit n";
		case FIT_NONE:
			return "Fit none";
		case FIT_T:
			return "Fit t";
		case FIT_T_N:
			return "Fit t n";
		default:
			return "Fit none";
		}
	}
}