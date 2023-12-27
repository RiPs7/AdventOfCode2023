package com.rips7.util.algorithms.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Function;

@SuppressWarnings("unused")
public class AStar<T> {

  public List<T> run(final T start, final T end,
                     final Function<T, Map<T, Float>> neighborsGetter,
                     final Function<T, Float> heuristic) {
    final Map<AStarNode<T>, Float> gScore = new HashMap<>();
    final Map<AStarNode<T>, Float> fScore = new HashMap<>();
    final Queue<AStarNode<T>> frontier = new PriorityQueue<>(Comparator.comparing(n -> fScore.getOrDefault(n, Float.MAX_VALUE)));

    final AStarNode<T> startNode = node(start);
    final AStarNode<T> endNode = node(end);

    frontier.add(startNode);
    gScore.put(startNode, 0.0f);
    fScore.put(startNode, heuristic.apply(start));

    while (!frontier.isEmpty()) {
      final AStarNode<T> current = frontier.poll();
      if (current.equals(endNode)) {
        return backtrack(current);
      }
      neighborsGetter.apply(current.data()).entrySet().stream()
        .map(entry -> {
          final AStarNode<T> neighbor = node(entry.getKey());
          final float cost = entry.getValue();
          final float tentativeGScore = gScore.get(current) + cost;
          if (tentativeGScore >= gScore.getOrDefault(neighbor, Float.MAX_VALUE)) {
            return null;
          }
          neighbor.parent = current;
          gScore.put(neighbor, tentativeGScore);
          fScore.put(neighbor, tentativeGScore + heuristic.apply(neighbor.data()));
          return neighbor;
        })
        .filter(Objects::nonNull)
        .filter(n -> !frontier.contains(n))
        .forEach(frontier::add);
    }
    throw new RuntimeException("No solution found");
  }

  private static <T> AStarNode<T> node(final T data) {
    return new AStarNode<>(data);
  }

  private List<T> backtrack(final AStarNode<T> end) {
    final List<AStarNode<T>> path = new ArrayList<>();
    AStarNode<T> current = end;
    while (current != null) {
      path.add(current);
      current = current.parent;
    }
    Collections.reverse(path);
    return path.stream().map(AStarNode::data).toList();
  }

  public static final class AStarNode<T> {
    private final T data;
    private AStarNode<T> parent;

    private AStarNode(final T data) {
      this.data = data;
    }

    public T data() {
      return data;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(data);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof AStarNode<?> other)) return false;
      return Objects.equals(this.data, other.data);
    }

    @Override
    public String toString() {
      return "[%s]".formatted(data);
    }
  }

}
