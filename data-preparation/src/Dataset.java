import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Dataset implements Cloneable {

    private final String directory_prefix;
    private List<Instance> instances = new ArrayList<>();
    private final String filename;

    private Set<String> filter;

    private boolean whitelist = false;
    private int n_bins;

    private int cv;

    private int amount = -1;

    private int features_amount = -1;

    private int labels_amount = -1;

    public Dataset(String output) {
        this.filename = output;
        this.directory_prefix = ".";
    }

    public Dataset(String directory_prefix, String filename) {
        this.directory_prefix = directory_prefix;
        this.filename = filename;
    }

    public void addInstance(Instance instance) {
        instances.add(instance);
    }

    public void toCsv() throws IOException {
        String prefix = this.whitelist ? "-test" : "";

        if (this.amount > 0) {
            prefix = "-" + this.amount + "-size";
        } else if (this.features_amount > 0) {
            prefix = "-" + this.features_amount + "-features";
        } else if (this.labels_amount > 0) {
            prefix = "-" + this.labels_amount + "-labels";
        }

        File file = new File(this.directory() + this.filename() + prefix + ".txt");
        file.getParentFile().mkdirs();
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
        int count = 0;

        for (Instance inst : this.instances) {
            if (amount > 0 && count >= amount) break;
            if (filter != null && inst instanceof ASLibInstance) {
                ASLibInstance tmp = (ASLibInstance) inst;
                boolean in = filter.contains(tmp.name);
                if (whitelist ^ in) continue;
            }
            fileWriter.write(inst.toString(this.features_amount));
            count++;
        }

        fileWriter.close();
    }

    public void binarizeFeature(int f_index, int bins) throws IOException {
        int new_features_count = bins - 1;
        double[] values = new double[instances.size()];
        {
            int i = 0;
            for (Instance inst : instances) {
                values[i++] = inst.features[f_index];
            }
        }
        Arrays.sort(values);
        double[] thresholds = new double[new_features_count];
        for (int i = 0; i < new_features_count; i++) {
            thresholds[i] = values[(i + 1) * (values.length / bins)];
        }
        int new_f_index = new_features_count * f_index;
        for (Instance inst : instances) {
            int length = inst.getN_features() * new_features_count;
            if (inst.bin_features == null || inst.bin_features.length != length) {
                inst.bin_features = new double[length];
            }
            for (int delta_i = 0; delta_i < new_features_count; delta_i++) {
                if (inst.features[f_index] < thresholds[delta_i]) {
                    inst.bin_features[new_f_index + delta_i] = 1;
                } else {
                    inst.bin_features[new_f_index + delta_i] = 0;
                }
            }
        }
        File file = new File(this.directory() + this.filename() + "_info.txt");
        file.getParentFile().mkdirs();
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file, true));
        fileWriter.write("feature index: " + f_index + " thresholds: " + Arrays.toString(thresholds));
        fileWriter.newLine();
        fileWriter.close();
    }

    public Instance getInstance(int index) {
        return instances.get(index);
    }

    public int numLabels() {
        return this.instances.get(0).runtimes.length;
    }

    public void subtractBest() {
        for (Instance i : instances) {
            i.subtractBest();
        }
    }

    public void binarizeAll(int bins) throws IOException {
        Instance example = this.instances.get(0);
        for (int f = 0; f < example.numFeatures(); f++) {
            this.binarizeFeature(f, bins);
        }
    }

    public void stats() {
        Instance example = this.instances.get(0);
        for (int f = 0; f < example.numFeatures(); f++) {
            int ix = 0;
            double[] list = new double[this.instances.size()];
            for (Instance i : instances) {
                list[ix++] = i.features[f];
            }
            double mean = Utils.mean(list);
            double min = Utils.min(list);
            double max = Utils.max(list);
            double sd = Math.sqrt(Utils.variance(list, null));
            System.out.println("Feature: " + f);
            System.out.println("min: " + min + " max: " + max + " avg: " + mean + " sd: " + sd);
        }
    }

    public ASLibInstance getASLibByName(String name) {
        for (Instance i : this.instances) {
            if (i instanceof ASLibInstance) {
                ASLibInstance tmp = (ASLibInstance) i;
                if (tmp.name.equals(name)) return tmp;
            }
        }
        return null;
    }

    public void removeInstance(Instance instance) {
        if (!this.instances.remove(instance)) {
            throw new RuntimeException("tried to remove instance that was not there, should not happen");
        }
    }

    public void setN_bins(int n_bins) {
        this.n_bins = n_bins;
    }

    private String filename() {
        return this.filename + "-" + this.n_bins + "-bins";
    }

    private String directory() {
        String p1 = this.directory_prefix + "/" + this.n_bins + "-bins/";
        if (this.cv > 0)
            return p1 + this.cv + "-cv/";
        else if (this.amount > 0)
            return p1 + "subset-instances/";
        else if (this.features_amount > 0)
            return p1 + "subset-features/";
        else if (this.labels_amount > 0)
            return p1 + "subset-labels/";
        else
            return p1;
    }

    public void setCv(int cv) {
        this.cv = cv;
    }

    public void toCsv(List<String> test_instances) throws IOException {
        // set filter to train, write

        this.filter = new HashSet<>(test_instances);
        this.whitelist = false;
        this.toCsv();
        // set filter to test, write
        this.whitelist = true;
        this.toCsv();

        this.whitelist = false; // reset to original

    }

    public void instancesSubsetsCSV() throws IOException {
        int size = this.instances.size();
        for (int a = size / 10; a < size; a += size / 10) {
            this.amount = a;
            this.toCsv();
        }
        this.amount = size;
        this.toCsv();
        this.amount = -1;

    }

    public void featuresSubsetsCSV() throws IOException {
        int feature_size = this.instances.get(0).bin_features.length;
        for (int f = feature_size / 10; f < feature_size; f += feature_size / 10) {
            this.features_amount = f;
            this.toCsv();
        }
        this.features_amount = feature_size;
        this.toCsv();
        this.features_amount = -1;

    }

    // this one is a pain
    public void labelsSubsetCSV() throws IOException {

        int n_labels = this.instances.get(0).runtimes.length;
        Dataset clone = this.clone();

        for (int n = n_labels / 10; n < n_labels; n += n_labels / 10) {
            // rewrite all instances to only use n labels
            assert clone != null;
            for (Instance i : clone.instances) {
                i.remakeRuntimes(n);
            }
            clone.labels_amount = n;
            clone.toCsv();
            clone.instances = new ArrayList<>();
            for (Instance i : this.instances) {
                clone.instances.add(i.clone());
            }
        }
        this.labels_amount = n_labels;
        this.toCsv();
        this.labels_amount = -1;

    }

    @Override
    public Dataset clone() {
        try {
            Dataset clone = (Dataset) super.clone();
            clone.instances = new ArrayList<>();
            for (int i = 0; i < this.instances.size(); i++) {
                Instance inst = this.instances.get(i);
                clone.instances.add(inst.clone());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void writeStats() throws IOException {

        File file = new File(this.directory() + this.filename() + "_info.txt");
        file.getParentFile().mkdirs();
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file, true));
        fileWriter.write("Num labels: " + this.numLabels());
        fileWriter.newLine();
        fileWriter.close();
    }

    public void removeSames() {
        // for each feature
        // get values of feature over each instance
        // if 2 are the same, remove one, remake bin_features
        Map<Integer, List<Double>> f_values = new HashMap<>();
        int n_features;
        {
            Instance example = this.instances.get(0);
            n_features = example.bin_features.length;

            for (int f = 0; f < n_features; f++) {
                List<Double> values = new ArrayList<>(instances.size());
                for (Instance i : this.instances) {
                    double v = i.bin_features[f];
                    values.add(v);
                }
                f_values.put(f, values);
            }
        }
        List<Integer> to_remove = new ArrayList<>();
        for (int f1 = 0; f1 < n_features; f1++) {
            if (to_remove.contains(f1)) continue;
            for (int f2 = f1 + 1; f2 < n_features; f2++) {
                if (f_values.get(f1).equals(f_values.get(f2))) {
                    to_remove.add(f2);
                }
            }
        }

        if (to_remove.isEmpty()) return;
        System.out.println("removing " + to_remove.size() + " features");

        for (Instance i : instances) {
            int idx = 0;
            double[] bin_f = new double[i.bin_features.length - to_remove.size()];
            for (int f = 0; f < n_features; f++) {
                if (to_remove.contains(f)) continue;
                bin_f[idx++] = i.bin_features[f];
            }
            i.bin_features = bin_f;
        }

    }
}
