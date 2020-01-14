# DCA: a fast exact algorithm for separable convex resource allocation with nested bound constraints

This repository contains the Java implementation of DCA, an infeasibility-guided divide-and-conquer algorithm for a discrete resource allocation problem with nested bound constraints (DRAP-NC), as well as a set of instances that could be used for testing the performance of DCA. The details of the algorithm and the generation of test instances are described in [the following paper](http://www.optimization-online.org/DB_FILE/2018/11/6902.pdf):

	"A new combinatorial algorithm for separable convex resource allocation with nested bound constraints", Zeyang Wu, Kameng Nip, Qie He, working paper, University of Minnesota, 2019.

## Running the test
There are two sets of numerical experiments conducted in the paper.

1. Solve DRAP-NC with linear and quadratic objectives with DCA, in comparison with Gurobi.
	- Test file: TestGurobiDCA_Lin.java, TestGurobiDCA_Qua.java, TestSparseGurobiDCA_Lin, TestSparseGurobiDCA_Qua
	- Test instances: DRAP-NC with linear and quadratic objectives;
	- Benchmark: Gurobi
	- Requirements: Gurobi is installed 

2. Solve DRAP-NC with three benchmark convex objectives, in comparison with the Monotonic Decomposition Algorithm (MDA), the algorithm with the current best time complexity.
	- Test file: TestFlow1Con
	- Test instances: DRAP-NC with three classes of benchmark convex objectives, [F], [CRASH], and [FUEL]
	- Benchmark: MDA (Please refer to [this paper](https://pubsonline.informs.org/doi/abs/10.1287/ijoo.2018.0004) for the implementation details of MDA)
	- Requirements: None

## A short description of all the files 


1. Main source files:
	* Algorithms: 
		- Function.java : This is an abstract class for the function oracles	
		- RAP.java : This a class for discrete simple resource allocation (DRAP) with separable convex objectives. Hochbaum's algorithm (1994) is implemented to solve DRAP with general convex objectives. In addition, for linear objectives, a more efficient O(n) time algorithm is also implemented.	
		- RAPNC.java : This is a class for RAPNC with separable convex objectives. It provides two methods to solve the problem (DCA and MDA).	
		- RAP_Continuous.java : This is a class for simple resource allocation with separable convex quadratic objectives. A bisection method is implemented to solve the RAP subproblems in the two algorithms to speed up the performance.	
		- RAPNC_Continuous.java : This is class for resource allocation problem with lower and upper nested constraints (RAPNC). DCA and MDA for RAP-NC is implemented to solve an RAP-NC instance.

 	* ResultType files:
	  	- ResultTypeRAP.java : This is a class for storing the solution to an instance of DRAP
		- ResultTypeMDA.java : This is a class for storing the solution to the subproblems of DRAP-NC in the MDA method
		- ResultTypeRAPNC.java : This is a class for storing the solution to an instance of DRAP-NC
		- ResultTypeRAP_Continuous.java : This is a class for storing the solution to an instance of continuous RAP 
		- ResultTypeMDA_Continuous.java : This is a class for storing the solution to the subproblems of RAP-NC in the MDA method
		- ResultTypeRAPNC_Continuous.java : This is a class for storing the solution to an instance of continuous RAP-NC


2. Numerical experiments on solving DRAP-NC with linear and quadratic objectives with DCA and Gurobi:	In the first numerical experiment, we compare the performance of DCA and Gurobi on DRAP-NC instances with linear objectives. When the objectives are linear, Gurobi can solve the problems as linear programs due to the total unimodularity of the constraint matrix in each instance.

	- TestGurobiDCA_Lin.java : This is a test class to evaluate the numerical performance of DCA, and Gurobi on DRAP-NC with linear objectives
	- TestGurobiDCA_Qua.java : This is a test class to evaluate the numerical performance of DCA, and Gurobi on DRAP-NC with quadratic objectives
	- TestSparseGurobiDCA_Lin.java : This is a test class to evaluate the numerical performance of DCA and Gurobi on DRAP-NC with linear objectives under a sparse formulation. The running time of Gurobi is sped up by more than 30 times.
	- TestSparseGurobiDCA_Qua.java : This is a test class to evaluate the numerical performance of DCA and Gurobi on DRAP-NC with quadratic objectives under a sparse formulation. The running time of Gurobi is sped up by more than 30 times.


3. Numerical experiments on solving DRAP-NC with three benchmark convex objectives with DCA and MDA.

	- TestFlow1Con: This is a test class to evaluate the numerical performance of DCA and MDA on DRAP-NC with three benchmark objectives. You can test arbitrary convex objectives by changing the function oracles. 




