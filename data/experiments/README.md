# DelftBlue experiments pipeline

1. Load modules

For all:
```
module load 2022r2

module load cmake

module load python
module load py-numpy
module load py-scikit-learn
module load py-pillow
module load py-pip
```

2. Build STREED, should be in `../../STREED`
3. Install `lab` using `pip`
4. Run `setup_scalability.py` with the desired experiment number (`--exp-number=X`), note that experiment number 4 expects some variables set, check comments in the file for details
5. Configure global variable `MAIL` and `ACCOUNT` in `lab_runner.py`
6. Run `lab_runner.py`
7. Run `lab_aggregate.py` to aggregate results into a single `results.csv`