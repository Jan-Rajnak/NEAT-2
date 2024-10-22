package sk.rajniacik.AI.NEAT.main.alg.genome;

public class Node {
    private final int id;
    private double value;
    private double bias;
    private boolean calculated = false;

    public Node(int id) {
        this.id = id;
        this.bias = Math.random() * 2 - 1;
        value = 0;
    }

    public Node(int id, double bias) {
        this.id = id;
        this.bias = bias;
        value = 0;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

//    public double calculateValue(Genome g) {
//        if (value != 0) {
//            return value;
//        }
//        int def = -(g.getEnv().getOutputSize() + 1);
//        int ID = def;
//        for (Node node : g.getNodes().values()) {
//            if (node == this) {
//                ID = node.getId();
//                break;
//            }
//        }
//        if (ID == def) {
//            throw new IllegalArgumentException("Node not found in the genome.");
//        }
//        double sum = bias;
//        for (Connection c : g.getConnections().values()) {
//            if (c != null && c.getTo() == ID) {
//                if (c.isEnabled()) {
//                    Node fromNode = g.getNodes().get(c.getFrom());
//                    if (fromNode == null) {
//                        throw new IllegalStateException("Node with ID " + c.getFrom() + " does not exist.");
//                    }
//
//                    try {
//                        sum += c.getWeight() * fromNode.calculateValue(g);
//                    } catch (StackOverflowError e) {
//                        System.out.println("Stack overflow error \n Connection: " + c + "\n From node: " + fromNode);
//                    }
//                }
//            }
//        }
//        value = 1 / (1 + Math.exp(-sum)); // sigmoid
//        return value;
//    }

    public double calculateValue(Genome genome) {
        if (calculated) {
            return value;
        }

        // Base case: if the node is an input node, return its value
        if (id > 0 && id <= genome.getEnv().getInputSize()) {
            calculated = true;
            value += bias;
            value = activationFunction(value);
            return value;
        }

        // Recursive case: calculate the value based on incoming connections
        value = bias;
        for (Connection connection : genome.getConnections().values()) {
            if (connection.getTo() == id && connection.isEnabled()) {
                value += connection.getWeight() * genome.getNodes().get(connection.getFrom()).calculateValue(genome);
            }
        }

        calculated = true;
        value = activationFunction(value);
        return value;
    }

    private double activationFunction(double x) {
//        return Math.max(0, x);
        return Math.tanh(x);
    }

    public void reset() {
        value = 0;
        calculated = false;
    }

    public double getBias() {
        return bias;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", value=" + value +
                ", bias=" + bias +
                '}';
    }

    public int getID() {
        return id;
    }

    public void setBias(double v) {
        bias = v;
    }
}
