package sk.rajniacik.AI.NEAT.lib.util;

import javax.crypto.MacSpi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Logger {

    private final List<Log> logs = new ArrayList<>();
    private final Map<String, Integer> depths = new HashMap<>();

    public Logger() {
        depths.put("DEFAULT", 0);
        logs.add(new Log("Logger initialized", Type.INFO, "DEFAULT",0));
    }

    public Logger(Map<String, Integer> depths) {
        this.depths.put("DEFAULT", 0);
        this.depths.putAll(depths);
        logs.add(new Log("Logger initialized", Type.INFO, "DEFAULT",0));
    }

    public void log(String message) {
        logs.add(new Log(message, Type.INFO, "DEFAULT",0));
    }

    public void log(String message, Type type, String level) {
        logs.add(new Log(message, type, level, depths.getOrDefault(level, 0)));
    }

    public String getLogs() {
        StringBuilder sb = new StringBuilder();
        for (Log log : logs) {
            String string = "";
            for (int i = 0; i < log.depth; i++) {
                string += "     ";
            }
            string += log.toString();
            sb.append(string).append("\n");
        }
        return sb.toString();
    }

    public void clearLogs() {
        logs.clear();
    }

    @Override
    public String toString() {
        return getLogs();
    }

    private static class Log {
        private final String message;
        private final String level;
        private final int depth;
        private final Type type;

        public Log(String message, Type type, String level, int depth) {
            this.message = message;
            this.level = level;
            this.depth = depth;
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public String getLevel() {
            return level;
        }

        public Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return type.getColor() + level + ": " + message + "\u001B[0m";
        }
    }

    public enum Type {
        INFO, SUCCESS, WARNING, ERROR;

        public String getColor() {
            return switch (this) {
                case INFO -> "\u001B[34m";
                case SUCCESS -> "\u001B[32m";
                case WARNING -> "\u001B[33m";
                case ERROR -> "\u001B[31m";
                default -> "\u001B[0m";
            };
        }
    }
}
