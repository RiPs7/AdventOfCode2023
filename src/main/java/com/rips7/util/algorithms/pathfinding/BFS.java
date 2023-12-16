package com.rips7.util.algorithms.pathfinding;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

@SuppressWarnings("unused")
public class BFS<T> {

  public List<T> run(final T start, final T end, final Function<T, List<T>> neighborsGetter) {
    return run(start, end, neighborsGetter, false);
  }

  public List<T> run(final T start, final T end, final Function<T, List<T>> neighborsGetter, final boolean isStartSameAsEnd) {
    final Queue<BFSNode<T>> frontier = new ArrayDeque<>();
    final Set<BFSNode<T>> closed = new HashSet<>();

    final BFSNode<T> startNode = node(start);
    final BFSNode<T> endNode = node(end);

    if (isStartSameAsEnd) {
      neighborsGetter.apply(start).stream().map(BFS::node).peek(n -> n.parent = startNode).forEach(frontier::add);
    } else {
      frontier.add(startNode);
    }
    while (!frontier.isEmpty()) {
      final BFSNode<T> current = frontier.poll();
      if (current.equals(endNode)) {
        return backtrack(current);
      }
      if (closed.contains(current)) {
        continue;
      }
      neighborsGetter.apply(current.data()).stream()
          .map(BFS::node)
          .filter(n -> !Objects.equals(n, current.parent))
          .peek(n -> n.parent = current)
          .forEach(frontier::add);
      closed.add(current);
    }
    throw new RuntimeException("No solution found");
  }

  private static <T> BFSNode<T> node(final T data) {
    return new BFSNode<>(data);
  }

  private List<T> backtrack(final BFSNode<T> end) {
    List<BFSNode<T>> path = new ArrayList<>();
    BFSNode<T> current = end;
    while (current != null) {
      path.add(current);
      current = current.parent;
    }
    Collections.reverse(path);
    return path.stream().map(BFSNode::data).toList();
  }

  public static final class BFSNode<T> {
    private final T data;
    private BFSNode<T> parent;

    private BFSNode(final T data) {
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
      if (!(obj instanceof BFS.BFSNode<?> other)) return false;
      return Objects.equals(this.data, other.data);
    }

    @Override
    public String toString() {
      return "[%s]".formatted(data);
    }
  }

}
