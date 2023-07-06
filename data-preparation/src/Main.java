import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private final static String DIR_PREFIX = "./instances/";

    public static void main(String[] args) throws IOException {
        Arguments arg = handleArguments(args);
        AlgorithmCollection collection = new AlgorithmCollection();
        Dataset set = new Dataset(arg.output);
        List<String> benchNames = null;
        switch (arg.type) {
            case "sat":
                benchNames = readRuntimesSat(arg.input_times, collection, set);
                break;
            case "gc":
                throw new RuntimeException("TODO");
            case "miplib":
                benchNames = readRuntimesBoas(arg.input_times, set);
                break;
            case "aslib":
                // this is really different, do it and return
                ASLib.readAll(arg.input_times);
                return;
            default:
                System.out.println("unrecognized problem type: " + arg.type);
                System.exit(1);
        }

        System.out.println("Finished runtimes with " + benchNames.size() + " instances and " + collection.size() + " algorithms");
        switch (arg.type) {
            case "sat":
                handleSat(set, benchNames);
                System.out.println("We have: " + SATInstance.NUM_FEATURES + " features");
                break;
            case "gc":
                handleGC();
                break;
            case "miplib":
                handleMip(set, benchNames);
                break;
            default:
                System.out.println("unrecognized problem type: " + arg.type);
                break;
        }
        System.out.println("Writing to file: " + arg.output);
        System.out.println("Num labels: " + set.numlabels());
        set.stats();
        set.subtractBest();
        set.binarizeAll(arg.bins);
        set.toCsv();
    }


    private static Arguments handleArguments(String[] args) {
        if (args.length != 4) {
            throw new IllegalArgumentException("wrong number of args");
        }
        String type = args[0];
        String input_times = args[1];
        String output = args[2];
        int bins = Integer.parseInt(args[3]);
        return new Arguments(type, input_times, output, bins);
    }


    static class Arguments {
        String type;
        String input_times;
        String output;

        int bins;

        public Arguments(String type, String input_times, String output, int bins) {
            this.type = type;
            this.input_times = input_times;
            this.output = output;
            this.bins = bins;
        }
    }


    /**
     * Read runtimes from given file.
     *
     * @return list of names of benchmark files to use
     */
    private static List<String> readRuntimesSat(String filename, AlgorithmCollection collection, Dataset set) throws IOException {
        List<String> bench_names = new ArrayList<>();
        String strCurrentLine;
        BufferedReader objReader = new BufferedReader(new FileReader(filename));
        collection.fromString(objReader.readLine()); // first line is all algs
        while ((strCurrentLine = objReader.readLine()) != null) {
            String[] splitted = strCurrentLine.split(",");
            Instance instance = new SATInstance(collection.size());
            for (int i = 1; i < splitted.length; i++) {
                double runtime = Double.parseDouble(splitted[i].split(" ")[0]); // ugly, but works for MaxSat, other datasets i will check
                instance.addRuntime(i - 1, runtime);
            }
            if (!instance.allTimeouts()) {
                set.addInstance(instance);
                bench_names.add(splitted[0]);
            }
        }

        return bench_names;

    }

    private static List<String> readRuntimesBoas(String filename, Dataset set) throws IOException {
        List<String> bench_names = new ArrayList<>();
        String strCurrentLine;
        BufferedReader objReader = new BufferedReader(new FileReader(filename));
        String current_instance = null;
        MIPInstance instance = null;
        String inst_name = null;
        List<Double> current_runtimes = new ArrayList<>();
        while ((strCurrentLine = objReader.readLine()) != null) {
            // each row is:  instance algorithm time
            String[] splitted = strCurrentLine.split("\\s+");
            inst_name = splitted[0];
            String alg = splitted[1];
            double runtime = Double.parseDouble(splitted[2]);
            if (!inst_name.equals(current_instance)) {
                if (instance != null) {
                    // new instance, put it in set
                    instance.runtimes = new double[current_runtimes.size()];
                    for (int i = 0; i < current_runtimes.size(); i++) {
                        instance.addRuntime(i, current_runtimes.get(i));
                    }
                    current_runtimes = new ArrayList<>(current_runtimes.size());
                    if (!instance.allTimeouts()) {
                        set.addInstance(instance);
                        bench_names.add(current_instance);
                    }
                }

                current_instance = inst_name;
                instance = new MIPInstance();
                instance.name = inst_name;
            }
            current_runtimes.add(runtime);

        }
        // last instance
        assert instance != null;
        instance.runtimes = new double[current_runtimes.size()];
        for (int i = 0; i < current_runtimes.size(); i++) {
            instance.addRuntime(i, current_runtimes.get(i));
        }
        if (!instance.allTimeouts()) {
            set.addInstance(instance);
            bench_names.add(inst_name);
        }

        return bench_names;
    }

    private static void handleSat(Dataset set, List<String> instancesNames) throws IOException {
        int index = 0;
        for (String instance : instancesNames) {
            System.out.println("starting instance " + index + " " + instance);
            // extract features and add them. NOT BINARIZED YET
            extractSATFeatures(set.getInstance(index++), instance);
        }
    }

    private static void handleMip(Dataset set, List<String> instanceNames) throws IOException {
        int index = 0;
        BufferedReader objReader = new BufferedReader(new FileReader("features.txt"), 67108864);
        String strCurrentLine;
        while ((strCurrentLine = objReader.readLine()) != null) {
            String[] splitted = strCurrentLine.split("\\s+");

            System.out.println("starting instance " + index + " " + splitted[0]);
            if (!instanceNames.contains(splitted[0])) {
                System.out.println("skipping instance " +  splitted[0]);
                continue;
            }

            // extract features and add them. NOT BINARIZED YET
            MIPInstance instance = (MIPInstance) set.getInstance(index++);
            assert instance.name.equals(splitted[0]) ;
            assert splitted.length - 1 == MIPInstance.NUM_FEATURES;
            for (int i = 1; i < splitted.length; i++) {
                double v = Double.parseDouble(splitted[i]);
                instance.addFeature(v);
            }
        }
    }

    private static void extractSATFeatures(Instance instance, String file_name) throws IOException {
        file_name = file_name.substring(0, file_name.length() - 3);
        BufferedReader objReader = new BufferedReader(new FileReader(DIR_PREFIX + file_name), 67108864);
        String strCurrentLine;
        int n_var = -1;
        int n_clauses = -1;
        double[] posToNegPerClause = null;
        double[] clausesLengths = null;
        int clause_i = 0;
        double n_unary = 0, n_binary = 0, n_ternary = 0;
        double[][] var_pos_neg_count = null;
        double[] var_in_horn_formula = null;
        double horn_formulas = 0;
        boolean initialized = false;
        //StringBuilder sb = new StringBuilder();
        while ((strCurrentLine = objReader.readLine()) != null) {
            if (strCurrentLine.startsWith("c")) {
                if (!initialized) {
                    if (strCurrentLine.contains("nvars")) {
                        String number = strCurrentLine.split(" ")[2];
                        number = number.substring(0, number.length() - 1);
                        n_var = Integer.parseInt(number);
                    } else if (strCurrentLine.contains("ncls")) {
                        String number = strCurrentLine.split(" ")[2];
                        number = number.substring(0, number.length() - 1);
                        n_clauses = Integer.parseInt(number);
                    }
                }
            } else if (strCurrentLine.startsWith("p")) {
                String[] splitted = strCurrentLine.split(" ");
                if (!initialized) {
                    n_var = Integer.parseInt(splitted[2]);
                    n_clauses = Integer.parseInt(splitted[3]);
                    posToNegPerClause = new double[n_clauses];
                    clausesLengths = new double[n_clauses];
                    var_pos_neg_count = new double[n_var][2];
                    var_in_horn_formula = new double[n_var];
                    initialized = true;
                }

            } else { // not the arguments line or a comment
                if (!initialized) {
                    posToNegPerClause = new double[n_clauses];
                    clausesLengths = new double[n_clauses];
                    var_pos_neg_count = new double[n_var][2];
                    var_in_horn_formula = new double[n_var];
                    initialized = true;
                }
//                sb.append(clause_i);
//                sb.append("/");
//                sb.append(n_clauses);
//                sb.append("\r");
//                System.out.print(sb);
//                sb.setLength(0);
                String[] values = strCurrentLine.split(" ");
                int[] parsed_values = Arrays.stream(values, 1, values.length).mapToInt(Integer::parseInt).toArray();

                // 0 is weight, -1 is always 0
                {
                    // count ration of pos and neg
                    double count_p = 0, count_n = 0;
                    int[] vars = new int[parsed_values.length - 1];
                    int v_i = 0;
                    for (int parsedValue : parsed_values) {
                        if (parsedValue < 0) {
                            count_n++;
                            var_pos_neg_count[-parsedValue - 1][1]++;
                            vars[v_i++] = -parsedValue;
                        } else if (parsedValue > 0) {
                            count_p++;
                            var_pos_neg_count[parsedValue - 1][0]++;
                            vars[v_i++] = parsedValue;
                        }
                    }
                    posToNegPerClause[clause_i] = (count_p / (count_p + count_n));
                    if (count_p == 1 && count_n > 0) {
                        assert var_in_horn_formula != null;
                        horn_formulas++;
                        for (int v : vars) {
                            var_in_horn_formula[v - 1]++;
                        }
                    }
                }
                {
                    // check unary, double, ternary
                    switch (parsed_values.length) {
                        case 2:
                            n_unary++;
                            break;
                        case 3:
                            n_binary++;
                            break;
                        case 4:
                            n_ternary++;
                            break;
                    }
                }
                {
                    // clauses lengths
                    assert clausesLengths != null;
                    clausesLengths[clause_i] = parsed_values.length - 1;
                }
                ++clause_i;
            }
        }
        instance.addFeature(n_var); // 0
        instance.addFeature(n_clauses); // 1
        double ratio = ((double) n_var / n_clauses);
        instance.addFeature(ratio); // 2
        instance.addFeature(ratio * ratio); // 3
        instance.addFeature(ratio * ratio * ratio); // 4
        ratio = 1.0 / ratio;
        instance.addFeature(ratio); //5
        instance.addFeature(ratio * ratio); // 6
        instance.addFeature(ratio * ratio * ratio); // 7
        Utils.addMeanVarMinMaxEntropy(instance, posToNegPerClause); //8 9 10 11 12
        instance.addFeature(n_unary / n_clauses); // 13
        instance.addFeature(n_binary / n_clauses); // 14
        instance.addFeature(n_ternary / n_clauses); // 15
        // build ratios
        double[] ratios = new double[n_var];
        for (int i = 0; i < n_var; i++) {
            ratios[i] = var_pos_neg_count[i][0] / (var_pos_neg_count[i][0] + var_pos_neg_count[i][1]);
        }
        Utils.addMeanVarMinMaxEntropy(instance, ratios); // 16-17-18-19-20
        instance.addFeature(horn_formulas / (double) n_clauses); // 21
        Utils.addMeanVarMinMaxEntropy(instance, var_in_horn_formula); // 22-23-24-25-26
        Utils.addMeanVarMinMaxEntropy(instance, clausesLengths); // 27-28-29-30-31


    }

    private static void handleGC() {
    }

}
