package sk.rajniacik.AI.NEAT.lib.neat;

import sk.rajniacik.AI.NEAT.main.Environment;
import sk.rajniacik.AI.NEAT.main.alg.genome.Connection;
import sk.rajniacik.AI.NEAT.main.alg.genome.Genome;
import sk.rajniacik.AI.NEAT.main.alg.genome.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Topology {
    public static void main(String[] args) {
        Environment env = new Environment() {
            @Override
            public void evaluateFitness(ArrayList<Genome> population) {

            }
        };
        env.setConfigs(List.of(Environment.Config.INPUTS, Environment.Config.OUTPUTS, Environment.Config.POPULATION, Environment.Config.LOG, Environment.Config.FITNESS_THRESHOLD),
                List.of(2.0, 1.0, 150.0, 1.0, 3.0));

        Genome genome = new Genome(env);
        genome.getNodes().put(3, new Node(3)); //use for gene with nodes in
        genome.getConnections().put(2, new Connection(1, 3));
        genome.getConnections().put(3, new Connection(2, 3));
        genome.getConnections().put(4, new Connection(3, -1));
        genome.getConnections().put(5, new Connection(3, 1));

        System.out.println(genome);
        System.out.println(hasCycle(genome));
        System.out.println(printLayeredNodes(topologicalSort(genome)));

    }

    public static boolean hasCycle(Genome g){
        return topologicalSort(g) == null;
    }
    public static Map<Node, Integer> topologicalSort(Genome genome) {
        Map<Integer, Connection> connections = new HashMap<>(genome.getConnections());
        Map<Integer, Node> nodes = new HashMap<>(genome.getNodes());

        // Topological sort
        Map<Node, Integer> nodesByLayer = new HashMap<>();

        // Deal with input nodes
        for (int i = 1; i <= genome.getEnv().getInputSize(); i++) {
            nodesByLayer.put(nodes.get(i), 1);
            nodes.remove(i);
        }
        // Remove connections from input nodes
        List<Integer> connectionsToRemove = new ArrayList<>();
        for (Connection connection : connections.values()) {
            if (connection.getFrom() <= genome.getEnv().getInputSize()) {
                connectionsToRemove.add(connection.getInnovation());
            }
        }
        for (Integer integer : connectionsToRemove) {
            connections.remove(integer);
        }

        // Deal with output nodes
        List<Node> outputNodes = new ArrayList<>();
        for (int i = -1; i >= -genome.getEnv().getOutputSize(); i--) {
            outputNodes.add(nodes.get(i));
            nodes.remove(i);
        }

        int currentLayer = 2;
        int nodesProcessed = 0;
        int nodesSize = nodes.size();
        while(nodesProcessed < nodesSize) {
            Node node = nodes.get(nodesProcessed+1+genome.getEnv().getInputSize());
            if (node == null) {
                nodesProcessed++;
                continue;
            }
            List<Connection> connsIn = new ArrayList<>();
            for (Connection connection : connections.values()) {
                if (connection.getTo() == node.getID()) {
                    connsIn.add(connection);
                }
            }
            if (connsIn.isEmpty()) {
                nodesByLayer.put(node, currentLayer);
                nodes.remove(node.getID());
                // Remove connections from this node
                connectionsToRemove.clear();
                for (Connection connection : connections.values()) {
                    if (connection.getFrom() == node.getID()) {
                        connectionsToRemove.add(connection.getInnovation());
                    }
                }
                for (Integer integer : connectionsToRemove) {
                    connections.remove(integer);
                }
                nodesProcessed = 0;
                currentLayer++;
            } else nodesProcessed++;
        }
        // Add output nodes
        for (Node outputNode : outputNodes) {
            nodesByLayer.put(outputNode, currentLayer);
        }
        if (nodes.isEmpty()){
            return nodesByLayer;
        } else {
            return null;
        }
    }
    public static String printLayeredNodes(Map<Node, Integer> nodesByLayer) {
        StringBuilder sb = new StringBuilder();
        Map<Integer, List<Node>> layers = new HashMap<>();
        for (Node node : nodesByLayer.keySet()) {
            int layer = nodesByLayer.get(node);
            if (!layers.containsKey(layer)) {
                layers.put(layer, new ArrayList<>());
            }
            layers.get(layer).add(node);
        }
        for (int i = 1; i <= layers.size(); i++) {
            sb.append("Layer ").append(i).append(": ");
            for (Node node : layers.get(i)) {
                sb.append(node.getID()).append(", ");
            }
            sb.replace(sb.length()-2, sb.length(), "");
            sb.append("\n");
        }
        return sb.toString();
    }
}
