# Import seaborn
import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns

base_folder = "./plots/"

sns.set_style({'font.family': 'Times New Roman'})
sns.set_style(style='darkgrid')
sns.color_palette("mako")
sns.set(font_scale=0.4)


def exp1():
    df = pd.read_csv("./exp1/exp1-results.csv", sep=",")

    ax = sns.lineplot(
        data=df,
        x="depth",
        y="time",
        hue="method",
        style="train_data",
        markers=True,
        markersize=8,
        legend=True,
    )

    ax.set(xlabel='Depth of the tree',
           ylabel='Runtime [s]',  # Runtime [s]
           title='Streed vs MIP')  #

    ax.set_xticks([0, 1, 2, 3, 4, 5, 6])

    ax.set_yscale("log")
    ax.set_ylim(0.0001, 3400)
    handles, labels = ax.get_legend_handles_labels()

    plt.savefig(base_folder + "exp1.svg")

    del ax
    plt.clf()


def exp2():
    df = pd.read_csv("./exp2/exp2-results.csv", sep=",")

    df_instances = df[df["extra_info"].str.contains("instances")]
    df_features = df[df["extra_info"].str.contains("features")]
    df_labels = df[df["extra_info"].str.contains("labels")]

    df_instances["extra_info"] = df_instances["extra_info"].str[:-10].astype(int)
    df_features["extra_info"] = df_features["extra_info"].str[:-9].astype(int)
    df_labels["extra_info"] = df_labels["extra_info"].str[:-7].astype(int)

    ax = sns.lineplot(
        data=df_instances,
        x="extra_info",
        y="time",
        markers=True,
        markersize=8,
        legend=True,
    )

    ax.set(xlabel='Number of instances',
           ylabel='Runtime [s]',  # Runtime [s]
           title='Runtime with changing instances')  #

    plt.savefig(base_folder + "exp2-instances.svg")

    del ax
    plt.clf()

    ax = sns.lineplot(
        data=df_features,
        x="extra_info",
        y="time",
        markers=True,
        markersize=8,
        legend=True,
    )

    ax.set(xlabel='Number of features',
           ylabel='Runtime [s]',  # Runtime [s]
           title='Runtime with changing features')  #

    plt.savefig(base_folder + "exp2-features.svg")

    del ax
    plt.clf()

    ax = sns.lineplot(
        data=df_labels,
        x="extra_info",
        y="time",
        markers=True,
        markersize=8,
        legend=True,
    )

    ax.set(xlabel='Number of labels',
           ylabel='Runtime [s]',  # Runtime [s]
           title='Runtime with changing labels')  #

    plt.savefig(base_folder + "exp2-labels.svg")

    del ax
    plt.clf()


def exp4():
    df = pd.read_csv("./exp4/exp4-results.csv", sep=",")

    ax = sns.catplot(
        data=df,
        x="scenario",
        y="mcp",
        hue="model",
        kind="bar",
        height=7,
        aspect=4
    )

    ax.set(xlabel='Dataset',
           ylabel='Relative PAR10-MCP [s/s] (lower is better)',  # Runtime [s]
           title='Streed vs llama')  #
    ax.despine(left=True)
    # ax.set(ylim=(0, 3))

    plt.savefig(base_folder + "exp4.svg")

    del ax
    plt.clf()


if __name__ == "__main__":
    exp4()
