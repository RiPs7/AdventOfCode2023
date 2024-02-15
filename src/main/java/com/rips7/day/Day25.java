package com.rips7.day;

import com.rips7.util.Util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class Day25 implements Day<Integer> {

    @Override
    public Integer part1(String input) {
        final Graph<String> graph = Graph.parse(input);
        return graph.solve();
    }

    @Override
    public Integer part2(String input) {
        return 0;
    }

    private record Graph<T>(List<T> nodes, Map<T, Set<T>> edges) {
        private Graph() {
            this(new ArrayList<>(), new HashMap<>());
        }

        private static Graph<String> parse(final String input) {
            final Graph<String> graph = new Graph<>();
            Util.lines(input)
                .map(line -> line.split(": "))
                .forEach(parts -> {
                    final String start = parts[0];
                    Arrays.stream(parts[1].split(" "))
                        .forEach(end -> graph.addEdge(start, end));
                });
            return graph;
        }

        private void addEdge(final T start, final T end) {
            edges.computeIfAbsent(start, k -> new HashSet<>()).add(end);
            edges.computeIfAbsent(end, k -> new HashSet<>()).add(start);
            if (!nodes.contains(start)) {
                nodes.add(start);
            }
            if (!nodes.contains(end)) {
                nodes.add(end);
            }
        }

        private int solve() {
            final int edgesToRemove = 3;
            for (int i = 0; i < nodes.size(); i++) {
                final T start = nodes.get(i);
                for (int j = 0; j < i; j++) {
                    final T end = nodes.get(j);
                    final Graph<T> copy = copy();
                    for (int k = 0; k < edgesToRemove; k++) {
                        copy.removeSinglePath(start, end);
                    }
                    if (copy.isDisconnected(start, end)) {
                        return copy.calcGroupSize(start) * copy.calcGroupSize(end);
                    }
                }
            }
            throw new RuntimeException("No solution");
        }

        private void removeSinglePath(final T start, final T end) {
            final Queue<T> frontier = new ArrayDeque<>();
            frontier.add(start);
            final Set<T> closedSet = new HashSet<>();
            closedSet.add(start);
            final Map<T, T> cameFrom = new HashMap<>();
            while (!frontier.isEmpty()) {
                final T current = frontier.poll();
                if (current.equals(end)) {
                    break;
                }
                edges.get(current).stream()
                    .filter(neighbor -> !closedSet.contains(neighbor))
                    .forEach(neighbor -> {
                        closedSet.add(neighbor);
                        frontier.add(neighbor);
                        cameFrom.put(neighbor, current);
                    });
            }

            T current = end;
            while (!current.equals(start)) {
                final T previous = cameFrom.get(current);
                if (previous == null) {
                    break;
                }
                edges.get(current).remove(previous);
                edges.get(previous).remove(current);
                current = previous;
            }
        }

        private boolean isDisconnected(final T start, final T end) {
            final Queue<T> frontier = new ArrayDeque<>();
            frontier.add(start);
            final Set<T> closedSet = new HashSet<>();
            closedSet.add(start);
            while (!frontier.isEmpty()) {
                final T current = frontier.poll();
                if (current.equals(end)) {
                    return false;
                }
                edges.get(current).stream()
                    .filter(neighbor -> !closedSet.contains(neighbor))
                    .forEach(neighbor -> {
                        closedSet.add(neighbor);
                        frontier.add(neighbor);
                    });
            }
            return true;
        }

        private int calcGroupSize(final T start) {
            final Queue<T> frontier = new ArrayDeque<>();
            frontier.add(start);
            final Set<T> closedSet = new HashSet<>();
            closedSet.add(start);
            while (!frontier.isEmpty()) {
                final T current = frontier.poll();
                edges.get(current).stream()
                    .filter(neighbor -> !closedSet.contains(neighbor))
                    .forEach(neighbor -> {
                        closedSet.add(neighbor);
                        frontier.add(neighbor);
                    });
            }
            return closedSet.size();
        }

        private Graph<T> copy() {
            return new Graph<>(
                new ArrayList<>(nodes),
                edges.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue()))));
        }
    }
}