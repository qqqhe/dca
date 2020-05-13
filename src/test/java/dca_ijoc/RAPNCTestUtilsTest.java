package dca_ijoc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
/**
 * Unit test for simple App.
 */
class RAPNCTestUtilsTest {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    @Test
    void testBuildObjectFunction() {
        double a = 0.5;
        double b = 0.6;
        double[] test_values = new double[]{0.5, 1, 20, 100, -10};

        Function linearfun =  RAPNCTestUtils.buildObjectFunction(a, b, "linear");
        Function quafun =  RAPNCTestUtils.buildObjectFunction(a, b, "quadratic");
        Function ffun =  RAPNCTestUtils.buildObjectFunction(a, b, "f");
        Function fuelfun =  RAPNCTestUtils.buildObjectFunction(a, b, "fuel");
        Function crashfun =  RAPNCTestUtils.buildObjectFunction(a, b, "crash");

        for (int i = 0; i < test_values.length; i++) {
            double x = test_values[i];
            assert(b * x == linearfun.getValue(x));
            assert(a * x * x + b * x == quafun.getValue(x));
            assert(x * x * x * x / 4 + b * x == ffun.getValue(x));
            assert(a * b * b / x / x / x == fuelfun.getValue(x));
            assert(10 * b + a / x == crashfun.getValue(x));
        }     
    }

    @Test
    void testGenerateInstanceData() {
        int[] test_sizes = new int[]{10, 20, 100, 200, 800, 1600};
        String[] ObjFuncTypes = new String[]{"linear", "quadratic", "f", "fuel", "crash"};
        for (String ObjFuncType : ObjFuncTypes) {
            for (int i = 0; i < test_sizes.length; i++) {
                RAPNCTestUtils.RAPNCInstanceData data1 = RAPNCTestUtils.generateInstanceData(ObjFuncType, test_sizes[i], 100);
                RAPNCTestUtils.RAPNCInstanceData data2 = RAPNCTestUtils.generateInstanceData(ObjFuncType, test_sizes[i], 100, null);
                RAPNCTestUtils.RAPNCInstanceData data3 = RAPNCTestUtils.generateInstanceData(ObjFuncType, test_sizes[i], 100, new Random());
                solveAndCheck(data1);
                solveAndCheck(data2);
                solveAndCheck(data3);
                
                System.out.println("Test generateInstanceData() for object: " + ObjFuncType);   
            }
        }
        solveAndCheck(RAPNCTestUtils.generateInstanceData("fuel", 50, 100, null));

        System.out.println(ANSI_GREEN+ "Test generateInstanceData() passed!" + ANSI_RESET);

        
    }

    @Test
    void testJSONize() {
        RAPNCTestUtils.RAPNCInstanceData data1 = RAPNCTestUtils.generateInstanceData("crash", 5, 100);

        RAPNCTestUtils.RAPNCInstanceData obj1 = RAPNCTestUtils.RAPNCInstanceData.readJsonData(
            data1.JSONize()
        );
        assertEquals(obj1.objFuncType, data1.objFuncType);
        assertEquals(obj1.dimension, data1.dimension);
        assertArrayEquals(obj1.capacity, data1.capacity);
        assertArrayEquals(obj1.lbNested, data1.lbNested);
        assertArrayEquals(obj1.ubNested, data1.ubNested);
        assertArrayEquals(obj1.cost_param_a, data1.cost_param_a);
        assertArrayEquals(obj1.cost_param_b, data1.cost_param_b);
        
        RAPNCTestUtils.RAPNCInstanceData data2 = new RAPNCTestUtils.RAPNCInstanceData(
            "fuel",
            5,
            new long[]{56,110,83,77,97},
            new long[]{26, 79, 133, 206, 276},
            new long[]{49, 110, 169, 235, 276},
            new double[]{0.4, 9.6, 1.33, 2.06, 27.6},
            new double[]{-0.62, 2.61,  -0.93, 2.06, -0.276}
        );

        RAPNCTestUtils.RAPNCInstanceData obj2 = RAPNCTestUtils.RAPNCInstanceData.readJsonData(
            data2.JSONize()
        );

        assertEquals(obj2.objFuncType, "fuel");
        assertEquals(obj2.dimension, 5);
        assertArrayEquals(obj2.capacity, new long[]{56,110,83,77,97});
        assertArrayEquals(obj2.lbNested, new long[]{26, 79, 133, 206, 276});
        assertArrayEquals(obj2.ubNested, new long[]{49, 110, 169, 235, 276});
        assertArrayEquals(obj2.cost_param_a, new double[]{0.4, 9.6, 1.33, 2.06, 27.6});
        assertArrayEquals(obj2.cost_param_b, new double[]{-0.62, 2.61, -0.93, 2.06, -0.276});
        
        System.out.print(data2.JSONize());

        solveAndCheck(data1);
        solveAndCheck(data2);
    }

    private static void solveAndCheck(RAPNCTestUtils.RAPNCInstanceData data) {        
        RAPNC test_instance = data.toRAPNC();
        ResultTypeRAPNC res = test_instance.solveIntegerDCA();
        RAPNC test_instance2 = data.toRAPNC();
        ResultTypeMDA resFastMDA = test_instance2.FastMDA();
        assert(res.feasible);
        for (int j = 0; j < data.dimension; j++) {
            assert(Math.abs(res.sol[j] - resFastMDA.aa[j]) <= 0.01);           
        }
    }

    @Test 
    void testCompare_DCA_MDA() {
        int[] test_sizes = new int[]{10, 20, 100, 200, 800, 1600};
        String[] ObjFuncTypes = new String[]{"linear", "quadratic", "f", "fuel", "crash"};

        for (String ObjFuncType : ObjFuncTypes) {
            for (int test_size : test_sizes) {
                RAPNCTestUtils.RAPNCInstanceData data = RAPNCTestUtils.generateInstanceData(ObjFuncType, test_size, 100);
                System.out.println(RAPNCTestUtils.compare_DCA_MDA(data));
            }
        }
        
    }
}
