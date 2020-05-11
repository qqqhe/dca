/*
 *  Compilation:  javac -d . -classpath C:/gurobi801/win64/lib/gurobi.jar;. TestGurobiDCA_Qua.java
 *  Execution:    java -classpath C:/gurobi801/win64/lib/gurobi.jar;. TestGurobiDCA_Qua
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
import java.text.DecimalFormat;
import gurobi.*;

public class TestGurobiDCA_Qua {
	/**
     * main method
     * initial the test and record the test result in a text file
     * You can modify the name, the header of the text file as well as the test cases
     */
	public static void main(String[] args) throws FileNotFoundException{
		//Set output environment
		long currentTime = System.currentTimeMillis();
		PrintStream o = new PrintStream(new File("DCA----Gurobi" + " Quadratic " + Long.toString(currentTime) + "   Vb 100" + ".txt"));
		System.setOut(o);

		
		//test settings (dimension)
		//For gurobi, it is recommend to set size less than 10000.

		int[] testSize = new int[]{50, 100, 200, 220, 240, 260, 280, 300, 320, 340, 360, 280, 300, 320, 340, 360, 380, 400};
		testSize = new int[]{10, 20};
		int rep = 10;
		int varBound = 10;

		System.out.println("Test: Variable bound " + varBound + ": objectives type: Quadratic Function");
		System.out.println("Dimension        DCA   Gurobi   Gap      NodeCount(Explored)    RootGap      HGap");

		for (int i = 0; i < testSize.length; i++) {
			for (int j = 0; j < rep; j++) {
				test(testSize[i], varBound);
			}
			System.out.println(" ");
		}
		
		return;
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
    public static void test (int size, int varBound) {

    	//generate a feasible RAPNC instance
		RAPNCTestUtils.RAPNCInstanceData instance_data = RAPNCTestUtils.generateInstanceData("quadratic", size, varBound);
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
		    }		
			
			GRBVar[] varlist = model.addVars(lbVarGurobi, ubVarGurobi, null, typeGurobi, varNames);
      		
      		// Set objective
			// Quadratic objectives
            GRBQuadExpr objGurobi = new GRBQuadExpr();
            for (int i = 0; i < dimension; i++) {
              objGurobi.addTerm(cost_b[i], varlist[i]);
              objGurobi.addTerm(cost_a[i], varlist[i], varlist[i]);
            }
            
      		model.setObjective(objGurobi);

      		//Nested constraints
      		//reset from 0 to dimension - 1
      		for (int i = 0; i < dimension - 1; i++) {
      			GRBLinExpr exprNested = new GRBLinExpr();
      			for (int j = 0; j <= i; j++) {
      				exprNested.addTerm(1.0, varlist[j]);
      			}
      			model.addConstr(exprNested, GRB.LESS_EQUAL, (double) ubNested[i], "u" + Integer.toString(i));
      			model.addConstr(exprNested, GRB.GREATER_EQUAL, (double) lbNested[i], "l" + Integer.toString(i));
      		}
            
            // Add constraint:\sum x_i = B
      		GRBLinExpr expr = new GRBLinExpr();
      		for (int i = 0; i < dimension; i++) {
      			expr.addTerm(1.0, varlist[i]);
      		}
      		model.addConstr(expr, GRB.EQUAL, ubNestGurobi[dimension-1], "c0");

      		//solve the problem by DCA algorithm
      		RAPNC test = instance_data.toRAPNC();
      		long startTime = System.currentTimeMillis();
			ResultTypeRAPNC res = test.solveIntegerDCA();
			for (int i = 0; i < 100; i++) {
				res = test.solveIntegerDCA();
			}

			//time 
			long endTime = System.currentTimeMillis();
			long timeDCA = endTime - startTime;
    		//System.out.println("That took " + timeDCA + " milliseconds");

 			// Optimize Gurobi model
 			startTime = System.currentTimeMillis();
 			// model.write("example_instance_300.mps");
			model.optimize();
			endTime = System.currentTimeMillis();
			long timeGurobi = endTime - startTime;

			double MipGap = model.get(GRB.DoubleAttr.MIPGap);
			DecimalFormat df = new DecimalFormat("#.#####"); 
			String MipGapFormatted = df.format(MipGap); 
			int NodeCount = (int) model.get(GRB.DoubleAttr.NodeCount);
			double sum = 0;
      
			for (int i = 0; i < dimension; i++) {
				double val = varlist[i].get(GRB.DoubleAttr.X);
				if (Math.abs(val - res.sol[i]) >= 0.01) {
					System.out.println("No, the solution x[i] is different.");
					System.out.print(val);
					System.out.print(" ");
					System.out.println(res.sol[i]);
				}
				sum += test.obj.get(i).getValue(res.sol[i]);
			}

			if (Math.abs(sum - model.get(GRB.DoubleAttr.ObjVal)) > 0.001) {
				System.out.println(sum);
				System.out.println(model.get(GRB.DoubleAttr.ObjVal));
				System.out.println("No!!!!!!! The obj is not the same. But the values are close. Gurobi is not optimal");
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
