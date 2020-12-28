package dca_ijoc;

/*
* A class for storing the solution to an instance of continuous RAP
*/
public class ResultTypeContinuousRAP {
	boolean feasible;
	double[] sol;
	public ResultTypeContinuousRAP(boolean feasible, double[] sol) {
		this.feasible = feasible;
		this.sol = sol;
	}
}