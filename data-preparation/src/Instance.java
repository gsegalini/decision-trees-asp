import java.util.List;

public abstract class Instance {

    protected double[] runtimes;

    protected double[] features;

    protected boolean all0 = false;

    protected double[] bin_features;

    private int n_features = 0;
    private int best_alg = -1;

    public void addRuntime(int alg, double runtime) {
        this.runtimes[alg] = runtime;
        if (best_alg == -1 || runtime < runtimes[best_alg]) {
            best_alg = alg;
        }
    }

    public void addFeature(int f_index, double value) {
        this.features[f_index] = value;
        ++n_features;
    }

    public void addFeature(double value) {
        this.features[n_features++] = value;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(best_alg);
        // extra data
        for (double runtime : runtimes) {
            builder.append(" ");
            builder.append(runtime);
        }
        if (bin_features == null) {
            throw new RuntimeException("features were not binarized");
        }
        for (Double d : bin_features) {
            builder.append(" ");
            builder.append(d.intValue());
        }
        builder.append("\n");
        return builder.toString();
    }

    public boolean allTimeouts() {
        double v0 = runtimes[0];
        if (v0 < 10) return false; // assume timeout > 10 seconds
        for (double v : runtimes) {
            if (v != v0) return false;
        }
        // System.out.println("removing instance");
        return true;
    }

    public void subtractBest() {
        double mx = Utils.min(runtimes);
        this.all0 = true;
        for (int i = 0; i < runtimes.length; i++) {
            runtimes[i] -= mx;
            if (runtimes[i] != 0) this.all0 = false;
        }
    }

    public int getN_features() {
        return n_features;
    }

    public abstract int numFeatures();


}
