from boas_model import Boas
from read import Dataset
from sklearn.metrics import accuracy_score
import sys

datasets = Dataset.read_datasets("data/datasets.json")

dataset_names = [
    "maxsat21",
    # "maxsat22",
]

datasets = {n: ds for n, ds in datasets.items() if n in dataset_names}


def eval(model, dataset):
    y_pred = model.predict(dataset.X_test)
    acc = accuracy_score(dataset.y_test, y_pred)
    return acc


def main():
    for name, dataset in datasets.items():
        print(f"Test {name}")
        dataset.read_data()
        depth = 2
        model = Boas(D=depth, time_limit=3600, verbose=True)

        dataset.split(binary=True, train_test_same=True)

        model.fit(dataset)
        model.plot()
        # acc = eval(model, dataset)
        # print(f"\nAccuracy: {acc}\n")


if __name__ == "__main__":
    exit(main())
