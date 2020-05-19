/*
 *  Compilation:  javac -d . -classpath C:/gurobi801/win64/lib/gurobi.jar;. TestSparseGurobiDCA_Qua.java
 *  Execution:    java -classpath C:/gurobi801/win64/lib/gurobi.jar;. TestSparseGurobiDCA_Qua
 *
 *  Ubuntu Compilation: javac -cp ".:$HOME/Documents/gurobi801/linux64/lib/gurobi.jar" TestSparseGurobiDCA_Qua.java
 *  Ubuntu Execution: java -cp ".:$HOME/Documents/gurobi801/linux64/lib/gurobi.jar" TestSparseGurobiDCA_Qua
 *  
 *  Please change the path above to the directory of gurobi
 *
 *  A test class to evaluate the numerical performance of DCA, and Gurobi for DRAP-NC 
 *  with quadratic objectives
 *  @author Zeyang Wu @ University of Minnesota
 *
*/
package dca_ijoc;

import java.io.*;
import java.util.*;
import gurobi.*;
import java.text.DecimalFormat;

public class TestSparseGurobiDCA_Qua{
	/**
     * main method
     * initial the test and record the test result in a text file
     * You can modify the name, the header of the text file as well as the test cases
     */
	public static void main(String[] args) {
		int[] sizes = new int[]{50, 100, 200, 220, 240, 260, 280, 300, 320, 340, 360, 280, 300, 320, 340, 360, 380, 400};
		sizes = new int[]{10, 20, 50, 100, 120};
		int rep = 10;
		int[] varBounds = new int[]{100, 100};

		testRAPNCDCAGurobiQua(sizes, varBounds[0], rep, new Random(3000));
		testRAPNCDCAGurobiQua(sizes, varBounds[1], rep, new Random(4000));
		
		return;
	}

	private static void testRAPNCDCAGurobiQua(int[] sizes, int varBound, int rep, Random generator) {
		try {
			long time_stamp = System.currentTimeMillis();
			PrintStream o = new PrintStream(
				new File(
					"test_logs/DCA---Gurobi numerical experiment_" 
					+ String.format("Time=%s", time_stamp)
					+ String.format("_ObjType=%s", "quadratic")
					+ String.format("_varBound=%d.txt", varBound)
				)
			);
			System.setOut(o);
			System.out.println("Dimension	100*DCA	Gurobi		Gap  NodeCount(Explored)  RootGap  HGap");

			for (int i : sizes) {
				for (int j = 0; j < rep; j++) {
					compareDCAGurobiQuadratic(i, varBound, generator);
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
    private static void compareDCAGurobiQuadratic(int size, int varBound, Random generator) {

    	//generate a feasible RAPNC instance
		RAPNCTestUtils.RAPNCInstanceData instance_data = RAPNCTestUtils.generateInstanceData("quadratic", size, varBound, generator);
		int dimension = instance_data.dimension;
		long[] lbVar = instance_data.lbVar;
		long[] ubVar = instance_data.capacity;
		long[] lbNested = instance_data.lbNested;
		long[] ubNested = instance_data.ubNested;
		double[] cost_a = instance_data.cost_param_a;
		double[] cost_b = instance_data.cost_param_b;
        
        //Generate Gurobi instance
    	try {
    		GRBEnv env = new GRBEnv("qp.log");    		
      		GRBModel model = new GRBModel(env);
      		model.set("OutputFlag", "1");   
      		model.set("TimeLimit", "1200.0");   		
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
				typeGurobi[i] = GRB.INTEGER;
                varNames[i] = "x" + Integer.toString(i);
                
                lbNestGurobi[i] = (double) lbNested[i];
      			ubNestGurobi[i] = (double) ubNested[i];
      			varNamesY[i] = "y" + Integer.toString(i); 
		    }		
			
			GRBVar[] varlist = model.addVars(lbVarGurobi, ubVarGurobi, null, typeGurobi, varNames);
            GRBVar[] varlistY = model.addVars(lbNestGurobi, ubNestGurobi, null, typeGurobi, varNamesY);
            
      		// Set objective
			// Quadratic objectives
      		GRBQuadExpr objGurobi = new GRBQuadExpr();
      		for (int i = 0; i < dimension; i++) {
				objGurobi.addTerm(cost_b[i], varlist[i]);
				objGurobi.addTerm(cost_a[i], varlist[i], varlist[i]);
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

      		// Solve the problem by DCA algorithm
      		RAPNC test = instance_data.toRAPNC();
      		long startTime = System.currentTimeMillis();
			ResultTypeRAPNC res = test.solveIntegerDCA();
			if (dimension < 10000) {
				for (int i = 0; i < 99; i++) {
					res = test.solveIntegerDCA();
				}
			}
			long endTime = System.currentTimeMillis();
			long timeDCA = endTime - startTime;
			
 			// Optimize Gurobi model
 			startTime = System.currentTimeMillis();
			model.optimize();
			endTime = System.currentTimeMillis();
			long timeGurobi = endTime - startTime;

			double MipGap = 0; //model.get(GRB.DoubleAttr.MIPGap);
			DecimalFormat df = new DecimalFormat("#.#####"); 
			String MipGapFormatted = df.format(MipGap); 
			int NodeCount = (int) model.get(GRB.DoubleAttr.NodeCount);
			double sum = 0;
	  
			/* Sanity check 
			 *
			 */
			for (int i = 0; i < dimension; i++) {
				/*
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

			if (Math.abs((sum - model.get(GRB.DoubleAttr.ObjVal))/sum) > 0.001) {
				System.out.println(sum);
				System.out.println(model.get(GRB.DoubleAttr.ObjVal));
				System.out.println("The obj values differ by 0.1%!");
			}

			// Root Gap 
			model.reset();
			model.set("NodeLimit", "0");      
			model.optimize();
			double RootGap = model.get(GRB.DoubleAttr.MIPGap);
			DecimalFormat rdf = new DecimalFormat("#.#####"); 
			String RootGapFormatted = rdf.format(RootGap);

			// Node 1 Gap 
			model.reset();
			model.set("NodeLimit", "1");      
			model.optimize();
			double HGap = model.get(GRB.DoubleAttr.MIPGap);
			DecimalFormat hdf = new DecimalFormat("#.#####"); 
			String HGapFormatted = hdf.format(HGap);

			System.out.println(
				String.format("%10s", dimension) 
				+ String.format("%10s", ((double) timeDCA) / 1000) 
				+ String.format("%10s", ((double) timeGurobi) / 1000) 
				+ String.format("%10s", MipGapFormatted) 
				+ String.format("%20s", NodeCount) 
				+ String.format("%10s", RootGapFormatted) 
				+ String.format("%10s", HGapFormatted) 
			);
      		// Dispose of model and environment 
            
      		model.dispose();
      		env.dispose();

    	} catch (GRBException e) {
      		System.out.println("Error code: " + e.getErrorCode() + ". " +
          	e.getMessage());
    	}
    }

}