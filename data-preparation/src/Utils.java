import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static double mean(List<Double> list) {
        assert list.size() > 0;
        return list.stream()
                .mapToDouble(a -> a)
                .average().getAsDouble();
    }

    private static double variance(List<Double> list, Double mean) {
        if (mean == null)
            mean = mean(list);
        double variance = 0;
        for (double d : list)
            variance += (d - mean) * (d - mean);
        return variance / list.size();
    }

    public static double mean(double[] arr) {
        return mean(Arrays.stream(arr)
                .boxed()
                .collect(Collectors.toList()));
    }

    public static double variance(double[] arr, Double mean) {
        return variance(Arrays.stream(arr)
                .boxed()
                .collect(Collectors.toList()), mean);
    }

    public static double min(double[] arr) {
        double m = Double.MAX_VALUE;
        for (double d : arr) {
            m = Math.min(d, m);
        }
        return m;
    }

    public static double max(double[] arr) {
        double m = Double.MIN_VALUE;
        for (double d : arr) {
            m = Math.max(d, m);
        }
        return m;
    }

    public static void addMeanVarMinMaxEntropy(Instance instance, double[] arr) {
        double mean = Utils.mean(arr);
        instance.addFeature(mean);
        instance.addFeature(Utils.variance(arr, mean));
        instance.addFeature(Utils.min(arr));
        instance.addFeature(Utils.max(arr));
        instance.addFeature(Utils.entropy(arr));
    }

    private static double entropy(double[] arr) {
        // assume not normalized
        double sum = Arrays.stream(arr)
                .sum();
        if (sum == 0) return 0;
        return -Arrays.stream(arr)
                .map((d) -> d / sum)
                .map((d) -> {
                    if (d == 0) return 0;
                    else return d * Math.log(d);
                })
                .sum();
    }
}
