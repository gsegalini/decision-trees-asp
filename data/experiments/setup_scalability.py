#! /usr/bin/env python
# creates the datasets and experiment file for running the scalability experiments

import argparse
import json
import os
import random
import re
from pathlib import Path

SCRIPT_DIR = Path(__file__).parent.resolve()

labels = {
    "maxsat21-32f-3-bins.txt": 8,
    "maxsat22-32f-3-bins.txt": 11,
    "subset-features": 11,
    "subset-instances": 11,

}
aslib_base = SCRIPT_DIR / "../../data-preparation/aslib_data-master"
names = os.path.join(aslib_base, "names.txt")

bins = [2, 3, 5, 7, 10]

# regularisations = [(0, 0), (0.001, 0.001), (0.01, 0.001), (0.001, 0.01), (0.01, 0.01)]
regularisations = [(0, 0)]


def generate_experiments2():
    dataset_folders = ["subset-features", "subset-instances", "subset-labels"]
    experiments = []
    for type in dataset_folders:
        depth = 4
        path = SCRIPT_DIR / ".." / "exp2" / type
        datasets = [f for f in os.listdir(path) if os.path.isfile(path / f)]
        for dataset in datasets:
            n_labels = 0
            extra = ""
            if "labels" in type:
                n_labels = int(re.findall(r'\d+-labels', dataset)[0][:-7])
                extra = f"{n_labels}-labels"
            elif "features" in type:
                n_labels = labels[type]
                n_features = int(re.findall(r'\d+-features', dataset)[0][:-9])
                extra = f"{n_features}-features"
            elif "instances" in type:
                n_labels = labels[type]
                n_instances = int(re.findall(r'\d+-size', dataset)[0][:-5])
                extra = f"{n_instances}-instances"

            streed = {
                "method": "streed",
                "timeout": 3600,
                "depth": depth,
                "train_data": os.path.join(path, dataset),
                "test_data": "",
                "beta": 0,
                "tau": 0,
                "cost_file": "",
                "num_labels": n_labels,
                "extra_info": extra,
                "mode": "direct"
            }
            experiments.append(streed)
    random.shuffle(experiments)
    return experiments


def generate_experiments1():
    dataset_files = ["maxsat21-32f-3-bins.txt", "maxsat22-32f-3-bins.txt"]
    experiments = []
    for dataset in dataset_files:
        for depth in range(1, 7):
            streed = {
                "method": "streed",
                "timeout": 3600,
                "depth": depth,
                "train_data": os.path.join(SCRIPT_DIR / ".." / "exp1", dataset),
                "test_data": "",
                "beta": 0.01,
                "tau": 0.02,
                "cost_file": "",
                "num_labels": labels[dataset],
                "mode": "direct"
            }
            experiments.append(streed)
    random.shuffle(experiments)
    return experiments


def generate_experiments3():
    experiments = []
    dataset_files = ["ASP-POTASSCO", "SAT20-MAIN"]

    for dataset in dataset_files:
        base_folder = os.path.join(aslib_base, dataset)
        for binarization in bins:
            bin_folder = os.path.join(base_folder, f"{binarization}-bins")
            info_file = os.path.join(bin_folder, (dataset + "-{}-bins_info.txt").format(binarization))

            if dataset not in labels:
                with open(info_file) as info:
                    line = info.readline()
                    labels[dataset] = int(re.findall(r'\d+', line)[0])

            for cv in range(1, 10 + 1):
                cv_folder = os.path.join(bin_folder, f"{cv}-cv")
                csv_path = os.path.join(cv_folder, (dataset + "-{}-bins.txt").format(binarization))
                test_file = os.path.join(cv_folder, (dataset + "-{}-bins-test.txt").format(binarization))
                cost_file = os.path.join(bin_folder, (dataset + "-{}-bins-costs.txt").format(binarization))
                cost_file = cost_file if os.path.isfile(cost_file) else ""
                for depth in range(0, 8):
                    for (beta, tau) in regularisations:
                        streed = {
                            "method": "streed",
                            "timeout": 3600,
                            "depth": depth,
                            "train_data": csv_path,
                            "test_data": test_file,
                            "beta": beta,
                            "tau": tau,
                            "cost_file": cost_file,
                            "num_labels": labels[dataset],
                            "mode": "direct"
                        }
                        experiments.append(streed)

    # Randomize experiment order so no methods gets an unfair advantage on average
    random.shuffle(experiments)
    return experiments


def generate_experiments4():
    experiments = []

    dataset_files = []
    with open(names) as datasets_file:
        dataset_files.extend([f.strip() for f in datasets_file.readlines()])

    for dataset in dataset_files:
        base_folder = os.path.join(aslib_base, dataset)
        binarization = 2  # fill from exp3
        bin_folder = os.path.join(base_folder, f"{binarization}-bins")
        info_file = os.path.join(bin_folder, (dataset + "-{}-bins_info.txt").format(binarization))

        if dataset not in labels:
            with open(info_file) as info:
                line = info.readline()
                labels[dataset] = int(re.findall(r'\d+', line)[0])

        for cv in range(1, 10 + 1):
            cv_folder = os.path.join(bin_folder, f"{cv}-cv")
            csv_path = os.path.join(cv_folder, (dataset + "-{}-bins.txt").format(binarization))
            test_file = os.path.join(cv_folder, (dataset + "-{}-bins-test.txt").format(binarization))
            cost_file = os.path.join(bin_folder, (dataset + "-{}-bins-costs.txt").format(binarization))
            cost_file = cost_file if os.path.isfile(cost_file) else ""
            depth = 0  # fill from exp 3
            (beta, tau) = (0, 0)  # fill from exp3 maybe
            streed = {
                "method": "streed",
                "timeout": 3600,
                "depth": depth,
                "train_data": csv_path,
                "test_data": test_file,
                "beta": beta,
                "tau": tau,
                "cost_file": cost_file,
                "num_labels": labels[dataset],
                "mode": "hyper"
            }
            experiments.append(streed)

    # Randomize experiment order so no methods gets an unfair advantage on average
    random.shuffle(experiments)
    return experiments


if __name__ == "__main__":
    parser = argparse.ArgumentParser(prog="Setup experiments")
    parser.add_argument("--file", default=str(SCRIPT_DIR / "experiments.json"))
    parser.add_argument("--exp-number", default=3)
    args = parser.parse_args()

    n = int(args.exp_number)
    experiments = None
    if n == 3:
        experiments = generate_experiments3()
    elif n == 1:
        experiments = generate_experiments1()
    elif n == 4:
        experiments = generate_experiments4()
    elif n == 2:
        experiments = generate_experiments2()
    else:
        print("invalid experiment {}".format(n))
        exit(-1)
    with open(args.file, "w") as experiments_file:
        json.dump(experiments, experiments_file, indent=4)
