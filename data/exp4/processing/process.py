import numpy as np
import pandas as pd
from pathlib import Path

SCRIPT_DIR = Path(__file__).parent.resolve()


aslib_base = SCRIPT_DIR / "../../../data-preparation/aslib_data-master"

df = pd.read_csv("results-llama-par10.csv", sep=",")

df = df[df["vbs"] >= 0.0]

df["mcp"] = df["mcp"] - df["vbs"]
df["singleb"] = df["singleb"] - df["vbs"]
df["vbs"] = df["vbs"] - df["vbs"]



df.to_csv("exp4-llama.csv", index=False)

del df

df = pd.read_csv("results-streed.csv", sep=",")

names = df["train_data"].unique()

result = []

for name in names:
    rows = df.loc[df['train_data'] == name]
    metrics = rows["metric_test"]
    s = np.sum(metrics)
    folder = name[:-7]

    bins = 3

    info_file = aslib_base / folder / f"{bins}-bins" / (folder + "-{}-bins_info.txt").format(bins)

    with open(info_file) as info:
        _ = info.readline()
        line = info.readline()
        total = int(line.split()[4][:-1])

    avg = s / total
    row = {
        "scenario": folder,
        "model": "streed",
        "vbs": 0,
        "mcp": avg if avg >= 0 else -1
    }
    result.append(row)

streed_new = pd.DataFrame(result)
streed_new.to_csv("exp4-streed.csv", index=False)

