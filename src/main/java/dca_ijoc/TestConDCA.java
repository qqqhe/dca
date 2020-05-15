/*
 *  Compilation:  javac -cp ./ TestFlow1Con.java
 *  Execution:    java -cp ./ TestFlow1Con
 *  
 *  A test class to evaluate the numerical performance of DCA, MDA
 *  @author Zeyang Wu @ University of Minnesota
 *
*/

package dca_ijoc;
import java.util.*;

import java.io.*;
import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

public class TestConDCA {
    final static long ExperimentInPaperSeed_F = 9999; 
    final static long ExperimentInPaperSeed_FUEL = 8888; 
    final static long ExperimentInPaperSeed_CRASH = 7777; 
    /**
     * genTestCollectionToFiles method This is a method that generates an instance
     * of RAPNC.
     * 
     * @param objFuncTypes types of object function, we support ["linear",
     *                    "quadratic", "f", "fuel", "crash"]
     * @param varBounds    the upperbounds of capacity
     * @param sizes        sizes of the instances
     * @param Random generator so that we can control the random instance
     * @return a collection of instance of RAPNC data
     */
    public static void genTestCollectionToFiles(String[] objFuncTypes, int[] varBounds, int[] sizes, int rep, String folderpath, Random generator) {
        for (String objFuncType : objFuncTypes) {
            for (int varBound : varBounds) {
                Map<Integer, ArrayList<String>> testCollectionMap = new HashMap<>();
                for (int size : sizes) {
                    // write files
                    ArrayList<String> filelocations = new ArrayList<>();
                    String filepath = folderpath
                            + String.format("/Instance%s/VB=%d/n=%d/", objFuncType, varBound, size);
                    new File(filepath).mkdirs();
                    for (int i = 0; i < rep; i++) {
                        RAPNCTestUtils.RAPNCInstanceData data = RAPNCTestUtils.generateInstanceData(objFuncType, size, varBound, generator);
                        String filename = genDataFileName(objFuncType, varBound, size, i);

                        // Write data to file
                        data.JSONizeToFile(filepath + filename);
                        filelocations.add(filepath + filename);
                    }
                    testCollectionMap.put(Integer.valueOf(size), filelocations);
                }

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try {
                    // create a writer
                    Writer writer = new FileWriter(
                            folderpath 
                            + String.format(
                                "/%s",
                                genDataCollectionFileName(objFuncType, varBound)
                            )
                        );

                    // convert map to JSON File
                    gson.toJson(testCollectionMap, writer);

                    // close the writer
                    writer.close();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void genTestCollectionToFiles(String[] objFuncTypes, int[] varBounds, int[] sizes, int rep, String folderpath) {
        Random generator = new Random();
        genTestCollectionToFiles(objFuncTypes, varBounds, sizes, rep, folderpath, generator);
    }

    /**
     * readFilesAndEvaluate method 
     * A method that reads all instance json files and evaluate their performance.
     * 
     * @param objFuncType types of object function, we support ["linear",
     *                    "quadratic", "f", "fuel", "crash"]
     * @param size        sizes of the instances
     * @param varBound    the upperbounds of capacity
     * @param time_stamp a unique timestamp that indicates the folder of test instances
     * @return void, will output the test data in test_logs
     */
    public static void readFilesAndEvaluate(String objFuncType, int varBound, int[] sizes, String time_stamp) {
        Gson gson = new Gson();
        String fileCollectionJSON = String.format("./test_instances/%s/", time_stamp) 
            + genDataCollectionFileName(objFuncType, varBound);
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileCollectionJSON));
            Type empMapType = new TypeToken<Map<Integer, ArrayList<String>>>() {
            }.getType();
            Map<Integer, ArrayList<String>> jsonfiles = gson.fromJson(
                bufferedReader, 
                empMapType
            );

            (new File("test_logs/")).mkdirs();
            PrintStream o = new PrintStream(
                new File(
                    "test_logs/DCA---MDA numerical experiment_" 
                    + String.format("Time=%s", time_stamp)
                    + String.format("_ObjType=%s", objFuncType)
                    + String.format("_varBound=%d.txt", varBound)
                )
            );

            System.setOut(o);

            System.out.println("Test: Variable bound " + varBound + ":General Convex Function");
		    System.out.println("Dimension        DCA       FastMDA	      number_subproblem_DCA      number_subproblem_MDA");
            
            for (int size : sizes) {
                ArrayList<String> instance_json_files = jsonfiles.get(Integer.valueOf(size));
                for (String instance_json_file : instance_json_files) {
                    
                    BufferedReader br = new BufferedReader(new FileReader(instance_json_file));
                    RAPNCTestUtils.RAPNCInstanceData instance = gson.fromJson(
                        br, 
                        RAPNCTestUtils.RAPNCInstanceData.class
                    );

                    System.out.println(RAPNCTestUtils.compare_DCA_MDA(instance));
                }

                System.out.println();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * evaluateDCAMDAInMemory method 
     * Evalute the performance of MDA and DCA in Memory without storing test instances. 
     * 
     * @param objFuncType type of object function, we support ["linear",
     *                    "quadratic", "f", "fuel", "crash"]
     * @param sizes        sizes of the instances
     * @param varBound    the upperbounds of capacity
     * @param generator   rand generator so that we can control the random instance
     * @return void, will output the test data in test_logs
     */
    public static void evaluateDCAMDAInMemory(String objFuncType, int varBound, int[] sizes, int rep, Random generator) {
        try {
            PrintStream o = new PrintStream(
                new File(
                    "test_logs/DCA---MDA numerical experiment_" 
                    + String.format("Time=%d", System.currentTimeMillis())
                    + String.format("_ObjType=%s", objFuncType)
                    + String.format("_varBound=%d.txt", varBound)
                )
            );

            System.setOut(o);

            System.out.println("Test: Variable bound " + varBound + ":General Convex Function");
            System.out.println("Dimension        DCA       FastMDA	      number_subproblem_DCA      number_subproblem_MDA");
            for (int size : sizes) {
                for (int i = 0; i < rep; i++) {
                    RAPNCTestUtils.RAPNCInstanceData instance = RAPNCTestUtils.generateInstanceData(objFuncType, size, varBound, generator);
                    System.out.println(RAPNCTestUtils.compare_DCA_MDA(instance));
                }
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * evaluateDCAMDAInMemory method  
     * Evalute the performance of MDA and DCA in Memory without storing test instances. 
     * 
     * @param objFuncType types of object function, we support ["linear",
     *                    "quadratic", "f", "fuel", "crash"]
     * @param size        sizes of the instances
     * @param varBound    the upperbounds of capacity
     * @return void, will output the test data in test_logs
     */
    public static void evaluateDCAMDAInMemory(String objFuncType, int varBound, int[] sizes, int rep) {
        Random generator = new Random();
        evaluateDCAMDAInMemory(objFuncType, varBound, sizes, rep, generator);
    }

    /**
     * evaluateCollectionsDCAMDAInMemory method 
     * Evalute the performance of MDA and DCA in Memory without storing test instances for multiple inputs. 
     * @param objFuncTypes types of object function, we support ["linear",
     *                    "quadratic", "f", "fuel", "crash"]
     * @param sizes        sizes of the instances
     * @param varBounds    the upperbounds of capacity
     * @param generator   rand generator so that we can control the random instance
     * @return void, will output the test data in test_logs
     */
    public static void evaluateCollectionsDCAMDAInMemory(String[] objFuncTypes, int[] varBounds, int[] sizes, int rep, Random generator) {
        for (String objFuncType : objFuncTypes) {
            for (int varBound : varBounds) {
                evaluateDCAMDAInMemory(objFuncType, varBound, sizes, rep, generator);
            }
        }
    }

     /**
     * evaluateCollectionsDCAMDAInMemory method 
     * Evalute the performance of MDA and DCA in Memory without storing test instances for multiple inputs. 
     * @param objFuncTypes types of object function, we support ["linear",
     *                    "quadratic", "f", "fuel", "crash"]
     * @param varBounds    the upperbounds of capacity
     * @param sizes        sizes of the instances
     * @return void, will output the test data in test_logs
     */
    public static void evaluateCollectionsDCAMDAInMemory(String[] objFuncTypes, int[] varBounds, int[] sizes, int rep) {
        Random generator = new Random();
        evaluateCollectionsDCAMDAInMemory(objFuncTypes, varBounds, sizes, rep, generator);
    }

    /**
     * ExperimentInPaper method 
     * Evalute the performance of MDA and DCA in Memory without storing test instances. 
     * This method can reproduce the numerical experiment in the paper:
     *  "A new combinatorial algorithm for separable convex resource allocation with nested bound constraints", 
     *  Zeyang Wu, Kameng Nip, Qie He, To appear in INFORMS Journal on Computing, University of Minnesota, 2019.
     * @param objFuncTypes types of object function, we support ["linear",
     *                    "quadratic", "f", "fuel", "crash"]
     * @param sizes        sizes of the instances
     * @param varBounds    the upperbounds of capacity
     * @return void, will output the test data in test_logs
     */
    public static void ExperimentInPaper() {
        int[] sizes = new int[]{800, 1600, 3200, 6400, 12800, 25600, 51200, 51200<<1, 51200<<2, 51200<<3, 51200<<4, 51200<<5, 51200<<6, 51200<<7};
        // String[] objFuncTypes = new String[]{"f", "fuel", "crash"};
        sizes = new int[]{800, 1600, 3200};
        int[] varBounds = new int[]{100};
        int rep = 10;

        evaluateCollectionsDCAMDAInMemory(new String[]{"f",}, varBounds, sizes, rep, new Random(ExperimentInPaperSeed_F));
        evaluateCollectionsDCAMDAInMemory(new String[]{"fuel",}, varBounds, sizes, rep, new Random(ExperimentInPaperSeed_FUEL));
        evaluateCollectionsDCAMDAInMemory(new String[]{"crash",}, varBounds, sizes, rep, new Random(ExperimentInPaperSeed_CRASH));

        return;
    }

    public static void main2(String[] args) {
        ExperimentInPaper();
    }

    public static void main(String[] args) {
        /*
         * Please be aware of the json file size. For an instance with 1M variables, the data file in json format is roughly 100Mb. 
         */
        int[] sizes = new int[]{800, 1600, 3200, 6400, 12800, 25600, 51200, 51200<<1, 51200<<2};
        String[] objFuncTypes = new String[]{"f", "fuel", "crash"};
        int[] varBounds = new int[]{100};
        int rep =10;
        String folderpath = String.format("./test_instances/%s/", "1589214682686");

        /*
         * Use gen_instance to generate new test instance files
         * If you are interested in testing new instances, please change the random seed and the folderpath
         */ 
        // String func = "gen_instance";
        /*
         * Use execute_test to execute the test from the saved data files
         * If you are interested in testing new instances, please change the folderpath
        */ 
        String func = "execute_test";
        String datafile_timestamp = "1589214682686";

        try {
            if (func == "gen_instance") {
                genTestCollectionToFiles(new String[]{"f",}, varBounds, sizes, rep, folderpath,new Random(ExperimentInPaperSeed_F));
                genTestCollectionToFiles(new String[]{"fuel",}, varBounds, sizes, rep, folderpath, new Random(ExperimentInPaperSeed_FUEL));
                genTestCollectionToFiles(new String[]{"crash",}, varBounds, sizes, rep, folderpath, new Random(ExperimentInPaperSeed_CRASH));
            } else if (func == "execute_test") {
                for (int varBound : varBounds) {
                    for (String objFuncType : objFuncTypes) {
                        readFilesAndEvaluate(objFuncType, varBound, sizes, datafile_timestamp);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Please modify the following inputs in the main function: func and datafile_timestamp");
        }
    }

    public static String genDataCollectionFileName(String objFuncType, int varBound) {
        return String.format("Instance_%s_VB=%d.json", objFuncType, varBound);
    }

    public static String genDataFileName(String objFuncType, int varBound, int size, int index) {
        return String.format("%s_VB=%d_n=%d_%d.json", objFuncType, varBound, size, index);
    }
}