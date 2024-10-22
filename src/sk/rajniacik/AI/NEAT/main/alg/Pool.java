package sk.rajniacik.AI.NEAT.main.alg;

import sk.rajniacik.AI.NEAT.main.Environment;
import sk.rajniacik.AI.NEAT.main.alg.genome.Genome;
import sk.rajniacik.AI.NEAT.main.alg.genome.Species;

import java.util.ArrayList;
import java.util.List;

public class Pool {
    private final Environment env;
    private List<Species> species;
    private int generation;
    private double topFitness;
    private int poolStaleness;

    public Pool(Environment environment) {
        this.env = environment;
        species = new ArrayList<>();
        generation = 0;
        topFitness = 0;
        poolStaleness = 0;
    }

    // Initialize population
    public void initializePopulation() {
        for (int i = 0; i < env.getPopulationSize(); i++) {
            Genome genome = new Genome(env);
            Species.addToSpecies(genome, species);
        }
    }

    // Train the pool
    public Genome run(){
        Genome bestGenome;
        while (true) {
            evaluateFitness();
            bestGenome = getBestGenome();
            System.out.println("Generation: " + generation + ", Top Fitness: " + bestGenome.getFitness());
            System.out.println("Pool: " + this);
            System.out.println("Top Genome: " + bestGenome);
            System.out.println("Best predictions: " + bestGenome.evaluate(new double[]{0, 0})[0] + " " + bestGenome.evaluate(new double[]{0, 1})[0] + " " + bestGenome.evaluate(new double[]{1, 0})[0] + " " + bestGenome.evaluate(new double[]{1, 1})[0]);
            if (bestGenome.getFitness() >= env.getFitnessThreshold()) {
                break;
            }
            breedNewGeneration();
            generation++;
        }
        return bestGenome;
    }

    // Breed new generation
    public List<Genome> breedNewGeneration(){
        removeWeakGenomes();
        removeStaleSpecies();
        calculateGenomeAdjustedFitness();
        ArrayList<Species> survived = new ArrayList<>();
        double globalAdjustedFitness = calculateGlobalAdjustedFitness();
        ArrayList<Genome> children = new ArrayList<>();
        double carryOver = 0;

        for (Species s : species) {
            double breedDouble = env.getPopulationSize() * s.getTotalAdjustedFitness() / globalAdjustedFitness;
            int breedInt = (int) breedDouble;
            carryOver += breedDouble - breedInt;
            if (carryOver > 1) {
                breedInt++;
                carryOver--;
            }
            if (breedInt < 1) continue;
            survived.add(new Species(s.getBestGenome()));
            for (int i = 0; i < breedInt; i++) {
                children.add(s.breedChild());
            }
        }

        species = survived;
        for (Genome g : children) {
            Species.addToSpecies(g, species);
        }
        return children;
    }


    // Remove weak and stale
    private void removeWeakGenomes() {
        for (Species s : species) {
            s.removeWeakGenomes(false);
        }
    }
    private void removeStaleSpecies() {
        species.removeIf(s -> s.getStaleness() > env.getStaleSpeciesThreshold());
    }

    // Evaluate fitness of the pool
    public void evaluateFitness() {
        ArrayList<Genome> allGenomes = new ArrayList<>();
        for (Species s : species) {
            allGenomes.addAll(s.getGenomes());
        }
        env.evaluateFitness(allGenomes);
        rankGlobally(allGenomes);
    }

    // Rank globally
    private void rankGlobally(List<Genome> allGenomes) {
        allGenomes.sort((g1, g2) -> Double.compare(g2.getFitness(), g1.getFitness()));
        for (int i = 0; i < allGenomes.size(); i++) {
            // Set global rank the lower, the better
            allGenomes.get(i).setGlobalRank(i);
        }
    }


    private void calculateGenomeAdjustedFitness() {
        for (Species s : species) {
            s.calculateGenomeAdjustedFitness();
        }
    }

    private double calculateGlobalAdjustedFitness() {
        double globalAdjustedFitness = 0;
        for (Species s : species) {
            globalAdjustedFitness += s.getTotalAdjustedFitness();
        }
        return globalAdjustedFitness;
    }

    // Get best genome
    private Genome getBestGenome(){
        List<Genome> allGenomes = new ArrayList<>();
        for (Species s : species) {
            allGenomes.addAll(s.getGenomes());
        }
        allGenomes.sort((g1, g2) -> Double.compare(g2.getFitness(), g1.getFitness()));
        return allGenomes.getFirst();
    }

    // toString
    @Override
    public String toString() {
        return "Pool{" +
                "env=" + env +
                ", species=" + species +
                ", generation=" + generation +
                ", topFitness=" + topFitness +
                ", poolStaleness=" + poolStaleness +
                "} \n";
    }
}
