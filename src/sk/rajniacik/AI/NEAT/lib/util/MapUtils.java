package sk.rajniacik.AI.NEAT.lib.util;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class MapUtils {
    public static <V> Map.Entry<Integer, V> getRandomEntryInRange(Map<Integer, V> map, int minKey, int maxKey) {
        List<Map.Entry<Integer, V>> filteredEntries = map.entrySet().stream()
                .filter(entry -> entry.getKey() >= minKey && entry.getKey() <= maxKey)
                .toList();

        if (filteredEntries.isEmpty()) {
            return null; // or throw an exception if preferred
        }

        Random random = new Random();
        return filteredEntries.get(random.nextInt(filteredEntries.size()));
    }

    public static <V> Map.Entry<Integer, V> getRandomEntryInRange(Map<Integer, V> map, int minKey1, int maxKey1, int minKey2, int maxKey2) {
        List<Map.Entry<Integer, V>> filteredEntries = map.entrySet().stream()
                .filter(entry -> (entry.getKey() >= minKey1 && entry.getKey() <= maxKey1) || (entry.getKey() >= minKey2 && entry.getKey() <= maxKey2))
                .toList();

        if (filteredEntries.isEmpty()) {
            return null; // or throw an exception if preferred
        }

        Random random = new Random();
        return filteredEntries.get(random.nextInt(filteredEntries.size()));
    }

    public static void main(String[] args) {
        // Demo
        Map<Integer, String> map = Map.of(0, "1", 1, "2", 2, "3", 3, "h1", -1, "-1");

//        for (int i = 0; i < 10; i++) {
//            Map.Entry<Integer, String> randomEntry = getRandomEntryInRange(map, 2, 3);
//            if (randomEntry != null) {
//                System.out.println("Random entry: " + randomEntry.getKey() + " -> " + randomEntry.getValue());
//            } else {
//                System.out.println("No entries found in the specified range.");
//            }
//        }

        // Demo 2
        for (int j = 0; j < 10; j++) {
            Map.Entry<Integer, String> randomEntry = getRandomEntryInRange(map, -1, 0, 2, 3);
            if (randomEntry != null) {
                System.out.println("Random entry: " + randomEntry.getKey() + " -> " + randomEntry.getValue());
            } else {
                System.out.println("No entries found in the specified range.");
            }
        }
    }
}