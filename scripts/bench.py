import subprocess, sys, random
executable = "STREED"

# data = ["maxsat21-32f", "maxsat22-32f"]
# data = ["maxsat21-32f-3-bins", "maxsat22-32f-3-bins", "maxsat21-32f-5-bins", "maxsat22-32f-5-bins", "maxsat21-32f", "maxsat22-32f"]
data = ["miplib-2-bins", "miplib-3-bins", "miplib-5-bins"]
labels = {"maxsat21-32f" : "8", 
          "maxsat22-32f" : "11", 
          "maxsat21-32f-3-bins" : "8", 
          "maxsat22-32f-3-bins" : "11", 
          "maxsat21-32f-5-bins" : "8", 
          "maxsat22-32f-5-bins" : "11", 
          "miplib-2-bins": "532",
          "miplib-3-bins": "532",
          "miplib-5-bins": "532"}

results = {}

do_no_bounds = True
split = "0.1"
seed = 42 # random.randint(0, 32768)
tau = 10
beta = 500
mode = "direct"
min_depth = 1
max_depth = 2
for dt in data:
	# filename_normal = data + "-d{}.txt"
	print(f"Data: {dt}")
	results[dt] = {}
	results[dt]["yes-bounds"] = {}
	for d in range(min_depth, max_depth + 1):
		nodes = str(2**d - 1)
		# f = filename.format(d)
		# writing_in = open(f, 'w+')
		print(f"Depth {d} with bounds")
		tmp = subprocess.check_output(
		["./{}".format(executable),
		"-task", "algorithm-selection", 
		"-file", "../streed2/data/alg-sel/{}.txt".format(dt), 
		"-max-depth", "{}".format(d), 
		"-max-num-nodes", nodes, 
		"-num-labels", labels[dt], 
		"-train-test-split", split, 
		"-random-seed", str(seed), 
		"-beta", str(beta), 
		"-tau", str(tau), "-mode", mode]
		).decode(sys.stdout.encoding).strip()
		results[dt]["yes-bounds"][d] = tmp
		# writing_in.close()
		
	if do_no_bounds:
		results[dt]["no-bounds"] = {}
		for d in range(min_depth, max_depth + 1):
			nodes = str(2**d - 1)

			# f = filename.format(d)
			# writing_in = open(f, 'w+')
			print(f"Depth {d} with no bounds")
			tmp = subprocess.check_output(
			    ["./{}".format(executable), 
			    "-task", "algorithm-selection", 
			    "-file", "../streed2/data/alg-sel/{}.txt".format(dt), 
			    "-max-depth", "{}".format(d), 
			    "-max-num-nodes", nodes, 
			    "-num-labels", labels[dt], 
			    "-use-lower-bound", "0", 
			    "-use-upper-bound", "0", 
			    "-train-test-split", split, 
			    "-random-seed", str(seed), 
			    "-beta", str(beta), 
			    "-tau", str(tau), 
			    "-mode", mode]
			    ).decode(sys.stdout.encoding).strip()
			results[dt]["no-bounds"][d] = tmp
			# writing_in.close()

print("data depth bound runtime calls metric_tr metric_ts")
for dt in data:
	dic = results[dt]
	for k2 in dic.keys():
		dic2 = dic[k2]
		for d in dic2.keys():
			s = dic2[d]
			lines = s.split("\n")
			runtime = "-1"
			calls = "-1"
			metric_tr = "-1"
			metric_ts = "-1"
			for l in lines:
				if "CLOCKS" in l:
					runtime = l.split()[3]
				if "Terminal calls:" in l:
					calls = l.split()[2]
				if "Solution 0" in l:
				    sp = l.split()
				    metric_tr = sp[4]
				    metric_ts = sp[5]
				    
			print("{} {} {} {} {} {} {}".format(dt, d, k2, runtime, calls, metric_tr, metric_ts))
