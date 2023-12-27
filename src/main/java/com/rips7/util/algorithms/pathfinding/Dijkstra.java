package com.rips7.util.algorithms.pathfinding;

import com.rips7.util.maths.Combinatorics.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class Dijkstra<T> {

    public Pair<List<T>, Double> run(final T start, final Predicate<T> endPredicate,
                                     final BiFunction<T, Double, Map<T, Double>> neighborGetter) {
        final Set<T> closedSet = new HashSet<>();
        final PriorityQueue<Node<T>> frontier = new PriorityQueue<>(Comparator.comparing(node -> node.cost.get()));
        frontier.add(new Node<>(start));

        while(!frontier.isEmpty()) {
            final Node<T> current = frontier.poll();
            if (endPredicate.test(current.data)) {
                return Pair.of(backtrack(current), current.cost.get());
            }
            if (closedSet.contains(current.data)) {
                continue;
            }
            closedSet.add(current.data);
            neighborGetter.apply(current.data, current.cost.get()).entrySet().stream()
                .map((neighborAndCost) -> new Node<>(neighborAndCost.getKey(), neighborAndCost.getValue()))
                .forEach(frontier::add);
        }

        throw new RuntimeException("No path to end");
    }

    private List<T> backtrack(final Node<T> end) {
        final List<Node<T>> path = new ArrayList<>();
        Node<T> current = end;
        while(current != null) {
            path.add(current);
            current = current.parent.get();
        }
        Collections.reverse(path);
        return path.stream().map(Node::data).toList();
    }

    private record Node<T>(T data, AtomicReference<Double> cost, AtomicReference<Node<T>> parent) {
        private Node(final T data) {
            this(data, 0);
        }

        private Node(final T data, final double cost) {
            this(data, cost, null);
        }

        private Node(final T data, final double cost, final Node<T> parent) {
            this(data, new AtomicReference<>(cost), new AtomicReference<>(parent));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Node<?> node = (Node<?>) o;
            return data.equals(node.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }
    }


}
