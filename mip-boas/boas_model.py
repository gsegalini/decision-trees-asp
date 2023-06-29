import time

import gurobipy as gp
from gurobipy import GRB
import numpy as np
from sklearn import preprocessing
import pandas as pd
from sklearn.metrics import accuracy_score
import math

"""
This method implements the MIP model of Vilas-Boas et al., 2020
Optimal Decision Trees for the Algorithm Selection Problem:
Integer Programming Based Approaches
"""


class Boas:

    def __init__(self, D=2, beta=50, time_limit=3600, minimum_leaf_node_size=10, verbose=True):
        # maximum depth
        self.D = D
        # number of leaf nodes
        self.L = pow(2, self.D)
        # number of branching nodes
        self.B = pow(2, self.D) - 1
        # small leaf penalty
        self.beta = beta
        # time limit in seconds
        self.time_limit = time_limit
        # minimum leaf_node_size
        self.threshold = minimum_leaf_node_size
        self.verbose = verbose

    def normalize(self, X):
        x = X.values
        min_max_scaler = preprocessing.MinMaxScaler()
        x_scaled = min_max_scaler.fit_transform(x)
        return pd.DataFrame(x_scaled, index=X.index, columns=X.columns)

    def fit(self, dataset):
        self.initialize(dataset)
        start = time.time()
        self.solve()
        end = time.time()
        elapsed = end - start
        print("ELAPSED TIME: {}".format(self.m.runtime))

    def initialize(self, dataset):
        self.dataset = dataset
        X = dataset.X_train
        # y = dataset.y_train

        # number features

        self.nf = dataset.features
        self.nalgs = dataset.classes

        self.c = [[0] for _ in range(self.nf)]  # binary feature, either you are <= 0 or > 0
        # unique labels
        self.Ks = [i for i in range(self.nalgs)]
        # number of unique labels
        self.K = len(self.Ks)
        # indices per unique label
        # ixs = y.groupby(y).indices
        # self.K_ixs = [ixs[k] for k in self.Ks]

        self.g = [[-1 for _ in range(2 ** l + 1)] for l in range(self.D + 1)]
        self.h = [[-1 for _ in range(2 ** l + 1)] for l in range(self.D + 1)]
        for l in range(1, self.D + 1):
            for n in range(1, pow(2, l) + 1):
                if n % 2 == 0:
                    self.g[l][n] = (int((n + 1) / 2))
                else:
                    self.h[l][n] = (int((n + 1) / 2))

        # number of instances
        self.n = X.shape[0]
        # number of features
        # self.p = X.shape[1]
        # X = self.normalize(X)

        try:
            self.m = gp.Model("Boas")
            self.m.setParam("LogToConsole", self.verbose)

            self.x = [None] * self.D
            for l in range(self.D):
                sz = 2 ** l
                ln = [None] * (sz + 1)
                for n in range(1, sz + 1):
                    lf = [None] * self.nf
                    for f in range(self.nf):
                        lc = [None] * (len(self.c[f]))
                        for c in self.c[f]:
                            v = self.m.addVar(vtype=GRB.BINARY, name="x({},{},{},{})".format(l, n, f, c))
                            lc[c] = v
                        lf[f] = lc
                    ln[n] = lf
                self.x[l] = ln

            self.y = [None] * (self.D + 1)
            for l in range(1, self.D + 1):
                sz = 2 ** l
                ln = [None] * (sz + 1)
                for n in range(1, sz + 1):
                    lp = [None] * self.n
                    for p in range(self.n):
                        v = self.m.addVar(vtype=GRB.BINARY, name="y({}, {}, {})".format(l, n, p))
                        lp[p] = v
                    ln[n] = lp
                self.y[l] = ln

            self.z = [None] * (self.L + 1)
            for n in range(1, self.L + 1):
                la = [None] * self.nalgs
                for a in self.Ks:
                    v = self.m.addVar(vtype=GRB.BINARY, name="z({}, {})".format(n, a))
                    la[a] = v
                self.z[n] = la

            self.u = [None] * (self.L + 1)
            self.missings = [None] * (self.L + 1)
            for n in range(1, self.L + 1):
                v = self.m.addVar(vtype=GRB.BINARY, name="u({})".format(n))
                self.u[n] = v
                v = self.m.addVar(vtype=GRB.INTEGER, name="m({})".format(n))
                self.missings[n] = v

            self.w = [None] * self.n
            for p in range(self.n):
                ln = [None] * (self.L + 1)
                for n in range(1, self.L + 1):
                    la = [None] * self.nalgs
                    for a in self.Ks:
                        v = self.m.addVar(vtype=GRB.BINARY, name="w({}, {}, {})".format(p, n, a))
                        la[a] = v
                    ln[n] = la
                self.w[p] = ln

            self.m.update()

            ## Add constraints

            # 3
            for l in range(self.D):
                for n in range(1, 2 ** l + 1):
                    self.m.addConstr(
                        gp.quicksum([self.x[l][n][f][c] for f in range(self.nf) for c in self.c[f]]) == 1,
                        name="constr_3 {} {}".format(l, n))

            # 4
            for p in range(self.n):
                self.m.addConstr(gp.quicksum([self.w[p][n][a] for a in self.Ks for n in range(1, self.L + 1)]) == 1,
                                 name="constr_4 {}".format(p))

            # 5
            for n in range(1, self.L + 1):
                self.m.addConstr(gp.quicksum([self.z[n][a] for a in self.Ks]) == 1, name="constr 5 {}".format(n))

            # 6 7 8
            for p in range(self.n):
                for n in range(1, self.L + 1):
                    for a in self.Ks:
                        self.m.addConstr(self.w[p][n][a] <= self.z[n][a], name="constr 6 {} {} {}".format(p, n, a))
                        self.m.addConstr(self.w[p][n][a] <= self.y[self.D][n][p],
                                         name="constr 7 {} {} {}".format(p, n, a))
                self.m.addConstr(self.u[n] >= self.y[self.D][n][p], name="constr 8 {} {}".format(n, p))

            # 9 and 18
            for n in range(1, self.L + 1):
                self.m.addConstr(
                    gp.quicksum([self.y[self.D][n][p] for p in range(self.n)]) + self.missings[n] >= self.threshold * self.u[
                        n],
                    name="constr 9 {}".format(n))
                self.m.addConstr(self.missings[n] >= 0, name="m in Z+")

            # 10
            for l in range(2, self.D + 1):
                for n in range(1, 2 ** l + 1):
                    for p in range(self.n):
                        parent = np.max([self.g[l][n], self.h[l][n]])
                        self.m.addConstr(self.y[l][n][p] <= self.y[l - 1][parent][p],
                                         name="constr 10 {} {} {}".format(l, n, p))

            # 11 12
            for l in range(1, self.D + 1):
                for n in range(1, 2 ** l + 1):
                    for p in range(self.n):
                        for f in range(self.nf):
                            for c in self.c[f]:
                                if self.g[l][n] != -1 and X.at[p, "f{}".format(f)] <= c:
                                    self.m.addConstr(self.y[l][n][p] <= 1 - self.x[l - 1][self.g[l][n]][f][c],
                                                     name="constr 11 {} {} {} {} {}".format(l, n, p, f, c))
                                elif self.h[l][n] != -1 and X.at[p, "f{}".format(f)] > c:
                                    self.m.addConstr(self.y[l][n][p] <= 1 - self.x[l - 1][self.h[l][n]][f][c],
                                                     name="constr 12 {} {} {} {} {}".format(l, n, p, f, c))

            self.m.update()

            ## Set objective
            self.m.setObjective(
                gp.quicksum([self.beta * self.missings[n] for n in range(1, self.L + 1)]) + gp.quicksum(
                    [X.at[p, "r{}".format(a)] * self.w[p][n][a] for a in self.Ks for p in range(self.n) for n in
                     range(1, self.L + 1)]), GRB.MINIMIZE)

            self.m.update()
        except Exception as e:
            raise (e)

    def solve(self):
        try:
            self.m.params.Threads = 4
            self.m.params.TimeLimit = self.time_limit
            self.m.printStats()
            self.m.optimize()
            self.m.printQuality()


        except Exception as e:
            raise (e)

    def predict(self, X):
        X = self.normalize(X)
        n = X.shape[0]
        y = []
        for i in range(n):
            x = X.iloc[i, :]
            t = 1
            for d in range(self.D):
                a = np.dot([self.a[j][t - 1].x for j in range(self.p)], x)
                if a + 1e-6 >= self.b[t - 1].x:
                    t = t * 2 + 1
                else:
                    t = t * 2
            for k in range(self.K):
                if self.c[k][t - self.B - 1].x > 0.5:
                    y.append(self.Ks[k])
                    break
            if len(y) < i + 1:
                y.append(self.Ks[0])
        return y

    def plot(self):
        for v in self.m.getVars():
            if v.VarName.startswith("x") or v.VarName.startswith("z"):
                print(f"{v.VarName} = {v.X}")
