package dca_ijoc;

/*
* An abstract class for function oracles which is convex in the nonnegative real line.  
* To extend this class, please implement the getValue method. 
*/
abstract public class Function{
	abstract public double getValue(double x);
}