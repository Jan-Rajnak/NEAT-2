package sk.rajniacik.AI.NEAT.main.alg.genome;

import sk.rajniacik.AI.NEAT.lib.neat.InnovationCounter;

public class Connection {
    private final int in;
    private final int out;
    private final double weight;
    private final boolean enabled;
    private int innovation = -1;

    public Connection(int in, int out, double weight, boolean enabled) {
        this.in = in;
        this.out = out;
        this.weight = weight;
        this.enabled = enabled;
    }

    public Connection(int in, int out){
        this(in, out, Math.random() * 2 - 1, true);
    }

    // Getters
    public int getFrom() {
        return in;
    }
    public int getTo() {
        return out;
    }
    public double getWeight() {
        return weight;
    }
    public boolean isEnabled() {
        return enabled;
    }

    // Setters
    public Connection setWeight(double weight) {
        return new Connection(in, out, weight, enabled);
    }
    public Connection setEnabled(boolean enabled) {
        return new Connection(in, out, weight, enabled);
    }


    // toString
    @Override
    public String toString() {
        return "Connection{" +
                "in=" + in +
                ", out=" + out +
                ", weight=" + weight +
                ", enabled=" + enabled +
                '}';
    }

    public int getInnovation() {
        if (innovation == -1) {
            innovation = InnovationCounter.getInnovation(in, out);
        }
        return innovation;
    }
}
