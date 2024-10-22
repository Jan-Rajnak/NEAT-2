package sk.rajniacik.AI.NEAT.main.alg.genome;

import sk.rajniacik.AI.NEAT.lib.neat.Topology;
import sk.rajniacik.AI.NEAT.lib.util.Logger;
import sk.rajniacik.AI.NEAT.lib.util.MapUtils;
import sk.rajniacik.AI.NEAT.main.Environment;

import java.util.*;

import static sk.rajniacik.AI.NEAT.lib.neat.InnovationCounter.getInnovation;
import static sk.rajniacik.AI.NEAT.lib.util.Logger.Type;
import static sk.rajniacik.AI.NEAT.lib.util.Logger.Type.INFO;
import static sk.rajniacik.AI.NEAT.lib.util.Logger.Type.SUCCESS;

public class Genome {
    private final Environment env;
    Map<Integer, Node> nodes; // Node ID -> Node
    Map<Integer, Connection> connections; // Connection Innovation -> Connection
    double globalRank;
    Logger logs;
    boolean log;
    private double fitness;
    private double adjustedFitness;

    public Genome(){
        env = null;
    }

    public Genome(Environment env) {
        this.env = env;
        nodes = new HashMap<>();
        connections = new HashMap<>();
        fitness = 0;
        adjustedFitness = 0;
        globalRank = 0;
        log = env.log();
        if (log) {
            logs = new Logger(Map.of("Pool", 1, "Species", 2, "Genome", 3, "Node", 4, "Connection", 4));
            logs.log("Initializing genome", INFO, "Genome");
        }
        initialize();
        if (!log) return;
        logs.log("Genome initialized successfully", SUCCESS, "Genome");
    }

    public Genome(Genome g) {
        this(g.env);
        this.nodes = new HashMap<>(g.nodes);
        this.connections = new HashMap<>(g.connections);
        this.fitness = g.fitness;
        this.adjustedFitness = g.adjustedFitness;
        this.globalRank = g.globalRank;
        if (!log) return;
        logs.log("Genome copied successfully", SUCCESS, "Genome");
    }

    public static boolean isSameSpecies(Genome g1, Genome g2, Environment env) {
        Map<Integer, Connection> c1 = new HashMap<>(g1.getConnections());
        Map<Integer, Connection> c2 = new HashMap<>(g2.getConnections());

        int matching = 0;
        int disjoint = 0;
        int excess = 0;
        double weightDiff = 0;
        double delta = 0;

        int maxInnovation1 = c1.keySet().stream().max(Integer::compareTo).orElse(0);
        int maxInnovation2 = c2.keySet().stream().max(Integer::compareTo).orElse(0);
        int lowMaxInnovation = Math.min(maxInnovation1, maxInnovation2);

        Set<Integer> innovs1 = c1.keySet();
        Set<Integer> innovs2 = c2.keySet();

        Set<Integer> innovs = new HashSet<>(innovs1);
        innovs.addAll(innovs2);

        for (int i : innovs) {
            if (c1.containsKey(i) && c2.containsKey(i)) {
                matching++;
                weightDiff += Math.abs(c1.get(i).getWeight() - c2.get(i).getWeight());
            } else if (i <= lowMaxInnovation) {
                disjoint++;
            } else {
                excess++;
            }
        }

        int n = matching + disjoint + excess;
        if (n > 0) {
            delta = (env.getExcessCoefficient() * excess + env.getDisjointCoefficient() * disjoint) / n + env.getWeightCoefficient() * weightDiff / matching;
        }

        return delta < env.getCompatibilityThreshold();
    }
    public static Genome crossover(Genome g1, Genome g2, Environment env) {
        // Set the fitter parent to g1
        if (g2.getFitness() > g1.getFitness()) {
            Genome temp = g1;
            g1 = g2;
            g2 = temp;
        }

        Genome[] parents = new Genome[2];
        parents[0] = g1;
        parents[1] = g2;

        ChildGenome childGenome = new ChildGenome(env);
        TreeMap<Integer, Connection> connections1 = new TreeMap<>(g1.getConnections());
        TreeMap<Integer, Connection> connections2 = new TreeMap<>(g2.getConnections());

        Set<Integer> innovs1 = connections1.keySet();
        Set<Integer> innovs2 = connections2.keySet();

        Set<Integer> innovs = new HashSet<>(innovs1);
        innovs.addAll(innovs2);

        // Add connections
        for (int i : innovs) {
            int fromParent = 0;
            Connection connection = null;
            if (connections1.containsKey(i) && connections2.containsKey(i)) { // Matching gene
                fromParent = Math.random() > 0.5 ? 1 : 2;
                connection = fromParent == 1 ? connections1.get(i) : connections2.get(i);
            } else if (connections1.containsKey(i)) { // Disjoint or excess gene
                connection = connections1.get(i);
                fromParent = 1;
            } else if (connections2.containsKey(i)) { // Disjoint or excess gene
                connection = connections2.get(i);
                fromParent = 2;
            }

            if (connection != null){
                List<Integer> addedNodes = new ArrayList<>();
                // Temporarily add connection and nodes
                childGenome.addConnection(connection);
                if (!childGenome.nodes.containsKey(connection.getFrom())){
                    childGenome.addNode(new Node(connection.getFrom(),parents[fromParent-1].nodes.get(connection.getFrom()).getBias()));
                    addedNodes.add(connection.getFrom());
                }
                if (!childGenome.nodes.containsKey(connection.getTo())){
                    childGenome.addNode(new Node(connection.getTo(),parents[fromParent-1].nodes.get(connection.getTo()).getBias()));
                    addedNodes.add(connection.getTo());
                }
                // Check for cycles
                if (Topology.hasCycle(childGenome.build())){
                    // Remove connection and nodes
                    childGenome.connections.remove(connection.getInnovation());
                    for (Integer id : addedNodes){
                        childGenome.nodes.remove(id);
                    }
                }
            }
        }
//
//        // Add nodes
//        for (Connection c : childGenome.connections.values()) {
//            if (!childGenome.nodes.containsKey(c.getFrom())) {
//                childGenome.addNode(new Node(c.getFrom()));
//            }
//            if (!childGenome.nodes.containsKey(c.getTo())) {
//                childGenome.addNode(new Node(c.getTo()));
//            }
//        }

        Genome finalChild = childGenome.build();

        if (Topology.hasCycle(finalChild)) {
               throw new IllegalStateException("Cycle detected in child genome after crossover");
        }

        if (!env.log()) {
            return finalChild;
        }
        finalChild.log("Genome crossovered successfully" + finalChild, SUCCESS, "Genome");
        return finalChild;
    }
    // Initialize genome
    private void initialize() {
        // Input nodes (IDs from 1 to inputSize)
        for (int i = 1; i <= env.getInputSize(); i++) {
            nodes.put(i, new Node(i));
            if (!log) continue;
            logs.log(nodes.get(i) + " added", SUCCESS, "Node");
        }
        // Output nodes (from -1 to -outputSize)
        for (int i = 0; i < env.getOutputSize(); i++) {
            nodes.put(-i - 1, new Node(-i - 1));
            if (!log) continue;
            logs.log(nodes.get(-i - 1) + " added", SUCCESS, "Node");
        }
        // Connect all input nodes to all output nodes
        for (int i = 1; i <= env.getInputSize(); i++) {
            for (int j = -1; j >= -env.getOutputSize(); j--) {
                Connection connection = new Connection(i, j);
                connections.put(getInnovation(i, j), connection);
                if (!log) continue;
                logs.log(connection + " added", SUCCESS, "Connection");
            }
        }
    }

    // Evaluate genome
    public double[] evaluate(double[] inputs) {
        double[] outputs = new double[env.getOutputSize()];
        // Reset node values
        for (Node node : nodes.values()) {
            node.reset();
        }
        // Set input values
        for (int i = 1; i <= env.getInputSize(); i++) {
            nodes.get(i).setValue(inputs[i-1]);
        }
        for (int i = 0; i < env.getOutputSize(); i++) {
            outputs[i] = nodes.get(-i - 1).calculateValue(this);
        }
        return outputs;
    }

    // Mutate the genome
    public void mutate() {
        if (log) {
            logs.log("Mutating genome" + this, INFO, "Genome");
        }

        // Randomly select one mutation type
        int mutationType = (int) (Math.random() * 6);

        // Probability to perform the selected mutation
        double mutationChance = Math.random();

        switch (mutationType) {
            case 0:
                if (mutationChance <= env.getWeightMutationChance()) {
                    mutateWeight();
                }
                break;
            case 1:
                if (mutationChance <= env.getBiasMutationChance()) {
                    mutateBias();
                }
                break;
            case 2:
                if (mutationChance <= env.getAddNodeChance()) {
                    mutateAddNode();
                }
                break;
            case 3:
                if (mutationChance <= env.getAddConnectionChance()) {
                    mutateAddConnection();
                }
                break;
            case 4:
                if (mutationChance <= env.getEnableConnectionChance()) {
                    mutateEnableConnection();
                }
                break;
            case 5:
                if (mutationChance <= env.getDisableConnectionChance()) {
                    mutateDisableConnection();
                }
                break;
        }

        if (Topology.hasCycle(this)) {
            throw new IllegalStateException("Cycle detected in genome after mutation no. " + mutationType); // 1->weight, 2->bias, 3->add node, 4->add connection, 5->enable connection, 6->disable connection
        }

        if (log) {
            logs.log("Genome mutated successfully" + this, SUCCESS, "Genome");
        }
    }

    // Mutate
    private void mutateWeight() {
        for (Connection connection : connections.values()) {
            if (Math.random() < env.getWeightMutationPerturbChance()) {
                connection.setWeight(connection.getWeight() + Math.random() * env.getWeightMutationStep() * 2 - env.getWeightMutationStep());
            } else {
                connection.setWeight(Math.random() * 2 - 1);
            }
            if (!log) continue;
            logs.log(connection + " weight mutated", SUCCESS, "Connection");
        }
    }
    private void mutateBias() {
        for (Node node : nodes.values()) {
            node.setBias(node.getBias() + Math.random() * env.getBiasMutationStep() * 2 - env.getBiasMutationStep());
            if (!log) continue;
            logs.log(node + " bias mutated", SUCCESS, "Node");
        }
    }
    private void mutateAddNode(){
        Connection connection = (Connection) connections.values().toArray()[(int) (Math.random() * connections.size())];
        connection.setEnabled(false);
        Node newNode = new Node(nodes.keySet().stream().max(Integer::compareTo).orElse(0) + 1);
        nodes.put(newNode.getID(), newNode);
        Connection connection1 = new Connection(connection.getFrom(), newNode.getID(), 1, true);
        Connection connection2 = new Connection(newNode.getID(), connection.getTo(), connection.getWeight(), true);
        connections.put(connection1.getInnovation(), connection1);
        connections.put(connection2.getInnovation(), connection2);
        if (!log) return;
        logs.log(newNode + " added", SUCCESS, "Node");
        logs.log(connection1 + " added", SUCCESS, "Connection");
        logs.log(connection2 + " added", SUCCESS, "Connection");
    }
    private void mutateAddConnection(){
        // Get random input or hidden node
        Map.Entry<Integer, Node> fromEntry = MapUtils.getRandomEntryInRange(nodes, 1, Collections.max(nodes.keySet()));
        // Get random output node or hidden node
        Map.Entry<Integer, Node> toEntry = MapUtils.getRandomEntryInRange(nodes, -env.getOutputSize(), -1, env.getInputSize(), Collections.max(nodes.keySet()));
        // TODO: check if exception is not needed
        if (fromEntry == null || toEntry == null) return;
        if (Objects.equals(fromEntry.getKey(), toEntry.getKey())) return;
        int from = fromEntry.getKey();
        int to = toEntry.getKey();
        // Check if adding this connection will create a cycle
        if (willCreateCycle(from, to)) {
            return;
        }
        if (connections.containsKey(getInnovation(from, to))) {
            return;
        }
        Connection connection = new Connection(from, to);
        connections.put(connection.getInnovation(), connection);
        if (!log) return;
        logs.log(connection + " added", SUCCESS, "Connection");
    }
    private void mutateEnableConnection(){
        Connection connection = (Connection) connections.values().toArray()[(int) (Math.random() * connections.size())];
        connection.setEnabled(true);
        if (!log) return;
        logs.log(connection + " enabled", SUCCESS, "Connection");
    }
    private void mutateDisableConnection(){
        Connection connection = (Connection) connections.values().toArray()[(int) (Math.random() * connections.size())];
        connection.setEnabled(false);
        if (!log) return;
        logs.log(connection + " disabled", SUCCESS, "Connection");
    }

    // Getters
    public double getFitness() {
        return fitness;
    }

    // Setters
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getAdjustedFitness() {
        return adjustedFitness;
    }

    public void setAdjustedFitness(double adjustedFitness) {
        this.adjustedFitness = adjustedFitness;
    }

    public double getGlobalRank() {
        return globalRank;
    }

    public void setGlobalRank(double globalRank) {
        this.globalRank = globalRank;
    }

    public Map<Integer, Node> getNodes() {
        return nodes;
    }

    public Map<Integer, Connection> getConnections() {
        return connections;
    }

    public Environment getEnv() {
        return env;
    }

    public void log(String message, Type type, String level) {
        logs.log(message, type, level);
    }

    // toString method
    @Override
    public String toString() {
        if (env == null) {
            return "Genome{}";
        }
        return "\nGenome{" +
                "nodes=" + nodes.keySet() +
                ", connections=" + connections +
                ", fitness=" + fitness +
                ", adjustedFitness=" + adjustedFitness +
                ", globalRank=" + globalRank +
                "} ";
    }

    public String toStringWithLogs() {
        if (env == null) {
            return "Genome{}";
        }
        return this + logs.toString();
    }

    // Check if adding a connection will create a cycle
    private boolean willCreateCycle(int from, int to) {
        // temporary connection
        Connection connection = new Connection(from, to);
        connections.put(connection.getInnovation(), connection);

        boolean hasCycle = Topology.hasCycle(this);

        // remove temporary connection
        connections.remove(connection.getInnovation());

        return hasCycle;
    }

    private static class ChildGenome {
        private final Environment env;
        private final Map<Integer, Node> nodes;
        private final Map<Integer, Connection> connections;

        public ChildGenome(Environment env) {
            this.env = env;
            this.nodes = new HashMap<>();
            this.connections = new HashMap<>();
        }

        public void addNode(Node node) {
            nodes.put(node.getID(), node);
        }

        public void addConnection(Connection connection) {
            connections.put(connection.getInnovation(), connection);
        }

        public void addConnection(int from, int to, double weight, boolean enabled) {
            Connection connection = new Connection(from, to, weight, enabled);
            connections.put(connection.getInnovation(), connection);
        }

        public boolean willCreateCycle(int from, int to) {
            return build().willCreateCycle(from, to);
        }

        public Genome build() {
            Genome genome = new Genome(env);
            genome.nodes = nodes;
            genome.connections = connections;
            return genome;
        }
    }
}
