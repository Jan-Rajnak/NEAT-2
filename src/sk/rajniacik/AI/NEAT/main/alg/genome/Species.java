package sk.rajniacik.AI.NEAT.main.alg.genome;

import sk.rajniacik.AI.NEAT.lib.util.Logger;
import sk.rajniacik.AI.NEAT.main.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Species {
    private final Environment env;
    List<Genome> genomes;
    Genome bestGenome = new Genome();
    double bestFitness;
    double averageFitness;
    int staleness = 0;
    double totalAdjustedFitness = 0;

    public Species(Environment env) {
        this.env = env;
        genomes = new ArrayList<>();
    }

    public Species(Genome g){
        this(g.getEnv());
        genomes.add(g);
    }

    public static void addToSpecies(Genome genome, List<Species> species) {
        Environment env = genome.getEnv();
        for (Species s : species) {
            if (s.genomes.isEmpty())
                continue;
            Genome g0 = s.genomes.getFirst();
            if (Genome.isSameSpecies(genome, g0, env)) {
                s.genomes.add(genome);
                if (genome.log)
                    genome.log(("Genome added to species " + species.indexOf(s)), Logger.Type.INFO, "Species");
                return;
            }
        }
        Species childSpecies = new Species(genome);
        species.add(childSpecies);
        if (genome.log)
            genome.log("Genome added to new species", Logger.Type.INFO, "Species");
    }

    public void removeWeakGenomes(boolean allButOne) {
        genomes.sort((g1, g2) -> Double.compare(g2.getFitness(), g1.getFitness()));
        // Remove all genomes with fitness 0
        genomes.removeIf(genome -> genome.getFitness() == 0);
        int surviveCount = 1;
        if (!allButOne) {
            surviveCount = (int) Math.ceil(genomes.size() / 2f);
        }
        List<Genome> survivedGenomes = new ArrayList<>();
        for (int i = 0; i < surviveCount; i++) {
            survivedGenomes.add(new Genome(genomes.get(i)));
        }
        genomes = survivedGenomes;
    }

    public void calculateGenomeAdjustedFitness() {
        for (Genome genome : genomes) {
            genome.setAdjustedFitness(genome.getGlobalRank() / genomes.size());
        }
    }

    // toString
    @Override
    public String toString() {
        evaluateSpecies();
        return "Species{" +
                "env=" + env +
                ", genomes=" + genomes +
                bestGenome == null ? "" : (", bestGenome=" + bestGenome.toStringWithLogs()) +
                ", bestFitness=" + bestFitness +
                ", averageFitness=" + averageFitness +
                ", staleness=" + staleness +
                ", totalAdjustedFitness=" + totalAdjustedFitness +
                "} \n";
    }

    private void evaluateSpecies() {
        bestGenome = getBestGenome();
        bestFitness = bestGenome.getFitness();
        averageFitness = genomes.stream().mapToDouble(Genome::getFitness).average().orElse(0);
        staleness = getStaleness();
        totalAdjustedFitness = getTotalAdjustedFitness();
    }

    public List<Genome> getGenomes() {
        return genomes;
    }

    public int getStaleness() {
        return staleness;
    }

    public double getTotalAdjustedFitness() {
        double value = 0;
        for (Genome genome : genomes) {
            value += genome.getAdjustedFitness();
        }
        return value;
    }

    public Genome getBestGenome() {
        genomes.sort((g1, g2) -> Double.compare(g2.getFitness(), g1.getFitness()));
        bestGenome = genomes.getFirst();
        return bestGenome;
    }

    public Genome breedChild() {
        Genome child;
        if (Math.random() < env.getCrossoverChance()) {
            Genome g1 = genomes.get((int) (Math.random() * genomes.size()));
            Genome g2 = genomes.get((int) (Math.random() * genomes.size()));
            child = Genome.crossover(g1, g2, env);
        } else {
            child = genomes.get((int) (Math.random() * genomes.size()));
        }
        child.mutate();
        return child;
    }
}
