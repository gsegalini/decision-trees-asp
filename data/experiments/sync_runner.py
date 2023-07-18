#! /usr/bin/env python
# Runs a set of experiments in the specified file

import argparse
import csv
import json
import re
from pathlib import Path
from typing import List

from methods.streed import run_streed

SCRIPT_DIR = Path(__file__).parent.resolve()
STREED_PATH = SCRIPT_DIR / ".." / ".." / "STREED"  # SCRIPT_DIR / "STREED"


def run_experiments(experiments: List):
    results = []

    for e in experiments:
        print(e)
        if e["method"] == "streed":
            result = run_streed(
                str(STREED_PATH),
                e["timeout"],
                e["depth"],
                e["train_data"],
                e["test_data"],
                e["beta"],
                e["tau"],
                e["cost_file"],
                e["num_labels"],
                True if e["mode"] == "hyper" else False
            )
            result["method"] = f'streed'  # beta and tau are separate paremeters

        result["timeout"] = e["timeout"]
        result["depth"] = e["depth"]
        result["beta"] = e["beta"]
        result["tau"] = e["tau"]

        # Save filename excuding .csv
        result["train_data"] = Path(e["train_data"]).name[:-4]
        result["test_data"] = Path(e["test_data"]).name[:-4]

        bins = int(re.findall(r'\d+-bins', e["train_data"])[0][:-5])
        # write number of bins
        result["bins"] = bins
        if "extra_info" in e:
            result["extra_info"] = e["extra_info"]
        else:
            result["extra_info"] = "n/a"
        results.append(result)
    return results


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        prog="Synchronous experiment runner",
        description="Runs, parses, and saves output of multiple experiments sequentially",
    )
    parser.add_argument("--in-file", default=str(SCRIPT_DIR / "experiments.json"))
    parser.add_argument("--out-file", default=str(SCRIPT_DIR / "results.csv"))
    args = parser.parse_args()

    with open(args.in_file, "r") as experiments_file:
        experiments = json.load(experiments_file)

    results = run_experiments(experiments)
    attributes = [
        "method",
        "timeout",
        "depth",
        "train_data",
        "test_data",
        "bins",
        "beta",
        "tau",
        "time",
        "metric_train",
        "metric_test",
        "leaves",
        "terminal_calls",
        "extra_info",
    ]

    results.sort(
        key=lambda v: (v["method"], v["train_data"], v["test_data"], v["depth"], v["bins"], v["beta"], v["tau"]))

    with open(args.out_file, "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(attributes)

        for run in results:
            row = [run[attribute] for attribute in attributes]
            writer.writerow(row)
