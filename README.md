# DCA: a new combinatorial algorithm for separable convex resource allocation with nested bound constraints

## Overview 
The discrete resource allocation problem with nested bound constraints (DRAP-NC) has many applications in production planning, logistics, portfolio management, telecommunication, statistical surveys, and machine learning. This repository contains the Java implementation of DCA, one of the currently most efficient exact algorithms for DRAP-NC. Our implementation is able to solve DRAP-NC instances with millions of variables in seconds. Details of the algorithm and instance generation are described in [the following paper](http://www.optimization-online.org/DB_FILE/2018/11/6902.pdf):

	"A new combinatorial algorithm for separable convex resource allocation with nested bound constraints", Zeyang Wu, Kameng Nip, Qie He, working paper, University of Minnesota, 2020.

## Replicate numerical experiments in the paper
All the source files are in folder `./src/main/java/dca_ijoc`. The main source file `RAPNC.java` contains the implementation of our algorithm DCA. The source file also contains the implementation of another algorithm MDA, which to our best knowledge has the current best worst-case time complexity in the literature. For more details of MDA, please refer to [this paper](https://arxiv.org/abs/1703.01484). Although the theoretical time complexity of DCA is worse than that of MDA, DCA has a better computational performance than MDA on all the random instances we have generated. Please refer to our paper for some potential reasons for this mismatch.

To replicate the numerical experiments in our paper, please execute the main methods of the following classes. 
1. Class `TestConDCA`: solving DRAP-NC with three convex objectives ([F], [FUEL], and [CRASH]) with both DCA and MDA. Under the default setting, users can evalute the performance of DCA and MDA on instances stored in this repo with up to 409,600 variables. To reproduce the results for all the instances (with up to millions of variables) in our paper, please use the static method `TestConDCA.ExperimentInPaper()` and keep the random seeds in the code unchanged. This method will generate and solve large-sized instances within system memory instead of exchanging instance data with external storage (a DRAP-NC instance with millions of variables is of several gigabytes and could take a long time to write to or read from external storage). 

2. Class `TestSparseGurobiDCA_Lin`: solving DRAP-NC with linear objectives with DCA and Gurobi's linear programming solver. To improve Gurobi's performance, we use the sparse formulation of DRAP-NC described in our paper. To reproduce the results for all the instances in our paper, please keep the random seeds in the code unchanged.
	
## Additional test instances for general mixed-integer nonlinear optimization solvers
Since DRAP-NC belongs to the class of convex mixed-integer nonlinear programming problems (MINLPs), we also provide two additional ways for interested researchers to evaluate the performance of an MINLP solver on DRAP-NC instances. 

1. DRAP-NC instances in the JSON file format. 

Users can generate DRAP-NC instances in the JSON file format with the static method `genTestCollectionToFiles()` in class `TestConDCA`. We also provide such DRAP-NC instances with up to 409,600 variables in the folder `./test_instances/`. Each file stores a struct with following fields.
	- string objFuncType: objective function type, such as [F], [FUEL] or [CRASH];
	- int dimension: instance size;
	- long[] lbVar: lower bounds of decision variables;
	- long[] ubVar: upper bounds of decision variables;
	- long[] lbNested: lower bounds in nested constraints;
	- long[] ubNested: upper bounds in nested constraint;
	- double[] cost_param_a and double[] cost_param_b: parameters used in the objective function.
	
2. DRAP-NC instances in the AMPL file format. 

Users could run the source code `./src/main/java/dca_ijoc/AMPLInstanceGenerator.java` to generate DRAP-NC test instances in the AMPL file format. A set of test instances (with the three sets of convex objectives studied in our paper) in the AMPL file format are provided in the folder `./ampl_instances/`. The first line of each .dat file gives the optimal objective value output by DCA. 

## ACKNOWLEDGMENT

We are really grateful to Thibaut Vidal for sharing the implementation details of MDA.
