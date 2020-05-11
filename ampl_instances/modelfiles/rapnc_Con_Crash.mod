param N; # number of resources

param capacity{1..N}; # d_i maximum capacity of each resource
param nested_lowerbound{1..N}; # lower bounds of the nested constraints where nested_lowerbound[N] = B
param nested_upperbound{1..N}; # upper bounds of the nested constraints where nested_upperbound[N] = B

param cost_a{1..N}; # cost coefficient
param cost_b{1..N}; # cost coefficient

var x {i in 1..N} integer >= 0, <= capacity[i]; # decision variables 

minimize Total_Cost:  sum {i in 1..N} (10 * cost_b[i] + cost_a[i] / (x[i] + 1)); # Crash function 

subject to nested_contraints {i in 1..N}: 
	nested_lowerbound[i] <= sum {j in 1..i} x[j] <= nested_upperbound[i];
