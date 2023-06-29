public class SATInstance extends Instance {


    /**
     * Instantiates a new Sat instance.
     *  FEATURES:
     *  0 -> n_var
     *  1 -> n_clauses
     *  2-4 -> ratios v/c (v/c)^2 (v/c  )^3
     *  5-7 -> reciprocals of above
     *  balance features
     *  8-12 -> fraction of positive to total per clause, mean. var, min, max, entropy
     *  13-15 -> fraction of unary, binary, ternary clauses
     *  16-20 -> fraction of positive occurrence for each variable, mean, var, min, max, entropy
     *  horn features
     *  21 -> fraction of horn clauses
     *  22-26 -> occurrences in horn clauses per variables mean, var, min, max, entropy
     *  clauses length
     *  27-31 -> length of clauses, mean, var, min, max, entropy
     *  variable-clause graph TODO
     */

    public final static int NUM_FEATURES = 32;
    public SATInstance(int n_runtimes) {
        this.runtimes = new double[n_runtimes];
        this.features = new double[NUM_FEATURES];
    }

    @Override
    public int numFeatures() {
        return NUM_FEATURES;
    }

}
