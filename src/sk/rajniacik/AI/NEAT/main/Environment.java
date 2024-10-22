package sk.rajniacik.AI.NEAT.main;

import sk.rajniacik.AI.NEAT.main.alg.genome.Genome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;

public interface Environment {

    void evaluateFitness(ArrayList<Genome> population);

    Map<Config, Double> configs = new HashMap<>(Config.defaultConfig);

    default void setConfig(Config config, double value) {
        configs.put(config, value);
    }
    default void setConfigs(Map<Config, Double> newConfigs) {
        configs.putAll(newConfigs);
    }
    default void setConfigs(List<Config> cons, List<Double> vals) {
        for (int i = 0; i < cons.size(); i++) {
            configs.put(cons.get(i), vals.get(i));
        }
    }

    // Getters for config values
    default int getInputSize() {
        return (int) round(configs.get(Config.INPUTS));
    }
    default int getOutputSize() {
        return (int) round(configs.get(Config.OUTPUTS));
    }
    default int getPopulationSize() {
        return (int) round(configs.get(Config.POPULATION));
    }
    default double getWeightMutationChance() {
        return configs.get(Config.WEIGHT_MUTATION_CHANCE);
    }
    default double getWeightMutationChanceInConnection() {
        return configs.get(Config.WEIGHT_MUTATION_CHANCE_IN_CONNECTION);
    }
    default double getWeightMutationStep() {
        return configs.get(Config.WEIGHT_MUTATION_STEP);
    }
    default double getWeightMutationPerturbChance() {
        return configs.get(Config.WEIGHT_MUTATION_PERTURB_CHANCE);
    }
    default double getBiasMutationChance() {
        return configs.get(Config.BIAS_MUTATION_CHANCE);
    }
    default double getBiasMutationStep() {
        return configs.get(Config.BIAS_MUTATION_STEP);
    }
    default double getBiasMutationPerturbChance() {
        return configs.get(Config.BIAS_MUTATION_PERTURB_CHANCE);
    }
    default double getAddNodeChance() {
        return configs.get(Config.ADD_NODE_CHANCE);
    }
    default double getAddConnectionChance() {
        return configs.get(Config.ADD_CONNECTION_CHANCE);
    }
    default double getDisableConnectionChance() {
        return configs.get(Config.DISABLE_CONNECTION_CHANCE);
    }
    default double getEnableConnectionChance() {
        return configs.get(Config.ENABLE_CONNECTION_CHANCE);
    }
    default double getCrossoverChance() {
        return configs.get(Config.CROSSOVER_CHANCE);
    }
    default int getStaleSpeciesThreshold() {
        return (int) round(configs.get(Config.STALE_SPECIES_THRESHOLD));
    }
    default int getTimeout() {
        return (int) round(configs.get(Config.TIMEOUT));
    }
    default double getCompatibilityThreshold() {
        return configs.get(Config.COMPATIBILITY_THRESHOLD);
    }
    default double getExcessCoefficient() {
        return configs.get(Config.EXCESS_COEFFICIENT);
    }
    default double getDisjointCoefficient() {
        return configs.get(Config.DISJOINT_COEFFICIENT);
    }
    default double getWeightCoefficient() {
        return configs.get(Config.WEIGHT_COEFFICIENT);
    }
    default double getFitnessThreshold() {
        return configs.get(Config.FITNESS_THRESHOLD);
    }
    default boolean log() {
        return configs.get(Config.LOG) == 1.0;
    }


    enum Config {
        WEIGHT_MUTATION_CHANCE,
        WEIGHT_MUTATION_CHANCE_IN_CONNECTION,
        WEIGHT_MUTATION_STEP,
        WEIGHT_MUTATION_PERTURB_CHANCE,
        BIAS_MUTATION_CHANCE,
        BIAS_MUTATION_STEP,
        BIAS_MUTATION_PERTURB_CHANCE,
        ADD_NODE_CHANCE,
        ADD_CONNECTION_CHANCE,
        DISABLE_CONNECTION_CHANCE,
        ENABLE_CONNECTION_CHANCE,
        CROSSOVER_CHANCE,
        STALE_SPECIES_THRESHOLD,
        TIMEOUT,
        COMPATIBILITY_THRESHOLD,
        EXCESS_COEFFICIENT,
        DISJOINT_COEFFICIENT,
        WEIGHT_COEFFICIENT,
        INPUTS,
        OUTPUTS,
        POPULATION,
        FITNESS_THRESHOLD,
        LOG;

        public static final Map<Config, Double> defaultConfig = new HashMap<>() {{
            put(Config.WEIGHT_MUTATION_CHANCE, 0.9);
            put(Config.WEIGHT_MUTATION_CHANCE_IN_CONNECTION, 0.3);
            put(Config.WEIGHT_MUTATION_STEP, 0.1);
            put(Config.WEIGHT_MUTATION_PERTURB_CHANCE, 0.9);
            put(Config.BIAS_MUTATION_CHANCE, 0.3);
            put(Config.BIAS_MUTATION_STEP, 0.1);
            put(Config.BIAS_MUTATION_PERTURB_CHANCE, 0.9);
            put(Config.ADD_NODE_CHANCE, 0.03);
            put(Config.ADD_CONNECTION_CHANCE, 0.05);
            put(Config.DISABLE_CONNECTION_CHANCE, 0.75);
            put(Config.ENABLE_CONNECTION_CHANCE, 0.25);
            put(Config.CROSSOVER_CHANCE, 0.75);
            put(Config.STALE_SPECIES_THRESHOLD, 15.0);
            put(Config.TIMEOUT, 20.0);
            put(Config.COMPATIBILITY_THRESHOLD, 3.0);
            put(Config.EXCESS_COEFFICIENT, 1.0);
            put(Config.DISJOINT_COEFFICIENT, 1.0);
            put(Config.WEIGHT_COEFFICIENT, 0.4);
            put(Config.INPUTS, 2.0);
            put(Config.OUTPUTS, 1.0);
            put(Config.POPULATION, 150.0);
            put(Config.FITNESS_THRESHOLD, 10.0);
            put(Config.LOG, 0.0); // 0 - no log, 1 - log
        }};

    }

    default void print() {
        StringBuilder sb = new StringBuilder();
        sb.append("Environment { \n");
        for (Config c : Config.values()) {
            sb.append(c).append(": ").append(configs.get(c)).append("\n");
        }
        sb.append("}");
        System.out.println(sb);
    }
}
