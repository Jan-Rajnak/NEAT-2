package sk.rajniacik.AI.NEAT.lib.neat;

import java.util.ArrayList;
import java.util.List;

public class InnovationCounter {

    private static int innovation;
    private static final List<Innovation> innovations = new ArrayList<>();

    public static int getInnovation(int in, int out) {
        for (Innovation i : innovations) {
            if (i.in == in && i.out == out) {
                return i.innovation;
            }
        }
        innovations.add(new Innovation(in, out, innovation));
        return innovation++;
    }

    private static class Innovation {
        private int in;
        private int out;
        private int innovation;

        public Innovation(int in, int out, int innovation) {
            this.in = in;
            this.out = out;
            this.innovation = innovation;
        }

        public boolean equals(Innovation i) {
            return in == i.in && out == i.out;
        }
    }
}
