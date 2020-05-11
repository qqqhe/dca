package dca_ijoc;

/*
* A class for storing the solution to an instance of DRAP-NC 
*/
public class ResultTypeRAPNC {
	boolean feasible;
	public long[] sol;
	public ResultTypeRAPNC(boolean feasible, long[] sol) {
		this.feasible = feasible;
		this.sol = sol;
	}
}
