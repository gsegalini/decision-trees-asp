import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

public class ASLib {

    private static final int[] bin_values = new int[]{2, 3, 5, 7, 10};
    private final String DIRECTORY;

    private String metric;

    private final Map<String, FeatureStep> steps = new HashMap<>();

    private final List<String> features_in_order = new ArrayList<>();

    private final List<Double> features_cost_in_order = new ArrayList<>();

    public ASLib(String DIRECTORY) {
        this.DIRECTORY = DIRECTORY;
    }

    private List<String> readRuntimes(AlgorithmCollection collection, Dataset set) throws IOException {
        List<String> bench_names = new ArrayList<>();
        String strCurrentLine;
        String runtimes = "algorithm_runs.arff";
        BufferedReader objReader = new BufferedReader(new FileReader(DIRECTORY + "/" + runtimes));
        int id = 0;
        boolean data_started = false;
        Map<String, Integer> attributes = new HashMap<>();
        Map<String, ASLibInstance> instances = new HashMap<>();
        while ((strCurrentLine = objReader.readLine()) != null) {
            if (strCurrentLine.startsWith("%")) {
                // comment
            } else if (data_started) {

                String[] splitted = strCurrentLine.split(",");
                String inst_name = splitted[attributes.get("instance_id")];
                String alg = splitted[attributes.get("algorithm")];
                double runtime = Double.parseDouble(splitted[attributes.get(this.metric)]);
                String status = splitted[attributes.get("runstatus")];
                if (!this.metric.equalsIgnoreCase("PAR10") && !status.equals("ok")) runtime *= 10;
                if (!instances.containsKey(inst_name)) {
                    ASLibInstance tmp = new ASLibInstance();
                    tmp.name = inst_name;
                    instances.put(inst_name, tmp);
                }
                ASLibInstance instance = instances.get(inst_name);
                int alg_id = collection.addAlgorithm(alg);
                instance.addRuntimeList(alg_id, runtime);

            } else if (strCurrentLine.toUpperCase().startsWith("@ATTRIBUTE")) {
                // save all attributes
                String[] splitted = strCurrentLine.split("\\s+");
                String name = splitted[1];
                attributes.put(name, id++);
                if (name.equals("runtime") || name.equalsIgnoreCase("PAR10")) {
                    this.metric = name;
                }
            } else if (strCurrentLine.toUpperCase().startsWith("@DATA")) {
                data_started = true;
            }
        }

        for (ASLibInstance instance : instances.values()) {
            instance.convertList();
            if (!instance.allTimeouts()) {
                bench_names.add(instance.name);
                set.addInstance(instance);
            }
        }
        return bench_names;
    }

    private void handleASlib(Dataset set, List<String> instanceNames) throws IOException {
        String features = "feature_values.arff";
        BufferedReader objReader = new BufferedReader(new FileReader(DIRECTORY + "/" + features), 67108864);
        String strCurrentLine;

        Map<String, Integer> attributes = new HashMap<>();
        boolean data_started = false;
        int id = 0;
        while ((strCurrentLine = objReader.readLine()) != null) {

            if (strCurrentLine.startsWith("%")) {
                // comment
            } else if (data_started) {
                String[] splitted = strCurrentLine.split(",");
                String inst_name = splitted[attributes.get("instance_id")];
                int repetition = Integer.parseInt(splitted[attributes.get("repetition")]);
                // System.out.println("starting instance " + index + " " + inst_name);

                if (!instanceNames.contains(inst_name)) {
                    // System.out.println("skipping instance " + splitted[0]);
                    continue;
                }

                // extract features and add them. NOT BINARIZED YET
                ASLibInstance instance = set.getASLibByName(inst_name);
                if (repetition <= 1) {
                    // multiple repetitions for stochastic features, for now do nothing
                    instance.features = new double[splitted.length - 2];


                    for (int i = 2; i < splitted.length; i++) {
                        if (splitted[i].equals("?")) {
                            // remove this instance
                            set.removeInstance(instance);
                            break;

                        } else {
                            double v = Double.parseDouble(splitted[i]);
                            instance.addFeature(v);
                        }
                    }
                }
            } else if (strCurrentLine.toUpperCase().startsWith("@ATTRIBUTE")) {
                String[] splitted = strCurrentLine.split("\\s+");
                String name = splitted[1];
                attributes.put(name, id++);
                if (!name.equalsIgnoreCase("instance_id") && !name.equalsIgnoreCase("repetition")) {
                    features_in_order.add(name);
                }
            } else if (strCurrentLine.toUpperCase().startsWith("@DATA")) {
                data_started = true;
            }

        }

    }


    private void readCosts() throws IOException {

        String features_costs = "feature_costs.arff";
        BufferedReader objReader = new BufferedReader(new FileReader(DIRECTORY + "/" + features_costs), 67108864);
        String strCurrentLine;
        boolean data_started = false;
        List<FeatureStep> steps = new ArrayList<>();
        Map<String, Integer> attributes = new HashMap<>();
        int id = 0;

        while ((strCurrentLine = objReader.readLine()) != null) {

            if (strCurrentLine.startsWith("%")) {
                // comment
            } else if (data_started) {
                String[] splitted = strCurrentLine.split(",");
                int repetition = Integer.parseInt(splitted[attributes.get("repetition")]);

                if (repetition <= 1) {
                    // multiple repetitions for stochastic features, for now do nothing

                    for (int i = 2; i < splitted.length; i++) {
                        if (!splitted[i].equals("?")) {
                            double v = Double.parseDouble(splitted[i]);
                            steps.get(i - 2).addCost(v);
                        }
                    }
                }
            } else if (strCurrentLine.toUpperCase().startsWith("@ATTRIBUTE")) {
                String[] splitted = strCurrentLine.split("\\s+");
                String name = splitted[1];
                attributes.put(name, id++);
                if (!name.equalsIgnoreCase("instance_id") && !name.equalsIgnoreCase("repetition")) {
                    // this is a step
                    steps.add(this.steps.get(name));
                }
            } else if (strCurrentLine.toUpperCase().startsWith("@DATA")) {
                data_started = true;
            }
        }
        System.out.println("completed reading costs");
    }


    private void readDescription() throws IOException {
        String description = "description.txt";
        InputStream inputStream = new FileInputStream(DIRECTORY + "/" + description);

        Yaml yaml = new Yaml();
        Map<String, Object> data = (Map<String, Object>) yaml.load(inputStream);
        ;
        Map<String, Object> stepsMap = (Map<String, Object>) data.get("feature_steps");
        for (String s : stepsMap.keySet()) {
            FeatureStep step;
            if (!this.steps.containsKey(s)) {
                step = new FeatureStep(s);
                this.steps.put(s, step);
            } else {
                step = this.steps.get(s);
            }
            Map<String, Object> step_yaml = ((Map<String, Object>) stepsMap.get(s));
            List<String> provides = (List<String>) step_yaml.get("provides");
            step.setProvides(provides);

            // requirements
            if (!step_yaml.containsKey("requires")) continue;
            List<String> requires = (List<String>) step_yaml.get("requires");
            for (String r_key : requires) {
                FeatureStep requirement;
                if (!this.steps.containsKey(r_key)) {
                    requirement = new FeatureStep(r_key);
                    this.steps.put(r_key, requirement);
                } else {
                    requirement = this.steps.get(r_key);
                }
                step.addRequirement(requirement);
            }

        }
        System.out.println("built requirements graph");
    }


    public static void readAll(String names_file) throws IOException {
        BufferedReader objReader = new BufferedReader(new FileReader(names_file), 67108864);
        String strCurrentLine;

        while ((strCurrentLine = objReader.readLine()) != null) {
            System.out.println("Doing scenario: " + strCurrentLine);
            ASLib scenario = new ASLib(strCurrentLine);
            Dataset set = new Dataset(scenario.DIRECTORY, scenario.DIRECTORY);
            AlgorithmCollection collection = new AlgorithmCollection();


            scenario.readDescription();

            List<String> names = scenario.readRuntimes(collection, set);

            scenario.handleASlib(set, names);
            boolean costs = false;
            try {
                scenario.readCosts();
                costs = true;

            } catch (FileNotFoundException e) {
                System.out.println("no feature costs, skipping");
            }

            // set.stats();
            set.subtractBest();

            if (costs) {
                scenario.makeFeatureCosts();
            }

            for (int b : bin_values) {
                if (costs ) scenario.writeFeatureCosts(b);
                set.setN_bins(b);
                set.binarizeAll(b);
                set.toCsv();

            }


        }
    }

    private void writeFeatureCosts(int bins) throws IOException {
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(DIRECTORY + "/" + DIRECTORY + "-" + bins + "-bins-costs.txt"));
        for (int i = 0; i < this.features_cost_in_order.size(); i++) {
            double d = this.features_cost_in_order.get(i);
            fileWriter.write((d + System.lineSeparator()).repeat(bins - 2));
            fileWriter.write(String.valueOf(d));
            if (i < this.features_cost_in_order.size() - 1)
                fileWriter.newLine();
        }

        fileWriter.close();
    }

    private void makeFeatureCosts() {
        // go through features in order

        for (String feat_name : this.features_in_order) {
            FeatureStep step = null;

            for (FeatureStep tmp : this.steps.values()) {
                if (tmp.provides.contains(feat_name)) {
                    step = tmp;
                    break;
                }
            }
            if (step == null) throw new RuntimeException("expected to find step for feature: " + feat_name);

            double cost = step.recursiveCost();
            this.features_cost_in_order.add(cost);

        }
    }

}
