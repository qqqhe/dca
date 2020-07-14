package dca_ijoc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for SegmentTree.java.
 */

public class SegmentTreeTest {
    @Test
    void Test() {
        long[] test_array = new long[]{1, 3, 4, 2, 1, -2, 4, 8};
        SegmentTree st = new SegmentTree(test_array);
        NaiveArrayOperation naive_query = new NaiveArrayOperation(test_array);
        
        // Print the segment tree whether the tree is built correctly
        for (SegmentTree.Node i : st.heap) {
            if (i != null) { 
                System.out.println("range: " + i.from + " to " + i.to + " with min " + i.min + " at position " + i.minIndex);
                assertEquals(st.rMinQ(i.from, i.to).value, naive_query.rMinQ(i.from, i.to));
            }
        }
        
        // Test rangeAdd
        System.out.println("Test rangeAdd");
        
        st.rangeAdd(0, 3, 2);
        naive_query.rangeAdd(0, 3, 2);
        for (SegmentTree.Node i : st.heap) {
            if (i != null) { 
                System.out.println("range: " + i.from + " to " + i.to + " with min " + i.min + " at position " + i.minIndex);
                assertEquals(st.rMinQ(i.from, i.to).value, naive_query.rMinQ(i.from, i.to));
            }
        }

        st.rangeAdd(1, 3, -12);
        naive_query.rangeAdd(1, 3, -12);
        for (SegmentTree.Node i : st.heap) {
            if (i != null) { 
                System.out.println("range: " + i.from + " to " + i.to + " with min " + i.min + " at position " + i.minIndex);
                assertEquals(st.rMinQ(i.from, i.to).value, naive_query.rMinQ(i.from, i.to));
            }
        }
        
    } 

    /*
     * This is a class that implements the naive rangeMinQuery and rangeAdd methods
     */
    public class NaiveArrayOperation {
        long[] array;
        public NaiveArrayOperation(long[] array) {
            this.array = array;
        }

        public long rMinQ(int from, int to) {
            long res = Integer.MAX_VALUE;
            if (from > to || from > array.length ) {
                return res;
            }
            
            int end = Math.min(to, array.length);
            
            for (int i = from; i <= end; i++) {
                res = Math.min(res, array[i]);
            }            
            
            return res;
        }

        public void rangeAdd(int from, int to, long value) {
            if (from > to || from > array.length ) {
                return;
            }

            int end = Math.min(to, array.length);

            for (int i = from; i <= end; i++) {
                array[i] += value;
            }            
        }
    }
}