/******************************************************************************
 *  Compilation:  javac -cp ./ AMPLTestGenerator.java
 *  Execution:    java -cp ./ AMPLTestGenerator
 *  
 *  A class to generate AMPL data files
 *  @author Zeyang Wu @ University of Minnesota
 *
 ******************************************************************************/
import java.util.*;

import java.io.*;

public class AMPLTestGenerator {

	/**
     * An internal class to define the Objective functions
     * The name is ObjectiveFunction, you can change it to any (convex) function 
     * by overwriting the abstract method getValue(double x)  
     * @param a, b, etc. parameter of the function
     */
	private static class ObjectiveFunction extends Function {
		double a;
		double b;
		public ObjectiveFunction(double a, double b) {
			this.a = a;
			this.b = b;
		}
		public double getValue(double x) {
			//return b * x;
			//return a * x * x + b * x; //quadratic function
			return 10 * b + a / (x + 1); //Crash function
			//return a * b * b / (x + 1) / (x + 1) / (x + 1); // Fuel function
			//return x * x * x * x / 4 +  b * x ; // F function
		}
	}


	/**
     * main method
     * initial the test and record the test result in a text file
     * You can modify the name, the header of the text file as well as the test cases
     */
	public static void main(String[] args) throws FileNotFoundException{
		//Set output environment

		//Please change the ObjectiveFunction above accordingly
		//String obj_function_type = "Linear";
		//String obj_function_type = "Quadratic";
		String obj_function_type = "Con_Crash";
		//String obj_function_type = "Con_Fuel";
		//String obj_function_type = "Con_F";

		int varBound = 100;

		//test settings (dimension)
		int[] testSize = new int[]{800, 1600, 3200, 6400, 12800, 25600, 51200, 51200<<1};
		testSize = new int[]{10, 20, 30, 100, 200, 800, 1600, 3200, 6400, 12800, 25600, 51200, 51200<<1, 51200<<2, 51200<<3, 51200<<4,  51200<<5, 51200<<6, 51200<<7};
		testSize = new int[]{10, 20, 30, 100, 200, 800, 1600, 3200, 6400, 12800, 25600, 51200, 51200<<1, 51200<<2};
		//testSize = new int[]{10, 20, 30, 100, 200};
		String filename = "rapnc_ampl_" + obj_function_type + "_n=";

		for (int i = 0; i < testSize.length; i++) {
			write_data_file(
				testSize[i], 
				varBound, 
				filename + Long.toString(testSize[i])+".dat"
			);
		}
		
		return;
	}
    
    /**
	 * write_data_file method A class to generate AMPL data files Default:
	 * 
	 * @param size     dimension of the problem
	 * @param varBound related to the upper bound of the box constraints: d_i < 1.3
	 *                 * varBound
	 * @throws FileNotFoundException
	 */
	public static void write_data_file(int size, int varBound, String filename) throws FileNotFoundException {
		PrintStream o = new PrintStream(new File("./ampl_instances/"+ filename));
		System.setOut(o);

    	List<Function> obj = new ArrayList<Function>();
		int dimension = size; 
		long[] lbVar = new long[dimension];
		long[] ubVar = new long[dimension];
		long[] lbNested = new long[dimension];
		long[] ubNested = new long[dimension];
		double[] cost_a = new double[dimension];
		double[] cost_b = new double[dimension];
		long B = (long) (0.75 * dimension * dimension);


		// Create box constraints on variables
      	for (int i = 0; i < dimension; i++) {
			long b = (long) ((Math.random() + 0.3)* varBound);
			long c = (long) (Math.random() * varBound);
			if (b < c) {
				lbVar[i] = b;
				ubVar[i] = c;
			} else {
				lbVar[i] = c;
				ubVar[i] = b;
			}				
			lbVar[i] = 0;
		}		
			
      		
      	// Set objective      		
      	for (int i = 0; i < dimension; i++) {
      		double a = Math.random();
      		double b = Math.random();
      		if (Math.random() < 0.5) {
      			b = -b;
      		}
      		ObjectiveFunction fi = new ObjectiveFunction(a, b);
			obj.add(fi);

			cost_a[i] = a;
			cost_b[i] = b; 
      	}
            

      	//Nested constraints
      	double a = 0;
      	double b = 0;
      	//reset from 1 to dimension
      	for (int i = 0; i < dimension - 1; i++) {
      		a += lbVar[i] + Math.random() * (ubVar[i] - lbVar[i]);
      		b += lbVar[i] + Math.random() * (ubVar[i] - lbVar[i]);
      		if (a > b) {
      			lbNested[i] = (long) b;
      			ubNested[i] = (long) a;
      		} else {
      			lbNested[i] = (long) a;
      			ubNested[i] = (long) b;
      		}
      	}
            
        // Add constraint:\sum x_i = B
      	a += lbVar[dimension - 1] + Math.random() * (ubVar[dimension - 1] - lbVar[dimension - 1]);
      	b += lbVar[dimension - 1] + Math.random() * (ubVar[dimension - 1] - lbVar[dimension - 1]);
      	B = (long) Math.max(a, b);
      	// Greedy Algorithm
      	lbNested[dimension - 1] = B;
      	ubNested[dimension - 1] = B;
		  
		// Record the optimal value by DCA
		RAPNC instance = new RAPNC(obj, lbVar, ubVar, lbNested, ubNested);
		ResultTypeRAPNC res = instance.solveIntegerDCA();
		double obj_optimal = 0;
		for (int i = 0; i < size; i++) {
			obj_optimal += obj.get(i).getValue(res.sol[i]);
		}	
		System.out.println(String.format("# Optimal value by DCA: %f", obj_optimal));
		// Write the data file
		System.out.println("data;");
		System.out.println("");
		System.out.println(String.format("param N = %d;", size));
		System.out.println("param capacity := ");
		for (int i = 0; i < size; i++) {
			System.out.println(String.format("%d %d", i + 1, ubVar[i]));
		}
		System.out.println(";");
		System.out.println("param nested_lowerbound := ");
		for (int i = 0; i < size; i++) {
			System.out.println(String.format("%d %d", i + 1, lbNested[i]));
		}
		System.out.println(";");
		System.out.println("param nested_upperbound := ");
		for (int i = 0; i < size; i++) {
			System.out.println(String.format("%d %d", i + 1, ubNested[i]));
		}
		System.out.println(";");
		System.out.println("param cost_a := ");
		for (int i = 0; i < size; i++) {
			System.out.println(String.format("%d %f", i + 1, cost_a[i]));
		}	
		System.out.println(";");
		System.out.println("param cost_b := ");
		for (int i = 0; i < size; i++) {
			System.out.println(String.format("%d %f", i + 1, cost_b[i]));
		}		
		System.out.println(";");	  
    }
}
