# Data-preparation

This program expects these arguments in all cases except aslib:
1. `type` of problem 
2. `time_input` input file containing times for each instance, for each algorithms
3. `output` output file
4. `bins` number of bins

Files need to be in a specific format depending on which dataset they are collected from.

For aslib:
1. first argument should be `aslib`
2. second argument is the input file containing in each line the name of each scenario's folder (same as `names.txt` in `bench_llama`).
3. Other arguments are ignored