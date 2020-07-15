/******************************************************************************
*  Compilation:  javac RAPNCFlowFormulation.java
*  Execution:    java RAPNCFlowFormulation
*  
*  This a class for RAPNC with convex production cost       
*  This class implements Scaled Flow-improving Algorithm  
*  The worst case time complexity is O(nlognlogB)
*  @para n the number of variables
*  @para B the total number of resources
*  @author Zeyang Wu
*****************************************************************************/
package dca_ijoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;


public class RAPNCFlowFormulation {
    // Number of periods: n
    int numPeriod;

    // Demands: an n-dimensional array for period 0, 1, ..., n - 1
    long[] demands;
    
    // Production cost: a list of function oracles for period 0, 1, ..., n - 1
    List<Function> obj;

    // No holding cost

    // Production capacity
    long[] prodCap;

    // Inventory capacity: 0 -> 1 -> ... -> n - 1, the length is n - 1
    long[] inventCap;

    public RAPNCFlowFormulation() {
        this.numPeriod = 0; 
        this.demands = null;
        this.obj = null;
        this.prodCap = null;
        this.inventCap = null;
    }

    // An internal class that stores the id and cost/capacity of a path from source node (id) to the current destination node
    private class PathData{
        int id;
        double value;

        public PathData(int id, double value) {
            this.id = id;
            this.value = value;
        }
    }

    private class PathDataComparator implements Comparator<PathData> {
        // Compare the cost
        public int compare(PathData a, PathData b) {    		
            if (a.id == b.id) {
                return 0;
            }    
            /* Break ties by maximum index rule */		
            if (a.value > b.value || (a.value == b.value && a.id < b.id)) {
    			return -1;
    		}
            return 1;
        }
    }

    /**
     * Successive Shortest Path Algorithm with step size s.
     * The is a special implementation for solving the newwork flow formulation of RAPNC with convex cost.
     * Balanced Binary Search tree is used to store the possible paths and supports O(log n) add/remove/min operations. 
     * Segment tree is used to maintain the capacities and supports O(log n) RangeMinQuery and RangeAdd operations.
     * <p>
     * Time-Complexity: O(n + m log(n)) where m is the total number of increments made during this process 
     *
     * @param s  step size
     * @param initFlow a initial pseudo flow, can be 0 or any component-wise lower of the optimal solution  
     */

    public void ssp(long s, long[] initFlow) {

        // Make copies of the parameters: O(n)
        double[] cost = new double[numPeriod];
        long[] u0 = Arrays.copyOfRange(prodCap, 0, numPeriod);
        long[] u = Arrays.copyOfRange(inventCap, 0, numPeriod - 1);
        long[] de = Arrays.copyOfRange(demands, 0, numPeriod);
        
        // Preprocess the initFlow to update the capacities: O(n)
        long  sum = 0; 
        for (int i = 0; i < numPeriod; i++) {
            cost[i] = obj.get(i).getValue(initFlow[i] + 1) - obj.get(i).getValue(initFlow[i]);
            
            u0[i] -= initFlow[i];
            if (sum + initFlow[i] <= de[i]) {				
                de[i] -= (initFlow[i] + sum);
                sum = 0;
            } else {
                sum += (initFlow[i] - de[i]);
                de[i] = 0;				
            }
  
            if (i < numPeriod - 1 && sum > 0) {
                u[i] -= sum;
            }
        }
        
  
        // Constuct two trees
        // A forward tree store the cost of all possible paths
        List<PathData> hashCost = new ArrayList<>();
  
        for (int i = 0; i < numPeriod; i++) {
            hashCost.add(new PathData(i, cost[i]));
        }
  
        // Current iteration index (current desination node)
        int curt = 0;
        // First path index with non-zero capacity
        int firstPathIndex = 0;

        TreeSet<PathData> forwardTree = new TreeSet<>(new PathDataComparator());
        forwardTree.add(hashCost.get(0));
  
        // Segment tree data structure used to store the capacities of the horizontal arcs
        SegmentTree horizonCap = new SegmentTree(u);
        
        // Main iteration
        while(curt < numPeriod) {
            // Satisfies the demand at node 0, 1, 2, ... n-1 in order
            while (de[curt] > 0) {		
  
                // For debugging. If the RAPNC instance is feasible, this will not pop up
                if (forwardTree.isEmpty()) {					
                    System.out.println(forwardTree.isEmpty());
                    System.out.println(Arrays.toString(initFlow));
                    System.out.println("Remaining Demand ");
                    System.out.println(Arrays.toString(de));
                    System.out.println(curt);
                    break;
                }
                
                // Find the minimum unit production cost path with the forward tree 
                int minIndex = forwardTree.pollLast().id;
  
                // Range minimum query to find the bottleneck in the capacities of the horizontal arcs
                long bottleneck = Integer.MAX_VALUE;
                int bottleneckIndex = -1;	
                
                if (minIndex < curt) {
                    bottleneck = horizonCap.rMinQ(minIndex, curt - 1).value;
                    bottleneckIndex = horizonCap.rMinQ(minIndex, curt - 1).id;
                }
                
                // Compute the step size
                long delta = Math.min(u0[minIndex], s);
                delta = Math.min(delta, bottleneck);
                delta = Math.min(delta, de[curt]);
  
                // Update the production capacities
                de[curt] -= delta;
                u0[minIndex] -= delta;
                initFlow[minIndex] += delta;
                
                // Update the cost		
                hashCost.get(minIndex).value = obj.get(minIndex).getValue(initFlow[minIndex] + 1) - obj.get(minIndex).getValue(initFlow[minIndex]);			
                forwardTree.add(hashCost.get(minIndex));
                
                // Range update the capacities of the horizontal arcs
                if (minIndex < curt) {
                    horizonCap.rangeAdd(minIndex, curt - 1, -delta);
                }				
                
                // Remove paths if its prodcution capacity is zero
                if (u0[minIndex] == 0) {
                    // System.out.print("Remove arc by prodcap ");
                    // System.out.print(minIndex);
                    forwardTree.remove(hashCost.get(minIndex));
                }
                
                // Update the firstPathIndex
                if (delta == bottleneck) {
                    for (int i = firstPathIndex; i <= bottleneckIndex; i++) {
                        forwardTree.remove(hashCost.get(i));
                    }
                    firstPathIndex = bottleneckIndex + 1;
                }
            }
  
            curt++;
            if (curt == numPeriod) {
                break;
            }
            
            // Add new path to the forward tree and move to another period
            forwardTree.add(hashCost.get(curt));
        }
    }

    /**
     * Solve operations.
     * With this operation you can solve the lot sizing problem with convex production cost
     * The is a scaled version of Hochbaum 2008 Solving Linear Cost Dynamic Lot-Sizing Problems in O(n log n) Time
     * <p>
     * Time-Complexity: O(n log(n) log(B)) where B is the total demand
     *
     * @param no param
     */
    public long[] solve() {
        long[] flow = new long[numPeriod];

        // ssp(1, prodFlow);
        
        long B = 0;
        for (int i = 0; i < numPeriod; i++) {
            B += demands[i];
        }
        // Initial step size
        long s = (long) Math.ceil(((double) B) / numPeriod / 2);
        
        while (s > 1) {
            // Successive Shortest Path Algorithm with step size s
            ssp(s, flow);			
            
            // Step back by s 
            for (int i = 0; i < numPeriod; i++) {			
                flow[i] = Math.max(flow[i] - s, 0);
            }

            if (s % 2 == 0) {
                s = s / 2;
            } else {
                s = s / 2 + 1;
            }
        }
        
        // Final Successive Shortest Path Algorithm with step size 1
        ssp(1, flow);
        
        return flow;
    }
}