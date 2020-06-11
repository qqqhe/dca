# DCA: A new combinatorial algorithm for separable convexresource allocation with nested bound constraints

## Overview 
This repository contains the Java implementation of DCA, an infeasibility-guided divide-and-conquer algorithm for a discrete resource allocation problem with nested bound constraints (DRAP-NC), as well as the code of generating random DRAP-NC instances used for testing the performance of DCA. DCA is a fast algorithm that can solve DRAP-NC with millions of variables in seconds. The details of the algorithm and the generation of test instances are described in [the following paper](http://www.optimization-online.org/DB_FILE/2018/11/6902.pdf):

	"A new combinatorial algorithm for separable convex resource allocation with nested bound constraints", Zeyang Wu, Kameng Nip, Qie He, working paper, University of Minnesota, 2020.

## Replicate the numerical experiments in the paper
All the source files are in folder `./src/main/java/dca_ijoc`. The main file is `RAPNC.java` which contains the implementation of our algorithm DCA as well as a benchmark algorithm MDA. MDA is a divide-and-conquer algorithm with the current best worst-case time complexity in the literature. For more details of MDA, see [the paper](https://arxiv.org/abs/1703.01484). Although the theoretical time complexity of DCA is worse than MDA, DCA has a better performance than MDA on all the random instances we test. Please refer to our paper for some explanation of this mismatch.

To replicate the numerical experiment in the paper, simply execute the main methods of the following classes. 
1. `TestConDCA`: solving DRAP-NC with three convex objectives with both DCA and MDA: [F], [FUEL] and [CRASH]. To obtain a fast result, readers can use the default setting to evalute DCA and MDA on the stored instances in this repo with up to 409600 variables. To reproduce the results for all large-sized instances in the paper, please use the static method `TestConDCA.ExperimentInPaper()` and do not change the random seeds in the code. This method will generate and solve large-sized instances with system memory instead of storing instances in external hard drivers (as one large-sized instance could be of several gigabytes). 

2. `TestSparseGurobiDCA_Lin` and `TestSparseGurobiDCA_Qua`: solving DRAP-NC with linear objectives with DCA and Gurobi's linear programming solver. To reproduce the the instances and optimal solutions in the paper, do not change the random seeds in the code.
	
## Test instances for general mixed-integer nonlinear optimization solvers
Since DRAP-NC belongs to the class of convex mixed-integer nonlinear programming problems (MINLPs), we also provide the two additional ways for researchers and developers to evaluate the performance of an MINLP solver on DRAP-NC instances. 
1. DRAP-NC instances in the JSON file format. Interested readers can generate DRAP-NC instances in the JSON file format with the static method `genTestCollectionToFiles()` in class `TestConDCA`. We also provide small-sized DRAP-NC instances in folder `./test_instances/TIMESTAMP/OBJFUNCTION/..`. Each file stores a struct with following fields.
	- sting objFuncType: the type of objective function, such as [F], [FUEL] or [CRASH];
	- int dimension: the size of the instances;
	- long[] lbVar: the lower bounds of decision variables;
	- long[] ubVar: the upper bounds of decision variables;
	- long[] lbNested: the lower bounds in nested constraints;
	- long[] ubNested: the upper bounds in nested constraint;
	- double[] cost_param_a and double[] cost_param_b: two parameters that define the convex objective function.
	
Due to the file sizes, we uploaded intances with up to 409,600 variables. 
	
2. DRAP-NC instances in the AMPL file format. Interested readers could use source code `./src/main/java/dca_ijoc/AMPLInstanceGenerator.java` to generate DRAP-NC test instances in the AMPL file format. A set of test instances with three sets of convex objectives in AMPL file format are provided in the folder `./ampl_instances/`. In the first line of each .dat file, we provide the optimal objective value given by DCA. 

## ACKNOWLEDGMENT

We are grateful to Thibaut Vidal for sharing the implementation details of MDA.
