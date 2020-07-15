/*
 *  Compilation:  javac -cp ./ TestConSFA.java
 *  Execution:    java -cp ./ TestConSFA
 *  
 *  A test class to evaluate the numerical performance of DCA, MDA, and SFA
 *  @author Zeyang Wu @ University of Minnesota
 *
*/

package dca_ijoc;

import java.util.*;
import java.io.*;

public class TestConSFA {
    // Ramdom seed for the SFA paper
    final static long ExperimentInPaperSeed_F = 6666; 
    final static long ExperimentInPaperSeed_FUEL = 5555; 
    final static long ExperimentInPaperSeed_CRASH = 4444; 

    /**
     * evaluateDCAMDASFAInMemory method 
     * Evalute the performance of DCA, MDA, and SFA in Memory without storing test instances. 
     * 
     * @param objFuncType type of object function, we support ["linear",
     *                    "quadratic", "f", "fuel", "crash"]
     * @param sizes        sizes of the instances
     * @param varBound    the upperbounds of capacity
     * @param generator   rand generator so that we can control the random instance
     * @return void, will output the test data in test_logs
     */
    public static void evaluateDCAMDASFAInMemory(String objFuncType, int varBound, int[] sizes, int rep, Random generator) {
        try {
            PrintStream o = new PrintStream(
                new File(
                    "test_logs/DCA---MDA---SFA numerical experiment_" 
                    + String.format("Time=%d", System.currentTimeMillis())
                    + String.format("_ObjType=%s", objFuncType)
                    + String.format("_varBound=%d.txt", varBound)
                )
            );

            System.setOut(o);

            System.out.println("Test: Variable bound " + varBound + ":General Convex Function");
            System.out.println("Dimension        DCA       FastMDA	      SFA");
            for (int size : sizes) {
                for (int i = 0; i < rep; i++) {
                    RAPNCTestUtils.RAPNCInstanceData instance = RAPNCTestUtils.generateInstanceData(objFuncType, size, varBound, generator);
                    System.out.println(RAPNCTestUtils.compare_DCA_MDA_SFA(instance));
                }
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * evaluateDCAMDASFAInMemory method  
     * Evalute the performance of DCA, MDA, and SFA in Memory without storing test instances. 
     * 
     * @param objFuncType types of object function, we support ["linear",
     *                    "quadratic", "f", "fuel", "crash"]
     * @param size        sizes of the instances
     * @param varBound    the upperbounds of capacity
     * @return void, will output the test data in test_logs
     */
    public static void evaluateDCAMDASFAInMemory(String objFuncType, int varBound, int[] sizes, int rep) {
        Random generator = new Random();
        evaluateDCAMDASFAInMemory(objFuncType, varBound, sizes, rep, generator);
    }

    /**
     * evaluateCollectionsDCAMDASFAInMemory method 
     * Evalute the performance of DCA, MDA, SFA in Memory without storing test instances for multiple inputs. 
     * @param objFuncTypes types of object function, we support ["linear",
     *                    "quadratic", "f", "fuel", "crash"]
     * @param sizes        sizes of the instances
     * @param varBounds    the upperbounds of capacity
     * @param generator   rand generator so that we can control the random instance
     * @return void, will output the test data in test_logs
     */
    public static void evaluateCollectionsDCAMDASFAInMemory(String[] objFuncTypes, int[] varBounds, int[] sizes, int rep, Random generator) {
        for (String objFuncType : objFuncTypes) {
            for (int varBound : varBounds) {
                evaluateDCAMDASFAInMemory(objFuncType, varBound, sizes, rep, generator);
            }
        }
    }

     /**
     * evaluateCollectionsDCAMDASFAInMemory method 
     * Evalute the performance of DCA, MDA, and SFA in Memory without storing test instances for multiple inputs. 
     * @param objFuncTypes types of object function, we support ["linear",
     *                    "quadratic", "f", "fuel", "crash"]
     * @param varBounds    the upperbounds of capacity
     * @param sizes        sizes of the instances
     * @return void, will output the test data in test_logs
     */
    public static void evaluateCollectionsDCAMDASFAInMemory(String[] objFuncTypes, int[] varBounds, int[] sizes, int rep) {
        Random generator = new Random();
        evaluateCollectionsDCAMDASFAInMemory(objFuncTypes, varBounds, sizes, rep, generator);
    }

    /**
     * ExperimentInPaper method 
     * Evalute the performance of DCA, MDA, and SFA in Memory without storing test instances. 
     * This method can reproduce the numerical experiment in the paper:
     *  "Title TBD"
     * @param objFuncTypes types of object function, we support ["linear",
     *                    "quadratic", "f", "fuel", "crash"]
     * @param sizes        sizes of the instances
     * @param varBounds    the upperbounds of capacity
     * @return void, will output the test data in test_logs
     */
    public static void ExperimentInPaper() {
        int[] sizes = new int[]{800, 1600, 3200, 6400, 12800, 25600, 51200, 51200<<1, 51200<<2, 51200<<3, 51200<<4, 51200<<5, 51200<<6, 51200<<7};
        /* The small-sized instances are for testing purpose */ 
        sizes = new int[]{51200, 51200<<1, 51200<<2, 51200<<3};
        int[] varBounds = new int[]{100};
        int rep = 10;

        evaluateCollectionsDCAMDASFAInMemory(new String[]{"f",}, varBounds, sizes, rep, new Random(ExperimentInPaperSeed_F));
        evaluateCollectionsDCAMDASFAInMemory(new String[]{"fuel",}, varBounds, sizes, rep, new Random(ExperimentInPaperSeed_FUEL));
        evaluateCollectionsDCAMDASFAInMemory(new String[]{"crash",}, varBounds, sizes, rep, new Random(ExperimentInPaperSeed_CRASH));

        return;
    }

    public static void main(String[] args) {
        ExperimentInPaper();
    }

}