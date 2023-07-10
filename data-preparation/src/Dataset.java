import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Dataset {

    private final String directory_prefix;
    private final List<Instance> instances = new ArrayList<>();
    private final String filename;

    private Set<String> filter;

    private boolean whitelist = false;
    private int n_bins;

    private int cv;

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
        File file = new File(this.directory() + this.filename() + prefix + ".txt");
        file.getParentFile().mkdirs();
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
        for (Instance inst : this.instances) {
            if (filter != null && inst instanceof ASLibInstance) {
                ASLibInstance tmp = (ASLibInstance) inst;
                boolean in = filter.contains(tmp.name);
                if (whitelist ^ in) continue;
            }
            fileWriter.write(inst.toString());
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

    public int numlabels() {
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
        return this.directory_prefix + "/" + this.n_bins + "-bins/" + this.cv + "-cv/";
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

    }
}
