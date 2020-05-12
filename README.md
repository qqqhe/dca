# DCA: A new combinatorial algorithm for separable convexresource allocation with nested bound constraints

## Overview 
This repository contains the Java implementation of DCA, an infeasibility-guided divide-and-conquer algorithm for a discrete resource allocation problem with nested bound constraints (DRAP-NC), as well as the code of generating random DRAP-NC instances used for testing the performance of DCA. DCA is a fast algorithm that can solve DRAP-NC with millions of variables in seconds. The details of the algorithm and the generation of test instances are described in [the following paper](http://www.optimization-online.org/DB_FILE/2018/11/6902.pdf):

	"A new combinatorial algorithm for separable convex resource allocation with nested bound constraints", Zeyang Wu, Kameng Nip, Qie He, working paper, University of Minnesota, 2019.

## To run the numerical experiment
All the source files are in folder `./src/main/java/dca_ijoc`. The main file is `RAPNC.java` that implements the two algorithms used to solve DRAP-NC: DCA and a benchmark algorithm MDA. MDA is a divide-and-conquer algorithm with the best worst-case time complexity in the literature. For more details of MDA, see [the paper](https://arxiv.org/abs/1703.01484). Though the time complexity of DCA is worse than MDA, DCA's infeasibility guided divide-and-conquer framework gives it better performance on all the test instances
.

To run the numerical experiment, simply execute the main methods of the following classes. 
1. `TestConDCA`: Compare the performance of DCA and MDA with three convex objectives: [F], [FUEL] and [CRASH]. To obtain a fast result, readers can use the default setting to evalute DCA and MDA on the stored instances in this repo with up to 409600 variables. To reproduce the whole numerical experiment in the paper, please use the static method `TestConDCA.ExperimentInPaper()`. This method will generate large test instances in the system memory in a real-time manner as such a single instance could be of several gigabytes. 
2. `TestSparseGurobiDCA_Lin` and `TestSparseGurobiDCA_Qua`: Compare the performance of DCA and Gurobi in solving DRAP-NC with linear/quadratic objectives. 
	
## Test instances data files
We provide the test instances of RAPNC used in the paper. 
1. Test instances in the numerical experiment section. In the section Numerical experiments of the paper, we compare the performance of DCA with MDA on three convex objectives on randomly generated instance. We stored the test instances in folder `./test_instances/TIMESTAMP/OBJFUNCTION/..`. The data files are json files. Each file stores a struct with following fields: 
	- sting objFuncType, the type of objective function, currently we have [F], [FUEL] and [CRASH].
	- int dimension, the size of the instances
	- long[] lbVar, the lower bounds of decision variables
	- long[] ubVar, the upper bounds of decision variables
	- long[] lbNested, the lower bounds of nested constraint
	- long[] ubNested, the upper bounds of nested constraint
	- double[] cost_param_a, 
	- double[] cost_param_b, two parameters that define the convex objective function
Due to size limit, we only uploaded the intances with up to 409600 variables. Interested readers can generate new test instances with the static method `genTestCollectionToFiles()` in class `TestConDCA` with appropriate inputs. 
	
2. Test instances in the AMPL data files format. These instances are used to evaluate the performance of commercial solver. The files are stored in `./ampl_instances/datafiles/..`. The coresponding .mod files are in `./ampl_instances/modelfiles/..`. In the first line of each .dat file, we provide the optimal value (solved by DCA). We also share the source code `AMPLTestGenerator` used to generate these instances. Interested researchers could modify the objective function and the problem size in the code to generate other test instances of larger sizes. 

## ACKNOWLEDGMENT

We are extremely grateful to Thibaut Vidal for sharing the implementation details of MDA. His comments on an earlier version of our algorithm helped improve the quality of our manuscript greatly. We also thank the anonymous referees for their careful reading of our manuscript and their many insightful comments and suggestions.
