package sk.rajniacik.AI.NEAT.main.drivers;

import sk.rajniacik.AI.NEAT.main.Environment;
import sk.rajniacik.AI.NEAT.main.alg.Pool;
import sk.rajniacik.AI.NEAT.main.alg.genome.Genome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XOR implements Environment {
    public static void main(String[] args){
        XOR xor = new XOR();
        xor.setConfigs(List.of(Config.INPUTS, Config.OUTPUTS, Config.POPULATION, Config.LOG, Config.FITNESS_THRESHOLD),
                List.of(2.0, 1.0, 150.0, 1.0, 3.0));
        Pool pool = new Pool(xor);
        pool.initializePopulation();
        System.out.println(pool);
        Genome trainedGenome = pool.run();
    }

    @Override
    public void evaluateFitness(ArrayList<Genome> genomes) {
        for (Genome genome : genomes) {
            double fitness = 0;
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    double[] inputs = {i, j};
                    double[] output = genome.evaluate(inputs);
                    int expected = i ^ j;
                    if ((i ^j) == 1) {
                        double error = Math.abs(expected - output[0]);
                        fitness += (1 -2* error);
                        System.out.println("Inputs: " + Arrays.toString(inputs) + ", Output: " + Arrays.toString(output) + ", Expected: " + expected + ", Error: " + error);
                        continue;
                    }
                    double error = Math.abs(expected - output[0]);
                    fitness += (1 - error);
                    System.out.println("Inputs: " + Arrays.toString(inputs) + ", Output: " + Arrays.toString(output) + ", Expected: " + expected + ", Error: " + error);
                }
            }
            int genomeSize = genome.getConnections().size() + genome.getNodes().size();
            double complexity = (double) genomeSize / 50;
//            fitness = fitness<0?0: fitness * fitness / 4;
            fitness -= complexity;
            genome.setFitness(fitness);
            System.out.println("Genome fitness: " + fitness);
        }
    }

    private double evaluateFitness(Genome genome) {
        double fitness = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                double[] inputs = {i, j};
                double[] output = genome.evaluate(inputs);
                int expected = i ^ j;
                fitness += (1 - Math.abs(expected - output[0]));
            }
        }
        fitness = fitness*fitness/4;
        return fitness;
    }
}
