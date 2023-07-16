import java.util.Arrays;

public abstract class Instance implements Cloneable{

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


    public void addFeature(double value) {
        this.features[n_features++] = value;
    }


    public String toString(int features_amount) {
        if (features_amount <= 0) features_amount = this.bin_features.length;
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
        for (int i = 0; i < features_amount; i++) {
            double d = this.bin_features[i];
            builder.append(" ");
            builder.append((int) d);
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


    public void remakeRuntimes(int amount) {
        this.runtimes = Arrays.copyOf(this.runtimes, amount);
        this.subtractBest();
        this.setBest();


    }

    private void setBest() {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < this.runtimes.length; i++) {
            if (this.runtimes[i] < min) {
                min = this.runtimes[i];
                this.best_alg = i;
            }
        }

    }

    @Override
    public Instance clone() {
        try {
            Instance clone = (Instance) super.clone();
            clone.runtimes = Arrays.copyOf(this.runtimes, this.runtimes.length);
            clone.features = Arrays.copyOf(this.features, this.features.length);
            clone.bin_features = Arrays.copyOf(this.bin_features, this.bin_features.length);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


}
