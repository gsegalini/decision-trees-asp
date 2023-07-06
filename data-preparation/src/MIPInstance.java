public class MIPInstance extends Instance {

    /**
     * 1 -> cols
     * 2-4 -> n types
     * 5-10->obj coefficients variations
     * 11-12 -> non zeros and density
     * 13 -> rows
     * 14-25 -> number of constraints of types
     * 26-31 -> RHS features
     * 32-37 -> variation of coefficients
     */
    public final static int NUM_FEATURES = 37;

    public String name;

    @Override
    public int numFeatures() {
        return NUM_FEATURES;
    }

    public MIPInstance() {
        this.features = new double[NUM_FEATURES];
    }


}
