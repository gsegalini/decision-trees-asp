#! /usr/bin/env python
# Runs a set of experiments in the specified file

from pathlib import Path
import csv
import json
import argparse
import os
from typing import List

SCRIPT_DIR = Path(__file__).parent.resolve()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        prog="Experiment aggregator",
        description="Aggregates the results from multiple sync runs",
    )
    parser.add_argument("--in-dir", default=str(SCRIPT_DIR / "tmp" / "results"))
    parser.add_argument("--out-file", default=str(SCRIPT_DIR / "results.csv"))
    args = parser.parse_args()

    results = []
    for (dirpath, _, filenames) in os.walk(args.in_dir):
        print(f"Reading directory {dirpath}")
        for file in filenames:
            print(f"Reading results: {file}")
            with open(Path(dirpath) / file, "r") as results_file:
                reader = csv.DictReader(results_file)
                for row in reader:
                    results.append(row)

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
        "extra_info"
    ]

    results.sort(
        key=lambda v: (v["method"], v["train_data"], v["test_data"], v["depth"], v["bins"], v["beta"], v["tau"]))

    with open(args.out_file, "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(attributes)

        for run in results:
            row = [run[attribute] for attribute in attributes]
            writer.writerow(row)
