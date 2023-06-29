import java.util.HashMap;
import java.util.Map;

public class AlgorithmCollection {


    private final Map<String, Integer> map;

    public AlgorithmCollection() {
        this.map = new HashMap<>();
    }

    public int addAlgorithm(String alg) {
        if (map.containsKey(alg)) return map.get(alg);
        int key = map.size();
        map.put(alg, key);
        return key;
    }

    public int getAlgId(String alg) {
        return map.get(alg);
    }


    public void fromString(String input) {
        String[] splitted = input.split(",");
        for (int i = 1;i < splitted.length;i++){
            this.addAlgorithm(splitted[i]);
        }
    }

    public int size() {
        return this.map.size();
    }



}
