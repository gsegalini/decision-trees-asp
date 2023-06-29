import pandas as pd
import json
from sklearn.model_selection import train_test_split


class Dataset:

    def __init__(self, dataset_name, attributes):
        self.name = dataset_name
        for key, val in attributes:
            setattr(self, key.replace(" ", "_"), val)
        self.df = None
        self.bin_df = None

    def read_data(self):
        if self.bin_df is None:
            # Read the dataset with binarized features
            self.bin_df = pd.read_csv(self.bin_path, sep=" ")  # .sample(frac=1)

            # separate label and features
            self.bin_y = self.bin_df[self.target_label]
            self.bin_X = self.bin_df.drop(self.target_label, axis=1, inplace=False)

        return self.bin_df

    @staticmethod
    def read_datasets(datasets_file="data/datasets.json"):
        with open(datasets_file, "r") as f:
            data = json.load(f)
            datasets = {
                dataset_name: Dataset(dataset_name, data[dataset_name].items())
                for dataset_name in data.keys()
            }
            return datasets

    """
    Split the dataset in a training set and a test set.
    The arguments are the same as those of sklearn.model_selection.train_test_split
    """

    def split(self, binary=False, train_test_same=False, *args, **kwargs):
        if binary:
            X = self.bin_X
            y = self.bin_y
        else:
            X = self.X
            y = self.y
        pf = self.protected_features[0] if len(self.protected_features) > 0 else None
        if train_test_same:
            self.X_train, self.X_test, self.y_train, self.y_test = X, X, y, y
            return self.X_train, self.X_test, self.y_train, self.y_test
        if binary and not pf is None:  # Stratified:
            self.X_train, self.X_test, self.y_train, self.y_test = train_test_split(X, y,
                                                                                    stratify=[(y.at[i], X.at[i, pf]) for
                                                                                              i in X.index], *args,
                                                                                    **kwargs)
        elif True:
            self.X_train, self.X_test, self.y_train, self.y_test = train_test_split(X, y, *args, **kwargs)
        return self.X_train, self.X_test, self.y_train, self.y_test
