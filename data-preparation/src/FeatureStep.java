import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FeatureStep implements Comparable<FeatureStep>{

    String name;

    int id;

    List<FeatureStep> requires = new ArrayList<>();

    List<String> provides = new ArrayList<>();

    List<Double> costs = new ArrayList<>();

    private Double avg_cost = null;

    public void addRequirement(FeatureStep requirement) {
        this.requires.add(requirement);
    }

    public FeatureStep(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureStep that = (FeatureStep) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public void setProvides(List<String> provides) {
        this.provides = provides;
    }

    public void addCost(double cost) {
        this.costs.add(cost);
    }

    public double getAverageCost() {
        if (this.avg_cost == null) {
            this.avg_cost = Utils.mean(this.costs);
        }
        return this.avg_cost;

    }

    public double recursiveCost() {
        double cost = this.getAverageCost();
        for (FeatureStep parent: requires) {
            cost += parent.recursiveCost();
        }
        return cost;
    }


    @Override
    public int compareTo(FeatureStep featureStep) {
        return Integer.compare(this.id, featureStep.id);
    }

    public List<Integer> requirementsIds() {
        return this.requires.stream().mapToInt(i -> i.id).boxed().collect(Collectors.toList());
    }
}
