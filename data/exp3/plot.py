# Import seaborn
import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns

plt.figure(figsize=(6.5, 4))

sns.set_style({'font.family': 'Times New Roman'})
sns.set_style(style='darkgrid')
sns.color_palette("mako")
# Load an example dataset

legend_font = 18

df = pd.read_csv("exp3-results.csv", sep=",")

df = df[df["metric_train"] >= 0.0]

df["metric_test"] = df.groupby(['train_data', 'depth'])['metric_test'].transform('sum')

df = df.drop_duplicates(subset=['train_data', 'depth'])

df_potass = df[df["train_data"].str.contains("POTASS")]

df_sat = df[df["train_data"].str.contains("SAT")]

# Create a visualization
# POTASSCO metric
ax = sns.lineplot(
    data=df_potass,
    x="depth",
    y="metric_test",
    hue="bins",
    style="bins",
    markers=True,
    markersize=8,
    legend=True,
    estimator=sum
    # errorbar="pi"
)

ax.set(xlabel='Depth of the tree',
       ylabel='MCP',  # Runtime [s]
       title='POTASS mcp')  #

ax.set_xticks([0, 1, 2, 3, 4, 5])

# ax.set_ylim(0.001, mx)
handles, labels = ax.get_legend_handles_labels()

plt.tight_layout()
plt.savefig("potass-mcp.svg")

del ax
plt.clf()

# POTASSCO runtime
ax = sns.lineplot(
    data=df_potass,
    x="depth",
    y="time",
    hue="bins",
    style="bins",
    markers=True,
    markersize=8,
    legend=True,
    # errorbar="pi"
)

ax.set(xlabel='Depth of the tree',
       ylabel='Runtime [s]',  # Runtime [s]
       title='Runtime for different depths POTASS')  #
ax.set_yscale("log")
ax.set_xticks([0, 1, 2, 3, 4])
# ax.set_ylim(0.001, mx)
handles, labels = ax.get_legend_handles_labels()

plt.tight_layout()
plt.savefig("potass-runtime.svg")

del ax
plt.clf()

# SAT20 metric
ax = sns.lineplot(
    data=df_sat,
    x="depth",
    y="metric_test",
    hue="bins",
    style="bins",
    markers=True,
    markersize=8,
    legend=True,
    estimator=sum
    # errorbar="pi"
)

ax.set(xlabel='Depth of the tree',
       ylabel='MCP',  # Runtime [s]
       title='SAT20 mcp')  #
ax.set_xticks([0, 1, 2, 3, 4])
# ax.set_ylim(0.001, mx)
handles, labels = ax.get_legend_handles_labels()

plt.tight_layout()
plt.savefig("sat-mcp.svg")

del ax
plt.clf()

# SAT20 runtime
ax = sns.lineplot(
    data=df_sat,
    x="depth",
    y="time",
    hue="bins",
    style="bins",
    markers=True,
    markersize=8,
    legend=True,
    # errorbar="pi"
)

ax.set(xlabel='Depth of the tree',
       ylabel='Runtime [s]',  # Runtime [s]
       title='Runtime for different depths SAT20')  #
ax.set_yscale("log")
ax.set_xticks([0, 1, 2, 3, 4])
# ax.set_ylim(0.001, mx)
handles, labels = ax.get_legend_handles_labels()

plt.tight_layout()
plt.savefig("sat-runtime.svg")
