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
    /**
     * genTestCollectionToFiles method This is a method that generates an instance
     * of RAPNC. Time-Complexity: O(n)
     * 
     * @param objFuncType types of object function, we support ["linear",
     *                    "quadratic", "f", "fuel", "crash"]
     * @param size        sizes of the instances
     * @param varBound    the upperbounds of capacity
     * @return a collection of instance of RAPNC data
     */
    public static void genTestCollectionToFiles(String[] objFuncTypes, int[] sizes, int[] varBounds, int rep,
            String folderpath) {
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
                        RAPNCTestUtils.RAPNCInstanceData data = RAPNCTestUtils.generateInstanceData(objFuncType, size,
                                varBound);
                        String filename = String.format("%s_VB=%d_n=%d_%d", objFuncType, varBound, size, i) + ".json";

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

    public static void readFilesAndEvaluate(String objFuncType, int varBound, int[] sizes) {

        Gson gson = new Gson();
        String fileCollectionJSON = "./test_instances/1589167641338/" + genDataCollectionFileName(objFuncType, varBound);
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
                    + String.format("Time=%d", System.currentTimeMillis())
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
    public static void main(String[] args) {
        int[] sizes = new int[]{10, 20, 100, 200, 800, 1600};
        String[] objFuncTypes = new String[]{"f", "fuel", "crash"};
        int[] varBounds = new int[]{10, 100};
        int rep =10;
        String folderpath = String.format("./test_instances/%d/", System.currentTimeMillis());

        // genTestCollectionToFiles(objFuncTypes, sizes, varBounds, rep, folderpath);

        // Read the files and evaluate the performance of DCA and MDA
        readFilesAndEvaluate("f", 10, sizes);

        /*
        for (int varBound : varBounds) {
            for (String objFuncType : objFuncTypes) {
                readFilesAndEvaluate(objFuncType, varBound, sizes);
            }
        }
        */
    }

    public static String genDataCollectionFileName(String objFuncType, int varBound) {
        return String.format("Instance_%s_VB=%d.json", objFuncType, varBound);
    }
}