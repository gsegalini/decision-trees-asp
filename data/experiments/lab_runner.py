#! /usr/bin/env python
# distribute experiments over multiple instances of sync_runner to paralellize workload.

import json
import math
import os
import sys
from pathlib import Path
from typing import List

from lab.environments import LocalEnvironment
from lab.environments import SlurmEnvironment
from lab.experiment import Experiment

SCRIPT_DIR = Path(__file__).parent.resolve()
MAIL = ""
ACCOUNT = ""

class DelftBlueEnvironment(SlurmEnvironment):
    MAX_TASKS = 1_000

    def __init__(
            self,
            email=None,
            account="innovation",
            partition="compute",
            time_limit_per_task=None,
            memory_per_cpu=None,
    ):
        super().__init__(
            email=email,
            extra_options=f"#SBATCH --account={account}",
            partition=partition,
            time_limit_per_task=time_limit_per_task,
            memory_per_cpu=memory_per_cpu,
            qos="normal",
        )


def add_run(experiment: Experiment, chunk: List, id: int):
    run = experiment.add_run()

    id_str = f"sync_runner_{id}"

    chunk_path = SCRIPT_DIR / "tmp" / "chunks" / f"{id_str}.json"
    results_path = SCRIPT_DIR / "tmp" / "results" / f"{id_str}.csv"
    os.makedirs(chunk_path.parent.resolve(), exist_ok=True)
    os.makedirs(results_path.parent.resolve(), exist_ok=True)
    with open(chunk_path, "w") as chunk_file:
        json.dump(chunk, chunk_file)

    run.add_command(
        f"sync_runner",
        [
            sys.executable,
            str(SCRIPT_DIR / "sync_runner.py"),
            "--in-file",
            chunk_path,
            "--out-file",
            results_path,
        ],
        hard_stdout_limit=1024 * 1024,
        hard_stderr_limit=1024 * 1024
    )
    run.set_property("id", [id_str])  # id is a required unique attribute


def make_chunks(n: int, l: List):
    n_chunks = (len(l) + n - 1) // n
    return [l[i * n: (i + 1) * n] for i in range(n_chunks)]


def add_runs(experiment: Experiment, experiments: List):
    n = len(experiments)
    chunk_size = min([8, math.ceil(n / 32)])
    chunks = make_chunks(chunk_size, experiments)
    for i in range(len(chunks)):
        add_run(experiment, chunks[i], i)


if __name__ == "__main__":
    env = (
        LocalEnvironment(1)
        if os.cpu_count() <= 4
        else DelftBlueEnvironment(
            MAIL,
            ACCOUNT,
            "compute",
            "16:00:00",  # Big chunk of time for running multiple experiments in one task
            "16G",
        )
    )

    # The folder in which experiment files are generated.
    exp = Experiment(environment=env, path=SCRIPT_DIR / "experiment")

    with open(SCRIPT_DIR / "experiments.json", "r") as experiments_file:
        experiments = json.load(experiments_file)
    add_runs(exp, experiments)

    exp.add_step("build", exp.build)
    exp.add_step("start", exp.start_runs)
    # Do result aggregation and reporting manually since it is too slow for large amount of small experiments

    exp.run_steps()
