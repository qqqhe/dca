# DCA: a fast exact algorithm for 

This repository contains the Java implementation of DCA, an infeasibility-guided divide-and-conquer algorithm for a discrete resource allocation problem with nested bound constraints (DRAP-NC), as well as the code of generating random DRAP-NC instances used for testing the performance of DCA. The details of the algorithm and the generation of test instances are described in [the following paper](http://www.optimization-online.org/DB_FILE/2018/11/6902.pdf):

	"A new combinatorial algorithm for separable convex resource allocation with nested bound constraints", Zeyang Wu, Kameng Nip, Qie He, working paper, University of Minnesota, 2019.
	
## Introduction
This repo contains all source files of the numerical experiment conducted in the paper. It contains 
1. Implementation of DCA and MDA (in java). All the source files are in folder `./src/main/java/dca_ijoc`. The main file is RAPNC that implements the two algorithms used to solve DRAP-NC: DCA and MDA. 
2. Code to reporduce the experiment. The main class is `TestConDCA`. 
2. Data files of test instances used in the paper in json format. Please see the details in the later section. 

## To run the numerical experiment
To run the numerical experiment, simply execute the main methods of the following class. 
1. `TestConDCA`: Compare the performance of DCA and MDA with three convex objectives: [F], [FUEL] and [CRASH]. 
2. `TestSparseGurobiDCA_Lin` and `TestSparseGurobiDCA_Qua`: Compare the performance of DCA and Gurobi in solving DRAP-NC with linear/quadratic objectives. 
	
## Test instances data files
In this Repo, we provide the some test instances of RAPNC
1. Test instances in the paper. In the section Numerical experiments of the paper, we compare the performance of DCA with MDA on three convex objectives on randomly generated instance. We stored the test instances in folder `./test_instances/TIMESTAMP/OBJFUNCTION/..`. The data files are json files. Each file stores a struct with following fields: 
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
