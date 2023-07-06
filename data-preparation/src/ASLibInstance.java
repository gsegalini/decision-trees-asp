import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ASLibInstance extends Instance {

    /**
     * Variable number of features
     * and runtimes
     */

    private int num_features;
    public String name;

    private final List<Double> runtimes_list;

    public ASLibInstance() {
        runtimes_list = new ArrayList<>();
    } // create empty instance

    public void addRuntimeList(int id, double runtime) {
        runtimes_list.add(id, runtime);
    }

    public void convertList() {
        this.runtimes = new double[runtimes_list.size()];
        for (int i = 0; i < this.runtimes.length; i++) {
            this.addRuntime(i, runtimes_list.get(i));
        }
    }

    @Override
    public void addFeature(double f) {
        this.num_features++;
        super.addFeature(f);
    }

    @Override
    public int numFeatures() {
        return num_features;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASLibInstance instance = (ASLibInstance) o;
        return Objects.equals(name, instance.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
