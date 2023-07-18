Each file provides a function to run that method and returns a dictionary with the following:
- terminal_calls (-1 for all methods except STreeD)
- time (time taken for method to compute tree)
- leaves (amount of leaf nodes in computed tree)
- metric_train (mean squared error of tree for training data)
- metric_test (mean squared error of tree for test data)

Some important points
- When a method times out, it should return a valid value, and return a time larger than the timeout in `time` (should use unix command `timeout` to ensure method does not continue to run)
- When a method crashes for some reason (bug/out of memory) it should return a valid value and -1 in `time`