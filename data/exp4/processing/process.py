from pathlib import Path

import numpy as np
import pandas as pd

SCRIPT_DIR = Path(__file__).parent.resolve()

aslib_base = SCRIPT_DIR / "../../../data-preparation/aslib_data-master"

df = pd.read_csv("results-llama-par10.csv", sep=",")

df = df[df["vbs"] >= 0.0]

df["mcp"] = df["mcp"] - df["vbs"]
df["singleb"] = df["singleb"] - df["vbs"]
df["vbs"] = df["vbs"] - df["vbs"]

df.to_csv("exp4-llama.csv", index=False)

df_llama = df

del df

df = pd.read_csv("results-streed.csv", sep=",")

names = df["train_data"].unique()

result = []
result_all = []
for name in names:
    rows = df.loc[df['train_data'] == name]
    assert len(rows) == 10

    bins = 5
    cut = len(str(bins)) + 5 + 1

    rows_llama = df_llama.loc[df_llama['scenario'] == name[:-cut]]

    metrics = rows["metric_test"]
    s = np.sum(metrics)
    folder = name[:-7]

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
        "mcp": avg if avg >= 0 else -1,
        "singleb": "n/a"
    }
    result.append(row)


    if name != "SAT20-MAIN":
    
        baseline = np.min(rows_llama["mcp"])

        rows_llama["mcp"] = rows_llama["mcp"] / baseline
        row_relative = {
            "scenario": folder,
            "model": "streed",
            "vbs": 0,
            "mcp": avg / baseline if avg >= 0 else -1,
            "singleb": "n/a"
        }

        result_all.append(row_relative)
        for i, r in rows_llama.iterrows():
            result_all.append(r.to_dict())
streed_new = pd.DataFrame(result)
streed_new.to_csv("exp4-streed.csv", index=False)

all_new = pd.DataFrame(result_all)
all_new.to_csv("exp4-results.csv", index=False)
