package dca_ijoc;

import java.io.*;
import java.util.*;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

/******************************************************************************
 *  Compilation:  javac -cp ./ RAPNCTestUtils.java
 *  Execution:    java -cp ./ RAPNCTestUtils
 *  
 *  A utility class for test algorithms for RAPNC
 *  @author Zeyang Wu @ University of Minnesota
 *
 ******************************************************************************/

public class RAPNCTestUtils {

    private static final class LinearFunction extends Function {
        double a;
        double b;

        public LinearFunction(final double a, final double b) {
            this.a = a;
            this.b = b;
        }

        public double getValue(double x) {
            return b * x; // Linear function
        }
    }

    private static final class QuadraticFunction extends Function {
        double a;
        double b;

        public QuadraticFunction(final double a, final double b) {
            this.a = a;
            this.b = b;
        }

        public double getValue(double x) {
			return a * x * x + b * x; //quadratic function
		}
    }
    
    private static final class FFunction extends Function {
		double a;
		double b;
		public FFunction(final double a, final double b) {
            this.a = a;
            this.b = b;
        }

        public double getValue(double x) {
            return x * x * x * x / 4 + b * x; // [F] function
        }
    }

    private static final class FUELFunction extends Function {
        double a;
        double b;

        public FUELFunction(final double a, final double b) {
            this.a = a;
            this.b = b;
        }

        public double getValue(double x) {
            return a * b * b / x / x / x; // [FUEL] function
        }
    }

    private static final class CRASHFunction extends Function {
        double a;
        double b;

        public CRASHFunction(final double a, final double b) {
            this.a = a;
            this.b = b;
        }

        public double getValue(double x) {
            return 10 * b + a / x; // [CRASH] function
        }
    } 

    /*
    * This function is specially design for AMPL test instances as commercial nonlinear solver cannot handle the edge case when x = 0.
    */
    private static final class AMPLFUELFunction extends Function {
        double a;
        double b;

        public AMPLFUELFunction(final double a, final double b) {
            this.a = a;
            this.b = b;
        }

        public double getValue(double x) {
            return a * b * b / (x + 0.01) / (x + 0.01) / (x + 0.01); // [FUEL] function
        }
    }

    /*
    * This function is specially design for AMPL test instances as commercial nonlinear solver cannot handle the edge case when x = 0.
    */
    private static final class AMPLCRASHFunction extends Function {
        double a;
        double b;

        public AMPLCRASHFunction(final double a, final double b) {
            this.a = a;
            this.b = b;
        }

        public double getValue(double x) {
            return 10 * b + a / (x + 0.01); // [CRASH] function
        }
    } 

    /**
     * buildObjectFunction method 
     * returns an instance of correponding Function that implements a getValue(double x) method
     * Time-Complexity: O(1) 
     * @param double a
     * @param double b
     * @param String objFuncType
     */
    public static Function buildObjectFunction(double a, double b, String objFuncType){
        switch (objFuncType) {
            case "linear":
                return new LinearFunction(a, b);
            case "quadratic":
                return new QuadraticFunction(a, b);
            case "f":
            case "Con_F":
                return new FFunction(a, b);
            case "fuel":
                return new FUELFunction(a, b);
            case "crash":
                return new CRASHFunction(a, b);
            case "Con_FUEL":
                return new AMPLFUELFunction(a, b);
            case "Con_CRASH":
                return new AMPLCRASHFunction(a, b);
            default: 
                return null;
        } 
    }

    static class RAPNCInstanceData {
        String objFuncType;
        int dimension; 
        long[] lbVar;
		long[] capacity;
		long[] lbNested;
		long[] ubNested;
		double[] cost_param_a;
        double[] cost_param_b;

        public RAPNCInstanceData(
            String objFuncType,
            int dimension,
            long[] capacity,
            long[] lbNested,
            long[] ubNested,
            double[] cost_param_a,
            double[] cost_param_b
        ) {
            this.objFuncType = objFuncType;
            this.dimension = dimension;
            this.lbVar = new long[dimension];
            this.capacity = capacity;
            this.lbNested = lbNested;
            this.ubNested = ubNested;
            this.cost_param_a = cost_param_a;
            this.cost_param_b = cost_param_b;
        }

        /**
         * JSONize method 
         * A method that serilize the data to a json string with pretty printing format
         * Time-Complexity: O(n) 
         */
        public String JSONize() {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(this);
        }

        /**
         * JSONizeToFile method 
         * A method that serilize the data to a json string with pretty printing format
         * Time-Complexity: O(n) 
         */
        public void JSONizeToFile(String filename) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try {
                // create a writer
                Writer writer = new FileWriter(filename);
            
                // convert map to JSON File
                gson.toJson(this, writer);
            
                // close the writer
                writer.close();
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public static RAPNCInstanceData readJsonData(String jsondata){          
            Gson gson = new Gson();
            return gson.fromJson(jsondata, RAPNCInstanceData.class);
        }


        /**
         * toRAPNC method 
         * This is a method that build a RAPNC instance from the RAPNCInstanceData
         * Time-Complexity: O(n) 
         */
        public RAPNC toRAPNC() {
            List<Function> obj = new ArrayList<Function>();
            for (int i = 0; i < this.dimension; i++) {
                obj.add(buildObjectFunction(cost_param_a[i], cost_param_b[i], this.objFuncType));
            }
            // JSONObject json = new JSONObject();
            return new RAPNC(obj, this.lbVar, this.capacity, this.lbNested, this.ubNested);
        }
    }

    /**
     * generateTestInstanceData method 
     * This is a method that generates an instance of RAPNC.
     * Time-Complexity: O(n) 
     * @param objFuncType type of object function, we support ["linear", "quadratic", "f", "fuel", "crash"]
     * @param size size of the instances 
     * @param varBound the upperbound of capacity 
     * @return an instance of RAPNC data
     */
    public static RAPNCInstanceData generateInstanceData(String objFuncType, int size, int varBound) {
        int dimension = size; 
		long[] lbVar = new long[dimension];
		long[] ubVar = new long[dimension];
		long[] lbNested = new long[dimension];
		long[] ubNested = new long[dimension];
		double[] cost_param_a = new double[dimension];
		double[] cost_param_b = new double[dimension];


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
            cost_param_a[i] = a;
            cost_param_b[i] = b; 
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
      	long B = (long) Math.max(a, b);
      	// Greedy Algorithm
      	lbNested[dimension - 1] = B;
        ubNested[dimension - 1] = B;
          
        return new RAPNCInstanceData(
            objFuncType,
            dimension,
            ubVar,
            lbNested,
            ubNested,
            cost_param_a,
            cost_param_b
        );
    }

    /**
     * compare_DCA_MDA method 
     * This is a method that compares the efficiency of DCA and MDA
     * @param RAPNCInstanceData RAPNCInstanceData
     */
    public static String compare_DCA_MDA(RAPNCInstanceData data) {
        /********************************************************************************
        **
        ** DCA: solve the problem by DCA and record the time
        **
        ********************************************************************************/
        RAPNC test_instance = data.toRAPNC();
        long startTime = System.currentTimeMillis();
        ResultTypeRAPNC res = test_instance.solveIntegerDCA();

        long endTime = System.currentTimeMillis();
        long timeDCA = endTime - startTime;
        
        // Number of subproblems solved by DCA
		long num_subprob_DCA = test_instance.number_subproblem;
        
        /********************************************************************************
        **
        **MDA: solve the problem by FastMDA and record the time
        **
        ********************************************************************************/
        RAPNC test_instance_MDA = data.toRAPNC();
        startTime = System.currentTimeMillis();
        ResultTypeMDA resFastMDA = test_instance_MDA.FastMDA();
		endTime = System.currentTimeMillis();
        long timeFastMDA = endTime - startTime;

        // Number of subproblems solved by MDA
        long num_subpro_MDA = test_instance_MDA.number_subproblem;
        
        // Sainity check
        for (int i = 0; i < test_instance.dimension; i++) {
			if (Math.abs(res.sol[i] - resFastMDA.aa[i]) >= 0.01) {
				System.out.println("The solution x[" + i + "] is different.");
				System.out.println(res.sol[i] + " " + " " + resFastMDA.aa[i]);
			}
			
		}

        // Return test 
        return (
            String.format("%10s", test_instance.dimension) 
            + String.format("%10s", ((double) timeDCA) / 1000) 
            + String.format("%10s", ((double) timeFastMDA) / 1000) 
            + String.format("%20s", (num_subprob_DCA)) 
            + String.format("%20s", (num_subpro_MDA)) 
        );
    }
}