import jdk.jshell.execution.Util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dataset {

    private final String directory_prefix;
    private final List<Instance> instances = new ArrayList<>();
    private String filename;

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
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(directory_prefix + "/" + this.filename + ".txt"));
        for (Instance inst : this.instances) {
            // if (inst.all0) continue; // skip timed out
            fileWriter.write(inst.toString());
        }
        fileWriter.close();
    }

    public void binarizeFeature(int f_index) throws IOException {
        double[] values = new double[instances.size()];
        {
            int i = 0;
            for (Instance inst : instances) {
                values[i++] = inst.features[f_index];
            }
        }
        Arrays.sort(values);
        double threshold = values[values.length / 2];
        for (Instance inst : instances) {
            if (inst.bin_features == null) {
                inst.bin_features = new double[inst.getN_features()];
            }
            inst.bin_features[f_index] = inst.features[f_index] < threshold ? 0.0 : 1.0;
        }
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(directory_prefix + "/" + this.filename + "_info.txt", true));
        fileWriter.write("feature index: " + f_index + " threshold: " + threshold);
        fileWriter.newLine();
        fileWriter.close();

    }


    public void binarizeFeature(int f_index, int bins) throws IOException {
        if (bins == 2) {
            binarizeFeature(f_index);
            return;
        }
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
            if (inst.bin_features == null) {
                inst.bin_features = new double[inst.getN_features() * new_features_count];
            }
            for (int delta_i = 0; delta_i < new_features_count; delta_i++) {
                if (inst.features[f_index] < thresholds[delta_i]) {
                    inst.bin_features[new_f_index + delta_i] = 1;
                } else {
                    inst.bin_features[new_f_index + delta_i] = 0;
                }
            }
        }

        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(directory_prefix + "/" + this.filename + "_info.txt", true));
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
}
