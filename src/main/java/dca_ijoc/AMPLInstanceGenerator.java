/******************************************************************************
 *  Compilation:  javac -cp ./ AMPLTestGenerator.java
 *  Execution:    java -cp ./ AMPLTestGenerator
 *  
 *  A class to generate AMPL data files
 *  @author Zeyang Wu @ University of Minnesota
 *
 ******************************************************************************/
package dca_ijoc;
import java.util.*;
import java.io.*;

public class AMPLInstanceGenerator {
	/**
     * main method
     * initial the test and record the test result in a text file
     * You can modify the name, the header of the text file as well as the test cases
     */
	public static void main(String[] args) throws FileNotFoundException{

        String[] objFuncTypes = new String[]{"Con_F", "Con_FUEL", "Con_CRASH"};

		int varBound = 100;

		//test settings (dimension)
        int[] testSize = new int[]{10, 20, 30, 100, 200, 800, 1600, 3200, 6400, 12800, 25600, 51200, 51200<<1};
        //testSize = new int[]{10, 20, 30, 100, 200};
        
        for (String objFuncType : objFuncTypes) {
            String filename = "rapnc_ampl_" + objFuncType + "_n=";
            for (int test_size : testSize) {
                write_data_file(
                    objFuncType,
                    test_size, 
                    varBound, 
                    filename + Long.toString(test_size)+".dat"
                );
            }
        }

		return;
	}
    
    /**
	 * write_data_file method A class to generate AMPL data files Default:
	 * 
     * @param objFuncType string that represents the function type
	 * @param size     dimension of the problem
	 * @param varBound related to the upper bound of the box constraints: d_i < 1.3 * varBound     
	 * @throws FileNotFoundException
	 */
	public static void write_data_file(String objFuncType, int size, int varBound, String filename) throws FileNotFoundException {
        //Set up output enviroment
        File filepath = new File("./ampl_instances/datafiles/" + objFuncType);
        filepath.mkdirs();
        filepath = new File("./ampl_instances/datafiles/" + objFuncType + '/' + filename);
        PrintStream output = new PrintStream(filepath);
		System.setOut(output);

        RAPNCTestUtils.RAPNCInstanceData data = RAPNCTestUtils.generateInstanceData(objFuncType, size, varBound);
		long[] ubVar = data.capacity;
		long[] lbNested = data.lbNested;
		long[] ubNested = data.ubNested;
		double[] cost_a = data.cost_param_a;
		double[] cost_b = data.cost_param_b;
		  
		// Record the optimal value by DCA
		RAPNC instance = data.toRAPNC();
		ResultTypeRAPNC res = instance.solveIntegerDCA();
        double obj_optimal = 0;
        List<Function> objs = instance.obj;
		for (int i = 0; i < size; i++) {
			obj_optimal += objs.get(i).getValue(res.sol[i]);
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
