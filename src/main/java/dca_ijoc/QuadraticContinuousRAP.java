/******************************************************************************
 *  Compilation:  javac QuadraticContinuousRAP.java
 *  Execution:    java QuadraticContinuousRAP
 *  
 *  A class for simple resource allocation problem(RAP) written by Zeyang Wu @ University of Minnesota
 *
 ******************************************************************************/

/******************************************************************************
* This is a class for simple resource allocation with separable convex quadratic objectives
*      
* Method: solve_QuaRAP_bisection()
* Bisection is used to find the optimal dual variable. 
* 
* Method: solve_QuaRAP_bucker84()
* An O(n) algorithm introduced in Brucker, Peter. "An O (n) algorithm for quadratic knapsack problems." Operations Research Letters 3.3 (1984): 163-166.
*
* @author Zeyang Wu
*/

package dca_ijoc;

import java.util.*;

public class QuadraticContinuousRAP {
    // List of function oracles: we assume the function is convex quadratic functions: a_i/2 x_i^2 + b_i x_i
    // We assume that lbVat are normalized to 0. 
	List<Function> obj;
	long B;
	long[] lbVar;
	long[] ubVar;
	int dimension;

	public QuadraticContinuousRAP(List<Function> obj, long B, long[] lbVar, long[] ubVar) {
		this.obj = obj;
		this.B = B;
		this.lbVar = lbVar;
		this.ubVar = ubVar;
		this.dimension = lbVar.length;	
    }

    //The DataType class represents a resource with its id and unit allocation cost
    private class DataType{
    	int id;
        double value;
        int type;
    	public DataType(int id, double value, int type) {
    		this.id = id;
            this.value = value;
            this.type = type;
    	}
    }

    private class DataTypeComparator implements Comparator<DataType> {
    	public int compare(DataType a, DataType b) {    		
    		if (a.id == b.id) {
    			return 0;
    		}    		
    		if (a.value > b.value) {
    			return 1;
    		}
    		return -1;
    	}
    }
    
    /**
     * solve_QuaRAP_bisection operations.
     * With this operation you can solve the simple resource allocation problem with quadratic objectives
     * It use bisection to find the optimal dual variable
     * It can be generalized to general separable convex objectives if the derivative is provided.
     * <p>
     * 
     * @param no param
     */
	ResultTypeContinuousRAP solve_QuaRAP_bisection() {
		double[] a = new double[dimension];
		double[] b = new double[dimension];

		double left = Double.MAX_VALUE/2;
        double right = -Double.MAX_VALUE/2;
        
        // Extract the coefficients
		for (int i = 0; i < dimension; i++) {
			a[i] = obj.get(i).getValue(1) + obj.get(i).getValue(-1);
			b[i] = obj.get(i).getValue(1) - obj.get(i).getValue(-1);
			b[i] /= 2;

			right = Math.max(right, - a[i] * lbVar[i] - b[i]);
			left = Math.min(left, - a[i] * ubVar[i] - b[i]);
		}
		
		double sum = 0;
		double accuracy = 1e-9;

		while (Math.abs(right - left) > accuracy || Math.abs(sum - B) > accuracy)  {
			double mid = left + (right - left) / 2;
			sum = 0;
			for (int i = 0; i < dimension; i++) {
				if (mid > - a[i] * lbVar[i] - b[i]) {
					sum += lbVar[i];
				} else if (mid < - a[i] * ubVar[i] - b[i]) {
					sum += ubVar[i];
				} else {
					sum += - (mid + b[i]) / a[i];
				}
			}

			if (sum >= B) {
				left = mid;
			} else {
				right = mid;
			}
			//System.out.print("Current mid: ");
			//System.out.println(mid);
			//System.out.print("Current sum: ");
			//System.out.println(sum);
		}

		if (Math.abs(right - left) > accuracy || Math.abs(sum - B) > accuracy) {
			System.out.println("The problem is infeasible");
			return new ResultTypeContinuousRAP(false, null);
		}
        
        System.out.println(left);

		double[] sol = new double[dimension];
		for (int i = 0; i < dimension; i++) {
			if (left > - a[i] * lbVar[i] -b[i]) {
				sol[i] = lbVar[i];
			} else if (left < - a[i] * ubVar[i] - b[i]) {
				sol[i] = ubVar[i];
			} else {
				sol[i] = - (left + b[i]) / a[i];
			}
		}

		return new ResultTypeContinuousRAP(true, sol);
    }
    
    /**
     * solve_QuaRAP_bucker84 operations.
     * With this operation you can solve the simple resource allocation problem with quadratic objectives
     * An O(n) algorithm introduced in Brucker, Peter. "An O (n) algorithm for quadratic knapsack problems." Operations Research Letters 3.3 (1984): 163-166.
     * <p>
     * Complexity: O(n)
     * 
     * @param no param
     */
    ResultTypeContinuousRAP solve_QuaRAP_bucker84() {
        ResultTypeContinuousRAP res = new ResultTypeContinuousRAP(true, new double[dimension]);
		double[] a = new double[dimension];
        double[] b = new double[dimension];
        double R = 0;

        DataType[] breakPoints = new DataType[2*dimension];        
        // 
        /*Extract the coefficients
        * And create the set of critical values. The set contains 2n numbers in the form of 
        * t^L_i = - a_i * l_i - b_i
        * t^U_i = - a_i * u_i - b_i
        */ 
        for (int i = 0; i < dimension; i++) {
			a[i] = obj.get(i).getValue(1) + obj.get(i).getValue(-1);
			b[i] = obj.get(i).getValue(1) - obj.get(i).getValue(-1);
            b[i] /= 2;
            
            // Critial values
            breakPoints[2*i] = new DataType(i, - b[i], 0);
            breakPoints[2*i + 1] = new DataType(i, - a[i] * ubVar[i] - b[i], 1);

            // R 
            R += ubVar[i];
        }
                      
        // Bisection to find dual break point;
	 	int start = 0;
        int end = 2 * dimension - 1;
         
        // This prefix sums are summed over [0...Start]
        double P = 0;
        double Q = 0;
        
	 	while(start + 1 < end) {
	 		int mid = start + (end - start) / 2;

            this.kthSmallest(breakPoints, start, end, mid - start + 1);
             
            // The prefix sums, temp
            double P_temp = P;
            double Q_temp = Q;
            double R_temp = R;

            // Updare prefix sums 
            for (int i = start; i < mid; i++) {
                int index = breakPoints[i].id;
                int type = breakPoints[i].type;
                
                if (type == 1) { 
                    R_temp -= ubVar[index];
                    Q_temp += 1 / a[index];
                    P_temp += b[index] / a[index];
                } else {
                    Q_temp -= 1 / a[index];
                    P_temp -= b[index] / a[index];
                }
            }
            
            // Compute the dual constraints
            double sum = (-breakPoints[mid].value) * Q_temp - P_temp + R_temp;
             
            // Bisection
	 		if (sum >= B) {
	 			start = mid;
	 		} else {
	 			end = mid;
	 		}

	 		// Reset prefix sums to start
	 		if (start == mid) {
                P = P_temp;
                Q = Q_temp;
                R = R_temp;
            }
        }
         
        //trivial cases (two break points. or just 1 variables)
	 	//In case that The bisection is not performed for (end - start + 1) = 2.
	 	//This is essential
	 	if (breakPoints[start].value > breakPoints[end].value) {
	 		swap(breakPoints, start, end);
        }
         
        // Output solution
        int index = breakPoints[start].id;
        int type = breakPoints[start].type;                
        if (type == 1) { 
            R -= ubVar[index];
            Q += 1 / a[index];
            P += b[index] / a[index];
        } else {
            Q -= 1 / a[index];
            P -= b[index] / a[index];
        }

        double dual_solution = (B + P - R) / Q;

        /* Debug
        System.out.println(start);
        System.out.println(breakPoints[start].value);
        System.out.println(breakPoints[end].value);
        System.out.println(dual_solution);
        */

	 	for (int i = 0; i < dimension; i++) {
            if (dual_solution >=  a[i] * ubVar[i] + b[i]) {
                res.sol[i] = ubVar[i];
            } else if (dual_solution >=  b[i]) {
                res.sol[i] = (dual_solution - b[i]) / a[i];
            } else {
                res.sol[i] = 0;
            }
	 	} 	

        return res;
    }

    /**
	* kthSmallest operation: median of the median algorithm
  	* Returns k'th smallest element in arr[l..r] in worst case linear time. 
  	* ASSUMPTION: ALL ELEMENTS IN ARR[] ARE DISTINCT
  	* <p>
  	* Time-Complexity: O(n) where n is the length of the array 
  	* @author adapt from the C++ code for geeksForgeeks
  	* @param array the array 
  	* @param start the start index
  	* @param end the end index
  	* @param k the k-th smallest element in a[s]...a[e]
  	*/
  	public DataType kthSmallest(DataType[] array, int start, int end, int k) {
        // If k is smaller than number of elements in array
        if (k <= 0 || k > end - start + 1) {
            return null;
        }

        //number of elements
        int n = end - start + 1;

        // Divide arr[] in groups of size 5, calculate median of every group and store it in median[] array.
        int i = 0;
        DataType[] median = new DataType[(n+4) / 5];

        for (i = 0; i < n / 5; i++) {
            int left = start + 5 * i;
            int right = left + 4;

            Arrays.sort(array, left, right + 1, new DataTypeComparator());
            int mid = left + (right - left) / 2;
            median[i] = array[mid];
        }

        //For the last few elements
        if (i * 5 < n) {
            Arrays.sort(array, start + 5*i, end + 1, new DataTypeComparator());
            median[i] = array[start + 5*i + n%5/2];
            i++;
        }

        // Find median of all medians using recursive call.
        // If median[] has only one element, then no need
        // of recursive call
        DataType medOfMed = (i == 1) ? median[i - 1] : kthSmallest(median, 0, i - 1, i / 2);

        int pos = partition(array, start, end, medOfMed);

        // If position is same as k
        if (pos - start == k-1) {
            return array[pos];
        }

        // If position is more, recur for left
        if (pos - start > k - 1) {
            return kthSmallest(array, start, pos - 1, k);
        }
  
        // Else recur for right subarray
        return kthSmallest(array, pos + 1, end, k - pos + start -1);
    }


    private static void swap(DataType[] arr, int i, int r) {
        DataType temp = arr[i];
        arr[i] = arr[r];
        arr[r] = temp;
    }


    // It searches for x in arr[l..r], and partitions the array 
    // around x.
    private static int partition(DataType[] arr, int l, int r, DataType x) {
        // Search for x in arr[l..r] and move it to end
        int i;
        for (i = l; i < r; i++) {
            if (arr[i].value == x.value) {
                break;
            }
        }

        swap(arr, i, r);

        // Standard partition algorithm
        i = l;
        for (int j = l; j <= r - 1; j++) {
            if (arr[j].value <= x.value) {
                swap(arr, i, j);
                i++;
            }
        }
        swap(arr, i, r);

        return i;
    }
}
