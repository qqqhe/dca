/*
 *  Compilation:  javac -d . -classpath C:/gurobi801/win64/lib/gurobi.jar;. TestSparseGurobiDCA_Lin.java
 *  Execution:    java -classpath C:/gurobi801/win64/lib/gurobi.jar;. TestSparseGurobiDCA_Lin
 *
 *  Ubuntu Compilation: javac -cp ".:$HOME/Documents/gurobi801/linux64/lib/gurobi.jar" TestSparseGurobiDCA_Lin.java
 *  Ubuntu Execution: java -cp ".:$HOME/Documents/gurobi801/linux64/lib/gurobi.jar" TestSparseGurobiDCA_Lin
 *  
 *  Please change the path above to the directory of gurobi
 *
 *  A test class to evaluate the numerical performance of DCA, and Gurobi for DRAP-NC 
 *  with linear objectives
 *  @author Zeyang Wu @ University of Minnesota
 *
*/

package dca_ijoc;

import java.util.*;
import java.io.*;
import gurobi.*;

public class TestSparseGurobiDCA_Lin {
	/**
     * main method
     * initial the test and record the test result in a text file
     * You can modify the name, the header of the text file as well as the test cases
     */
	public static void main(String[] args) throws FileNotFoundException {
		int[] sizes = new int[]{3200, 6400, 12800, 25600, 52100};
		sizes = new int[]{3200, 6400, 12800, 25600, 52100, 51200<<1, 51200<<2, 51200<<3, 51200<<4, 51200<<5};
		int rep = 10;
		int[] varBounds = new int[]{10, 100};

		testRAPNCDCAGurobiLinear(sizes, varBounds[0], rep, new Random(1000));
		testRAPNCDCAGurobiLinear(sizes, varBounds[1], rep, new Random(2000));
	}

	public static void testRAPNCDCAGurobiLinear(int[] sizes, int varBound, int rep, Random generator) {
		try {
			long time_stamp = System.currentTimeMillis();
			PrintStream o = new PrintStream(
				new File(
					"test_logs/DCA---Gurobi numerical experiment_" 
					+ String.format("Time=%s", time_stamp)
					+ String.format("_ObjType=%s", "linear")
					+ String.format("_varBound=%d.txt", varBound)
				)
			);
			System.setOut(o);
			System.out.println("Dimension		DCA		Gurobi");

			for (int i : sizes) {
				for (int j = 0; j < rep; j++) {
					compareDCAGurobiLinear(i, varBound, generator);
				}
				System.out.println("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
     * test method
     * This is a static method that performs the tests.
     * You can modify the parameters to perform any tests 
     * The generation procedure ensures that every test instance is feasible
     * Default:
     * @param size dimension of the problem
     * @param varBound related to the upper bound of the box constraints: d_i < 1.3 * varBound
     */
    public static void compareDCAGurobiLinear(int size, int varBound, Random generator) {

    	//generate a feasible RAPNC instance
		RAPNCTestUtils.RAPNCInstanceData instance_data = RAPNCTestUtils.generateInstanceData("linear", size, varBound, generator);
		int dimension = instance_data.dimension;
		long[] lbVar = instance_data.lbVar;
		long[] ubVar = instance_data.capacity;
		long[] lbNested = instance_data.lbNested;
		long[] ubNested = instance_data.ubNested;
		// double[] cost_a = instance_data.cost_param_a; This parameter is not used in linear function
        double[] cost_b = instance_data.cost_param_b;
        

		// Generate Gurobi instances
	   	try {
    		GRBEnv env = new GRBEnv("qp.log");    		
      		GRBModel model = new GRBModel(env);
      		model.set("OutputFlag", "0"); //Disable logging to avoid I/O time
            //model.set(GRB.DoubleParam.FeasibilityTol, 0.000001);
              
            String[] varNames = new String[dimension];
            String[] varNamesY = new String[dimension];
      		double[] lbVarGurobi = new double[dimension];
            double[] ubVarGurobi = new double[dimension];
            double[] lbNestGurobi = new double[dimension];
      		double[] ubNestGurobi = new double[dimension];
            char[] typeGurobi = new char[dimension];
            
			// Create variables
      		for (int i = 0; i < dimension; i++) {
				lbVarGurobi[i] = (double) lbVar[i];
				ubVarGurobi[i] = (double) ubVar[i];		
				typeGurobi[i] = GRB.CONTINUOUS;
                varNames[i] = "x" + Integer.toString(i);
                
                lbNestGurobi[i] = (double) lbNested[i];
      			ubNestGurobi[i] = (double) ubNested[i];
      			varNamesY[i] = "y" + Integer.toString(i);    	
		    }		
			
			GRBVar[] varlist = model.addVars(lbVarGurobi, ubVarGurobi, null, typeGurobi, varNames);
            GRBVar[] varlistY = model.addVars(lbNestGurobi, ubNestGurobi, null, typeGurobi, varNamesY);

      		// Set objective
      		GRBLinExpr objGurobi = new GRBLinExpr();
      		for (int i = 0; i < dimension; i++) {
				objGurobi.addTerm(cost_b[i], varlist[i]);
      		}
    
      		model.setObjective(objGurobi);

            // Set nested constraint
      		for (int i = 0; i < dimension; i++) {
                GRBLinExpr exprNested = new GRBLinExpr();
                exprNested.addTerm(1.0, varlistY[i]);
                exprNested.addTerm(-1.0, varlist[i]);
                if (i > 0) {
                    exprNested.addTerm(-1.0, varlistY[i-1]);
                }
                model.addConstr(exprNested, GRB.EQUAL, 0, "NESTED" + Integer.toString(i));
            }

      		//solve the problem by DCA 
      		RAPNC test = instance_data.toRAPNC();
      		long startTime = System.currentTimeMillis();
			ResultTypeRAPNC res = test.solveIntegerLinearDCA();

			if (dimension < 10000) {
				for (int i = 0; i < 9; i++) {
					res = test.solveIntegerLinearDCA();
				}
			}
			long endTime = System.currentTimeMillis();
			long timeDCA = endTime - startTime;

			if (dimension < 10000) {
				timeDCA = Math.round(((double) timeDCA) / 10);
            } 
			
			
 			// Optimize Gurobi model
 			startTime = System.currentTimeMillis();
			model.optimize();
			endTime = System.currentTimeMillis();
			long timeGurobi = endTime - startTime;

			double sum = 0;
      
			for (int i = 0; i < dimension; i++) {
				/* The solution might be differernt, but the objective value is the same.
				double val = varlist[i].get(GRB.DoubleAttr.X);
				if (Math.abs(val - res.sol[i]) >= 0.01) {
					System.out.println("No, the solution x[i] is different.");
					System.out.print(val);
					System.out.print(" ");
					System.out.println(res.sol[i]);
				}
				*/
				sum += test.obj.get(i).getValue(res.sol[i]);
			}

			if (Math.abs(sum - model.get(GRB.DoubleAttr.ObjVal)) > 0.001) {
				System.out.println(sum);
				System.out.println(model.get(GRB.DoubleAttr.ObjVal));
				System.out.println("No! The obj is not the same. But the values are close. Gurobi is not optimal");
			}

      		System.out.println(
				String.format("%10s", dimension) 
				+ String.format("%10s", ((double) timeDCA) / 1000) 
				+ String.format("%10s", ((double) timeGurobi) / 1000) 
			);
			  
			// Dispose the model and environment 
      		model.dispose();
      		env.dispose();

    	} catch (GRBException e) {
      		System.out.println("Error code: " + e.getErrorCode() + ". " +
          	e.getMessage());
    	}
    }

}

