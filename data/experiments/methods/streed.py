import os
import re
import subprocess
import sys

float_pattern = r"[-+]?(\d+(\.\d*)?|\.\d+)([eE][-+]?\d+)?"  # https://docs.python.org/3/library/re.html#simulating-scanf


def parse_output(content, timeout):
    props = {}
    if "Solution 0" not in content:
        # Timeout
        props["time"] = timeout + 1
        props["metric_train"] = -1
        props["metric_test"] = -1
        props["leaves"] = -1
        props["terminal_calls"] = -1
        return props

    # STreeD
    txt_pattern = {
        "terminal_calls": (r"Terminal calls: (\d+)", int),
        "solution": (r"Solution 0:\s+(.*)", str),
        "time": (r"CLOCKS FOR SOLVE: (" + float_pattern + ")", float),
    }

    matches = {}
    for i in txt_pattern:
        matches[i] = txt_pattern[i][1](
            re.search(txt_pattern[i][0], content, re.M).group(1)
        )

    # depth, branching nodes, train, test, avg. path length
    solution_vals = matches["solution"].split()

    props["terminal_calls"] = matches["terminal_calls"]
    props["time"] = matches["time"]
    props["leaves"] = (
        int(solution_vals[1]) + 1
    )  # Solution prints branching nodes, but want leaf nodes
    props["metric_train"] = float(solution_vals[2])
    props["metric_test"] = float(solution_vals[3])
    return props


def run_streed(
    exe,
    timeout,
    depth,
    train_data,
    test_data,
    beta,
    tau,
    cost_file,
    num_labels,
    hyper

):
    try:
        result = subprocess.check_output(
            [
                "timeout",
                str(timeout),
                exe,
                "-task",
                "algorithm-selection",
                "-file",
                train_data,
                "-test-file",
                test_data,
                "-max-depth",
                str(depth),
                "-max-num-nodes",
                str(2**depth - 1),
                "-time",
                str(timeout + 10),
                "-beta",
                str(beta),
                "-tau",
                str(tau),
                "-cost-file",
                cost_file,
                "-num-labels",
                str(num_labels),
                "-mode",
                "hyper" if hyper else "direct"
            ],
            timeout=timeout,
        )
        output = result.decode()
        # print(output)
        parsed = parse_output(output, timeout)
        return parsed
    except subprocess.TimeoutExpired as e:
        # print(e.stdout.decode())
        return parse_output("", timeout)
    except subprocess.CalledProcessError as e:
        print(str(e.returncode) + " " + e.stdout.decode(), file=sys.stderr, flush=True)
        print(os.getcwd())
        return {"time": -1, "metric_train": -1, "metric_test": -1, "leaves": -1, "terminal_calls": -1}
