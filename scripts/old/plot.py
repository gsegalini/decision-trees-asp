# Import seaborn
import seaborn as sns
import matplotlib.pyplot as plt
import pandas as pd
plt.figure(figsize=(6.5, 4))

sns.set_style({'font.family': 'Times New Roman'})
sns.set_style(style='darkgrid')
sns.color_palette("mako")
# Load an example dataset
data = ["data21", "data22"]

oneto8 = list(range(1,9))
oneto5 = list(range(1,6))

legend_font = 18

for dt in data:
    print(dt)
    filename = dt + ".csv"
    filename_prune = dt + "-prune.csv"
    filename_bins = dt + "-bins.csv"
    df = pd.read_csv(filename, sep=" ")


    name = ""
    mx = -1
    if "21" in dt:
        name = "2021"
        mx = 750
    else:
        name = "2022"
        mx = 1200

    # Create a visualization
    ax = sns.lineplot(
        data=df,
        x="depth", 
        y="runtime",
        hue="name",
        style="name",
        markers=True,
        markersize=8,
        legend=True,
        # errorbar="pi"
    )

    ax.set(xlabel='Depth of the tree',
        ylabel='Runtime [s]', # Runtime [s]
        title='Runtime for different depths with MaxSAT {} dataset'.format(name)) # 
    ax.set_yscale("log")

    ax.set_ylim(0.001, mx)
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles=handles, labels=['This work', "Boas et al."], title="Legend", fontsize=legend_font)

    plt.tight_layout()
    plt.xticks(oneto8)
    fname = 'maxsat{}.svg'.format(dt[-2:])   
    plt.savefig(fname=fname, bbox_inches='tight')
    
    del df
    plt.clf()

    df = pd.read_csv(filename_prune, sep=" ")

    # Create a visualization
    ax = sns.lineplot(
        data=df,
        x="depth", 
        y="calls",
        hue="name",
        style="name",
        markers=True,
        # markersize=8,
        legend=True
    )

    ax.set(xlabel='Depth of the tree',
        ylabel='Number of terminal calls', # Runtime [s]
        title='Number of calls to terminal solver for different depths with MaxSAT {} dataset'.format(name)) # 
    # ax.set_yscale("log")
    if name == "2021":
        mx = 2250000
    else:
        mx = 2200000

    ax.set_ylim(0.8, mx)
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles=handles, labels=['Using bounds-based pruning', "Not using bounds-based pruning"], title="Legend", fontsize=legend_font)
    
    plt.xticks([1,2,3,4,5,6,7])
    plt.tight_layout()
    fname = 'maxsat{}-prune.svg'.format(dt[-2:])    
    plt.savefig(fname=fname, bbox_inches='tight')

    del df
    plt.clf()
    
    df = pd.read_csv(filename_bins, sep=" ")

    # Create a visualization
    ax = sns.lineplot(
        data=df,
        x="depth", 
        y="runtime",
        hue="data",
        style="data",
        markers=True,
        # markersize=8,
        legend=True
    )

    ax.set(xlabel='Depth of the tree',
        ylabel='Runtime [s]', # Runtime [s]
        title='Runtime for different depths and k-bins binarization with MaxSAT {} dataset'.format(name)) # 
    ax.set_yscale("log")
    if name == "2021":
        mx = 250
    else:
        mx = 350 

    ax.set_ylim(0.001, mx)
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles=handles, labels=['3-Bins', "5-Bins", "2-Bins"], title="Legend", fontsize=legend_font)
    plt.xticks(oneto5)
    plt.tight_layout()
    fname = 'maxsat{}-bins.svg'.format(dt[-2:])    
    plt.savefig(fname=fname, bbox_inches='tight')

    plt.clf()
    
    ax = sns.lineplot(
        data=df,
        x="depth", 
        y="metric_tr",
        hue="data",
        style="data",
        markers=True,
        # markersize=8,
        legend=True
    )

    ax.set(xlabel='Depth of the tree',
        ylabel='Optimized metric (total runtime) [s]', # Runtime [s]
        title='Total runtime of instances in dataset with associated algorithm for different depths and k-bins binarization with MaxSAT {} dataset'.format(name)) # 
    # ax.set_yscale("log")

    # ax.set_ylim(0.001, mx)
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles=handles, labels=['3-Bins', "5-Bins", "2-Bins"], title="Legend", fontsize=legend_font)
    plt.xticks(oneto5)
    plt.tight_layout()
    fname = 'maxsat{}-bins-train.svg'.format(dt[-2:])    
    plt.savefig(fname=fname, bbox_inches='tight')

    plt.clf()
    del df
    


df = pd.read_csv("data-metric-split-50-25.csv", sep=" ")

# Create a visualization
ax = sns.lineplot(
    data=df,
    x="depth", 
    y="metric_ts",
    hue="data",
    style="data",
    markers=True,
    legend=True
)

ax.set(xlabel='Depth of the tree',
    ylabel='Optimized metric (total runtime) [s]', # Runtime [s]
    title='Total runtime of instances in test dataset with associated algorithm') # 
# ax.set_yscale("log")

ax.set_ylim(600, 18000)
handles, labels = ax.get_legend_handles_labels()
ax.legend(handles=handles, labels=['MaxSAT 2021', "MaxSAT 2022"], title="Legend", fontsize=legend_font)

plt.xticks(oneto8)
plt.tight_layout()
fname = "metric-ts.svg"   
plt.savefig(fname=fname, bbox_inches='tight')
plt.clf()

# Create a visualization
ax = sns.lineplot(
    data=df,
    x="depth", 
    y="metric_tr",
    hue="data",
    style="data",
    markers=True,
    legend=True
)

ax.set(xlabel='Depth of the tree',
    ylabel='Optimized metric (total runtime) [s]', # Runtime [s]
    title='Total runtime of instances in train dataset with associated algorithm') # 
# ax.set_yscale("log")

ax.set_ylim(8000, 85000)
handles, labels = ax.get_legend_handles_labels()
ax.legend(handles=handles, labels=['MaxSAT 2021', "MaxSAT 2022"], title="Legend", fontsize=15)

plt.xticks(oneto8)
plt.tight_layout()
fname = "metric-tr.svg"   
plt.savefig(fname=fname, bbox_inches='tight')
